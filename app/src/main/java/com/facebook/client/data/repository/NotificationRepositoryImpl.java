package com.facebook.client.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.Notification;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository triển khai gọi API thật từ máy chủ Backend FastAPI (Sprint 2 - [D8-TQH]).
 * <p>
 * Tính năng chính:
 * 1. Gọi endpoint GET /api/notifications?user_id=... lấy danh sách thông báo từ SQLite server.
 * 2. Gọi endpoint POST /api/notifications/{id}/read để cập nhật trạng thái đã đọc lên database.
 * 3. Hỗ trợ bộ nhớ đệm (In-memory Cache): Giữ nguyên trạng thái đã đọc khi người dùng chuyển qua lại giữa các Tab.
 * 4. Cơ chế Graceful Degradation (Chống sập ứng dụng): Nếu server FastAPI chưa bật, tự động fallback về MockDataGenerator an toàn.
 */
public class NotificationRepositoryImpl implements INotificationRepository {

    // Địa chỉ máy chủ FastAPI (10.0.2.2 là localhost của máy tính trên máy ảo Android Emulator)
    private static final String BASE_URL = "http://10.0.2.2:8000/api/notifications";

    // Thời gian chờ kết nối tối đa (3 giây)
    private static final int TIMEOUT_MS = 3000;

    // Luồng thực thi ngầm tránh làm đơ giao diện chính (UI Thread)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Bộ nhớ đệm danh sách thông báo để giữ trạng thái đã đọc không bị reset khi chuyển Tab
    private static final List<Notification> cachedNotifications = new CopyOnWriteArrayList<>();

    @Override
    public void getNotifications(String userId, RepositoryCallback<List<Notification>> callback) {
        if (callback == null) return;

        executor.execute(() -> {
            String targetId = (userId != null && !userId.trim().isEmpty()) ? userId : "user_current";
            List<Notification> resultList = null;

            // 1. CỐ GẮNG KẾT NỐI VÀ LẤY DỮ LIỆU THẬT TỪ FASTAPI SERVER
            try {
                String fullUrl = BASE_URL + "?user_id=" + targetId;
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

                    // Parse JSON mảng thông báo trả về từ Backend
                    JSONArray jsonArray = new JSONArray(responseBuilder.toString());
                    resultList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Notification notif = new Notification(
                                obj.getString("id"),
                                obj.optString("recipientId", targetId),
                                obj.optString("senderId", ""),
                                obj.optString("senderName", "Người dùng Facebook"),
                                obj.optString("senderAvatar", null),
                                obj.optString("type", Notification.TYPE_LIKE),
                                obj.optString("targetPostId", null),
                                obj.optString("message", ""),
                                obj.optBoolean("isRead", false),
                                obj.optLong("createdAt", System.currentTimeMillis())
                        );
                        resultList.add(notif);
                    }

                    // Cập nhật bộ nhớ đệm
                    cachedNotifications.clear();
                    cachedNotifications.addAll(resultList);
                }
            } catch (Exception e) {
                // Server FastAPI chưa bật hoặc mạng lỗi -> Tự động chuyển sang cơ chế Fallback
                resultList = null;
            }

            // 2. CƠ CHẾ DỰ PHÒNG (FALLBACK TO CACHE / MOCK DATA KHI OFFLINE)
            if (resultList == null) {
                if (cachedNotifications.isEmpty()) {
                    List<Notification> mockList = MockDataGenerator.getInstance().getNotifications(targetId);
                    cachedNotifications.addAll(mockList);
                }
                resultList = new ArrayList<>(cachedNotifications);
            }

            // Sắp xếp thông báo mới nhất lên đầu danh sách
            Collections.sort(resultList, (n1, n2) -> Long.compare(n2.getCreatedAt(), n1.getCreatedAt()));

            final List<Notification> finalResult = resultList;
            mainHandler.post(() -> callback.onSuccess(finalResult));
        });
    }

    @Override
    public void markAsRead(String notificationId, RepositoryCallback<Boolean> callback) {
        if (notificationId == null) return;

        // 1. CẬP NHẬT TRẠNG THÁI NGAY TRONG BỘ NHỚ ĐỆM (GIỮ MÀU TRẮNG KHI CHUYỂN TAB)
        for (Notification n : cachedNotifications) {
            if (notificationId.equals(n.getId())) {
                n.setRead(true);
                break;
            }
        }
        MockDataGenerator.getInstance().markNotificationAsRead(notificationId);

        // 2. GỬI YÊU CẦU POST LÊN SERVER FASTAPI ĐỂ LƯU VÀO SQLITE DATABASE
        executor.execute(() -> {
            try {
                String fullUrl = BASE_URL + "/" + notificationId + "/read";
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestProperty("Accept", "application/json");

                conn.getResponseCode(); // Kích hoạt kết nối gửi request
                conn.disconnect();
            } catch (Exception ignored) {
                // Nếu server chưa bật thì trạng thái vẫn đã được lưu trong bộ nhớ đệm
            }

            if (callback != null) {
                mainHandler.post(() -> callback.onSuccess(true));
            }
        });
    }
}
