package com.facebook.client.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.Post;
import com.facebook.client.data.model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository triển khai gọi API thực tế từ máy chủ Backend FastAPI cho phân hệ Trang cá nhân ([D9-TQH]).
 * <p>
 * Tính năng chính:
 * 1. Gọi GET /api/users/{id}/photos lấy danh sách ảnh của người dùng từ CSDL SQLite để hiển thị lên lưới 3 cột.
 * 2. Gọi GET /api/users/{id} lấy thông tin cá nhân (ảnh bìa, avatar tròn, tên hiển thị).
 * 3. Gọi GET /api/users/{id}/posts lấy danh sách bài viết cá nhân trên Tab Posts.
 * 4. Bộ nhớ đệm (In-memory Cache): Giữ nguyên danh sách ảnh khi người dùng chuyển đổi qua lại giữa Tab Photos và Posts.
 * 5. Cơ chế Graceful Degradation (Dự phòng an toàn): Tự động fallback về MockDataGenerator nếu máy chủ FastAPI chưa bật.
 */
public class ProfileRepositoryImpl implements IProfileRepository {

    // Địa chỉ gốc của máy chủ FastAPI (10.0.2.2 trỏ về localhost máy tính trên Android Emulator)
    private static final String BASE_URL = "http://10.0.2.2:8000/api/users";

    // Thời gian chờ phản hồi tối đa (3 giây)
    private static final int TIMEOUT_MS = 3000;

    // Luồng thực thi ngầm tránh làm đơ giao diện chính (UI Thread)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Bộ nhớ đệm danh sách ảnh của người dùng để tải tức thì khi chuyển tab
    private static final List<Post> cachedPhotos = new CopyOnWriteArrayList<>();
    private static String cachedUserId = null;

    @Override
    public void getUserPhotos(String userId, RepositoryCallback<List<Post>> callback) {
        if (callback == null) return;

        executor.execute(() -> {
            String targetId = (userId != null && !userId.trim().isEmpty())
                    ? userId
                    : MockDataGenerator.getInstance().getCurrentUserId();

            List<Post> resultList = null;

            // 1. CỐ GẮNG KẾT NỐI VÀ LẤY KHO ẢNH THẬT TỪ FASTAPI SERVER (GET /api/users/{id}/photos)
            try {
                String fullUrl = BASE_URL + "/" + targetId + "/photos";
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();
                    conn.disconnect();

                    JSONArray jsonArray = new JSONArray(responseBuilder.toString());
                    resultList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Post post = new Post(
                                obj.getString("id"),
                                obj.optString("authorId", targetId),
                                obj.optString("authorName", "Người dùng"),
                                obj.optString("authorAvatar", null),
                                obj.optString("content", ""),
                                obj.optString("mediaUrl", null),
                                obj.optBoolean("isPublic", true),
                                obj.optInt("likesCount", 0),
                                obj.optInt("commentsCount", 0),
                                obj.optBoolean("isLiked", false),
                                obj.optLong("createdAt", System.currentTimeMillis())
                        );
                        resultList.add(post);
                    }

                    // Lưu vào bộ nhớ đệm
                    cachedPhotos.clear();
                    cachedPhotos.addAll(resultList);
                    cachedUserId = targetId;
                }
            } catch (Exception e) {
                // Server chưa bật hoặc mạng lỗi -> tự động chuyển sang cơ chế Fallback
                resultList = null;
            }

            // 2. CƠ CHẾ DỰ PHÒNG (FALLBACK TO CACHE / MOCK DATA KHI OFFLINE)
            if (resultList == null) {
                if (targetId.equals(cachedUserId) && !cachedPhotos.isEmpty()) {
                    resultList = new ArrayList<>(cachedPhotos);
                } else {
                    List<Post> allPosts = MockDataGenerator.getInstance().getPosts();
                    resultList = new ArrayList<>();
                    for (Post p : allPosts) {
                        if (targetId.equals(p.getAuthorId()) && p.getMediaUrl() != null && !p.getMediaUrl().trim().isEmpty()) {
                            resultList.add(p);
                        }
                    }
                    // Nếu user có ít ảnh thì lấy thêm bài viết mẫu có ảnh để hiển thị đầy đủ lưới 3 cột
                    if (resultList.isEmpty()) {
                        for (Post p : allPosts) {
                            if (p.getMediaUrl() != null && !p.getMediaUrl().trim().isEmpty()) {
                                resultList.add(p);
                            }
                        }
                    }
                    cachedPhotos.clear();
                    cachedPhotos.addAll(resultList);
                    cachedUserId = targetId;
                }
            }

            final List<Post> finalResult = resultList;
            mainHandler.post(() -> callback.onSuccess(finalResult));
        });
    }

    @Override
    public void getUserProfile(String userId, RepositoryCallback<User> callback) {
        if (callback == null) return;

        executor.execute(() -> {
            String targetId = (userId != null && !userId.trim().isEmpty())
                    ? userId
                    : MockDataGenerator.getInstance().getCurrentUserId();

            User resultUser = null;

            // 1. Thử gọi API thật từ FastAPI server: GET /api/users/{id}
            try {
                String fullUrl = BASE_URL + "/" + targetId;
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();
                    conn.disconnect();

                    JSONObject obj = new JSONObject(responseBuilder.toString());
                    resultUser = new User(
                            obj.getString("id"),
                            obj.optString("email", ""),
                            obj.optString("displayName", "Người dùng"),
                            obj.optString("avatarUrl", null),
                            obj.optString("coverUrl", null),
                            obj.optLong("createdAt", System.currentTimeMillis())
                    );
                }
            } catch (Exception ignored) {
                resultUser = null;
            }

            // 2. Cơ chế Fallback sang MockDataGenerator
            if (resultUser == null) {
                resultUser = MockDataGenerator.getInstance().getUserById(targetId);
                if (resultUser == null) {
                    resultUser = MockDataGenerator.getInstance().getCurrentUser();
                }
            }

            final User finalUser = resultUser;
            mainHandler.post(() -> {
                if (finalUser != null) {
                    callback.onSuccess(finalUser);
                } else {
                    callback.onError("Không thể tải thông tin người dùng");
                }
            });
        });
    }

    @Override
    public void getUserPosts(String userId, RepositoryCallback<List<Post>> callback) {
        if (callback == null) return;
        final String viewerId =
                MockDataGenerator.getInstance().getCurrentUserId();
        executor.execute(() -> {
            String targetId = (userId != null && !userId.trim().isEmpty())
                    ? userId
                    : MockDataGenerator.getInstance().getCurrentUserId();

            List<Post> resultList = null;

            // 1. Thử gọi API thật từ FastAPI server: GET /api/users/{id}/posts
            try {
                String fullUrl = BASE_URL + "/"
                        + java.net.URLEncoder.encode(targetId, "UTF-8")
                        + "/posts?current_user_id="
                        + java.net.URLEncoder.encode(viewerId, "UTF-8");
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();
                    conn.disconnect();

                    JSONArray jsonArray = new JSONArray(responseBuilder.toString());
                    resultList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Post post = new Post(
                                obj.getString("id"),
                                obj.optString("authorId", targetId),
                                obj.optString("authorName", "Người dùng"),
                                obj.optString("authorAvatar", null),
                                obj.optString("content", ""),
                                obj.optString("mediaUrl", null),
                                obj.optBoolean("isPublic", true),
                                obj.optInt("likesCount", 0),
                                obj.optInt("commentsCount", 0),
                                obj.optBoolean("isLiked", false),
                                obj.optLong("createdAt", System.currentTimeMillis())
                        );
                        resultList.add(post);
                    }
                }
            } catch (Exception ignored) {
                resultList = null;
            }

            // 2. Cơ chế Fallback sang MockDataGenerator
            if (resultList == null) {
                List<Post> allPosts = MockDataGenerator.getInstance().getPosts();
                resultList = new ArrayList<>();
                for (Post p : allPosts) {
                    if (targetId.equals(p.getAuthorId())) {
                        resultList.add(p);
                    }
                }
            }

            final List<Post> finalResult = resultList;
            mainHandler.post(() -> callback.onSuccess(finalResult));
        });
    }
}
