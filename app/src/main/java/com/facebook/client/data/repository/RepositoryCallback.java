package com.facebook.client.data.repository;

/**
 * Interface Generic hứng kết quả bất đồng bộ từ tầng Repository (Mạng Retrofit / Database).
 * 
 * @param <T> Kiểu dữ liệu trả về khi thành công.
 */
public interface RepositoryCallback<T> {

    /** Được gọi khi tác vụ thực thi thành công (HTTP 200/201) */
    void onSuccess(T result);

    /** Được gọi khi xảy ra lỗi mạng hoặc server trả về lỗi (HTTP 400/500) */
    void onError(String message);
}
