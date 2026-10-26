package com.facebook.client.core.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Lớp bọc dữ liệu tổng quát (Generic State Wrapper) biểu diễn 3 trạng thái phản hồi mạng
 * giữa Repository, ViewModel và Giao diện UI ([D8-TD]):
 * <ul>
 *   <li>{@link Status#LOADING}: Đang thực hiện truy vấn hoặc tải dữ liệu lên máy chủ.
 *   <li>{@link Status#SUCCESS}: Yêu cầu thành công, kèm dữ liệu {@link #data}.
 *   <li>{@link Status#ERROR}: Yêu cầu thất bại, kèm thông điệp lỗi {@link #message}.
 * </ul>
 *
 * @param <T> Kiểu dữ liệu trả về (Post, List&lt;Post&gt;, User, String...).
 */
public class Resource<T> {

  /**
   * Trạng thái của tác vụ bất đồng bộ.
   */
  public enum Status {
    LOADING,
    SUCCESS,
    ERROR
  }

  @NonNull
  public final Status status;

  @Nullable
  public final T data;

  @Nullable
  public final String message;

  public Resource(@NonNull Status status, @Nullable T data, @Nullable String message) {
    this.status = status;
    this.data = data;
    this.message = message;
  }

  /**
   * Khởi tạo trạng thái đang xử lý (LOADING) không kèm dữ liệu tạm.
   */
  @NonNull
  public static <T> Resource<T> loading() {
    return new Resource<>(Status.LOADING, null, null);
  }

  /**
   * Khởi tạo trạng thái đang xử lý (LOADING) kèm dữ liệu hiện có.
   */
  @NonNull
  public static <T> Resource<T> loading(@Nullable T data) {
    return new Resource<>(Status.LOADING, data, null);
  }

  /**
   * Khởi tạo trạng thái thành công (SUCCESS) kèm dữ liệu kết quả.
   */
  @NonNull
  public static <T> Resource<T> success(@Nullable T data) {
    return new Resource<>(Status.SUCCESS, data, null);
  }

  /**
   * Khởi tạo trạng thái lỗi (ERROR) kèm thông báo lỗi tiếng Việt.
   */
  @NonNull
  public static <T> Resource<T> error(@NonNull String message) {
    return new Resource<>(Status.ERROR, null, message);
  }

  /**
   * Khởi tạo trạng thái lỗi (ERROR) kèm thông báo lỗi và dữ liệu dự phòng.
   */
  @NonNull
  public static <T> Resource<T> error(@NonNull String message, @Nullable T data) {
    return new Resource<>(Status.ERROR, data, message);
  }

  @NonNull
  public Status getStatus() {
    return status;
  }

  @Nullable
  public T getData() {
    return data;
  }

  @Nullable
  public String getMessage() {
    return message;
  }

  public boolean isLoading() {
    return status == Status.LOADING;
  }

  public boolean isSuccess() {
    return status == Status.SUCCESS;
  }

  public boolean isError() {
    return status == Status.ERROR;
  }
}
