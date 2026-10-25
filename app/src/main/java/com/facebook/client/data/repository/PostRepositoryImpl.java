package com.facebook.client.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.facebook.client.data.model.Comment;
import com.facebook.client.data.model.Post;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  Gọi RESTful API  từ Python FastAPI + SQLite .
 * Tính năng chính:
 * 1. Kết nối trực tiếp tới các endpoint của FastAPI:
 *    - GET  /api/posts (Lấy danh sách bài viết Bảng tin Feed)
 *    - POST /api/posts (Tạo bài viết mới)
 *    - POST /api/posts/{id}/like (Toggle Like bài viết trong bảng post_likes)
 *    - POST /api/posts/{id}/share (Chia sẻ bài viết kèm caption)
 *    - GET  /api/posts/{id}/comments (Lấy danh sách bình luận Text-only)
 *    - POST /api/posts/{id}/comments (Gửi bình luận văn bản mới lên SQLite)
 * 2. Cơ chế Graceful Degradation (Fallback an toàn):
 *    - Khi máy chủ FastAPI đang bật: Đọc/ghi trực tiếp vào CSDL SQLite.   
 */
@SuppressWarnings("SpellCheckingInspection")
public class PostRepositoryImpl implements IPostRepository {

    // Địa chỉ gốc của máy chủ FastAPI trên máy ảo Android (10.0.2.2 trỏ về localhost của máy tính)
    private static final String SERVER_ORIGIN = "http://10.0.2.2:8000";
    private static final String BASE_POSTS_URL = SERVER_ORIGIN + "/api/posts";

    // Thời gian chờ phản hồi tối đa (3 giây)
    private static final int TIMEOUT_MS = 3000;

    // Luồng thực thi ngầm tránh chặn giao diện chính (UI Thread)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Nguồn dữ liệu dự phòng khi chưa bật server FastAPI
    private final MockPostRepository fallbackMock = new MockPostRepository();

    @Override
    public void getFeedPosts(int limit, String currentUserId, RepositoryCallback<List<Post>> callback) {
        if (callback == null) {
            return;
        }

        executor.execute(() -> {
            try {
                String safeUserId = (currentUserId != null && !currentUserId.trim().isEmpty())
                        ? URLEncoder.encode(currentUserId.trim(), "UTF-8")
                        : "user_101";
                int safeLimit = limit > 0 ? limit : 20;

                String urlString = BASE_POSTS_URL + "?current_user_id=" + safeUserId + "&limit=" + safeLimit;
                HttpURLConnection conn = createConnection(urlString, "GET", false);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String responseBody = readStream(conn.getInputStream());
                    conn.disconnect();

                    JSONArray jsonArray = new JSONArray(responseBody);
                    List<Post> posts = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        posts.add(parsePost(obj));
                    }

                    mainHandler.post(() -> callback.onSuccess(posts));
                    return;
                }
                conn.disconnect();
            } catch (Exception ignored) {
                // Máy chủ FastAPI chưa bật -> Tự động chuyển sang Fallback bên dưới
            }

            // Fallback về MockPostRepository khi offline
            mainHandler.post(() -> fallbackMock.getFeedPosts(limit, currentUserId, callback));
        });
    }

    @Override
    public void createPost(String authorId, String content, String mediaUrl, boolean isPublic, RepositoryCallback<Post> callback) {
        if (callback == null) {
            return;
        }

        executor.execute(() -> {
            try {
                HttpURLConnection conn = createConnection(BASE_POSTS_URL, "POST", true);

                JSONObject payload = new JSONObject();
                payload.put("authorId", authorId != null ? authorId : "user_101");
                payload.put("content", content != null ? content : "");
                if (mediaUrl != null && !mediaUrl.trim().isEmpty()) {
                    payload.put("mediaUrl", mediaUrl.trim());
                } else {
                    payload.put("mediaUrl", JSONObject.NULL);
                }
                payload.put("isPublic", isPublic);

                writeJsonBody(conn, payload.toString());

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    String responseBody = readStream(conn.getInputStream());
                    conn.disconnect();

                    Post createdPost = parsePost(new JSONObject(responseBody));
                    // Đồng bộ thêm vào bộ nhớ tạm để giữ tính nhất quán
                    fallbackMock.createPost(authorId, content, mediaUrl, isPublic, null);
                    mainHandler.post(() -> callback.onSuccess(createdPost));
                    return;
                }
                conn.disconnect();
            } catch (Exception ignored) {
                // Máy chủ FastAPI chưa bật -> Tự động chuyển sang Fallback
            }

            mainHandler.post(() -> fallbackMock.createPost(authorId, content, mediaUrl, isPublic, callback));
        });
    }

    @Override
    public void toggleLike(String postId, String userId, RepositoryCallback<Boolean> callback) {
        if (postId == null) {
            return;
        }

        executor.execute(() -> {
            try {
                String safeUserId = (userId != null && !userId.trim().isEmpty())
                        ? URLEncoder.encode(userId.trim(), "UTF-8")
                        : "user_101";
                String urlString = BASE_POSTS_URL + "/" + URLEncoder.encode(postId, "UTF-8") + "/like?user_id=" + safeUserId;

                HttpURLConnection conn = createConnection(urlString, "POST", false);
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String responseBody = readStream(conn.getInputStream());
                    conn.disconnect();

                    JSONObject json = new JSONObject(responseBody);
                    boolean isLiked = json.optBoolean("isLiked", false);

                    if (callback != null) {
                        mainHandler.post(() -> callback.onSuccess(isLiked));
                    }
                    return;
                }
                conn.disconnect();
            } catch (Exception ignored) {
                // Máy chủ FastAPI chưa bật -> Tự động chuyển sang Fallback
            }

            mainHandler.post(() -> fallbackMock.toggleLike(postId, userId, callback));
        });
    }

    @Override
    public void getComments(String postId, RepositoryCallback<List<Comment>> callback) {
        if (callback == null || postId == null) {
            return;
        }

        executor.execute(() -> {
            try {
                String urlString = BASE_POSTS_URL + "/" + URLEncoder.encode(postId, "UTF-8") + "/comments";
                HttpURLConnection conn = createConnection(urlString, "GET", false);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String responseBody = readStream(conn.getInputStream());
                    conn.disconnect();

                    JSONArray jsonArray = new JSONArray(responseBody);
                    List<Comment> comments = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        comments.add(parseComment(obj, postId));
                    }

                    mainHandler.post(() -> callback.onSuccess(comments));
                    return;
                }
                conn.disconnect();
            } catch (Exception ignored) {
                // Máy chủ FastAPI chưa bật -> Tự động chuyển sang Fallback
            }

            mainHandler.post(() -> fallbackMock.getComments(postId, callback));
        });
    }

    @Override
    public void addComment(String postId, String authorId, String content, RepositoryCallback<Comment> callback) {
        if (callback == null || postId == null) {
            return;
        }

        executor.execute(() -> {
            try {
                String urlString = BASE_POSTS_URL + "/" + URLEncoder.encode(postId, "UTF-8") + "/comments";
                HttpURLConnection conn = createConnection(urlString, "POST", true);

                JSONObject payload = new JSONObject();
                payload.put("authorId", authorId != null ? authorId : "user_101");
                payload.put("content", content != null ? content.trim() : "");

                writeJsonBody(conn, payload.toString());

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    String responseBody = readStream(conn.getInputStream());
                    conn.disconnect();

                    Comment createdComment = parseComment(new JSONObject(responseBody), postId);
                    fallbackMock.addComment(postId, authorId, content, null);
                    mainHandler.post(() -> callback.onSuccess(createdComment));
                    return;
                }
                conn.disconnect();
            } catch (Exception ignored) {
                // Máy chủ FastAPI chưa bật -> Tự động chuyển sang Fallback
            }

            mainHandler.post(() -> fallbackMock.addComment(postId, authorId, content, callback));
        });
    }

    @Override
    public void sharePost(String originalPostId, String authorId, String caption, RepositoryCallback<Post> callback) {
        if (callback == null || originalPostId == null) {
            return;
        }

        executor.execute(() -> {
            try {
                String safeUserId = (authorId != null && !authorId.trim().isEmpty())
                        ? URLEncoder.encode(authorId.trim(), "UTF-8")
                        : "user_101";
                StringBuilder urlBuilder = new StringBuilder(BASE_POSTS_URL)
                        .append("/")
                        .append(URLEncoder.encode(originalPostId, "UTF-8"))
                        .append("/share?user_id=")
                        .append(safeUserId);

                if (caption != null && !caption.trim().isEmpty()) {
                    urlBuilder.append("&caption=").append(URLEncoder.encode(caption.trim(), "UTF-8"));
                }

                HttpURLConnection conn = createConnection(urlBuilder.toString(), "POST", false);
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    String responseBody = readStream(conn.getInputStream());
                    conn.disconnect();

                    Post sharedPost = parsePost(new JSONObject(responseBody));
                    fallbackMock.sharePost(originalPostId, authorId, caption, null);
                    mainHandler.post(() -> callback.onSuccess(sharedPost));
                    return;
                }
                conn.disconnect();
            } catch (Exception ignored) {
                // Máy chủ FastAPI chưa bật -> Tự động chuyển sang Fallback
            }

            mainHandler.post(() -> fallbackMock.sharePost(originalPostId, authorId, caption, callback));
        });
    }

    /**
     * Khởi tạo kết nối HTTP chuẩn tới FastAPI server.
     */
    private HttpURLConnection createConnection(String urlString, String method, boolean hasBody) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("Accept", "application/json");
        if (hasBody) {
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
        }
        return conn;
    }

    /**
     * Ghi dữ liệu JSON vào request body.
     */
    private void writeJsonBody(HttpURLConnection conn, String jsonBody) throws Exception {
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    /**
     * Đọc toàn bộ chuỗi JSON phản hồi từ InputStream.
     */
    private String readStream(InputStream inputStream) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    /**
     * Chuyển đổi JSONObject từ FastAPI (PostResponse) sang đối tượng Post của Android.
     */
    private Post parsePost(JSONObject obj) {
        String id = obj.optString("id", "");
        String authorId = obj.optString("authorId", obj.optString("author_id", ""));
        String authorName = obj.optString("authorName", obj.optString("author_name", "Người dùng Facebook"));
        String authorAvatar = normalizeUrl(optNullableString(obj, "authorAvatar", "author_avatar"));
        String content = optNullableString(obj, "content", "content");
        String mediaUrl = normalizeUrl(optNullableString(obj, "mediaUrl", "media_url"));
        boolean isPublic = obj.optBoolean("isPublic", obj.optBoolean("is_public", true));
        int likesCount = obj.optInt("likesCount", obj.optInt("likes_count", 0));
        int commentsCount = obj.optInt("commentsCount", obj.optInt("comments_count", 0));
        boolean isLiked = obj.optBoolean("isLiked", obj.optBoolean("is_liked", false));
        long createdAt = obj.optLong("createdAt", obj.optLong("created_at", System.currentTimeMillis()));

        return new Post(
                id,
                authorId,
                authorName,
                authorAvatar,
                content != null ? content : "",
                mediaUrl,
                isPublic,
                likesCount,
                commentsCount,
                isLiked,
                createdAt
        );
    }

    /**
     * Chuyển đổi JSONObject từ FastAPI (CommentResponse) sang đối tượng Comment của Android.
     */
    private Comment parseComment(JSONObject obj, String defaultPostId) {
        String id = obj.optString("id", "");
        String postId = obj.optString("postId", obj.optString("post_id", defaultPostId));
        String authorId = obj.optString("authorId", obj.optString("author_id", ""));
        String authorName = obj.optString("authorName", obj.optString("author_name", "Người dùng Facebook"));
        String authorAvatar = normalizeUrl(optNullableString(obj, "authorAvatar", "author_avatar"));
        String content = obj.optString("content", "");
        long createdAt = obj.optLong("createdAt", obj.optLong("created_at", System.currentTimeMillis()));

        return new Comment(id, postId, authorId, authorName, authorAvatar, content, createdAt);
    }

    /**
     * Lấy chuỗi từ JSON an toàn (tránh chuỗi "null" của JSONObject.optString).
     */
    @Nullable
    private String optNullableString(JSONObject obj, String camelKey, String snakeKey) {
        if (!obj.isNull(camelKey)) {
            String val = obj.optString(camelKey, "");
            if (!val.isEmpty() && !"null".equalsIgnoreCase(val)) {
                return val;
            }
        }
        if (!obj.isNull(snakeKey)) {
            String val = obj.optString(snakeKey, "");
            if (!val.isEmpty() && !"null".equalsIgnoreCase(val)) {
                return val;
            }
        }
        return null;
    }

    /**
     * Chuẩn hóa đường dẫn ảnh tĩnh từ FastAPI (/static/uploads/...) thành URL đầy đủ cho Glide.
     */
    @Nullable
    private String normalizeUrl(@Nullable String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return null;
        }
        String trimmed = rawUrl.trim();
        if (trimmed.startsWith("/")) {
            return SERVER_ORIGIN + trimmed;
        }
        return trimmed;
    }
}