package com.facebook.client.core.network;

import com.facebook.client.data.model.Comment;
import com.facebook.client.data.model.FriendRequest;
import com.facebook.client.data.model.Notification;
import com.facebook.client.data.model.Post;
import com.facebook.client.data.model.User;
import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Giao diện định nghĩa toàn bộ các RESTful API Endpoints kết nối với máy chủ
 * Python FastAPI &amp; SQLite ([D8-TD]).
 *
 * <p>Base URL chuẩn trên máy ảo Android: {@code http://10.0.2.2:8000/api/}
 */
public interface ApiService {

  // =========================================================================
  // 1. PHÂN HỆ BẢNG TIN & BÀI VIẾT (FEED & POSTS)
  // =========================================================================

  @GET("posts")
  Call<List<Post>> getFeedPosts(
      @Query("limit") int limit,
      @Query("current_user_id") String currentUserId);

  @POST("posts")
  Call<Post> createPost(@Body Map<String, Object> body);

  @POST("posts/{id}/like")
  Call<Map<String, Object>> toggleLike(
      @Path("id") String postId,
      @Query("user_id") String userId);

  @POST("posts/{id}/share")
  Call<Post> sharePost(
      @Path("id") String postId,
      @Query("user_id") String userId,
      @Query("caption") String caption);

  // =========================================================================
  // 2. PHÂN HỆ BÌNH LUẬN VĂN BẢN (COMMENTS - TEXT ONLY)
  // =========================================================================

  @GET("posts/{id}/comments")
  Call<List<Comment>> getComments(@Path("id") String postId);

  @POST("posts/{id}/comments")
  Call<Comment> addComment(
      @Path("id") String postId,
      @Body Map<String, String> body);

  // =========================================================================
  // 3. PHÂN HỆ UPLOAD ẢNH MULTIPART (MEDIA UPLOAD)
  // =========================================================================

  @Multipart
  @POST("upload")
  Call<Map<String, Object>> uploadImage(@Part MultipartBody.Part file);

  // =========================================================================
  // 4. PHÂN HỆ NGƯỜI DÙNG & TRANG CÁ NHÂN (USERS & PROFILE)
  // =========================================================================

  @GET("users")
  Call<List<User>> getAllUsers();

  @GET("users/{id}")
  Call<User> getUserProfile(@Path("id") String userId);

  @PUT("users/{id}")
  Call<User> updateUserProfile(
      @Path("id") String userId,
      @Body Map<String, Object> body);

  @GET("users/{id}/posts")
  Call<List<Post>> getUserPosts(@Path("id") String userId);

  @GET("users/{id}/photos")
  Call<List<Post>> getUserPhotos(@Path("id") String userId);

  // =========================================================================
  // 5. PHÂN HỆ BẠN BÈ & TÌM KIẾM (FRIENDS & SEARCH)
  // =========================================================================

  @GET("friends/requests")
  Call<List<FriendRequest>> getFriendRequests(@Query("user_id") String userId);

  @GET("friends")
  Call<List<User>> getFriends(@Query("user_id") String userId);

  @POST("friends/requests")
  Call<FriendRequest> sendFriendRequest(@Body Map<String, String> body);

  @POST("friends/accept")
  Call<Map<String, Object>> acceptFriendRequest(@Body Map<String, String> body);

  @POST("friends/reject")
  Call<Map<String, Object>> rejectFriendRequest(@Body Map<String, String> body);

  @GET("friends/search")
  Call<List<User>> searchFriends(
      @Query("q") String query,
      @Query("user_id") String userId);

  // =========================================================================
  // 6. PHÂN HỆ THÔNG BÁO & KIỂM TRA MẠNG (NOTIFICATIONS & HEALTH)
  // =========================================================================

  @GET("notifications")
  Call<List<Notification>> getNotifications(@Query("user_id") String userId);

  @POST("notifications/{id}/read")
  Call<Map<String, Object>> markNotificationAsRead(@Path("id") String notificationId);

  @GET("network-test")
  Call<Map<String, Object>> checkNetworkConnectivity();
}
