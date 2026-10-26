package com.facebook.client.data.repository;


import com.facebook.client.data.model.Comment;
import com.facebook.client.data.model.Post;

import java.io.File;
import java.util.List;

/**
 * Hợp đồng chức năng (Contract) cho phân hệ Bảng tin (Home Feed) và Tương tác bài viết.
 */
public interface IPostRepository {

    /** Lấy danh sách bài viết mới nhất trên Bảng tin Feed kèm cờ isLiked và số lượt like */
    void getFeedPosts(int limit, String currentUserId, RepositoryCallback<List<Post>> callback);

    /** Upload tệp hình ảnh lên máy chủ FastAPI (POST /api/upload) và trả về đường dẫn URL tĩnh */
    default void uploadImage(File imageFile, RepositoryCallback<String> callback) {
        if (callback != null) {
            if (imageFile == null || !imageFile.exists()) {
                callback.onError("Tệp hình ảnh không hợp lệ hoặc không tồn tại!");
            } else {
                callback.onSuccess(imageFile.toURI().toString());
            }
        }
    }

    /** Tạo bài viết mới lên hệ thống (yêu cầu có chữ hoặc có ảnh) */
    void createPost(String authorId, String content, String mediaUrl, boolean isPublic, RepositoryCallback<Post> callback);

    /** Bật/Tắt trạng thái Thích bài viết (Toggle Like trong bảng post_likes) */
    void toggleLike(String postId, String userId, RepositoryCallback<Boolean> callback);

    /** Lấy danh sách toàn bộ bình luận của 1 bài viết (Chuẩn Text-only theo đặc tả PDF) */
    void getComments(String postId, RepositoryCallback<List<Comment>> callback);

    /** Gửi thêm bình luận văn bản mới vào bài viết */
    void addComment(String postId, String authorId, String content, RepositoryCallback<Comment> callback);

    /** Chia sẻ bài viết ngay lên trang cá nhân kèm dòng cảm nghĩ ngắn */
    void sharePost(String originalPostId, String authorId, String caption, RepositoryCallback<Post> callback);
}
