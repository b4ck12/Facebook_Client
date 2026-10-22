package com.facebook.client.features.notifications;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.client.data.model.Notification;
import com.facebook.client.databinding.ItemNotificationBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RecyclerView Adapter quản lý danh sách các dòng Thông báo tương tác (Notifications Tab - [D4-TQH]).
 * Cung cấp các tính năng:
 * - Hiển thị danh sách thông báo từ Mock / Real API Repository.
 * - Hỗ trợ cập nhật trạng thái đã đọc tức thì (Optimistic UI markAsRead) khi người dùng bấm vào dòng thông báo.
 * - Điều phối sự kiện click xem nội dung và click nút ba chấm tùy chọn qua OnNotificationActionListener.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {

    /**
     * Interface giao tiếp sự kiện tương tác của người dùng trên từng dòng thông báo.
     */
    public interface OnNotificationActionListener {
        /**
         * Sự kiện khi người dùng nhấn vào toàn bộ dòng thông báo để xem chi tiết.
         *
         * @param notification Đối tượng thông báo được chọn.
         * @param position     Vị trí của thông báo trong Adapter để cập nhật đổi trạng thái đã đọc.
         */
        void onNotificationClick(@NonNull Notification notification, int position);

        /**
         * Sự kiện khi người dùng nhấn vào nút ba chấm ngang (More Options) ở góc phải.
         *
         * @param notification Đối tượng thông báo cần thao tác tùy chọn (gỡ, tắt thông báo...).
         * @param position     Vị trí của thông báo trong Adapter.
         */
        void onMoreOptionsClick(@NonNull Notification notification, int position);
    }

    private final List<Notification> notifications = new ArrayList<>();
    @Nullable
    private final OnNotificationActionListener listener;

    /**
     * Khởi tạo Adapter với bộ lắng nghe sự kiện tương tác.
     *
     * @param listener Bộ xử lý sự kiện click trên từng item.
     */
    public NotificationAdapter(@Nullable OnNotificationActionListener listener) {
        this.listener = listener;
    }

    /**
     * Khởi tạo Adapter với danh sách dữ liệu ban đầu.
     *
     * @param initialNotifications Danh sách thông báo ban đầu.
     * @param listener             Bộ xử lý sự kiện click trên từng item.
     */
    public NotificationAdapter(@Nullable List<Notification> initialNotifications,
                               @Nullable OnNotificationActionListener listener) {
        if (initialNotifications != null) {
            this.notifications.addAll(initialNotifications);
        }
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Khởi tạo ViewBinding từ layout res/layout/item_notification.xml
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(inflater, parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        // Gán dữ liệu thông báo vào ViewHolder tại vị trí position
        Notification notification = notifications.get(position);
        holder.bind(notification, position, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Cập nhật toàn bộ danh sách thông báo mới tải từ Repository và làm mới RecyclerView.
     *
     * @param newNotifications Danh sách thông báo mới.
     */
    public void setNotifications(@Nullable List<Notification> newNotifications) {
        notifications.clear();
        if (newNotifications != null) {
            notifications.addAll(newNotifications);
        }
        notifyDataSetChanged();
    }

    /**
     * Đánh dấu một thông báo tại vị trí position là đã đọc (isRead = true)
     * và chỉ thông báo cập nhật lại riêng dòng đó (Optimistic UI - không làm mới cả danh sách).
     *
     * @param position Vị trí của thông báo cần cập nhật.
     */
    public void markAsRead(int position) {
        if (position >= 0 && position < notifications.size()) {
            Notification item = notifications.get(position);
            if (!item.isRead()) {
                item.setRead(true);
                notifyItemChanged(position);
            }
        }
    }

    /**
     * Lấy đối tượng Notification tại một vị trí xác định.
     *
     * @param position Vị trí cần lấy.
     * @return Đối tượng Notification tương ứng hoặc null nếu vượt ngoài phạm vi.
     */
    @Nullable
    public Notification getNotificationAt(int position) {
        if (position >= 0 && position < notifications.size()) {
            return notifications.get(position);
        }
        return null;
    }

    /**
     * Lấy danh sách thông báo hiện tại (bản sao chỉ đọc).
     */
    @NonNull
    public List<Notification> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }
}
