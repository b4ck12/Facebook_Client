package com.facebook.client.features.notifications;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.facebook.client.data.model.Notification;
import com.facebook.client.data.repository.INotificationRepository;
import com.facebook.client.data.repository.NotificationRepositoryImpl;
import com.facebook.client.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel quản lý dữ liệu và logic nghiệp vụ cho màn hình Thông báo ([D8-TQH]).
 * <p>
 * Vai trò chính:
 * 1. Đóng vai trò trung gian giữa Giao diện (NotificationsFragment) và Tầng Dữ liệu (NotificationRepositoryImpl).
 * 2. Cung cấp LiveData để Fragment tự động cập nhật giao diện khi dữ liệu thay đổi.
 * 3. Bảo vệ dữ liệu sống sót qua các thay đổi cấu hình (như xoay màn hình máy ảo Pixel 10a).
 */
public class NotificationViewModel extends ViewModel {

    // Repository cung cấp dữ liệu thông báo từ server FastAPI
    private final INotificationRepository repository;

    // LiveData nắm giữ danh sách thông báo
    private final MutableLiveData<List<Notification>> notificationsLiveData = new MutableLiveData<>();

    // LiveData thông báo trạng thái đang tải dữ liệu (xoay tròn ProgressBar)
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();

    // LiveData thông báo lỗi mạng hoặc thông báo lỗi từ server
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    /**
     * Khởi tạo ViewModel mặc định sử dụng NotificationRepositoryImpl.
     */
    public NotificationViewModel() {
        this(new NotificationRepositoryImpl());
    }

    /**
     * Khởi tạo ViewModel cho phép truyền Repository từ ngoài vào (phục vụ Unit Test [D10-TQH]).
     */
    public NotificationViewModel(@NonNull INotificationRepository repository) {
        this.repository = repository;
    }

    /** Lấy LiveData danh sách thông báo để Fragment quan sát (Observe) */
    public LiveData<List<Notification>> getNotifications() {
        return notificationsLiveData;
    }

    /** Lấy LiveData trạng thái đang tải */
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    /** Lấy LiveData thông báo lỗi */
    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    /**
     * Tải danh sách thông báo từ Repository của người dùng chỉ định.
     *
     * @param userId Mã định danh người dùng cần lấy thông báo.
     */
    public void loadNotifications(String userId) {
        // Bật trạng thái Loading: setValue kích hoạt LiveData tự động báo cho Fragment hiển thị thanh xoay ProgressBar
        isLoadingLiveData.setValue(true);

        repository.getNotifications(userId, new RepositoryCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> result) {
                // Tắt trạng thái Loading (bắn tín hiệu false để Fragment ẩn ProgressBar)
                isLoadingLiveData.setValue(false);

                // setValue: LiveData tự động bắn danh sách thông báo mới sang Fragment đang observe để cập nhật Adapter
                notificationsLiveData.setValue(result != null ? result : new ArrayList<>());
            }

            @Override
            public void onError(String message) {
                // Tắt trạng thái Loading
                isLoadingLiveData.setValue(false);

                // setValue: LiveData tự động bắn thông điệp lỗi sang Fragment đang observe để hiển thị Toast cảnh báo
                errorMessageLiveData.setValue(message != null ? message : "Không thể tải thông báo");
            }
        });
    }

    /**
     * Đánh dấu một thông báo là đã đọc (isRead = true) và cập nhật lên máy chủ.
     *
     * @param notificationId Mã định danh thông báo cần đánh dấu.
     */
    public void markAsRead(@NonNull String notificationId) {
        // 1. Cập nhật trạng thái trực tiếp trong danh sách LiveData hiện tại
        List<Notification> currentList = notificationsLiveData.getValue();
        if (currentList != null) {
            for (Notification n : currentList) {
                if (notificationId.equals(n.getId())) {
                    n.setRead(true);
                    break;
                }
            }
            // setValue: LiveData bắn danh sách đã cập nhật trạng thái isRead sang Fragment/Adapter để giữ màu nền trắng
            notificationsLiveData.setValue(currentList);
        }

        // 2. Gửi yêu cầu cập nhật xuống tầng Repository
        repository.markAsRead(notificationId, null);
    }
}
