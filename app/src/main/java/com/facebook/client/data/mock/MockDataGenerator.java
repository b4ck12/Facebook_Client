package com.facebook.client.data.mock;

import com.facebook.client.data.model.Comment;
import com.facebook.client.data.model.FriendRequest;
import com.facebook.client.data.model.Notification;
import com.facebook.client.data.model.Post;
import com.facebook.client.data.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lớp khởi tạo và quản lý Dữ liệu Giả lập (Mock Data Generator) tập trung cho toàn bộ ứng dụng Android.
 * Đóng vai trò Single Source of Truth cho tầng dữ liệu Mock ở Tuần 1.
 * Đảm bảo 100% tính nhất quán dữ liệu (Data Parity) với CSDL SQLite (facebook.db) trên Backend.
 */
public class MockDataGenerator {

    private static volatile MockDataGenerator INSTANCE;

    private final List<User> users = new CopyOnWriteArrayList<>();
    private final List<Post> posts = new CopyOnWriteArrayList<>();
    private final Map<String, List<Comment>> commentsMap = new ConcurrentHashMap<>();
    private final List<FriendRequest> friendRequests = new CopyOnWriteArrayList<>();
    private final List<User> friends = new CopyOnWriteArrayList<>();
    private final List<FriendRequest> acceptedFriendRequests = new CopyOnWriteArrayList<>();
    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    private MockDataGenerator() {
        initData();
    }

    /**
     * Singleton Pattern an toàn đa luồng (Double-Checked Locking).
     */
    public static MockDataGenerator getInstance() {
        if (INSTANCE == null) {
            synchronized (MockDataGenerator.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MockDataGenerator();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Khởi tạo toàn bộ dữ liệu mẫu chuẩn Facebook Client cho 5 phân hệ.
     */
    private synchronized void initData() {
        users.clear();
        posts.clear();
        commentsMap.clear();
        friendRequests.clear();
        friends.clear();
        acceptedFriendRequests.clear();
        notifications.clear();

        long now = System.currentTimeMillis();

        // 1. Khởi tạo 6 Người dùng mẫu (Users)
        User currentUser = new User(
                "user_current",
                "current.user@facebook.com",
                "Người dùng hiện tại",
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                "https://images.unsplash.com/photo-1707343843437-caacff5cfa74?w=1200",
                now - (120L * 24 * 60 * 60 * 1000)
        );
        User user101 = new User(
                "user_101",
                "an.nguyen@facebook.com",
                "Nguyễn Văn An",
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200",
                now - (90L * 24 * 60 * 60 * 1000)
        );
        User user102 = new User(
                "user_102",
                "mai.tran@facebook.com",
                "Trần Thị Mai",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200",
                now - (80L * 24 * 60 * 60 * 1000)
        );
        User user103 = new User(
                "user_103",
                "nam.le@facebook.com",
                "Lê Hoàng Nam",
                "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
                "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=1200",
                now - (75L * 24 * 60 * 60 * 1000)
        );
        User user104 = new User(
                "user_104",
                "tuan.pham@facebook.com",
                "Phạm Minh Tuấn",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=1200",
                now - (60L * 24 * 60 * 60 * 1000)
        );
        User user105 = new User(
                "user_105",
                "thao.dang@facebook.com",
                "Đặng Thu Thảo",
                "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150",
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=1200",
                now - (45L * 24 * 60 * 60 * 1000)
        );

        users.add(currentUser);
        users.add(user101);
        users.add(user102);
        users.add(user103);
        users.add(user104);
        users.add(user105);

        // 2. Khởi tạo 10 Bài viết mẫu (Posts) chuẩn hóa từ MockPostRepository
        posts.add(new Post(
                "post_1001", "user_101", "Nguyễn Văn An",
                user101.getAvatarUrl(),
                "Cuối tuần đi săn mây Đà Lạt cùng hội bạn thân, thời tiết se lạnh tuyệt vời quá mọi người ơi!",
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
                true, 24, 3, false, now - (5 * 60 * 1000)
        ));

        posts.add(new Post(
                "post_1002", "user_102", "Trần Thị Mai",
                user102.getAvatarUrl(),
                "Đồ án môn Phát triển Ứng dụng Di động đang chạy đúng tiến độ! Cả nhóm 7 anh em cố gắng hoàn thành xuất sắc nhé!",
                null,
                true, 42, 2, true, now - (20 * 60 * 1000)
        ));

        posts.add(new Post(
                "post_1003", "user_103", "Lê Hoàng Nam",
                user103.getAvatarUrl(),
                "Góc làm việc mới setup để chiến code Android Studio và Python FastAPI xuyên màn đêm.",
                "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800",
                true, 18, 1, false, now - (60 * 60 * 1000)
        ));

        posts.add(new Post(
                "post_1004", "user_104", "Phạm Minh Tuấn",
                user104.getAvatarUrl(),
                "Bữa sáng nhẹ nhàng với phở Hà Nội truyền thống trước giờ lên giảng đường.",
                "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800",
                true, 35, 0, true, now - (2 * 60 * 60 * 1000)
        ));

        posts.add(new Post(
                "post_1005", "user_105", "Đặng Thu Thảo",
                user105.getAvatarUrl(),
                "Có ai biết quán cà phê nào yên tĩnh quanh khu vực trường mình để ngồi học nhóm không ạ? Cho mình xin địa chỉ với!",
                null,
                true, 12, 1, false, now - (3 * 60 * 60 * 1000)
        ));

        posts.add(new Post(
                "post_1006", "user_101", "Nguyễn Văn An",
                user101.getAvatarUrl(),
                "Một ly Espresso nóng cho buổi chiều tỉnh táo fix bug.",
                "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800",
                true, 29, 0, false, now - (5 * 60 * 60 * 1000)
        ));

        posts.add(new Post(
                "post_1007", "user_103", "Lê Hoàng Nam",
                user103.getAvatarUrl(),
                "Chiều nay 17h30 đá bóng sân cỏ nhân tạo nhé anh em! Đừng ai cao su nhé.",
                "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=800",
                true, 51, 0, true, now - (8 * 60 * 60 * 1000)
        ));

        posts.add(new Post(
                "post_1008", "user_104", "Phạm Minh Tuấn",
                user104.getAvatarUrl(),
                "Áp dụng kiến trúc MVVM + Clean Architecture kết hợp ViewBinding giúp code Android gọn gàng và dễ viết Unit Test hơn hẳn.",
                null,
                true, 64, 0, false, now - (12 * 60 * 60 * 1000)
        ));

        posts.add(new Post(
                "post_1009", "user_102", "Trần Thị Mai",
                user102.getAvatarUrl(),
                "Nhớ biển Phú Quốc mùa nắng đẹp.",
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
                true, 88, 0, true, now - (24 * 60 * 60 * 1000)
        ));

        posts.add(new Post(
                "post_1010", "user_105", "Đặng Thu Thảo",
                user105.getAvatarUrl(),
                "Cuốn sách Clean Code thực sự rất đáng đọc cho mọi lập trình viên.",
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800",
                true, 45, 0, false, now - (48 * 60 * 60 * 1000)
        ));

        // 3. Khởi tạo 7 Bình luận (Comments - Text only)
        List<Comment> cmtPost1001 = new CopyOnWriteArrayList<>();
        cmtPost1001.add(new Comment("cmt_1", "post_1001", "user_102", "Trần Thị Mai", user102.getAvatarUrl(),
                "Chuyến đi tuyệt vời quá bạn ơi!", now - (4 * 60 * 1000)));
        cmtPost1001.add(new Comment("cmt_2", "post_1001", "user_103", "Lê Hoàng Nam", user103.getAvatarUrl(),
                "Ảnh chụp nét căng, săn mây ở đồi nào thế An?", now - (3 * 60 * 1000)));
        cmtPost1001.add(new Comment("cmt_3", "post_1001", "user_104", "Phạm Minh Tuấn", user104.getAvatarUrl(),
                "Đợt tới bảo vệ đồ án xong cả nhóm mình làm một chuyến nhé!", now - (1 * 60 * 1000)));
        commentsMap.put("post_1001", cmtPost1001);

        List<Comment> cmtPost1002 = new CopyOnWriteArrayList<>();
        cmtPost1002.add(new Comment("cmt_4", "post_1002", "user_101", "Nguyễn Văn An", user101.getAvatarUrl(),
                "Quyết tâm đạt điểm A+ môn này nhé cả nhà!", now - (15 * 60 * 1000)));
        cmtPost1002.add(new Comment("cmt_5", "post_1002", "user_105", "Đặng Thu Thảo", user105.getAvatarUrl(),
                "Giao diện lên hình mượt lắm rồi!", now - (10 * 60 * 1000)));
        commentsMap.put("post_1002", cmtPost1002);

        List<Comment> cmtPost1003 = new CopyOnWriteArrayList<>();
        cmtPost1003.add(new Comment("cmt_6", "post_1003", "user_104", "Phạm Minh Tuấn", user104.getAvatarUrl(),
                "Góc setup gọn gàng và chất lượng quá Nam ơi.", now - (30 * 60 * 1000)));
        commentsMap.put("post_1003", cmtPost1003);

        List<Comment> cmtPost1005 = new CopyOnWriteArrayList<>();
        cmtPost1005.add(new Comment("cmt_7", "post_1005", "user_102", "Trần Thị Mai", user102.getAvatarUrl(),
                "Qua quán The Coffee House ngay cổng trường tầng 2 yên tĩnh lắm Thảo ơi.", now - (2 * 60 * 60 * 1000)));
        commentsMap.put("post_1005", cmtPost1005);

        // 4. Khởi tạo 2 Lời mời kết bạn (Friend Requests) gửi tới user_current
        friendRequests.add(new FriendRequest(
                "req_1", "user_101", "Nguyễn Văn An", user101.getAvatarUrl(),
                "user_current", FriendRequest.STATUS_PENDING, now - (10 * 60 * 1000)
        ));
        friendRequests.add(new FriendRequest(
                "req_2", "user_102", "Trần Thị Mai", user102.getAvatarUrl(),
                "user_current", FriendRequest.STATUS_PENDING, now - (2 * 60 * 60 * 1000)
        ));


        // Lời mời riêng cho ba tài khoản demo; không đổi hai lời mời của user_current.
        friendRequests.add(new FriendRequest("req_demo_101", "user_104", user104.getDisplayName(),
                user104.getAvatarUrl(), "user_101", FriendRequest.STATUS_PENDING, now - 600_000L));
        friendRequests.add(new FriendRequest("req_demo_102", "user_105", user105.getDisplayName(),
                user105.getAvatarUrl(), "user_102", FriendRequest.STATUS_PENDING, now - 900_000L));
        friendRequests.add(new FriendRequest("req_demo_103", "user_102", user102.getDisplayName(),
                user102.getAvatarUrl(), "user_103", FriendRequest.STATUS_PENDING, now - 1_200_000L));

        // Khởi tạo danh sách bạn bè ban đầu
        friends.add(user103);
        friends.add(user104);

        // 5. Khởi tạo 5 Thông báo (Notifications)
        notifications.add(new Notification(
                "notif_1", "user_current", "user_101", "Nguyễn Văn An", user101.getAvatarUrl(),
                Notification.TYPE_LIKE, "post_1001", "đã thích bài viết của bạn.", false, now - (5 * 60 * 1000)
        ));
        notifications.add(new Notification(
                "notif_2", "user_current", "user_102", "Trần Thị Mai", user102.getAvatarUrl(),
                Notification.TYPE_COMMENT, "post_1001", "đã bình luận về bài viết của bạn: \"Chuyến đi tuyệt vời quá bạn ơi!\"", false, now - (25 * 60 * 1000)
        ));
        notifications.add(new Notification(
                "notif_3", "user_current", "user_103", "Lê Hoàng Nam", user103.getAvatarUrl(),
                Notification.TYPE_FRIEND_REQUEST, null, "đã gửi cho bạn một lời mời kết bạn.", true, now - (2 * 60 * 60 * 1000)
        ));
        notifications.add(new Notification(
                "notif_4", "user_current", "user_104", "Phạm Minh Tuấn", user104.getAvatarUrl(),
                Notification.TYPE_FRIEND_ACCEPT, null, "đã chấp nhận lời mời kết bạn của bạn.", true, now - (24 * 60 * 60 * 1000)
        ));
        notifications.add(new Notification(
                "notif_5", "user_current", "user_105", "Đặng Thu Thảo", user105.getAvatarUrl(),
                Notification.TYPE_LIKE, "post_1002", "đã thích bài viết của bạn.", true, now - (48 * 60 * 60 * 1000)
        ));
    }

    // ==================== QUẢN LÝ NGƯỜI DÙNG (USERS) ====================

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    private volatile String currentUserId = "user_current";

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String userId) {
        if (userId != null && getUserById(userId) != null) {
            this.currentUserId = userId;
        }
    }

    public User getCurrentUser() {
        User u = getUserById(currentUserId);
        return u != null ? u : getUserById("user_current");
    }

    public List<User> getDemoAccounts() {
        List<User> demoList = new ArrayList<>();
        User u1 = getUserById("user_101");
        User u2 = getUserById("user_102");
        User u3 = getUserById("user_103");
        if (u1 != null) demoList.add(u1);
        if (u2 != null) demoList.add(u2);
        if (u3 != null) demoList.add(u3);
        return demoList;
    }

    public User getUserById(String userId) {
        if (userId == null) return null;
        for (User u : users) {
            if (userId.equals(u.getId())) {
                return u;
            }
        }
        return null;
    }

    // ==================== QUẢN LÝ BÀI VIẾT (POSTS) ====================

    public List<Post> getPosts() {
        return new ArrayList<>(posts);
    }

    public Post getPostById(String postId) {
        if (postId == null) return null;
        for (Post p : posts) {
            if (postId.equals(p.getId())) {
                return p;
            }
        }
        return null;
    }

    public void addPost(Post post) {
        if (post != null) {
            posts.add(0, post);
        }
    }

    public Post createPost(String authorId, String content, String mediaUrl, boolean isPublic) {
        User author = getUserById(authorId != null ? authorId : "user_current");
        String name = author != null ? author.getDisplayName() : "Người dùng hiện tại";
        String avatar = author != null ? author.getAvatarUrl() : "";

        Post newPost = new Post(
                "post_" + UUID.randomUUID().toString().substring(0, 8),
                authorId != null ? authorId : "user_current",
                name,
                avatar,
                content != null ? content.trim() : "",
                mediaUrl,
                isPublic,
                0,
                0,
                false,
                System.currentTimeMillis()
        );

        posts.add(0, newPost);
        return newPost;
    }

    public boolean toggleLike(String postId, String userId) {
        Post post = getPostById(postId);
        if (post == null) {
            return false;
        }

        boolean newLiked = !post.isLiked();
        post.setLiked(newLiked);
        int currentCount = post.getLikesCount();
        post.setLikesCount(newLiked ? currentCount + 1 : Math.max(0, currentCount - 1));
        return newLiked;
    }

    // ==================== QUẢN LÝ BÌNH LUẬN (COMMENTS) ====================

    public List<Comment> getComments(String postId) {
        if (postId == null) return new ArrayList<>();
        List<Comment> list = commentsMap.get(postId);
        if (list == null) {
            return new ArrayList<>();
        }
        List<Comment> sorted = new ArrayList<>(list);
        Collections.sort(sorted, (c1, c2) -> Long.compare(c1.getCreatedAt(), c2.getCreatedAt()));
        return sorted;
    }

    public Comment addComment(String postId, String authorId, String content) {
        if (postId == null || content == null || content.trim().isEmpty()) {
            return null;
        }

        User author = getUserById(authorId != null ? authorId : "user_current");
        String name = author != null ? author.getDisplayName() : "Bạn";
        String avatar = author != null ? author.getAvatarUrl() : "";

        Comment newComment = new Comment(
                "cmt_" + UUID.randomUUID().toString().substring(0, 8),
                postId,
                authorId != null ? authorId : "user_current",
                name,
                avatar,
                content.trim(),
                System.currentTimeMillis()
        );

        addComment(postId, newComment);
        return newComment;
    }

    public void addComment(String postId, Comment comment) {
        if (postId == null || comment == null) return;

        commentsMap.computeIfAbsent(postId, k -> new CopyOnWriteArrayList<>()).add(comment);

        Post post = getPostById(postId);
        if (post != null) {
            post.setCommentsCount(post.getCommentsCount() + 1);
        }
    }

    // ==================== QUẢN LÝ BẠN BÈ (FRIENDS) ====================


    /** Lời mời đang chờ, chỉ trả về cho đúng người nhận. */
    public List<FriendRequest> getFriendRequests(String userId) {
        String targetId = userId != null ? userId : currentUserId;
        List<FriendRequest> result = new ArrayList<>();
        for (FriendRequest request : friendRequests) {
            if (targetId.equals(request.getReceiverId())
                    && FriendRequest.STATUS_PENDING.equals(request.getStatus())) {
                result.add(request);
            }
        }
        result.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        return result;
    }

    /** Quan hệ bạn bè hai chiều; danh sách friends ban đầu thuộc user_current. */
    public List<User> getFriends(String userId) {
        String targetId = userId != null ? userId : currentUserId;
        Map<String, User> result = new java.util.LinkedHashMap<>();
        if ("user_current".equals(targetId)) {
            for (User friend : friends) result.put(friend.getId(), friend);
        } else {
            for (User friend : friends) {
                if (targetId.equals(friend.getId())) {
                    User owner = getUserById("user_current");
                    if (owner != null) result.put(owner.getId(), owner);
                }
            }
        }
        for (FriendRequest request : acceptedFriendRequests) {
            String otherId = null;
            if (targetId.equals(request.getReceiverId())) otherId = request.getRequesterId();
            else if (targetId.equals(request.getRequesterId())) otherId = request.getReceiverId();
            User other = getUserById(otherId);
            if (other != null && !targetId.equals(other.getId())) result.put(other.getId(), other);
        }
        return new ArrayList<>(result.values());
    }

    /** Chỉ tài khoản nhận lời mời mới được chấp nhận. */
    public synchronized boolean acceptFriendRequest(String requestId) {
        if (requestId == null) return false;
        for (FriendRequest request : friendRequests) {
            if (requestId.equals(request.getId())
                    && currentUserId.equals(request.getReceiverId())
                    && FriendRequest.STATUS_PENDING.equals(request.getStatus())) {
                request.setStatus(FriendRequest.STATUS_ACCEPTED);
                acceptedFriendRequests.add(request);
                friendRequests.remove(request);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean rejectFriendRequest(String requestId) {
        if (requestId == null) return false;
        for (FriendRequest request : friendRequests) {
            if (requestId.equals(request.getId())
                    && currentUserId.equals(request.getReceiverId())
                    && FriendRequest.STATUS_PENDING.equals(request.getStatus())) {
                request.setStatus(FriendRequest.STATUS_REJECTED);
                friendRequests.remove(request);
                return true;
            }
        }
        return false;
    }

    public List<User> searchFriends(String query) {
        return searchFriends(query, currentUserId);
    }

    /** Day 6: tìm trong danh sách bạn bè của tài khoản đang chọn. */
    public List<User> searchFriends(String query, String userId) {
        List<User> result = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return result;
        String keyword = query.trim().toLowerCase(java.util.Locale.ROOT);
        for (User user : getFriends(userId)) {
            if (user.getDisplayName() != null
                    && user.getDisplayName().toLowerCase(java.util.Locale.ROOT).contains(keyword)) {
                result.add(user);
            }
        }
        return result;
    }

    // ==================== QUẢN LÝ THÔNG BÁO (NOTIFICATIONS) ====================

    public List<Notification> getNotifications(String userId) {
        List<Notification> copy = new ArrayList<>(notifications);
        Collections.sort(copy, (n1, n2) -> Long.compare(n2.getCreatedAt(), n1.getCreatedAt()));
        return copy;
    }

    public boolean markNotificationAsRead(String notificationId) {
        if (notificationId == null) return false;

        for (Notification n : notifications) {
            if (notificationId.equals(n.getId())) {
                n.setRead(true);
                return true;
            }
        }
        return false;
    }

    /**
     * Khôi phục toàn bộ dữ liệu về trạng thái ban đầu phục vụ Unit Test hoặc làm mới ứng dụng.
     */
    public synchronized void resetData() {
        currentUserId = "user_current";
        initData();
    }
}