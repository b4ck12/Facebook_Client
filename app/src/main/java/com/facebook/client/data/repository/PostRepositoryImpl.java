package com.facebook.client.data.repository;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.facebook.client.core.network.ApiClient;
import com.facebook.client.core.network.ApiResponseHandler;
import com.facebook.client.core.network.ApiService;
import com.facebook.client.core.utils.PostValidator;
import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.Comment;
import com.facebook.client.data.model.Post;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Triển khai {@link IPostRepository} kết nối trực tiếp tới máy chủ Python FastAPI &amp; SQLite
 * thông qua tầng mạng chuẩn {@link ApiClient} và {@link ApiService} (Retrofit 2 - [D8-TD] &amp; [D8-ML]).
 *
 * <p>Tính năng chính:
 * <ul>
 *   <li>{@code GET /api/posts}: Lấy danh sách bài viết Bảng tin Feed.
 *   <li>{@code POST /api/upload}: Tải lên hình ảnh dạng {@code multipart/form-data} vào
 *       {@code /static/uploads/} và nhận đường dẫn URL ảnh tĩnh.
 *   <li>{@code POST /api/posts}: Tạo bài viết mới trong SQLite.
 *   <li>{@code POST /api/posts/{id}/like}: Toggle Like bài viết trong bảng {@code post_likes}.
 *   <li>{@code POST /api/posts/{id}/share}: Chia sẻ bài viết kèm caption.
 *   <li>{@code GET /api/posts/{id}/comments} &amp; {@code POST /api/posts/{id}/comments}:
 *       Tải và gửi bình luận văn bản (Text-only).
 *   <li>Cơ chế Graceful Degradation (Dự phòng an toàn): Khi máy chủ FastAPI chưa bật,
 *       tự động fallback về {@link MockPostRepository} và {@link MockDataGenerator}.
 * </ul>
 */
@SuppressWarnings("SpellCheckingInspection")
public class PostRepositoryImpl implements IPostRepository {

  private final ApiService apiService;
  private final MockPostRepository fallbackMock;

  /**
   * Khởi tạo Repository mặc định sử dụng {@link ApiClient#getApiService()}.
   */
  public PostRepositoryImpl() {
    this(ApiClient.getApiService(), new MockPostRepository());
  }

  /**
   * Constructor hỗ trợ tiêm {@link ApiService} tùy chỉnh (phục vụ Unit Test với Mockito).
   */
  public PostRepositoryImpl(@NonNull ApiService apiService) {
    this(apiService, new MockPostRepository());
  }

  public PostRepositoryImpl(
      @NonNull ApiService apiService, @NonNull MockPostRepository fallbackMock) {
    this.apiService = apiService;
    this.fallbackMock = fallbackMock;
  }

  @Override
  public void getFeedPosts(
      int limit, String currentUserId, RepositoryCallback<List<Post>> callback) {
    if (callback == null) {
      return;
    }

    String safeUserId =
        (currentUserId != null && !currentUserId.trim().isEmpty())
            ? currentUserId.trim()
            : "user_101";
    int safeLimit = limit > 0 ? limit : 20;

    apiService
        .getFeedPosts(safeLimit, safeUserId)
        .enqueue(
            new Callback<List<Post>>() {
              @Override
              public void onResponse(
                  @NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  List<Post> normalizedList = new ArrayList<>();
                  for (Post post : response.body()) {
                    if (post != null) {
                      normalizedList.add(normalizePost(post));
                    }
                  }
                  dispatchMain(() -> callback.onSuccess(normalizedList));
                } else {
                  dispatchMain(() -> fallbackMock.getFeedPosts(limit, currentUserId, callback));
                }
              }

              @Override
              public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                // Máy chủ FastAPI chưa bật -> Tự động chuyển sang Fallback
                dispatchMain(() -> fallbackMock.getFeedPosts(limit, currentUserId, callback));
              }
            });
  }

  @Override
  public void uploadImage(File imageFile, RepositoryCallback<String> callback) {
    if (callback == null) {
      return;
    }
    if (imageFile == null || !imageFile.exists() || imageFile.length() == 0) {
      dispatchMain(() -> callback.onError("Tệp hình ảnh không hợp lệ hoặc không tồn tại!"));
      return;
    }

    String mimeType = resolveMimeType(imageFile.getName());
    RequestBody requestFile = RequestBody.create(imageFile, MediaType.parse(mimeType));
    MultipartBody.Part filePart =
        MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

    apiService
        .uploadImage(filePart)
        .enqueue(
            new Callback<Map<String, Object>>() {
              @Override
              public void onResponse(
                  @NonNull Call<Map<String, Object>> call,
                  @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  Object rawUrlObj = response.body().get("url");
                  String uploadedUrl =
                      rawUrlObj != null ? ApiClient.normalizeStaticUrl(String.valueOf(rawUrlObj)) : null;
                  if (uploadedUrl != null && !uploadedUrl.isEmpty()) {
                    dispatchMain(() -> callback.onSuccess(uploadedUrl));
                  } else {
                    dispatchMain(() -> callback.onSuccess(imageFile.toURI().toString()));
                  }
                } else if (response.code() >= 400 && response.code() < 500) {
                  String errorMsg = ApiResponseHandler.parseHttpError(response);
                  dispatchMain(() -> callback.onError(errorMsg));
                } else {
                  // Lỗi máy chủ 5xx -> Dự phòng đường dẫn tệp cục bộ
                  dispatchMain(() -> callback.onSuccess(imageFile.toURI().toString()));
                }
              }

              @Override
              public void onFailure(
                  @NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                // Máy chủ FastAPI chưa bật -> Trả về URI tệp cục bộ để đăng bài ở chế độ Offline
                dispatchMain(() -> callback.onSuccess(imageFile.toURI().toString()));
              }
            });
  }

  @Override
  public void createPost(
      String authorId,
      String content,
      String mediaUrl,
      boolean isPublic,
      RepositoryCallback<Post> callback) {
    if (callback == null) {
      return;
    }

    if (!PostValidator.isValidPost(content, mediaUrl)) {
      dispatchMain(() -> callback.onError("Vui lòng nhập nội dung hoặc chọn ảnh!"));
      return;
    }

    String safeAuthorId =
        (authorId != null && !authorId.trim().isEmpty()) ? authorId.trim() : "user_101";
    String safeContent = content != null ? content.trim() : "";
    String safeMediaUrl =
        (mediaUrl != null && !mediaUrl.trim().isEmpty()) ? mediaUrl.trim() : null;

    Map<String, Object> payload = new HashMap<>();
    payload.put("authorId", safeAuthorId);
    payload.put("content", safeContent);
    payload.put("mediaUrl", safeMediaUrl);
    payload.put("isPublic", isPublic);

    apiService
        .createPost(payload)
        .enqueue(
            new Callback<Post>() {
              @Override
              public void onResponse(
                  @NonNull Call<Post> call, @NonNull Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                  Post createdPost = normalizePost(response.body());
                  // Đồng bộ vào bộ nhớ tạm để giữ tính nhất quán giữa các Tab
                  fallbackMock.createPost(safeAuthorId, safeContent, createdPost.getMediaUrl(), isPublic, null);
                  MockDataGenerator.getInstance().addPost(createdPost);
                  dispatchMain(() -> callback.onSuccess(createdPost));
                } else if (response.code() == 400 || response.code() == 422) {
                  String errorMsg = ApiResponseHandler.parseHttpError(response);
                  dispatchMain(() -> callback.onError(errorMsg));
                } else {
                  createOfflinePostFallback(safeAuthorId, safeContent, safeMediaUrl, isPublic, callback);
                }
              }

              @Override
              public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                // Máy chủ FastAPI chưa bật -> Tự động lưu vào bộ nhớ Fallback
                createOfflinePostFallback(safeAuthorId, safeContent, safeMediaUrl, isPublic, callback);
              }
            });
  }

  private void createOfflinePostFallback(
      String authorId,
      String content,
      String mediaUrl,
      boolean isPublic,
      RepositoryCallback<Post> callback) {
    dispatchMain(
        () -> {
          MockDataGenerator.getInstance().createPost(authorId, content, mediaUrl, isPublic);
          fallbackMock.createPost(authorId, content, mediaUrl, isPublic, callback);
        });
  }

  @Override
  public void toggleLike(String postId, String userId, RepositoryCallback<Boolean> callback) {
    if (postId == null || postId.trim().isEmpty()) {
      return;
    }

    String safeUserId =
        (userId != null && !userId.trim().isEmpty()) ? userId.trim() : "user_101";

    apiService
        .toggleLike(postId.trim(), safeUserId)
        .enqueue(
            new Callback<Map<String, Object>>() {
              @Override
              public void onResponse(
                  @NonNull Call<Map<String, Object>> call,
                  @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  Object likedVal = response.body().get("isLiked");
                  boolean isLiked =
                      likedVal instanceof Boolean
                          ? (Boolean) likedVal
                          : Boolean.parseBoolean(String.valueOf(likedVal));
                  if (callback != null) {
                    dispatchMain(() -> callback.onSuccess(isLiked));
                  }
                } else {
                  dispatchMain(() -> fallbackMock.toggleLike(postId, userId, callback));
                }
              }

              @Override
              public void onFailure(
                  @NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                dispatchMain(() -> fallbackMock.toggleLike(postId, userId, callback));
              }
            });
  }

  @Override
  public void getComments(String postId, RepositoryCallback<List<Comment>> callback) {
    if (callback == null || postId == null || postId.trim().isEmpty()) {
      return;
    }

    apiService
        .getComments(postId.trim())
        .enqueue(
            new Callback<List<Comment>>() {
              @Override
              public void onResponse(
                  @NonNull Call<List<Comment>> call, @NonNull Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  List<Comment> normalizedComments = new ArrayList<>();
                  for (Comment c : response.body()) {
                    if (c != null) {
                      normalizedComments.add(normalizeComment(c, postId.trim()));
                    }
                  }
                  dispatchMain(() -> callback.onSuccess(normalizedComments));
                } else {
                  dispatchMain(() -> fallbackMock.getComments(postId, callback));
                }
              }

              @Override
              public void onFailure(@NonNull Call<List<Comment>> call, @NonNull Throwable t) {
                dispatchMain(() -> fallbackMock.getComments(postId, callback));
              }
            });
  }

  @Override
  public void addComment(
      String postId, String authorId, String content, RepositoryCallback<Comment> callback) {
    if (callback == null || postId == null || postId.trim().isEmpty()) {
      return;
    }

    if (content == null || content.trim().isEmpty()) {
      dispatchMain(() -> callback.onError("Nội dung bình luận không được để trống!"));
      return;
    }

    String safeAuthorId =
        (authorId != null && !authorId.trim().isEmpty()) ? authorId.trim() : "user_101";
    String safeContent = content.trim();

    Map<String, String> payload = new HashMap<>();
    payload.put("authorId", safeAuthorId);
    payload.put("content", safeContent);

    apiService
        .addComment(postId.trim(), payload)
        .enqueue(
            new Callback<Comment>() {
              @Override
              public void onResponse(
                  @NonNull Call<Comment> call, @NonNull Response<Comment> response) {
                if (response.isSuccessful() && response.body() != null) {
                  Comment createdComment = normalizeComment(response.body(), postId.trim());
                  fallbackMock.addComment(postId, safeAuthorId, safeContent, null);
                  MockDataGenerator.getInstance().addComment(postId, createdComment);
                  dispatchMain(() -> callback.onSuccess(createdComment));
                } else {
                  dispatchMain(() -> fallbackMock.addComment(postId, safeAuthorId, safeContent, callback));
                }
              }

              @Override
              public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
                dispatchMain(() -> fallbackMock.addComment(postId, safeAuthorId, safeContent, callback));
              }
            });
  }

  @Override
  public void sharePost(
      String originalPostId, String authorId, String caption, RepositoryCallback<Post> callback) {
    if (callback == null || originalPostId == null || originalPostId.trim().isEmpty()) {
      return;
    }

    String safeUserId =
        (authorId != null && !authorId.trim().isEmpty()) ? authorId.trim() : "user_101";
    String safeCaption =
        (caption != null && !caption.trim().isEmpty()) ? caption.trim() : null;

    apiService
        .sharePost(originalPostId.trim(), safeUserId, safeCaption)
        .enqueue(
            new Callback<Post>() {
              @Override
              public void onResponse(
                  @NonNull Call<Post> call, @NonNull Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                  Post sharedPost = normalizePost(response.body());
                  fallbackMock.sharePost(originalPostId, safeUserId, safeCaption, null);
                  MockDataGenerator.getInstance().addPost(sharedPost);
                  dispatchMain(() -> callback.onSuccess(sharedPost));
                } else {
                  dispatchMain(
                      () -> fallbackMock.sharePost(originalPostId, safeUserId, safeCaption, callback));
                }
              }

              @Override
              public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                dispatchMain(
                    () -> fallbackMock.sharePost(originalPostId, safeUserId, safeCaption, callback));
              }
            });
  }

  @NonNull
  private Post normalizePost(@NonNull Post post) {
    if (post.getAuthorName() == null || post.getAuthorName().trim().isEmpty()) {
      post.setAuthorName("Người dùng Facebook");
    }
    if (post.getContent() == null) {
      post.setContent("");
    }
    post.setAuthorAvatar(ApiClient.normalizeStaticUrl(post.getAuthorAvatar()));
    post.setMediaUrl(ApiClient.normalizeStaticUrl(post.getMediaUrl()));
    if (post.getCreatedAt() <= 0) {
      post.setCreatedAt(System.currentTimeMillis());
    }
    return post;
  }

  @NonNull
  private Comment normalizeComment(@NonNull Comment comment, @NonNull String defaultPostId) {
    if (comment.getPostId() == null || comment.getPostId().trim().isEmpty()) {
      comment.setPostId(defaultPostId);
    }
    if (comment.getAuthorName() == null || comment.getAuthorName().trim().isEmpty()) {
      comment.setAuthorName("Người dùng Facebook");
    }
    if (comment.getContent() == null) {
      comment.setContent("");
    }
    comment.setAuthorAvatar(ApiClient.normalizeStaticUrl(comment.getAuthorAvatar()));
    if (comment.getCreatedAt() <= 0) {
      comment.setCreatedAt(System.currentTimeMillis());
    }
    return comment;
  }

  @NonNull
  private String resolveMimeType(@Nullable String filename) {
    if (filename == null) {
      return "image/jpeg";
    }
    String lower = filename.toLowerCase(Locale.ROOT);
    if (lower.endsWith(".png")) {
      return "image/png";
    }
    if (lower.endsWith(".webp")) {
      return "image/webp";
    }
    return "image/jpeg";
  }

  private void dispatchMain(@NonNull Runnable action) {
    try {
      Looper mainLooper = Looper.getMainLooper();
      if (mainLooper != null) {
        if (Thread.currentThread() == mainLooper.getThread()) {
          action.run();
        } else {
          new Handler(mainLooper).post(action);
        }
        return;
      }
    } catch (Throwable ignored) {
      // Trong môi trường JUnit Test trên JVM không mock Looper -> chạy trực tiếp
    }
    action.run();
  }
}