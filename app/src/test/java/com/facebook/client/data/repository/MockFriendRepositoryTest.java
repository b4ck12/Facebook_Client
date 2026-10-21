package com.facebook.client.data.repository;

import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.FriendRequest;
import com.facebook.client.data.model.User;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Unit Test kiểm thử MockFriendRepository.
 * Đảm bảo 100% tính nhất quán với MockDataGenerator và các luồng nghiệp vụ Bạn bè.
 */
public class MockFriendRepositoryTest {

  private MockFriendRepository repository;
  private MockDataGenerator generator;

  @Before
  public void setUp() {
    generator = MockDataGenerator.getInstance();
    generator.resetData();
    repository = new MockFriendRepository(generator);
  }

  @Test
  public void testGetFriendRequests_Success() {
    AtomicReference<List<FriendRequest>> resultRef = new AtomicReference<>();

    repository.getFriendRequests("user_current", new RepositoryCallback<List<FriendRequest>>() {
      @Override
      public void onSuccess(List<FriendRequest> data) {
        resultRef.set(data);
      }

      @Override
      public void onError(String message) {
        fail("Không được gọi onError: " + message);
      }
    });

    List<FriendRequest> requests = resultRef.get();
    assertNotNull("Danh sách lời mời kết bạn không được null", requests);
    assertEquals("Số lượng lời mời kết bạn ban đầu phải là 2", 2, requests.size());
    assertEquals("Người gửi lời mời 1 phải là user_101", "user_101", requests.get(0).getRequesterId());
    assertEquals("Người gửi lời mời 2 phải là user_102", "user_102", requests.get(1).getRequesterId());
  }

  @Test
  public void testGetFriends_Success() {
    AtomicReference<List<User>> resultRef = new AtomicReference<>();

    repository.getFriends("user_current", new RepositoryCallback<List<User>>() {
      @Override
      public void onSuccess(List<User> data) {
        resultRef.set(data);
      }

      @Override
      public void onError(String message) {
        fail("Không được gọi onError: " + message);
      }
    });

    List<User> friends = resultRef.get();
    assertNotNull("Danh sách bạn bè không được null", friends);
    assertEquals("Số lượng bạn bè ban đầu phải là 2", 2, friends.size());
    assertEquals("Bạn bè 1 phải là user_103", "user_103", friends.get(0).getId());
    assertEquals("Bạn bè 2 phải là user_104", "user_104", friends.get(1).getId());
  }

  @Test
  public void testAcceptFriendRequest_Success() {
    AtomicBoolean successFlag = new AtomicBoolean(false);

    repository.acceptFriendRequest("req_1", new RepositoryCallback<Boolean>() {
      @Override
      public void onSuccess(Boolean data) {
        successFlag.set(data);
      }

      @Override
      public void onError(String message) {
        fail("Không được gọi onError: " + message);
      }
    });

    assertTrue("Chấp nhận lời mời kết bạn phải thành công", successFlag.get());

    // Kiểm tra danh sách lời mời còn lại 1
    repository.getFriendRequests("user_current", new RepositoryCallback<List<FriendRequest>>() {
      @Override
      public void onSuccess(List<FriendRequest> data) {
        assertEquals("Số lời mời còn lại phải là 1", 1, data.size());
      }

      @Override
      public void onError(String message) {}
    });

    // Kiểm tra danh sách bạn bè tăng lên 3
    repository.getFriends("user_current", new RepositoryCallback<List<User>>() {
      @Override
      public void onSuccess(List<User> data) {
        assertEquals("Số bạn bè mới phải là 3", 3, data.size());
      }

      @Override
      public void onError(String message) {}
    });
  }

  @Test
  public void testRejectFriendRequest_Success() {
    AtomicBoolean successFlag = new AtomicBoolean(false);

    repository.rejectFriendRequest("req_2", new RepositoryCallback<Boolean>() {
      @Override
      public void onSuccess(Boolean data) {
        successFlag.set(data);
      }

      @Override
      public void onError(String message) {
        fail("Không được gọi onError: " + message);
      }
    });

    assertTrue("Từ chối lời mời kết bạn phải thành công", successFlag.get());

    // Kiểm tra danh sách lời mời còn lại 1
    repository.getFriendRequests("user_current", new RepositoryCallback<List<FriendRequest>>() {
      @Override
      public void onSuccess(List<FriendRequest> data) {
        assertEquals("Số lời mời còn lại phải là 1", 1, data.size());
      }

      @Override
      public void onError(String message) {}
    });
  }

  @Test
  public void testSearchFriend_Success() {
    AtomicReference<List<User>> resultRef = new AtomicReference<>();

    repository.searchFriend("Nam", "user_current", new RepositoryCallback<List<User>>() {
      @Override
      public void onSuccess(List<User> data) {
        resultRef.set(data);
      }

      @Override
      public void onError(String message) {
        fail("Không được gọi onError: " + message);
      }
    });

    List<User> searchResults = resultRef.get();
    assertNotNull("Kết quả tìm kiếm không được null", searchResults);
    assertEquals("Phải tìm thấy 1 người có tên chứa 'Nam'", 1, searchResults.size());
    assertEquals("Tên người tìm thấy phải là Lê Hoàng Nam", "Lê Hoàng Nam", searchResults.get(0).getDisplayName());
  }
}
