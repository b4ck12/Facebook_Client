package com.facebook.client.data.repository;

import com.facebook.client.data.model.Notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository giả lập (Mock Data) phục vụ phân hệ Thông báo (Notifications).
 * Cho phép nhóm Frontend phát triển giao diện độc lập ở Tuần 1 mà không phụ thuộc vào Backend.
 */
public class MockNotificationRepository implements INotificationRepository {

    private final List<Notification> mockNotifications = new ArrayList<>();

    public MockNotificationRepository() {
        initMockData();
    }

    /**
     * Khởi tạo bộ dữ liệu thông báo mẫu chuẩn Facebook với đầy đủ các loại tương tác.
     */
    private void initMockData() {
        long now = System.currentTimeMillis();

        // 1. Thông báo Thích bài viết (Chưa đọc - 5 phút trước)
        mockNotifications.add(new Notification(
                "notif_1",
                "user_current",
                "user_101",
                "Nguyễn Văn An",
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                Notification.TYPE_LIKE,
                "post_1001",
                "đã thích bài viết của bạn.",
                false,
                now - (5 * 60 * 1000)
        ));

        // 2. Thông báo Bình luận bài viết (Chưa đọc - 25 phút trước)
        mockNotifications.add(new Notification(
                "notif_2",
                "user_current",
                "user_102",
                "Trần Thị Mai",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                Notification.TYPE_COMMENT,
                "post_1001",
                "đã bình luận về bài viết của bạn: \"Chuyến đi tuyệt vời quá bạn ơi!\"",
                false,
                now - (25 * 60 * 1000)
        ));

        // 3. Thông báo Lời mời kết bạn (Đã đọc - 2 giờ trước)
        mockNotifications.add(new Notification(
                "notif_3",
                "user_current",
                "user_103",
                "Lê Hoàng Nam",
                "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
                Notification.TYPE_FRIEND_REQUEST,
                null,
                "đã gửi cho bạn một lời mời kết bạn.",
                true,
                now - (2 * 60 * 60 * 1000)
        ));

        // 4. Thông báo Chấp nhận kết bạn (Đã đọc - 1 ngày trước)
        mockNotifications.add(new Notification(
                "notif_4",
                "user_current",
                "user_104",
                "Phạm Minh Tuấn",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                Notification.TYPE_FRIEND_ACCEPT,
                null,
                "đã chấp nhận lời mời kết bạn của bạn.",
                true,
                now - (24 * 60 * 60 * 1000)
        ));

        // 5. Thông báo Thích bài viết cũ (Đã đọc - 2 ngày trước)
        mockNotifications.add(new Notification(
                "notif_5",
                "user_current",
                "user_105",
                "Đặng Thu Thảo",
                "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150",
                Notification.TYPE_LIKE,
                "post_1002",
                "đã thích bài viết của bạn.",
                true,
                now - (48 * 60 * 60 * 1000)
        ));
    }

    /**
     * Lấy toàn bộ danh sách thông báo của người dùng, sắp xếp mới nhất lên đầu.
     *
     * @param userId   Mã định danh người dùng hiện tại.
     * @param callback Callback trả về danh sách thông báo hoặc thông báo lỗi.
     */
    @Override
    public void getNotifications(String userId, RepositoryCallback<List<Notification>> callback) {
        if (callback == null) {
            return;
        }

        // Tạo bản sao danh sách để đảm bảo tính an toàn dữ liệu
        List<Notification> copyList = new ArrayList<>(mockNotifications);
        
        // Sắp xếp theo thời gian tạo giảm dần (mới nhất lên trước)
        Collections.sort(copyList, (n1, n2) -> Long.compare(n2.getCreatedAt(), n1.getCreatedAt()));

        callback.onSuccess(copyList);
    }

    /**
     * Đánh dấu một thông báo đã đọc (isRead = true).
     *
     * @param notificationId Mã thông báo cần đánh dấu.
     * @param callback       Callback trả về kết quả thành công hoặc lỗi.
     */
    @Override
    public void markAsRead(String notificationId, RepositoryCallback<Boolean> callback) {
        if (callback == null) {
            return;
        }

        for (Notification notification : mockNotifications) {
            if (notification.getId() != null && notification.getId().equals(notificationId)) {
                notification.setRead(true);
                callback.onSuccess(true);
                return;
            }
        }

        callback.onError("Không tìm thấy thông báo với mã: " + notificationId);
    }
}
