package com.facebook.client.data.repository;

import com.facebook.client.data.model.FriendRequest;
import com.facebook.client.data.model.User;

import java.util.List;

/**
 * Hợp đồng chức năng (Contract) cho phân hệ Bạn bè và Tìm kiếm bạn bè.
 */
public interface IFriendRepository {

    /** Lấy danh sách toàn bộ lời mời kết bạn gửi đến người dùng */
    void getFriendRequests(String userId, RepositoryCallback<List<FriendRequest>> callback);

    /** Lấy danh sách bạn bè hiện tại của người dùng */
    void getFriends(String userId, RepositoryCallback<List<User>> callback);

    /** Chấp nhận lời mời kết bạn (Cập nhật status sang ACCEPTED) */
    void acceptFriendRequest(String requestId, RepositoryCallback<Boolean> callback);

    /** Từ chối hoặc Xóa lời mời kết bạn (Xóa khỏi danh sách lời mời) */
    void rejectFriendRequest(String requestId, RepositoryCallback<Boolean> callback);

    /** Tìm kiếm bạn bè theo tên (hỗ trợ Debounce 300ms chống spam gọi API) */
    void searchFriend(String query, String userId, RepositoryCallback<List<User>> callback);
}
