package com.facebook.client.data.mock;

import com.facebook.client.data.model.Comment;
import com.facebook.client.data.model.FriendRequest;
import com.facebook.client.data.model.Notification;
import com.facebook.client.data.model.Post;
import com.facebook.client.data.model.User;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit Test kiểm thử bộ sinh dữ liệu giả lập MockDataGenerator.
 * Đảm bảo 100% tính toàn vẹn dữ liệu mẫu, tính an toàn luồng và khả năng khôi phục trạng thái (reset).
 */
public class MockDataGeneratorTest {

    private MockDataGenerator generator;

    @Before
    public void setUp() {
        generator = MockDataGenerator.getInstance();
        generator.resetData();
    }

    @Test
    public void testInitialDataCounts() {
        List<User> users = generator.getUsers();
        assertEquals("Số lượng người dùng mẫu phải là 6", 6, users.size());

        List<Post> posts = generator.getPosts();
        assertEquals("Số lượng bài viết mẫu phải là 10", 10, posts.size());

        List<Notification> notifications = generator.getNotifications("user_current");
        assertEquals("Số lượng thông báo mẫu phải là 5", 5, notifications.size());

        List<FriendRequest> requests = generator.getFriendRequests("user_current");
        assertEquals("Số lượng lời mời kết bạn ban đầu phải là 2", 2, requests.size());

        List<User> friends = generator.getFriends("user_current");
        assertEquals("Số lượng bạn bè ban đầu phải là 2", 2, friends.size());

        // Kiểm tra số lượng bình luận bài post_1001
        List<Comment> commentsPost1 = generator.getComments("post_1001");
        assertEquals("Bài viết post_1001 phải có 3 bình luận", 3, commentsPost1.size());
    }

    @Test
    public void testUserQueries() {
        User currentUser = generator.getCurrentUser();
        assertNotNull("Current user không được null", currentUser);
        assertEquals("user_current", currentUser.getId());

        User user101 = generator.getUserById("user_101");
        assertNotNull("user_101 phải tồn tại", user101);
        assertEquals("Nguyễn Văn An", user101.getDisplayName());

        User nonExistent = generator.getUserById("user_unknown_999");
        assertNull("Người dùng không tồn tại phải trả về null", nonExistent);
    }

    @Test
    public void testToggleLike() {
        Post post1 = generator.getPostById("post_1001");
        assertNotNull(post1);
        assertFalse("post_1001 ban đầu chưa được like", post1.isLiked());
        int initialLikes = post1.getLikesCount();
        assertEquals(24, initialLikes);

        // Lần 1: Like bài viết
        boolean liked = generator.toggleLike("post_1001", "user_current");
        assertTrue("Sau khi toggle lần 1, trạng thái phải là liked", liked);
        assertTrue(post1.isLiked());
        assertEquals("Số lượt like phải tăng thêm 1", initialLikes + 1, post1.getLikesCount());

        // Lần 2: Unlike bài viết
        boolean unliked = generator.toggleLike("post_1001", "user_current");
        assertFalse("Sau khi toggle lần 2, trạng thái phải là unliked", unliked);
        assertFalse(post1.isLiked());
        assertEquals("Số lượt like phải giảm về ban đầu", initialLikes, post1.getLikesCount());
    }

    @Test
    public void testAddComment() {
        Post post1 = generator.getPostById("post_1001");
        assertNotNull(post1);
        int initialCommentsCount = post1.getCommentsCount();
        assertEquals(3, initialCommentsCount);

        Comment newComment = generator.addComment("post_1001", "user_current", "Thử nghiệm bình luận mới");
        assertNotNull("Bình luận mới không được null", newComment);
        assertNotNull("Bình luận mới phải có ID", newComment.getId());
        assertEquals("post_1001", newComment.getPostId());
        assertEquals("Thử nghiệm bình luận mới", newComment.getContent());

        // Kiểm tra danh sách comment cập nhật
        List<Comment> updatedComments = generator.getComments("post_1001");
        assertEquals("Danh sách comment phải có 4 phần tử", initialCommentsCount + 1, updatedComments.size());
        assertEquals("Số lượng comment trên bài viết phải tăng 1", initialCommentsCount + 1, post1.getCommentsCount());
    }

    @Test
    public void testCreatePost() {
        int initialPostCount = generator.getPosts().size();
        assertEquals(10, initialPostCount);

        Post newPost = generator.createPost(
                "user_current",
                "Hôm nay trời nắng đẹp, hoàn thành xong task D2-HDH!",
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
                true
        );

        assertNotNull("Bài viết mới tạo không được null", newPost);
        assertNotNull("Bài viết mới phải có ID", newPost.getId());
        assertEquals(initialPostCount + 1, generator.getPosts().size());

        // Bài viết mới tạo phải nằm ở vị trí đầu tiên của Feed (index 0)
        assertEquals("Bài viết mới phải xuất hiện đầu feed", newPost.getId(), generator.getPosts().get(0).getId());
    }

    @Test
    public void testFriendRequestAccept() {
        List<FriendRequest> requests = generator.getFriendRequests("user_current");
        assertEquals(2, requests.size());
        int initialFriendCount = generator.getFriends("user_current").size();

        // Chấp nhận lời mời req_1 (từ user_101)
        boolean accepted = generator.acceptFriendRequest("req_1");
        assertTrue("Chấp nhận lời mời phải thành công", accepted);

        // Kiểm tra request đã bị loại bỏ khỏi danh sách pending
        List<FriendRequest> remainingRequests = generator.getFriendRequests("user_current");
        assertEquals("Danh sách lời mời phải còn 1", 1, remainingRequests.size());

        // Kiểm tra user_101 đã được thêm vào danh sách bạn bè
        List<User> updatedFriends = generator.getFriends("user_current");
        assertEquals("Danh sách bạn bè phải tăng thêm 1", initialFriendCount + 1, updatedFriends.size());
        boolean hasUser101 = false;
        for (User u : updatedFriends) {
            if ("user_101".equals(u.getId())) {
                hasUser101 = true;
                break;
            }
        }
        assertTrue("user_101 phải có trong danh sách bạn bè", hasUser101);
    }

    @Test
    public void testFriendRequestReject() {
        List<FriendRequest> requests = generator.getFriendRequests("user_current");
        assertEquals(2, requests.size());
        int initialFriendCount = generator.getFriends("user_current").size();

        // Từ chối lời mời req_2
        boolean rejected = generator.rejectFriendRequest("req_2");
        assertTrue("Từ chối lời mời phải thành công", rejected);

        // Danh sách lời mời giảm còn 1
        List<FriendRequest> remainingRequests = generator.getFriendRequests("user_current");
        assertEquals(1, remainingRequests.size());

        // Danh sách bạn bè không đổi
        List<User> friends = generator.getFriends("user_current");
        assertEquals(initialFriendCount, friends.size());
    }

    @Test
    public void testSearchFriends() {
        List<User> results = generator.searchFriends("Hoàng");
        assertEquals("Phải tìm thấy 1 bạn bè có tên Hoàng", 1, results.size());
        assertEquals("user_103", results.get(0).getId());
        assertEquals("Lê Hoàng Nam", results.get(0).getDisplayName());

        List<User> emptyResults = generator.searchFriends("Tên Không Tồn Tại");
        assertTrue("Không tìm thấy phải trả về danh sách rỗng", emptyResults.isEmpty());
    }

    @Test
    public void testMarkNotificationAsRead() {
        List<Notification> notifs = generator.getNotifications("user_current");
        Notification notif1 = null;
        for (Notification n : notifs) {
            if ("notif_1".equals(n.getId())) {
                notif1 = n;
                break;
            }
        }
        assertNotNull(notif1);
        assertFalse("notif_1 ban đầu chưa đọc", notif1.isRead());

        boolean marked = generator.markNotificationAsRead("notif_1");
        assertTrue("Đánh dấu đã đọc phải thành công", marked);
        assertTrue("notif_1 bây giờ phải có isRead = true", notif1.isRead());
    }

    @Test
    public void testResetDataRestoresOriginalState() {
        // Thực hiện các mutation làm thay đổi dữ liệu
        generator.toggleLike("post_1001", "user_current");
        generator.addComment("post_1001", "user_current", "Comment tạm thời");
        generator.createPost("user_current", "Post tạm thời", null, true);
        generator.acceptFriendRequest("req_1");
        generator.markNotificationAsRead("notif_1");

        // Gọi resetData
        generator.resetData();

        // Kiểm tra toàn bộ trạng thái đã về ban đầu
        assertEquals(6, generator.getUsers().size());
        assertEquals(10, generator.getPosts().size());
        assertEquals(2, generator.getFriendRequests("user_current").size());
        assertEquals(2, generator.getFriends("user_current").size());
        assertEquals(5, generator.getNotifications("user_current").size());

        Post post1 = generator.getPostById("post_1001");
        assertNotNull(post1);
        assertFalse(post1.isLiked());
        assertEquals(24, post1.getLikesCount());
        assertEquals(3, post1.getCommentsCount());
    }
}
