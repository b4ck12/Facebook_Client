package com.facebook.client.features.notifications;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.client.R;
import com.facebook.client.data.model.Notification;
import com.facebook.client.databinding.ItemNotificationBinding;

/**
 * ViewHolder quản lý giao diện hiển thị cho một dòng Thông báo (Notification Item - [D4-TQH]).
 * Tuân thủ chuẩn Facebook Client Design System:
 * 1. Tải và bo tròn avatar người tương tác bằng Glide.
 * 2. Đè huy hiệu badge loại tương tác (Like, Comment, Friend) ở góc dưới avatar.
 * 3. Hiển thị tên người tương tác in đậm kèm nội dung thông báo (SpannableStringBuilder).
 * 4. Tính toán và hiển thị thời gian tương đối ("5 phút trước", "2 giờ trước").
 * 5. Phản hồi trực quan trạng thái đã đọc (nền trắng, ẩn chấm xanh) / chưa đọc (nền xanh nhạt, hiện chấm xanh).
 */
public class NotificationViewHolder extends RecyclerView.ViewHolder {

    private final ItemNotificationBinding binding;

    public NotificationViewHolder(@NonNull ItemNotificationBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /**
     * Gán dữ liệu từ đối tượng Notification lên các view giao diện tương ứng.
     *
     * @param notification Đối tượng thông báo chứa thông tin người gửi, nội dung, loại tương tác.
     * @param position     Vị trí của phần tử trong danh sách RecyclerView.
     * @param listener     Bộ lắng nghe sự kiện click xem thông báo hoặc bấm nút ba chấm tùy chọn.
     */
    public void bind(@NonNull Notification notification, int position, @Nullable NotificationAdapter.OnNotificationActionListener listener) {
        Context context = binding.getRoot().getContext();

        // 1. TẢI ẢNH ĐẠI DIỆN VỚI GLIDE (BO TRÒN 48DP KÈM PLACEHOLDER)
        // Sử dụng circleCrop() để bo tròn và DiskCacheStrategy.ALL để tăng tốc độ cuộn danh sách
        String avatarUrl = notification.getSenderAvatar();
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl.trim())
                    .placeholder(R.drawable.bg_avatar_placeholder)
                    .error(R.drawable.bg_avatar_placeholder)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivSenderAvatar);
        } else {
            Glide.with(context).clear(binding.ivSenderAvatar);
            binding.ivSenderAvatar.setImageResource(R.drawable.bg_avatar_placeholder);
        }

        // 2. GẮN HUY HIỆU BADGE THEO LOẠI TƯƠNG TÁC (LIKE / COMMENT / FRIEND)
        // Góc dưới bên phải avatar sẽ có icon nhỏ tròn hiển thị bản chất tương tác
        bindNotificationBadge(notification.getType());

        // 3. ĐỊNH DẠNG NỘI DUNG VĂN BẢN (IN ĐẬM TÊN NGƯỜI GỬI - SPANNABLESTRINGBUILDER)
        // Chuẩn giao diện Facebook: Tên người tương tác viết đậm, nội dung sau viết thường
        String senderName = (notification.getSenderName() != null && !notification.getSenderName().trim().isEmpty())
                ? notification.getSenderName().trim()
                : "Người dùng Facebook";
        String message = (notification.getMessage() != null) ? notification.getMessage().trim() : "";

        SpannableStringBuilder contentBuilder = new SpannableStringBuilder();
        contentBuilder.append(senderName);
        // Thiết lập StyleSpan BOLD chỉ riêng cho phần tên người gửi
        contentBuilder.setSpan(
                new StyleSpan(Typeface.BOLD),
                0,
                senderName.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        if (!message.isEmpty()) {
            contentBuilder.append(" ").append(message);
        }
        binding.tvNotificationContent.setText(contentBuilder);

        // 4. HIỂN THỊ THỜI GIAN TƯƠNG ĐỐI ("Vừa xong", "X phút trước", "X giờ trước")
        binding.tvNotificationTime.setText(formatRelativeTime(notification.getCreatedAt()));

        // 5. CẬP NHẬT TRẠNG THÁI ĐÃ ĐỌC / CHƯA ĐỌC (UNREAD BLUE HIGHLIGHT & DOT)
        // - Chưa đọc (isRead = false): Nền màu xanh nhạt (#E7F3FF), hiện dấu chấm tròn xanh
        // - Đã đọc (isRead = true): Nền màu trắng bề mặt (#FFFFFF), ẩn dấu chấm tròn xanh
        if (!notification.isRead()) {
            binding.viewUnreadDot.setVisibility(View.VISIBLE);
            binding.layoutNotificationRoot.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.fb_blue_light)
            );
        } else {
            binding.viewUnreadDot.setVisibility(View.GONE);
            binding.layoutNotificationRoot.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.fb_surface)
            );
        }

        // 6. GẮN SỰ KIỆN CLICK CHO DÒNG THÔNG BÁO VÀ NÚT TÙY CHỌN BA CHẤM
        binding.layoutNotificationRoot.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification, position);
            }
        });

        binding.ibMoreOptions.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMoreOptionsClick(notification, position);
            }
        });
    }

    /**
     * Xác định và gán icon badge tương ứng với loại thông báo.
     *
     * @param type Chuỗi định danh loại tương tác: LIKE, COMMENT, FRIEND_REQUEST, FRIEND_ACCEPT.
     */
    private void bindNotificationBadge(@Nullable String type) {
        if (type == null) {
            binding.ivNotificationBadge.setImageResource(R.drawable.ic_notif_like);
            return;
        }

        switch (type) {
            case Notification.TYPE_COMMENT:
                // Icon bong bóng trò chuyện màu xanh lá
                binding.ivNotificationBadge.setImageResource(R.drawable.ic_notif_comment);
                break;

            case Notification.TYPE_FRIEND_REQUEST:
            case Notification.TYPE_FRIEND_ACCEPT:
                // Icon bạn bè hai người màu xanh Facebook
                binding.ivNotificationBadge.setImageResource(R.drawable.ic_notif_friend);
                break;

            case Notification.TYPE_LIKE:
            default:
                // Icon nút Thích ngón tay cái màu xanh Facebook
                binding.ivNotificationBadge.setImageResource(R.drawable.ic_notif_like);
                break;
        }
    }

    /**
     * Quy đổi thời gian Unix timestamp (mili-giây) sang dạng văn bản tương đối tiếng Việt.
     *
     * @param createdAt Mốc thời gian tạo thông báo.
     * @return Chuỗi mô tả thời gian ("Vừa xong", "5 phút trước",...).
     */
    @NonNull
    private String formatRelativeTime(long createdAt) {
        if (createdAt <= 0) {
            return "Vừa xong";
        }

        long diffMillis = System.currentTimeMillis() - createdAt;

        // Nhỏ hơn 1 phút -> "Vừa xong"
        if (diffMillis < 60_000L) {
            return "Vừa xong";
        }

        // Dưới 60 phút -> "X phút trước"
        long minutes = diffMillis / 60_000L;
        if (minutes < 60L) {
            return minutes + " phút trước";
        }

        // Dưới 24 giờ -> "X giờ trước"
        long hours = minutes / 60L;
        if (hours < 24L) {
            return hours + " giờ trước";
        }

        // Từ 1 ngày trở lên -> "X ngày trước"
        long days = hours / 24L;
        return days + " ngày trước";
    }
}
