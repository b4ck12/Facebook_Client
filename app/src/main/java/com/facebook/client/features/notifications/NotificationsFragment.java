package com.facebook.client.features.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.facebook.client.core.base.BaseFragment;
import com.facebook.client.data.model.Notification;
import com.facebook.client.data.repository.INotificationRepository;
import com.facebook.client.data.repository.MockNotificationRepository;
import com.facebook.client.data.repository.RepositoryCallback;
import com.facebook.client.databinding.FragmentNotificationsBinding;

import java.util.List;

/**
 * Fragment hiển thị danh sách Thông báo tương tác (Notifications Tab).
 * Kế thừa BaseFragment để tự động quản lý ViewBinding và giải phóng bộ nhớ khi hủy View.
 *
 * <p>Thành viên phụ trách phát triển chi tiết: Tạ Quang Huy (FE + QA - [D3-TQH], [D4-TQH]).
 */
public class NotificationsFragment extends BaseFragment<FragmentNotificationsBinding> {

    // Adapter quản lý hiển thị danh sách item_notification
    private NotificationAdapter notificationAdapter;

    // Repository cung cấp dữ liệu thông báo (Tuần 1 dùng Mock, Tuần 2 tích hợp Retrofit)
    private INotificationRepository notificationRepository;

    @Override
    protected FragmentNotificationsBinding inflateBinding(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        // Khởi tạo ViewBinding từ file layout res/layout/fragment_notifications.xml
        return FragmentNotificationsBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initViews() {
        // 1. Khởi tạo Repository giả lập (Mock Data)
        notificationRepository = new MockNotificationRepository();

        // 2. Cấu hình RecyclerView với LinearLayoutManager theo chiều dọc
        getBinding().rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 3. Khởi tạo NotificationAdapter kèm bộ xử lý sự kiện tương tác
        notificationAdapter = new NotificationAdapter(new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onNotificationClick(@NonNull Notification notification, int position) {
                // Xử lý khi bấm vào dòng thông báo: Nếu chưa đọc thì đổi sang đã đọc (Optimistic UI)
                if (!notification.isRead()) {
                    notificationAdapter.markAsRead(position);
                    notificationRepository.markAsRead(notification.getId(), null);
                }
                showToast("Đã chọn thông báo từ: " + notification.getSenderName());
            }

            @Override
            public void onMoreOptionsClick(@NonNull Notification notification, int position) {
                // Xử lý khi bấm vào nút ba chấm tùy chọn
                showToast("Tùy chọn cho thông báo của " + notification.getSenderName());
            }
        });

        // 4. Gắn Adapter vào RecyclerView
        getBinding().rvNotifications.setAdapter(notificationAdapter);

        // 5. Tải danh sách thông báo mẫu lên giao diện
        loadNotifications();
    }

    /**
     * Tải danh sách thông báo từ Repository và cập nhật trạng thái UI (Loading/Empty/Data).
     */
    private void loadNotifications() {
        // Bật thanh tải xoay tròn, ẩn danh sách và empty state
        getBinding().pbLoading.setVisibility(View.VISIBLE);
        getBinding().rvNotifications.setVisibility(View.GONE);
        getBinding().layoutEmptyState.setVisibility(View.GONE);

        notificationRepository.getNotifications("user_current", new RepositoryCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> result) {
                // Tắt ProgressBar khi có kết quả
                getBinding().pbLoading.setVisibility(View.GONE);

                if (result == null || result.isEmpty()) {
                    // Nếu danh sách rỗng, hiển thị Empty State
                    getBinding().layoutEmptyState.setVisibility(View.VISIBLE);
                    getBinding().rvNotifications.setVisibility(View.GONE);
                } else {
                    // Nếu có dữ liệu, hiển thị danh sách thông báo
                    getBinding().layoutEmptyState.setVisibility(View.GONE);
                    getBinding().rvNotifications.setVisibility(View.VISIBLE);
                    notificationAdapter.setNotifications(result);
                }
            }

            @Override
            public void onError(String message) {
                // Tắt ProgressBar và hiển thị thông báo lỗi
                getBinding().pbLoading.setVisibility(View.GONE);
                getBinding().layoutEmptyState.setVisibility(View.VISIBLE);
                showToast(message);
            }
        });
    }

    @Override
    protected void initObservers() {
        // Khung đăng ký lắng nghe LiveData từ NotificationViewModel (sẽ gắn ở Sprint 2 khi ghép API)
    }
}

