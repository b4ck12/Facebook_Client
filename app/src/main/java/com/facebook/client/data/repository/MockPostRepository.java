package com.facebook.client.data.repository;

import com.facebook.client.data.model.Comment;
import com.facebook.client.data.model.Post;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository giả lập (Mock Data) phục vụ phân hệ Bảng tin (Home Feed) và Bình luận (Comments).
 * Cho phép nhóm Frontend phát triển và kiểm thử giao diện độc lập ở Tuần 1 mà không phụ thuộc vào Backend.
 */
@SuppressWarnings("SpellCheckingInspection")
public class MockPostRepository implements IPostRepository {

    private static final List<Post> MOCK_POSTS = new ArrayList<>();
    private static final Map<String, List<Comment>> MOCK_COMMENTS_MAP = new HashMap<>();
    private static boolean isInitialized = false;

    public MockPostRepository() {
        if (!isInitialized) {
            initMockData();
            isInitialized = true;
        }
    }

    /**
     * Khởi tạo bộ dữ liệu 10 bài viết mẫu và các bình luận văn bản (Text-only) chuẩn đặc tả PDF.
     */
    private void initMockData() {
        long now = System.currentTimeMillis();

        // 1. Bài viết có cả chữ và ảnh (5 phút trước) - Khớp mã post_1001 với MockNotificationRepository
        MOCK_POSTS.add(new Post(
                "post_1001",
                "user_101",
                "Nguyễn Văn An",
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                "Cuối tuần đi săn mây Đà Lạt cùng hội bạn thân, thời tiết se lạnh tuyệt vời quá mọi người ơi!",
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
                true,
                24,
                3,
                false,
                now - (5 * 60 * 1000)
        ));

        // 2. Bài viết chỉ có văn bản thuần túy (20 phút trước) - Đã nhấn Like sẵn
        MOCK_POSTS.add(new Post(
                "post_1002",
                "user_102",
                "Trần Thị Mai",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                "Đồ án môn Phát triển Ứng dụng Di động đang chạy đúng tiến độ! Cả nhóm 7 anh em cố gắng hoàn thành xuất sắc nhé!",
                null,
                true,
                42,
                2,
                true,
                now - (20 * 60 * 1000)
        ));

        // 3. Bài viết có ảnh công nghệ (1 giờ trước)
        MOCK_POSTS.add(new Post(
                "post_1003",
                "user_103",
                "Lê Hoàng Nam",
                "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
                "Góc làm việc mới setup để chiến code Android Studio và Python FastAPI xuyên màn đêm.",
                "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800",
                true,
                18,
                1,
                false,
                now - (60 * 60 * 1000)
        ));

        // 4. Bài viết ẩm thực (2 giờ trước)
        MOCK_POSTS.add(new Post(
                "post_1004",
                "user_104",
                "Phạm Minh Tuấn",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                "Bữa sáng nhẹ nhàng với phở Hà Nội truyền thống trước giờ lên giảng đường.",
                "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800",
                true,
                35,
                0,
                true,
                now - (2 * 60 * 60 * 1000)
        ));

        // 5. Bài viết chỉ có chữ (3 giờ trước)
        MOCK_POSTS.add(new Post(
                "post_1005",
                "user_105",
                "Đặng Thu Thảo",
                "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150",
                "Có ai biết quán cà phê nào yên tĩnh quanh khu vực trường mình để ngồi học nhóm không ạ? Cho mình xin địa chỉ với!",
                null,
                true,
                12,
                1,
                false,
                now - (3 * 60 * 60 * 1000)
        ));

        // 6. Bài viết có ảnh cà phê (5 giờ trước)
        MOCK_POSTS.add(new Post(
                "post_1006",
                "user_101",
                "Nguyễn Văn An",
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                "Một ly Espresso nóng cho buổi chiều tỉnh táo fix bug.",
                "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800",
                true,
                29,
                0,
                false,
                now - (5 * 60 * 60 * 1000)
        ));

        // 7. Bài viết thể thao (8 giờ trước)
        MOCK_POSTS.add(new Post(
                "post_1007",
                "user_103",
                "Lê Hoàng Nam",
                "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
                "Chiều nay 17h30 đá bóng sân cỏ nhân tạo nhé anh em! Đừng ai cao su nhé.",
                "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=800",
                true,
                51,
                0,
                true,
                now - (8 * 60 * 60 * 1000)
        ));

        // 8. Bài viết chia sẻ kiến thức (12 giờ trước)
        MOCK_POSTS.add(new Post(
                "post_1008",
                "user_104",
                "Phạm Minh Tuấn",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                "Áp dụng kiến trúc MVVM + Clean Architecture kết hợp ViewBinding giúp code Android gọn gàng và dễ viết Unit Test hơn hẳn.",
                null,
                true,
                64,
                0,
                false,
                now - (12 * 60 * 60 * 1000)
        ));

        // 9. Bài viết du lịch biển (1 ngày trước)
        MOCK_POSTS.add(new Post(
                "post_1009",
                "user_102",
                "Trần Thị Mai",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                "Nhớ biển Phú Quốc mùa nắng đẹp.",
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
                true,
                88,
                0,
                true,
                now - (24 * 60 * 60 * 1000)
        ));

        // 10. Bài viết sách (2 ngày trước)
        MOCK_POSTS.add(new Post(
                "post_1010",
                "user_105",
                "Đặng Thu Thảo",
                "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150",
                "Cuốn sách Clean Code thực sự rất đáng đọc cho mọi lập trình viên.",
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800",
                true,
                45,
                0,
                false,
                now - (48 * 60 * 60 * 1000)
        ));

        // Khởi tạo bình luận mẫu cho bài viết post_1001
        List<Comment> commentsPost1001 = new ArrayList<>();
        commentsPost1001.add(new Comment(
                "cmt_1",
                "post_1001",
                "user_102",
                "Trần Thị Mai",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                "Chuyến đi tuyệt vời quá bạn ơi!",
                now - (4 * 60 * 1000)
        ));
        commentsPost1001.add(new Comment(
                "cmt_2",
                "post_1001",
                "user_103",
                "Lê Hoàng Nam",
                "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
                "Ảnh chụp nét căng, săn mây ở đồi nào thế An?",
                now - (3 * 60 * 1000)
        ));
        commentsPost1001.add(new Comment(
                "cmt_3",
                "post_1001",
                "user_104",
                "Phạm Minh Tuấn",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                "Đợt tới bảo vệ đồ án xong cả nhóm mình làm một chuyến nhé!",
                now - (60 * 1000)
        ));
        MOCK_COMMENTS_MAP.put("post_1001", commentsPost1001);

        // Khởi tạo bình luận mẫu cho bài viết post_1002
        List<Comment> commentsPost1002 = new ArrayList<>();
        commentsPost1002.add(new Comment(
                "cmt_4",
                "post_1002",
                "user_101",
                "Nguyễn Văn An",
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                "Quyết tâm đạt điểm A+ môn này nhé cả nhà!",
                now - (15 * 60 * 1000)
        ));
        commentsPost1002.add(new Comment(
                "cmt_5",
                "post_1002",
                "user_105",
                "Đặng Thu Thảo",
                "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150",
                "Giao diện lên hình mượt lắm rồi!",
                now - (10 * 60 * 1000)
        ));
        MOCK_COMMENTS_MAP.put("post_1002", commentsPost1002);

        // Khởi tạo bình luận mẫu cho bài viết post_1003
        List<Comment> commentsPost1003 = new ArrayList<>();
        commentsPost1003.add(new Comment(
                "cmt_6",
                "post_1003",
                "user_104",
                "Phạm Minh Tuấn",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                "Góc setup gọn gàng và chất lượng quá Nam ơi.",
                now - (30 * 60 * 1000)
        ));
        MOCK_COMMENTS_MAP.put("post_1003", commentsPost1003);

        // Khởi tạo bình luận mẫu cho bài viết post_1005
        List<Comment> commentsPost1005 = new ArrayList<>();
        commentsPost1005.add(new Comment(
                "cmt_7",
                "post_1005",
                "user_102",
                "Trần Thị Mai",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                "Qua quán The Coffee House ngay cổng trường tầng 2 yên tĩnh lắm Thảo ơi.",
                now - (2 * 60 * 60 * 1000)
        ));
        MOCK_COMMENTS_MAP.put("post_1005", commentsPost1005);
    }

    /**
     * Lấy danh sách bài viết mới nhất trên Bảng tin Feed, sắp xếp theo thời gian tạo giảm dần.
     */
    @Override
    public void getFeedPosts(int limit, String currentUserId, RepositoryCallback<List<Post>> callback) {
        if (callback == null) {
            return;
        }

        List<Post> resultList = new ArrayList<>(MOCK_POSTS);
        resultList.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
        if (limit > 0 && resultList.size() > limit) {
            resultList = new ArrayList<>(resultList.subList(0, limit));
        }

        callback.onSuccess(resultList);
    }

    /**
     * Tạo bài viết mới (Bắt buộc có nội dung chữ hoặc có ảnh/video theo đặc tả PDF).
     */
    @Override
    public void createPost(String authorId, String content, String mediaUrl, boolean isPublic, RepositoryCallback<Post> callback) {
        if (callback == null) {
            return;
        }

        boolean isContentEmpty = (content == null || content.trim().isEmpty());
        boolean isMediaEmpty = (mediaUrl == null || mediaUrl.trim().isEmpty());

        if (isContentEmpty && isMediaEmpty) {
            callback.onError("Vui lòng nhập nội dung hoặc chọn ảnh để đăng bài viết!");
            return;
        }

        Post newPost = new Post(
                "post_" + UUID.randomUUID().toString().substring(0, 8),
                authorId != null ? authorId : "user_current",
                "Người dùng hiện tại",
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                content != null ? content.trim() : "",
                mediaUrl,
                isPublic,
                0,
                0,
                false,
                System.currentTimeMillis()
        );

        MOCK_POSTS.add(0, newPost);
        callback.onSuccess(newPost);
    }

    /**
     * Bật/Tắt trạng thái Thích bài viết (Toggle Like) và tự động cập nhật số lượng lượt thích.
     */
    @Override
    public void toggleLike(String postId, String userId, RepositoryCallback<Boolean> callback) {
        if (callback == null) {
            return;
        }

        for (Post post : MOCK_POSTS) {
            if (post.getId() != null && post.getId().equals(postId)) {
                boolean newLikedState = !post.isLiked();
                post.setLiked(newLikedState);
                int updatedCount = newLikedState ? post.getLikesCount() + 1 : Math.max(0, post.getLikesCount() - 1);
                post.setLikesCount(updatedCount);
                callback.onSuccess(newLikedState);
                return;
            }
        }

        callback.onError("Không tìm thấy bài viết với mã: " + postId);
    }

    /**
     * Lấy danh sách toàn bộ bình luận văn bản (Text-only) của một bài viết, sắp xếp cũ nhất đến mới nhất.
     */
    @Override
    public void getComments(String postId, RepositoryCallback<List<Comment>> callback) {
        if (callback == null) {
            return;
        }

        List<Comment> comments = MOCK_COMMENTS_MAP.get(postId);
        if (comments == null) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<Comment> copyList = new ArrayList<>(comments);
                copyList.sort(Comparator.comparingLong(Comment::getCreatedAt));
        callback.onSuccess(copyList);
    }

    /**
     * Gửi bình luận văn bản mới vào bài viết (Chuẩn Text-only theo đặc tả PDF).
     */
    @Override
    public void addComment(String postId, String authorId, String content, RepositoryCallback<Comment> callback) {
        if (callback == null) {
            return;
        }

        if (content == null || content.trim().isEmpty()) {
            callback.onError("Nội dung bình luận không được để trống!");
            return;
        }

        Comment newComment = new Comment(
                "cmt_" + UUID.randomUUID().toString().substring(0, 8),
                postId,
                authorId != null ? authorId : "user_current",
                "Bạn",
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                content.trim(),
                System.currentTimeMillis()
        );

        List<Comment> comments = MOCK_COMMENTS_MAP.get(postId);
        if (comments == null) {
            comments = new ArrayList<>();
            MOCK_COMMENTS_MAP.put(postId, comments);
        }
        comments.add(newComment);

        // Tăng số lượng bình luận trên bài viết tương ứng
        for (Post post : MOCK_POSTS) {
            if (post.getId() != null && post.getId().equals(postId)) {
                post.setCommentsCount(post.getCommentsCount() + 1);
                break;
            }
        }

        callback.onSuccess(newComment);
    }

    /**
     * Chia sẻ bài viết ngay lên Bảng tin kèm dòng cảm nghĩ ngắn (Share now).
     */
    @Override
    public void sharePost(String originalPostId, String authorId, String caption, RepositoryCallback<Post> callback) {
        if (callback == null) {
            return;
        }

        Post originalPost = null;
        for (Post post : MOCK_POSTS) {
            if (post.getId() != null && post.getId().equals(originalPostId)) {
                originalPost = post;
                break;
            }
        }

        if (originalPost == null) {
            callback.onError("Không tìm thấy bài viết gốc để chia sẻ!");
            return;
        }

        String sharedContent = (caption != null && !caption.trim().isEmpty())
                ? caption.trim() + "\n\n[Chia sẻ từ " + originalPost.getAuthorName() + ": " + originalPost.getContent() + "]"
                : "[Chia sẻ từ " + originalPost.getAuthorName() + ": " + originalPost.getContent() + "]";

        Post sharedPost = new Post(
                "post_" + UUID.randomUUID().toString().substring(0, 8),
                authorId != null ? authorId : "user_current",
                "Bạn",
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                sharedContent,
                originalPost.getMediaUrl(),
                true,
                0,
                0,
                false,
                System.currentTimeMillis()
        );

        MOCK_POSTS.add(0, sharedPost);
        callback.onSuccess(sharedPost);
    }
}