package com.facebook.client.data.repository;
import com.facebook.client.data.model.FriendRequest;
import com.facebook.client.data.model.User;
import java.util.ArrayList;
import java.util.List;
public class MockFriendRepository implements IFriendRepository {
    private final List<FriendRequest> mockRequests = new ArrayList<>();
    private final List<User> mockFriends = new ArrayList<>();
    public MockFriendRepository() {
        initMockData();
}
private void initMockData(){
        long now = System.currentTimeMillis();
        mockRequests.add(new FriendRequest(
                "req_1",
                "user_1",
                "nguyen a",
                "https://i.pravatar.cc/150?img=1",
                "user_current",
                FriendRequest.STATUS_PENDING,
                now - (10*60*1000)
        ));
        mockRequests.add(new FriendRequest(
                "req_2",
                "user_2",
                "nguyen b",
                "https://i.pravatar.cc/150?img=2",
                "user_current",
                FriendRequest.STATUS_PENDING,
                now - (2*60*60*1000)
        ));
        mockFriends.add(new User(
                "user_103",
                "C@gmail.com",
                "nguyen c",
                "https://i.pravatar.cc/150?img=103",
                "https://i.pravatar.cc/300?img=103",
                now - (100*24*60*60*1000L)
        ));
    }
    @Override
    public void getFriendRequests(String userId, RepositoryCallback<List<FriendRequest>> callback) {
        if (callback != null) {
            callback.onSuccess(new ArrayList<>(mockRequests));
        }
    }
    @Override
    public void getFriends(String userId, RepositoryCallback<List<User>> callback) {
        if (callback != null) {
            callback.onSuccess(new ArrayList<>(mockFriends));
        }
    }
    @Override
    public void acceptFriendRequest(String requestId, RepositoryCallback<Boolean> callback) {
        if (callback == null) return;

        for (int i = 0; i < mockRequests.size(); i++) {
            FriendRequest req = mockRequests.get(i);
            if (req.getId().equals(requestId)) {
                // Xóa khỏi danh sách lời mời
                mockRequests.remove(i);

                // Thêm vào danh sách bạn bè
                mockFriends.add(new User(
                        req.getRequesterId(),
                        req.getRequesterName().toLowerCase().replace(" ", "") + "@gmail.com",
                        req.getRequesterName(),
                        req.getRequesterAvatar(),
                        "",
                        System.currentTimeMillis()
                ));

                callback.onSuccess(true);
                return;
            }
        }
        callback.onError("Không tìm thấy lời mời với ID: " + requestId);
    }
    @Override
    public void rejectFriendRequest(String requestId, RepositoryCallback<Boolean> callback) {
        if (callback == null) return;

        for (int i = 0; i < mockRequests.size(); i++) {
            if (mockRequests.get(i).getId().equals(requestId)) {
                mockRequests.remove(i);
                callback.onSuccess(true);
                return;
            }
        }
        callback.onError("Không tìm thấy lời mời với ID: " + requestId);
    }
    @Override
    public void searchFriend(String query, String userId, RepositoryCallback<List<User>> callback) {
        if (callback == null) return;

        List<User> searchResult = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            callback.onSuccess(searchResult);
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        for (User friend : mockFriends) {
            if (friend.getDisplayName() != null && friend.getDisplayName().toLowerCase().contains(lowerQuery)) {
                searchResult.add(friend);
            }
        }
        callback.onSuccess(searchResult);
    }
}