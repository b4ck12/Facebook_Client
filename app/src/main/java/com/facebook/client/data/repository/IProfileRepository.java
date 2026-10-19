package com.facebook.client.data.repository;

import com.facebook.client.data.model.Post;
import com.facebook.client.data.model.User;

import java.util.List;

/**
 * Hợp đồng chức năng (Contract) cho phân hệ Trang cá nhân (Profile).
 */
public interface IProfileRepository {

    /** Lấy thông tin cá nhân của người dùng (ảnh bìa 200dp, avatar tròn 120dp, tên hiển thị) */
    void getUserProfile(String userId, RepositoryCallback<User> callback);

    /** Lấy danh sách toàn bộ bài viết do chính user này đăng (Tab All Posts) */
    void getUserPosts(String userId, RepositoryCallback<List<Post>> callback);

    /** Lấy kho ảnh của user (Tab Photos/Videos dạng lưới 3 cột theo đặc tả PDF) */
    void getUserPhotos(String userId, RepositoryCallback<List<Post>> callback);
}
