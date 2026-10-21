package com.facebook.client.features.feed;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.client.R;
import com.facebook.client.data.model.Post;
import com.facebook.client.databinding.ItemPostBinding;

/**
 * ViewHolder hiển thị từng thẻ bài viết trên Bảng tin (Home Feed) và Trang cá nhân (Profile).
 * Sử dụng ViewBinding (ItemPostBinding), tải ảnh với Glide caching và cập nhật trạng thái
 * nút Like tức thì theo chuẩn Optimistic UI ([D4-ML]).
 */
public class PostViewHolder extends RecyclerView.ViewHolder {

    private final ItemPostBinding binding;

    public PostViewHolder(@NonNull ItemPostBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /**
     * Gán dữ liệu từ đối tượng Post lên các thành phần giao diện của thẻ bài viết.
     *
     * @param post     Dữ liệu bài viết cần hiển thị.
     * @param position Vị trí của bài viết trong danh sách.
     * @param listener Bộ lắng nghe sự kiện bấm Thích, Bình luận, Chia sẻ.
     */
    public void bind(@NonNull Post post, int position, @Nullable PostAdapter.OnPostActionListener listener) {
        Context context = binding.getRoot().getContext();

        // 1. Header: Tên tác giả, Thời gian đăng tương đối, Quyền riêng tư & Ảnh đại diện
        String authorName = (post.getAuthorName() != null && !post.getAuthorName().trim().isEmpty())
                ? post.getAuthorName().trim()
                : "Người dùng Facebook";
        binding.tvAuthorName.setText(authorName);
        binding.tvPostTime.setText(formatRelativeTime(post.getCreatedAt()));
        binding.ivPrivacyIcon.setImageResource(R.drawable.ic_public);

        String avatarUrl = post.getAuthorAvatar();
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl.trim())
                    .placeholder(R.drawable.bg_avatar_placeholder)
                    .error(R.drawable.bg_avatar_placeholder)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivAuthorAvatar);
        } else {
            Glide.with(context).clear(binding.ivAuthorAvatar);
            binding.ivAuthorAvatar.setImageResource(R.drawable.bg_avatar_placeholder);
        }

        // 2. Body: Văn bản nội dung bài viết (ẩn đi nếu bài viết chỉ có ảnh)
        String content = post.getContent();
        if (content != null && !content.trim().isEmpty()) {
            binding.tvPostContent.setVisibility(View.VISIBLE);
            binding.tvPostContent.setText(content.trim());
        } else {
            binding.tvPostContent.setVisibility(View.GONE);
            binding.tvPostContent.setText("");
        }

        // 3. Body: Ảnh đính kèm bài viết (ẩn đi nếu bài viết chỉ có chữ)
        String mediaUrl = post.getMediaUrl();
        if (mediaUrl != null && !mediaUrl.trim().isEmpty()) {
            binding.ivPostImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(mediaUrl.trim())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivPostImage);
        } else {
            Glide.with(context).clear(binding.ivPostImage);
            binding.ivPostImage.setVisibility(View.GONE);
        }

        // 4. Footer Stats: Số lượt thích và Số lượng bình luận
        int likesCount = Math.max(0, post.getLikesCount());
        int commentsCount = Math.max(0, post.getCommentsCount());
        binding.tvLikeCount.setText(likesCount + " lượt thích");
        binding.tvCommentCount.setText(commentsCount + " bình luận");

        // 5. Action Bar: Cập nhật trạng thái nút Thích (Optimistic UI đổi icon đặc & màu xanh #1877F2)
        bindLikeButtonState(context, post.isLiked());

        // 6. Gắn sự kiện click cho 3 nút Thích - Bình luận - Chia sẻ
        binding.layoutLikeAction.setOnClickListener(v -> {
            if (listener != null) {
                int currentPos = getAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) {
                    currentPos = position;
                }
                listener.onLikeClick(post, currentPos);
            }
        });

        binding.layoutCommentAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCommentClick(post);
            }
        });

        binding.layoutShareAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShareClick(post);
            }
        });
    }

    /**
     * Cập nhật giao diện nút Thích dựa trên trạng thái isLiked của bài viết.
     * - Khi đã thích (true): Icon ic_like_filled + Màu xanh thương hiệu fb_like_blue (#1877F2).
     * - Khi chưa thích (false): Icon ic_like_outline + Màu xám trung tính fb_action_gray (#65676B).
     */
    private void bindLikeButtonState(@NonNull Context context, boolean isLiked) {
        if (isLiked) {
            int activeBlue = ContextCompat.getColor(context, R.color.fb_like_blue);
            binding.ivLikeIcon.setImageResource(R.drawable.ic_like_filled);
            ImageViewCompat.setImageTintList(binding.ivLikeIcon, ColorStateList.valueOf(activeBlue));
            binding.tvLikeAction.setTextColor(activeBlue);
        } else {
            int inactiveGray = ContextCompat.getColor(context, R.color.fb_action_gray);
            binding.ivLikeIcon.setImageResource(R.drawable.ic_like_outline);
            ImageViewCompat.setImageTintList(binding.ivLikeIcon, ColorStateList.valueOf(inactiveGray));
            binding.tvLikeAction.setTextColor(inactiveGray);
        }
    }

    /**
     * Định dạng mốc thời gian Unix timestamp (milliseconds) sang chuỗi thời gian tương đối.
     */
    @NonNull
    private String formatRelativeTime(long createdAt) {
        if (createdAt <= 0) {
            return "Vừa xong";
        }

        long diffMillis = System.currentTimeMillis() - createdAt;
        if (diffMillis < 60_000L) {
            return "Vừa xong";
        }

        long minutes = diffMillis / 60_000L;
        if (minutes < 60L) {
            return minutes + " phút trước";
        }

        long hours = minutes / 60L;
        if (hours < 24L) {
            return hours + " giờ trước";
        }

        long days = hours / 24L;
        return days + " ngày trước";
    }
}