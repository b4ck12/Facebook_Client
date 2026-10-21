package com.facebook.client.data.repository;

import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.FriendRequest;
import com.facebook.client.data.model.User;

import java.util.List;

/**
 * Repository giả lập (Mock Data) phục vụ phân hệ Bạn bè (Friends) và Tìm kiếm bạn bè.
 * Kết nối trực tiếp với {@link MockDataGenerator} (Single Source of Truth) để đảm bảo 100% tính nhất quán dữ liệu.
 *
 * <p>Thành viên phụ trách phát triển: Phúc Đức (FE).
 */
public class MockFriendRepository implements IFriendRepository {

  private final MockDataGenerator generator;

  public MockFriendRepository() {
    this(MockDataGenerator.getInstance());
  }

  public MockFriendRepository(MockDataGenerator generator) {
    this.generator = generator != null ? generator : MockDataGenerator.getInstance();
  }

  @Override
  public void getFriendRequests(String userId, RepositoryCallback<List<FriendRequest>> callback) {
    if (callback == null) return;
    try {
      List<FriendRequest> requests = generator.getFriendRequests(userId);
      callback.onSuccess(requests);
    } catch (Exception e) {
      callback.onError("Lỗi khi tải danh sách lời mời kết bạn: " + e.getMessage());
    }
  }

  @Override
  public void getFriends(String userId, RepositoryCallback<List<User>> callback) {
    if (callback == null) return;
    try {
      List<User> friends = generator.getFriends(userId);
      callback.onSuccess(friends);
    } catch (Exception e) {
      callback.onError("Lỗi khi tải danh sách bạn bè: " + e.getMessage());
    }
  }

  @Override
  public void acceptFriendRequest(String requestId, RepositoryCallback<Boolean> callback) {
    if (callback == null) return;
    try {
      boolean success = generator.acceptFriendRequest(requestId);
      if (success) {
        callback.onSuccess(true);
      } else {
        callback.onError("Không tìm thấy lời mời với ID: " + requestId);
      }
    } catch (Exception e) {
      callback.onError("Lỗi khi chấp nhận lời mời kết bạn: " + e.getMessage());
    }
  }

  @Override
  public void rejectFriendRequest(String requestId, RepositoryCallback<Boolean> callback) {
    if (callback == null) return;
    try {
      boolean success = generator.rejectFriendRequest(requestId);
      if (success) {
        callback.onSuccess(true);
      } else {
        callback.onError("Không tìm thấy lời mời với ID: " + requestId);
      }
    } catch (Exception e) {
      callback.onError("Lỗi khi từ chối lời mời kết bạn: " + e.getMessage());
    }
  }

  @Override
  public void searchFriend(String query, String userId, RepositoryCallback<List<User>> callback) {
    if (callback == null) return;
    try {
      List<User> searchResult = generator.searchFriends(query);
      callback.onSuccess(searchResult);
    } catch (Exception e) {
      callback.onError("Lỗi khi tìm kiếm bạn bè: " + e.getMessage());
    }
  }
}
