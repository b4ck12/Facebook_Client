package com.facebook.client.features.feed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.facebook.client.data.model.Post;
import com.facebook.client.data.repository.IPostRepository;
import com.facebook.client.data.repository.PostRepositoryImpl;
import com.facebook.client.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel quản lý dữ liệu và logic nghiệp vụ cho màn hình Bảng tin.
 * <p>
 * Main :
 * 1. Kết nối với PostRepositoryImpl để tải danh sách bài viết thật từ FastAPI + SQLite (GET /api/posts).
 * 2. Quản lý trạng thái Loading, Empty State và danh sách bài viết qua LiveData.
 * 3. Đồng bộ thao tác Thích bài viết (POST /api/posts/{id}/like) và Chia sẻ bài viết (POST /api/posts/{id}/share) lên máy chủ.
 */
@SuppressWarnings("SpellCheckingInspection")
public class FeedViewModel extends ViewModel {

    private final IPostRepository postRepository;

    private final MutableLiveData<List<Post>> feedPostsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * Khởi tạo ViewModel mặc định sử dụng PostRepositoryImpl kết nối FastAPI ([D8-ML]).
     */
    public FeedViewModel() {
        this(new PostRepositoryImpl());
    }

    /**
     * Constructor hỗ trợ truyền Repository từ bên ngoài (phục vụ Unit Test).
     */
    public FeedViewModel(@NonNull IPostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @NonNull
    public LiveData<List<Post>> getFeedPostsLiveData() {
        return feedPostsLiveData;
    }

    @NonNull
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    @NonNull
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Tải danh sách bài viết mới nhất trên Bảng tin từ FastAPI + SQLite.
     *
     * @param limit         Số lượng bài viết tối đa cần lấy.
     * @param currentUserId Mã người dùng đang đăng nhập (để kiểm tra trạng thái isLiked).
     */
    public void loadFeedPosts(int limit, @Nullable String currentUserId) {
        isLoading.setValue(true);
        String validUserId = (currentUserId != null && !currentUserId.trim().isEmpty())
                ? currentUserId.trim()
                : "user_101";

        postRepository.getFeedPosts(limit, validUserId, new RepositoryCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> posts) {
                isLoading.postValue(false);
                feedPostsLiveData.postValue(posts != null ? new ArrayList<>(posts) : new ArrayList<>());
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message != null ? message : "Không thể tải bảng tin!");
            }
        });
    }

    /**
     * Đồng bộ thao tác bật/tắt Thích bài viết (Toggle Like) lên máy chủ FastAPI.
     *
     * @param post   Bài viết được bấm Thích.
     * @param userId Mã người dùng thực hiện thao tác Thích.
     */
    public void toggleLike(@NonNull Post post, @Nullable String userId) {
        String validUserId = (userId != null && !userId.trim().isEmpty())
                ? userId.trim()
                : "user_101";

        postRepository.toggleLike(post.getId(), validUserId, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isLikedOnServer) {
                // Đã đồng bộ trạng thái Like lên server SQLite thành công
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message != null ? message : "Không thể đồng bộ lượt thích!");
            }
        });
    }

    /**
     * Chia sẻ bài viết lên Bảng tin qua endpoint POST /api/posts/{id}/share.
     *
     * @param originalPostId Mã bài viết gốc.
     * @param authorId       Mã người dùng chia sẻ.
     * @param caption        Nội dung cảm nghĩ đính kèm khi chia sẻ.
     * @param callback       Callback trả kết quả về cho FeedFragment hiển thị thông báo và cuộn lên đầu.
     */
    public void sharePost(
            @NonNull String originalPostId,
            @Nullable String authorId,
            @Nullable String caption,
            @Nullable RepositoryCallback<Post> callback) {

        String validUserId = (authorId != null && !authorId.trim().isEmpty())
                ? authorId.trim()
                : "user_101";

        postRepository.sharePost(originalPostId, validUserId, caption, new RepositoryCallback<Post>() {
            @Override
            public void onSuccess(Post sharedPost) {
                List<Post> currentList = feedPostsLiveData.getValue();
                List<Post> updatedList = new ArrayList<>();
                updatedList.add(sharedPost);
                if (currentList != null) {
                    updatedList.addAll(currentList);
                }
                feedPostsLiveData.postValue(updatedList);

                if (callback != null) {
                    callback.onSuccess(sharedPost);
                }
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message != null ? message : "Chia sẻ bài viết thất bại!");
                if (callback != null) {
                    callback.onError(message);
                }
            }
        });
    }
}