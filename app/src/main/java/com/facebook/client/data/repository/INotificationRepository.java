package com.facebook.client.data.repository;

import com.facebook.client.data.model.Notification;

import java.util.List;

/**
 * Hợp đồng chức năng (Contract) cho phân hệ Thông báo (Notifications).
 */
public interface INotificationRepository {

    /** Lấy danh sách thông báo tương tác của người dùng */
    void getNotifications(String userId, RepositoryCallback<List<Notification>> callback);

    /** Đánh dấu thông báo đã đọc (Cập nhật is_read = 1) */
    void markAsRead(String notificationId, RepositoryCallback<Boolean> callback);
}

