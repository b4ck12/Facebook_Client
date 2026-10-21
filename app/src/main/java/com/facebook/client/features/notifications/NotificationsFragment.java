package com.facebook.client.features.notifications;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.facebook.client.core.base.BaseFragment;
import com.facebook.client.databinding.FragmentNotificationsBinding;

/**
 * Fragment hiển thị danh sách Thông báo tương tác (Notifications Tab).
 * Kế thừa BaseFragment để tự động quản lý ViewBinding và giải phóng bộ nhớ khi hủy View.
 *
 * <p>Thành viên phụ trách phát triển chi tiết: Tạ Quang Huy (FE + QA - [D3-TQH]).
 */
public class NotificationsFragment extends BaseFragment<FragmentNotificationsBinding> {

    @Override
    protected FragmentNotificationsBinding inflateBinding(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        // Khởi tạo ViewBinding từ file layout res/layout/fragment_notifications.xml
        return FragmentNotificationsBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initViews() {
        // Khung khởi tạo giao diện Thông báo (sẽ gắn NotificationAdapter ở task Day 4 [D4-TQH])
    }

    @Override
    protected void initObservers() {
        // Khung đăng ký lắng nghe LiveData từ NotificationViewModel (sẽ gắn ở Sprint 2)
    }
}
