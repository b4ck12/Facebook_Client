package com.facebook.client.features.comment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.facebook.client.data.model.Comment;
import com.facebook.client.data.repository.IPostRepository;
import com.facebook.client.data.repository.PostRepositoryImpl;//Day 9
import com.facebook.client.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel quản lý logic tải và gửi bình luận văn bản (Text-only) cho bài viết ([D6-ML]).
 * Sử dụng LiveData để cập nhật giao diện tự động theo chuẩn MVVM Clean Architecture.
 */
public class CommentViewModel extends ViewModel {

    private final IPostRepository postRepository;

    private final MutableLiveData<List<Comment>> commentsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public CommentViewModel() {
        this(new PostRepositoryImpl());//Day 9
    }

    public CommentViewModel(@NonNull IPostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * LiveData quan sát danh sách bình luận của bài viết.
     */
    @NonNull
    public LiveData<List<Comment>> getCommentsLiveData() {
        return commentsLiveData;
    }

    /**
     * LiveData quan sát trạng thái đang tải dữ liệu.
     */
    @NonNull
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * LiveData quan sát thông báo lỗi nếu có.
     */
    @NonNull
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Tải toàn bộ danh sách bình luận của một bài viết theo postId.
     *
     * @param postId Mã định danh bài viết cần lấy bình luận.
     */
    public void loadComments(@NonNull String postId) {
        isLoading.setValue(true);
        postRepository.getComments(postId, new RepositoryCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> result) {
                isLoading.postValue(false);
                commentsLiveData.postValue(result != null ? new ArrayList<>(result) : new ArrayList<>());
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message != null ? message : "Không thể tải bình luận, vui lòng thử lại!");
            }
        });
    }

    /**
     * Gửi một bình luận văn bản (Text-only) mới lên hệ thống.
     * Tự động thêm vào danh sách và cập nhật UI tức thì khi gửi thành công.
     *
     * @param postId   Mã bài viết được bình luận.
     * @param authorId Mã người gửi bình luận (lấy từ SessionManager).
     * @param content  Nội dung chữ của bình luận (bắt buộc không rỗng).
     */
    public void sendComment(@NonNull String postId, @Nullable String authorId, @Nullable String content) {
        if (content == null || content.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập nội dung bình luận!");
            return;
        }

        String validAuthorId = (authorId != null && !authorId.trim().isEmpty()) ? authorId : "user_101";

        postRepository.addComment(postId, validAuthorId, content.trim(), new RepositoryCallback<Comment>() {
            @Override
            public void onSuccess(Comment newComment) {
                // Lấy danh sách hiện tại, thêm comment mới vào và postValue cho UI
                List<Comment> currentList = commentsLiveData.getValue();
                List<Comment> updatedList = new ArrayList<>();
                if (currentList != null) {
                    updatedList.addAll(currentList);
                }
                updatedList.add(newComment);
                commentsLiveData.postValue(updatedList);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message != null ? message : "Gửi bình luận thất bại!");
            }
        });
    }
}