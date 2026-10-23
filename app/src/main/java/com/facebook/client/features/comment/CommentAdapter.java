package com.facebook.client.features.comment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.client.R;
import com.facebook.client.data.model.Comment;
import com.facebook.client.databinding.ItemCommentBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter quản lý danh sách bình luận (Text-only) hiển thị trong CommentBottomSheetDialog ([D5-ML]).
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> comments = new ArrayList<>();

    public CommentAdapter() {
    }

    public CommentAdapter(@Nullable List<Comment> initialComments) {
        if (initialComments != null) {
            this.comments.addAll(initialComments);
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCommentBinding binding = ItemCommentBinding.inflate(inflater, parent, false);
        return new CommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    /**
     * Cập nhật lại toàn bộ danh sách bình luận khi tải xong từ Repository.
     */
    public void setComments(@Nullable List<Comment> newComments) {
        comments.clear();
        if (newComments != null) {
            comments.addAll(newComments);
        }
        notifyDataSetChanged();
    }

    /**
     * Thêm 1 bình luận mới vào cuối danh sách và thông báo cho RecyclerView.
     */
    public void addComment(@NonNull Comment comment) {
        comments.add(comment);
        notifyItemInserted(comments.size() - 1);
    }

    /**
     * Lấy danh sách bình luận hiện tại trong Adapter.
     */
    @NonNull
    public List<Comment> getComments() {
        return new ArrayList<>(comments);
    }

    /**
     * ViewHolder ánh xạ từng bình luận bằng ViewBinding (ItemCommentBinding).
     */
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentBinding binding;

        public CommentViewHolder(@NonNull ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(@NonNull Comment comment) {
            Context context = binding.getRoot().getContext();

            // 1. Tên người bình luận
            String authorName = comment.getAuthorName();
            if (authorName != null && !authorName.trim().isEmpty()) {
                binding.tvCommentAuthor.setText(authorName.trim());
            } else {
                binding.tvCommentAuthor.setText("Người dùng Facebook");
            }

            // 2. Nội dung văn bản bình luận (Text-only)
            binding.tvCommentContent.setText(comment.getContent() != null ? comment.getContent() : "");

            // 3. Thời gian đăng tương đối (ví dụ: "Vừa xong", "5 phút trước")
            binding.tvCommentTime.setText(formatRelativeTime(comment.getCreatedAt()));

            // 4. Avatar tác giả bình luận (Glide nạp ảnh tròn)
            String avatarUrl = comment.getAuthorAvatar();
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                Glide.with(context)
                        .load(avatarUrl.trim())
                        .placeholder(R.drawable.bg_avatar_placeholder)
                        .error(R.drawable.bg_avatar_placeholder)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivCommentAvatar);
            } else {
                Glide.with(context).clear(binding.ivCommentAvatar);
                binding.ivCommentAvatar.setImageResource(R.drawable.bg_avatar_placeholder);
            }
        }

        /**
         * Định dạng Unix timestamp (milliseconds) sang chuỗi thời gian tương đối.
         */
        @NonNull
        private static String formatRelativeTime(long createdAt) {
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
}