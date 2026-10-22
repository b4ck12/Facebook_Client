package com.facebook.client.features.feed;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.client.data.model.Post;
import com.facebook.client.databinding.ItemPostBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter quản lý danh sách các thẻ bài viết (Post) trên Bảng tin (Home Feed)
 * và Trang cá nhân (Profile Posts Tab).
 * Hỗ trợ cập nhật từng phần tử (Optimistic Like UI) và chèn bài viết mới lên đầu danh sách ([D4-ML]).
 */
public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {

    /**
     * Giao diện lắng nghe các sự kiện tương tác của người dùng trên mỗi thẻ bài viết.
     */
    public interface OnPostActionListener {
        /**
         * Sự kiện khi người dùng nhấn nút Thích (Like) trên bài viết.
         *
         * @param post     Bài viết được tương tác.
         * @param position Vị trí của bài viết trong Adapter để cập nhật Optimistic UI.
         */
        void onLikeClick(@NonNull Post post, int position);

        /**
         * Sự kiện khi người dùng nhấn nút Bình luận (Comment) để mở CommentBottomSheetDialog.
         *
         * @param post Bài viết cần xem hoặc gửi bình luận.
         */
        void onCommentClick(@NonNull Post post);

        /**
         * Sự kiện khi người dùng nhấn nút Chia sẻ (Share) để mở hộp thoại Share now.
         *
         * @param post Bài viết gốc cần chia sẻ.
         */
        void onShareClick(@NonNull Post post);
    }

    private final List<Post> posts = new ArrayList<>();
    @Nullable
    private final OnPostActionListener listener;

    public PostAdapter(@Nullable OnPostActionListener listener) {
        this.listener = listener;
    }

    public PostAdapter(@Nullable List<Post> initialPosts, @Nullable OnPostActionListener listener) {
        if (initialPosts != null) {
            this.posts.addAll(initialPosts);
        }
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPostBinding binding = ItemPostBinding.inflate(inflater, parent, false);
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post, position, listener);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    /**
     * Thay thế toàn bộ danh sách bài viết và làm mới giao diện RecyclerView.
     *
     * @param newPosts Danh sách bài viết mới tải từ Repository.
     */
    public void setPosts(@Nullable List<Post> newPosts) {
        posts.clear();
        if (newPosts != null) {
            posts.addAll(newPosts);
        }
        notifyDataSetChanged();
    }

    /**
     * Lấy danh sách bài viết hiện tại trong Adapter.
     */
    @NonNull
    public List<Post> getPosts() {
        return new ArrayList<>(posts);
    }

    /**
     * Lấy đối tượng bài viết tại vị trí chỉ định.
     */
    @Nullable
    public Post getPostAt(int position) {
        if (position < 0 || position >= posts.size()) {
            return null;
        }
        return posts.get(position);
    }

    /**
     * Chèn một bài viết mới tạo hoặc vừa chia sẻ lên vị trí đầu tiên của Bảng tin.
     *
     * @param newPost Bài viết mới cần hiển thị ở đầu danh sách.
     */
    public void addPostToTop(@NonNull Post newPost) {
        posts.add(0, newPost);
        notifyItemInserted(0);
    }

    /**
     * Cập nhật lại giao diện của một bài viết tại vị trí cụ thể (phục vụ Optimistic Like hoặc tăng số Comment).
     *
     * @param position    Vị trí của bài viết trong danh sách.
     * @param updatedPost Đối tượng bài viết đã cập nhật dữ liệu.
     */
    public void updatePostAt(int position, @NonNull Post updatedPost) {
        if (position >= 0 && position < posts.size()) {
            posts.set(position, updatedPost);
            notifyItemChanged(position);
        }
    }
}