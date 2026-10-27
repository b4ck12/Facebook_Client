package com.facebook.client.features.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.facebook.client.R;
import com.facebook.client.core.base.BaseFragment;
import com.facebook.client.core.utils.SessionManager;
import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.Notification;
import com.facebook.client.databinding.FragmentNotificationsBinding;
import com.facebook.client.features.comment.CommentBottomSheetDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Fragment hiển thị danh sách Thông báo tương tác (Notifications Tab - [D8-TQH]).
 * <p>
 * Áp dụng kiến trúc MVVM chuẩn:
 * - Giao diện chỉ quan sát (Observe) LiveData từ NotificationViewModel.
 * - Tách biệt hoàn toàn tầng logic và tầng gọi API máy chủ Backend FastAPI.
 */
public class NotificationsFragment extends BaseFragment<FragmentNotificationsBinding> {

    // Adapter quản lý hiển thị các dòng thông báo
    private NotificationAdapter notificationAdapter;

    // ViewModel quản lý dữ liệu và kết nối API thông báo ([D8-TQH])
    private NotificationViewModel notificationViewModel;

    // Định danh người dùng hiện tại
    private String currentUserId;

    @Override
    protected FragmentNotificationsBinding inflateBinding(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentNotificationsBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initViews() {
        // 1. Xác định User ID hiện tại đang đăng nhập
        initUser();

        // 2. Khởi tạo ViewModel qua ViewModelProvider
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        // 3. Cấu hình RecyclerView với LinearLayoutManager theo chiều dọc
        getBinding().rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 4. Khởi tạo NotificationAdapter kèm bộ lắng nghe sự kiện tương tác
        notificationAdapter = new NotificationAdapter(new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onNotificationClick(@NonNull Notification notification, int position) {
                // Đổi trạng thái đã đọc ngay lập tức (Optimistic UI) và cập nhật lên server
                if (!notification.isRead()) {
                    notificationAdapter.markAsRead(position);
                    notificationViewModel.markAsRead(notification.getId());
                }
                // Điều hướng tới màn hình bài viết chi tiết hoặc Tab tương ứng ([D12-TQH])
                handleNotificationNavigation(notification);
            }

            @Override
            public void onMoreOptionsClick(@NonNull Notification notification, int position) {
                showToast("Tùy chọn cho thông báo của " + notification.getSenderName());
            }
        });

        getBinding().rvNotifications.setAdapter(notificationAdapter);

        // 5. Yêu cầu ViewModel tải danh sách thông báo từ API
        notificationViewModel.loadNotifications(currentUserId);
    }

    @Override
    protected void initObservers() {
        // observe(): Đăng ký lắng nghe LiveData từ ViewModel. Mỗi khi ViewModel gọi .setValue(), 
        // khối mã lambda này sẽ được tự động kích hoạt để đón nhận dữ liệu mới nhất
        notificationViewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications == null || notifications.isEmpty()) {
                // Hiển thị trạng thái rỗng nếu chưa có thông báo
                getBinding().layoutEmptyState.setVisibility(View.VISIBLE);
                getBinding().rvNotifications.setVisibility(View.GONE);
            } else {
                // Hiển thị danh sách thông báo
                getBinding().layoutEmptyState.setVisibility(View.GONE);
                getBinding().rvNotifications.setVisibility(View.VISIBLE);

                // Dữ liệu mới vừa bắn sang liền được truyền ngay vào Adapter để vẽ lên RecyclerView
                notificationAdapter.setNotifications(notifications);
            }
        });

        // Lắng nghe trạng thái đang tải (ProgressBar xoay tròn)
        notificationViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                getBinding().pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Lắng nghe thông báo lỗi mạng từ máy chủ
        notificationViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showToast(error);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Kiểm tra nếu người dùng đổi tài khoản Demo ở Top Bar thì tải lại thông báo của tài khoản mới
        if (getContext() != null) {
            String activeUserId = SessionManager.getInstance(getContext()).getCurrentUserId();
            if (activeUserId != null && !activeUserId.equals(currentUserId)) {
                currentUserId = activeUserId;
                if (notificationViewModel != null) {
                    notificationViewModel.loadNotifications(currentUserId);
                }
            }
        }
    }

    /**
     * Xác định mã ID người dùng hiện tại đang đăng nhập.
     */
    private void initUser() {
        if (getContext() != null) {
            currentUserId = SessionManager.getInstance(getContext()).getCurrentUserId();
        }
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            currentUserId = MockDataGenerator.getInstance().getCurrentUserId();
        }
    }

    /**
     * Điều hướng người dùng tới màn hình tương ứng khi bấm vào thông báo ([D12-TQH]).
     * - LIKE / COMMENT: Mở CommentBottomSheetDialog hiển thị chi tiết thảo luận của bài viết đích.
     * - FRIEND_REQUEST / FRIEND_ACCEPT: Chuyển sang Tab Bạn bè (nav_friends) trong Bottom Navigation.
     */
    private void handleNotificationNavigation(@NonNull Notification notification) {
        String targetPostId = notification.getTargetPostId();
        String type = notification.getType();

        if (targetPostId != null && !targetPostId.trim().isEmpty()) {
            // Mở CommentBottomSheetDialog xem thảo luận bài viết đích ([D12-TQH])
            CommentBottomSheetDialog dialog = CommentBottomSheetDialog.newInstance(targetPostId.trim());
            dialog.show(getChildFragmentManager(), "notification_post_comments");
            showToast("Đang mở bài viết của " + notification.getSenderName());
        } else if (Notification.TYPE_FRIEND_REQUEST.equalsIgnoreCase(type)
                || Notification.TYPE_FRIEND_ACCEPT.equalsIgnoreCase(type)) {
            // Chuyển sang Tab Bạn bè trong BottomNavigationView ([D12-TQH])
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_friends);
                }
            }
            showToast("Chuyển tới danh sách Bạn bè");
        } else {
            showToast("Đã xem thông báo từ " + notification.getSenderName());
        }
    }
}
