package com.facebook.client.features.feed;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.facebook.client.R;
import com.facebook.client.core.base.BaseFragment;
import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.Post;
import com.facebook.client.data.model.User;
import com.facebook.client.databinding.FragmentFeedBinding;
import java.util.List;

/**
 * Fragment hiển thị Bảng tin Trang chủ (Home Feed).
 * Kế thừa BaseFragment để tự động quản lý ViewBinding và giải phóng bộ nhớ khi hủy View.
 *
 * <p>Thành viên phụ trách phát triển chi tiết: Minh Lương (FE).
 * <p>Hỗ trợ kết nối điều phối điều hướng (FE Lead - Tùng Dương):
 * Mở CreatePostActivity từ thanh "Bạn đang nghĩ gì?", làm mới danh sách bài viết và tải avatar người dùng.
 */
public class FeedFragment extends BaseFragment<FragmentFeedBinding> {

  private PostAdapter postAdapter;

  private final ActivityResultLauncher<Intent> createPostLauncher =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
              // Làm mới danh sách bài viết trên Bảng tin khi vừa đăng bài thành công
              loadFeedPosts();
              if (getBinding() != null && getBinding().rvFeedPosts != null) {
                getBinding().rvFeedPosts.scrollToPosition(0);
              }
            }
          });

  @Override
  protected FragmentFeedBinding inflateBinding(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    // Khởi tạo ViewBinding từ file layout res/layout/fragment_feed.xml
    return FragmentFeedBinding.inflate(inflater, container, false);
  }

  @Override
  protected void initViews() {
    setupCreatePostBar();
    setupRecyclerView();
    loadFeedPosts();
  }

  @Override
  public void onResume() {
    super.onResume();
    loadCurrentUserAvatar();
  }

  @Override
  protected void initObservers() {
    // Khung đăng ký lắng nghe LiveData từ FeedViewModel (Minh Lương triển khai ở Sprint tiếp theo)
  }

  /**
   * Thiết lập sự kiện bấm mở màn hình Tạo bài viết (CreatePostActivity)
   * từ thanh giả lập nhập liệu "Bạn đang nghĩ gì?" và icon thêm ảnh.
   */
  private void setupCreatePostBar() {
    loadCurrentUserAvatar();

    View.OnClickListener openCreatePostListener =
        v -> {
          Intent intent = new Intent(requireContext(), CreatePostActivity.class);
          createPostLauncher.launch(intent);
        };

    getBinding().tvCreatePostPrompt.setOnClickListener(openCreatePostListener);
    getBinding().layoutCreatePostBar.setOnClickListener(openCreatePostListener);
    getBinding().ivAddPhoto.setOnClickListener(openCreatePostListener);
  }

  /**
   * Cấu hình RecyclerView và gắn PostAdapter với các bộ lắng nghe tương tác.
   */
  private void setupRecyclerView() {
    postAdapter =
        new PostAdapter(
            new PostAdapter.OnPostActionListener() {
              @Override
              public void onLikeClick(@NonNull Post post, int position) {
                User currentUser = MockDataGenerator.getInstance().getCurrentUser();
                String userId = currentUser != null ? currentUser.getId() : "user_current";
                MockDataGenerator.getInstance().toggleLike(post.getId(), userId);
                postAdapter.updatePostAt(position, post);
              }

              @Override
              public void onCommentClick(@NonNull Post post) {
                // Sẽ được Minh Lương kết nối với CommentBottomSheetDialog ([D5-ML])
                showToast("Tính năng Bình luận đang được hoàn thiện ([D5-ML])");
              }

              @Override
              public void onShareClick(@NonNull Post post) {
                // Sẽ được Minh Lương kết nối với AlertDialog Chia sẻ ([D6-ML])
                showToast("Tính năng Chia sẻ đang được hoàn thiện ([D6-ML])");
              }
            });

    getBinding().rvFeedPosts.setAdapter(postAdapter);
  }

  /**
   * Tải danh sách bài viết từ MockDataGenerator và cập nhật trạng thái hiển thị Bảng tin.
   */
  private void loadFeedPosts() {
    List<Post> posts = MockDataGenerator.getInstance().getPosts();
    if (postAdapter != null) {
      postAdapter.setPosts(posts);
    }

    boolean isEmpty = (posts == null || posts.isEmpty());
    getBinding().layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    getBinding().rvFeedPosts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
  }

  /**
   * Tải ảnh đại diện của người dùng đang đăng nhập vào thanh tạo bài viết nhanh.
   */
  private void loadCurrentUserAvatar() {
    User currentUser = MockDataGenerator.getInstance().getCurrentUser();
    if (currentUser != null && currentUser.getAvatarUrl() != null && getContext() != null) {
      Glide.with(this)
          .load(currentUser.getAvatarUrl())
          .circleCrop()
          .placeholder(R.drawable.bg_avatar_placeholder)
          .error(R.drawable.bg_avatar_placeholder)
          .into(getBinding().ivCurrentUserAvatar);
    }
  }
}
