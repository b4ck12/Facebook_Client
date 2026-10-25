package com.facebook.client.data.repository;

import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.Post;
import com.facebook.client.data.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository giả lập (Mock Data) phục vụ phân hệ Trang cá nhân (Profile - [D5-PD], [D6-TQH]).
 * Cung cấp thông tin người dùng (Ảnh bìa, Avatar, Tên), danh sách bài viết cá nhân và kho ảnh lưới 3 cột.
 */
public class MockProfileRepository implements IProfileRepository {

    private final MockDataGenerator generator = MockDataGenerator.getInstance();
    private final IPostRepository postRepository = new MockPostRepository();
    @Override
    public void getUserProfile(String userId, RepositoryCallback<User> callback) {
        if (callback == null) return;

        User user = (userId != null) ? generator.getUserById(userId) : generator.getCurrentUser();
        if (user == null) {
            user = generator.getCurrentUser();
        }

        if (user != null) {
            callback.onSuccess(user);
        } else {
            callback.onError("Không tìm thấy thông tin người dùng");
        }
    }

    @Override
    public void getUserPosts(
            String userId,
            RepositoryCallback<List<Post>> callback
    ) {
        if (callback == null) return;

        String targetId = userId != null
                ? userId
                : generator.getCurrentUserId();

        postRepository.getFeedPosts(
                0,
                generator.getCurrentUserId(),
                new RepositoryCallback<List<Post>>() {
                    @Override
                    public void onSuccess(List<Post> posts) {
                        List<Post> userPosts = new ArrayList<>();

                        for (Post post : posts) {
                            if (targetId.equals(post.getAuthorId())) {
                                userPosts.add(post);
                            }
                        }

                        callback.onSuccess(userPosts);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError("Unable to load posts.");
                    }
                }
        );
    }

    @Override
    public void getUserPhotos(String userId, RepositoryCallback<List<Post>> callback) {
        if (callback == null) return;

        String targetId = (userId != null) ? userId : generator.getCurrentUserId();
        List<Post> allPosts = generator.getPosts();
        List<Post> photoPosts = new ArrayList<>();

        // Lọc tất cả bài viết của user mà có ảnh đính kèm (mediaUrl != null)
        for (Post p : allPosts) {
            if (targetId.equals(p.getAuthorId()) && p.getMediaUrl() != null && !p.getMediaUrl().trim().isEmpty()) {
                photoPosts.add(p);
            }
        }

        // Bổ sung thêm một số ảnh mẫu đẹp chất lượng cao nếu user có ít ảnh (phục vụ hiển thị đầy đủ lưới 3 cột)

        callback.onSuccess(photoPosts);
    }
}
