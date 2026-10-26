package com.facebook.client.features.feed;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.client.R;
import com.facebook.client.core.base.BaseFragment;
import com.facebook.client.core.utils.SessionManager;
import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.Post;
import com.facebook.client.data.model.User;
import com.facebook.client.data.repository.RepositoryCallback;
import com.facebook.client.databinding.FragmentFeedBinding;
import com.facebook.client.features.comment.CommentBottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Fragment hiển thị Bảng tin Trang chủ (Home Feed - [D4-ML], [D6-ML], [D8-ML], [D9-ML]).
 * Kết nối với FeedViewModel và PostRepositoryImpl để tải bài viết thật từ FastAPI + SQLite,
 * xử lý Thích (Optimistic UI + đồng bộ Server), mở hộp thoại Bình luận và Chia sẻ bài viết.
 */
@SuppressWarnings("SpellCheckingInspection")
public class FeedFragment extends BaseFragment<FragmentFeedBinding> {

    private FeedViewModel feedViewModel;
    private PostAdapter postAdapter;

    @Override
    protected FragmentFeedBinding inflateBinding(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentFeedBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initViews() {
        Context context = requireContext();
        feedViewModel = new ViewModelProvider(this).get(FeedViewModel.class);

        // 1. Cấu hình RecyclerView bảng tin bài viết
        postAdapter = new PostAdapter(new PostAdapter.OnPostActionListener() {
            @Override
            public void onLikeClick(@NonNull Post post, int position) {
                handleLikeClick(post, position);
            }

            @Override
            public void onCommentClick(@NonNull Post post) {
                handleCommentClick(post);
            }

            @Override
            public void onShareClick(@NonNull Post post) {
                handleShareClick(post);
            }
        });

        getBinding().rvFeedPosts.setLayoutManager(new LinearLayoutManager(context));
        getBinding().rvFeedPosts.setHasFixedSize(true);
        getBinding().rvFeedPosts.setAdapter(postAdapter);

        // Tắt animation nhấp nháy để khi bấm Like danh sách không bị giật hay tự trượt
        if (getBinding().rvFeedPosts.getItemAnimator() instanceof androidx.recyclerview.widget.SimpleItemAnimator) {
            ((androidx.recyclerview.widget.SimpleItemAnimator) getBinding().rvFeedPosts.getItemAnimator())
                    .setSupportsChangeAnimations(false);
        }

        // 2. Nạp avatar người dùng đang đăng nhập vào thanh Tạo bài viết nhanh
        loadCurrentUserAvatar(context);

        // 3. Sự kiện bấm vào thanh "What's on your mind?" hoặc icon thêm ảnh -> Mở CreatePostActivity
        View.OnClickListener openCreatePostListener = v -> {
            Intent intent = new Intent(requireContext(), CreatePostActivity.class);
            startActivity(intent);
        };
        getBinding().layoutCreatePostBar.setOnClickListener(openCreatePostListener);
        getBinding().tvCreatePostPrompt.setOnClickListener(openCreatePostListener);
        getBinding().ivAddPhoto.setOnClickListener(openCreatePostListener);
    }

    @Override
    protected void initObservers() {
        // Quan sát trạng thái đang tải dữ liệu
        feedViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (!isAdded() || getView() == null) return;
            getBinding().pbLoading.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
        });

        // Quan sát danh sách bài viết từ FastAPI + SQLite (hoặc Fallback Mock)
        feedViewModel.getFeedPostsLiveData().observe(getViewLifecycleOwner(), posts -> {
            if (!isAdded() || getView() == null) return;
            if (posts == null || posts.isEmpty()) {
                getBinding().layoutEmptyState.setVisibility(View.VISIBLE);
                getBinding().rvFeedPosts.setVisibility(View.GONE);
            } else {
                getBinding().layoutEmptyState.setVisibility(View.GONE);
                getBinding().rvFeedPosts.setVisibility(View.VISIBLE);
                postAdapter.setPosts(posts);
            }
        });

        // Quan sát thông báo lỗi nếu có
        feedViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (!isAdded() || getView() == null || message == null || message.isEmpty()) return;
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });

        // Tải danh sách bài viết ban đầu
        feedViewModel.loadFeedPosts(20, getCurrentUserId());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null && isAdded()) {
            loadCurrentUserAvatar(getContext());
        }
        if (feedViewModel != null) {
            feedViewModel.loadFeedPosts(20, getCurrentUserId());
        }
    }

    /**
     * Xử lý bấm Thích bài viết (Optimistic Like UI + đồng bộ ngầm lên FastAPI server).
     */
    private void handleLikeClick(@NonNull Post post, int position) {
        // 1. Đảo trạng thái trực tiếp trên giao diện (Optimistic UI)
        boolean newLikedState = !post.isLiked();
        int newLikesCount = newLikedState ? post.getLikesCount() + 1 : Math.max(0, post.getLikesCount() - 1);

        post.setLiked(newLikedState);
        post.setLikesCount(newLikesCount);

        // 2. Cập nhật giao diện mượt mà tại đúng vị trí
        postAdapter.notifyItemChanged(position);

        // 3. Đồng bộ ngầm trạng thái Like lên FastAPI + SQLite
        feedViewModel.toggleLike(post, getCurrentUserId());
    }

    /**
     * Xử lý bấm Bình luận: Mở CommentBottomSheetDialog trượt từ đáy màn hình ([D6-ML], [D9-ML]).
     */
    private void handleCommentClick(@NonNull Post post) {
        CommentBottomSheetDialog dialog = CommentBottomSheetDialog.newInstance(post.getId());
        dialog.show(getChildFragmentManager(), "comment_bottom_sheet");
    }

    /**
     * Xử lý bấm Chia sẻ: Hiển thị AlertDialog "Chia sẻ ngay" (Share now) và đồng bộ lên FastAPI.
     */
    private void handleShareClick(@NonNull Post post) {
        Context context = requireContext();

        final EditText etCaption = new EditText(context);
        etCaption.setHint("Nói gì đó về bài viết này...");
        etCaption.setMaxLines(3);

        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int marginPx = (int) (16 * getResources().getDisplayMetrics().density);
        params.leftMargin = marginPx;
        params.rightMargin = marginPx;
        params.topMargin = marginPx / 2;
        params.bottomMargin = marginPx / 2;
        etCaption.setLayoutParams(params);
        container.addView(etCaption);

        new MaterialAlertDialogBuilder(context)
                .setTitle("Chia sẻ bài viết")
                .setMessage("Chia sẻ bài viết này lên Bảng tin cá nhân của bạn?")
                .setView(container)
                .setPositiveButton("Chia sẻ ngay", (dialog, which) -> {
                    String caption = etCaption.getText().toString().trim();
                    feedViewModel.sharePost(post.getId(), getCurrentUserId(), caption, new RepositoryCallback<Post>() {
                        @Override
                        public void onSuccess(Post sharedPost) {
                            Toast.makeText(context, "Đã chia sẻ bài viết lên Bảng tin!", Toast.LENGTH_SHORT).show();
                            if (isAdded() && getView() != null) {
                                getBinding().rvFeedPosts.scrollToPosition(0);
                            }
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Nạp ảnh đại diện của người dùng đang đăng nhập vào thanh tạo bài viết.
     */
    private void loadCurrentUserAvatar(@NonNull Context context) {
        String currentUserId = getCurrentUserId();
        User currentUser = MockDataGenerator.getInstance().getUserById(currentUserId);
        String avatarUrl = (currentUser != null) ? currentUser.getAvatarUrl() : null;

        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl.trim())
                    .placeholder(R.drawable.bg_avatar_placeholder)
                    .error(R.drawable.bg_avatar_placeholder)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(getBinding().ivCurrentUserAvatar);
        } else {
            getBinding().ivCurrentUserAvatar.setImageResource(R.drawable.bg_avatar_placeholder);
        }
    }

    @NonNull
    private String getCurrentUserId() {
        if (getContext() == null) return "user_101";
        String userId = SessionManager.getInstance(getContext()).getCurrentUserId();
        return (userId != null && !userId.isEmpty()) ? userId : "user_101";
    }
}