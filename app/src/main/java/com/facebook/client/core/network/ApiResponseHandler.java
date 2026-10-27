package com.facebook.client.core.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.facebook.client.core.common.Resource;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Trình xử lý phản hồi và phân loại ngoại lệ mạng tập trung ([D8-TD]).
 *
 * <p>Chuyển đổi các mã lỗi HTTP (400, 404, 413, 422, 500) từ FastAPI và các ngoại lệ kết nối
 * ({@link ConnectException}, {@link SocketTimeoutException}, {@link UnknownHostException})
 * thành thông điệp tiếng Việt rõ ràng hoặc đối tượng {@link Resource}.
 */
public final class ApiResponseHandler {

  private static final Gson GSON = new Gson();

  private ApiResponseHandler() {
    // Utility class
  }

  /**
   * Đóng gói {@link Response} của Retrofit thành {@link Resource}.
   */
  @NonNull
  public static <T> Resource<T> handleResponse(@NonNull Response<T> response) {
    if (response.isSuccessful()) {
      return Resource.success(response.body());
    }
    return Resource.error(parseHttpError(response));
  }

  /**
   * Đóng gói ngoại lệ kết nối mạng thành {@link Resource#error(String)}.
   */
  @NonNull
  public static <T> Resource<T> handleFailure(@Nullable Throwable throwable) {
    return Resource.error(parseErrorMessage(throwable));
  }

  /**
   * Trích xuất thông báo lỗi chi tiết từ trường {@code "detail"} hoặc {@code "message"}
   * trong JSON lỗi của FastAPI.
   */
  @NonNull
  public static <T> String parseHttpError(@Nullable Response<T> response) {
    if (response == null) {
      return "Không nhận được phản hồi từ máy chủ.";
    }

    ResponseBody errorBody = response.errorBody();
    if (errorBody != null) {
      try {
        String rawError = errorBody.string();
        if (rawError != null && !rawError.trim().isEmpty()) {
          JsonObject json = GSON.fromJson(rawError, JsonObject.class);
          if (json != null) {
            if (json.has("detail") && !json.get("detail").isJsonNull()) {
              String detail = json.get("detail").getAsString();
              if (detail != null && !detail.trim().isEmpty()) {
                return detail.trim();
              }
            }
            if (json.has("message") && !json.get("message").isJsonNull()) {
              String message = json.get("message").getAsString();
              if (message != null && !message.trim().isEmpty()) {
                return message.trim();
              }
            }
          }
        }
      } catch (Exception ignored) {
        // Bỏ qua lỗi parse JSON và dùng thông báo theo mã HTTP bên dưới
      }
    }

    int code = response.code();
    switch (code) {
      case 400:
        return "Dữ liệu gửi lên không hợp lệ (HTTP 400).";
      case 404:
        return "Không tìm thấy dữ liệu yêu cầu trên máy chủ (HTTP 404).";
      case 413:
        return "Kích thước tệp ảnh vượt quá giới hạn cho phép (HTTP 413).";
      case 422:
        return "Dữ liệu không đúng định dạng yêu cầu (HTTP 422).";
      case 500:
        return "Lỗi xử lý nội bộ trên máy chủ FastAPI (HTTP 500).";
      default:
        return "Lỗi máy chủ (HTTP " + code + ").";
    }
  }

  /**
   * Chuyển đổi {@link Throwable} khi mất kết nối mạng sang thông điệp tiếng Việt thân thiện.
   */
  @NonNull
  public static String parseErrorMessage(@Nullable Throwable throwable) {
    if (throwable == null) {
      return "Đã xảy ra lỗi không xác định.";
    }
    if (throwable instanceof ConnectException) {
      return "Không thể kết nối tới máy chủ FastAPI (10.0.2.2:8000). Vui lòng kiểm tra server đã bật chưa.";
    }
    if (throwable instanceof SocketTimeoutException) {
      return "Hết thời gian chờ phản hồi từ máy chủ FastAPI (Timeout).";
    }
    if (throwable instanceof UnknownHostException) {
      return "Không thể phân giải địa chỉ máy chủ. Vui lòng kiểm tra kết nối mạng.";
    }
    if (throwable instanceof IOException) {
      return "Lỗi đường truyền mạng khi giao tiếp với máy chủ.";
    }
    String msg = throwable.getMessage();
    return (msg != null && !msg.trim().isEmpty()) ? msg : "Đã xảy ra lỗi kết nối máy chủ.";
  }
}
