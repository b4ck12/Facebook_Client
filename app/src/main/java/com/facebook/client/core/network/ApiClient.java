package com.facebook.client.core.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Lớp khởi tạo và quản lý kết nối mạng trung tâm (Retrofit Singleton - [D8-TD]).
 *
 * <p>Cấu hình chuẩn:
 * <ul>
 *   <li>Base URL mặc định: {@code http://10.0.2.2:8000/api/} (trỏ từ máy ảo Android Pixel 10a
 *       về máy chủ FastAPI cổng 8000 trên máy tính host).
 *   <li>Tích hợp {@link HttpLoggingInterceptor} giúp theo dõi toàn bộ HTTP Request/Response
 *       trực quan trên Logcat.
 *   <li>Cấu hình timeout ngắn gọn (5 giây kết nối, 10 giây đọc/ghi) để kích hoạt nhanh
 *       cơ chế Graceful Degradation khi máy chủ FastAPI chưa bật.
 * </ul>
 */
public final class ApiClient {

  /** Địa chỉ gốc của máy chủ FastAPI trên máy ảo Android Emulator. */
  public static final String SERVER_ORIGIN = "http://10.0.2.2:8000";

  /** Đường dẫn RESTful API gốc bắt buộc kết thúc bằng dấu '/'. */
  public static final String BASE_URL = SERVER_ORIGIN + "/api/";

  private static final long CONNECT_TIMEOUT_SECONDS = 5L;
  private static final long READ_TIMEOUT_SECONDS = 10L;
  private static final long WRITE_TIMEOUT_SECONDS = 15L;

  private static volatile Retrofit retrofitInstance;
  private static volatile ApiService apiServiceInstance;
  private static volatile String activeBaseUrl = BASE_URL;

  private ApiClient() {
    // Ngăn khởi tạo trực tiếp lớp Singleton
  }

  /**
   * Lấy đối tượng {@link Retrofit} Singleton đã cấu hình Gson và OkHttp Logging Interceptor.
   */
  @NonNull
  public static Retrofit getClient() {
    if (retrofitInstance == null) {
      synchronized (ApiClient.class) {
        if (retrofitInstance == null) {
          retrofitInstance = buildRetrofit(activeBaseUrl);
        }
      }
    }
    return retrofitInstance;
  }

  /**
   * Lấy thể hiện {@link ApiService} Singleton để thực hiện các lời gọi REST API.
   */
  @NonNull
  public static ApiService getApiService() {
    if (apiServiceInstance == null) {
      synchronized (ApiClient.class) {
        if (apiServiceInstance == null) {
          apiServiceInstance = getClient().create(ApiService.class);
        }
      }
    }
    return apiServiceInstance;
  }

  /**
   * Cho phép ghi đè {@link ApiService} hoặc đổi Base URL (phục vụ Unit Test / Integration Test).
   */
  public static synchronized void setApiServiceForTesting(@Nullable ApiService customService) {
    apiServiceInstance = customService;
  }

  /**
   * Đổi Base URL động (ví dụ khi test với máy chủ local 127.0.0.1:8000 trên JVM).
   */
  public static synchronized void setBaseUrl(@NonNull String baseUrl) {
    String normalized = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    activeBaseUrl = normalized;
    retrofitInstance = buildRetrofit(normalized);
    apiServiceInstance = retrofitInstance.create(ApiService.class);
  }

  /**
   * Chuẩn hóa đường dẫn tài nguyên tĩnh (/static/uploads/...) thành URL đầy đủ.
   */
  @Nullable
  public static String normalizeStaticUrl(@Nullable String rawUrl) {
    if (rawUrl == null || rawUrl.trim().isEmpty()) {
      return null;
    }
    String trimmed = rawUrl.trim();
    if ("null".equalsIgnoreCase(trimmed)) {
      return null;
    }
    if (trimmed.startsWith("/")) {
      return SERVER_ORIGIN + trimmed;
    }
    return trimmed;
  }

  @NonNull
  private static Retrofit buildRetrofit(@NonNull String baseUrl) {
    HttpLoggingInterceptor loggingInterceptor =
        new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

    OkHttpClient okHttpClient =
        new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build();

    Gson gson = new GsonBuilder().setLenient().create();

    return new Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build();
  }
}
