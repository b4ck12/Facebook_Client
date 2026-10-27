package com.facebook.client.features.feed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.facebook.client.core.common.Resource;
import com.facebook.client.core.utils.PostValidator;
import com.facebook.client.data.model.Post;
import com.facebook.client.data.repository.IPostRepository;
import com.facebook.client.data.repository.PostRepositoryImpl;
import com.facebook.client.data.repository.RepositoryCallback;
import java.io.File;
import java.net.URI;

/**
 * ViewModel điều phối nghiệp vụ màn hình Tạo bài viết mới ([D8-TD]).
 *
 * <p>Trách nhiệm chính:
 * <ul>
 *   <li>Kiểm tra tính hợp lệ của bài viết qua {@link PostValidator} (chặn bài rỗng không có cả
 *       chữ lẫn ảnh).
 *   <li>Nếu người dùng đính kèm tệp ảnh từ thiết bị, gọi {@link IPostRepository#uploadImage}
 *       ({@code POST /api/upload} dạng Multipart) để nhận đường dẫn ảnh tĩnh trên máy chủ.
 *   <li>Gọi {@link IPostRepository#createPost} ({@code POST /api/posts}) để lưu bài viết mới vào
 *       CSDL SQLite và phát trạng thái {@link Resource} về cho {@link CreatePostActivity}.
 *   <li>Tuân thủ Clean Architecture: Tuyệt đối không giữ tham chiếu tới {@code Context} hay
 *       {@code View}.
 * </ul>
 */
public class CreatePostViewModel extends ViewModel {

  private final IPostRepository postRepository;
  private final MutableLiveData<Resource<Post>> createPostStatus = new MutableLiveData<>();

  /**
   * Khởi tạo ViewModel mặc định sử dụng {@link PostRepositoryImpl} kết nối FastAPI.
   */
  public CreatePostViewModel() {
    this(new PostRepositoryImpl());
  }

  /**
   * Constructor hỗ trợ tiêm {@link IPostRepository} (phục vụ Unit Test với Mockito).
   */
  public CreatePostViewModel(@NonNull IPostRepository postRepository) {
    this.postRepository = postRepository;
  }

  /**
   * LiveData theo dõi trạng thái quá trình tạo bài viết (LOADING, SUCCESS, ERROR).
   */
  @NonNull
  public LiveData<Resource<Post>> getCreatePostStatus() {
    return createPostStatus;
  }

  /**
   * Gửi bài viết mới với nội dung văn bản và đường dẫn URI ảnh (nếu có).
   *
   * @param authorId Mã định danh người đăng bài.
   * @param content  Nội dung văn bản của bài viết.
   * @param imageUri Đường dẫn URI hoặc URL của hình ảnh đính kèm (có thể null).
   */
  public void submitPost(
      @Nullable String authorId, @Nullable String content, @Nullable String imageUri) {
    File localFile = resolveLocalFileIfPresent(imageUri);
    submitPost(authorId, content, localFile, imageUri);
  }

  /**
   * Gửi bài viết mới kèm tệp ảnh đã nén (Multipart Upload) hoặc chỉ văn bản.
   *
   * @param authorId            Mã định danh người đăng bài.
   * @param content             Nội dung văn bản của bài viết.
   * @param compressedImageFile Tệp ảnh cục bộ đã nén sẵn sàng upload lên {@code POST /api/upload}.
   * @param rawImageUri         Chuỗi URI ảnh gốc (dùng để kiểm tra validation hoặc dự phòng).
   */
  public void submitPost(
      @Nullable String authorId,
      @Nullable String content,
      @Nullable File compressedImageFile,
      @Nullable String rawImageUri) {

    String effectiveMedia =
        (compressedImageFile != null && compressedImageFile.exists())
            ? compressedImageFile.getAbsolutePath()
            : rawImageUri;

    // 1. Kiểm tra ràng buộc: Bài viết bắt buộc phải có chữ hoặc có ảnh
    if (!PostValidator.isValidPost(content, effectiveMedia)) {
      createPostStatus.setValue(Resource.error("Vui lòng nhập nội dung hoặc chọn ảnh!"));
      return;
    }

    String safeAuthorId =
        (authorId != null && !authorId.trim().isEmpty()) ? authorId.trim() : "user_101";
    String safeContent = content != null ? content.trim() : "";

    // 2. Phát tín hiệu LOADING để giao diện khóa nút ĐĂNG tránh bấm đúp
    createPostStatus.setValue(Resource.loading());

    // 3. Nếu có tệp ảnh vật lý -> Upload Multipart lên POST /api/upload trước
    if (compressedImageFile != null
        && compressedImageFile.exists()
        && compressedImageFile.length() > 0) {
      postRepository.uploadImage(
          compressedImageFile,
          new RepositoryCallback<String>() {
            @Override
            public void onSuccess(String uploadedMediaUrl) {
              String finalMediaUrl =
                  (uploadedMediaUrl != null && !uploadedMediaUrl.trim().isEmpty())
                      ? uploadedMediaUrl.trim()
                      : rawImageUri;
              createPostOnServer(safeAuthorId, safeContent, finalMediaUrl);
            }

            @Override
            public void onError(String message) {
              createPostStatus.postValue(
                  Resource.error(
                      message != null ? message : "Tải ảnh lên máy chủ thất bại!"));
            }
          });
      return;
    }

    // 4. Trường hợp chỉ có chữ hoặc ảnh đã là đường dẫn URI/URL sẵn có
    String normalizedMedia =
        (rawImageUri != null && !rawImageUri.trim().isEmpty()) ? rawImageUri.trim() : null;
    createPostOnServer(safeAuthorId, safeContent, normalizedMedia);
  }

  private void createPostOnServer(
      @NonNull String authorId, @NonNull String content, @Nullable String mediaUrl) {
    postRepository.createPost(
        authorId,
        content,
        mediaUrl,
        true,
        new RepositoryCallback<Post>() {
          @Override
          public void onSuccess(Post createdPost) {
            createPostStatus.postValue(Resource.success(createdPost));
          }

          @Override
          public void onError(String message) {
            createPostStatus.postValue(
                Resource.error(
                    message != null ? message : "Không thể đăng bài viết. Vui lòng thử lại!"));
          }
        });
  }

  @Nullable
  private File resolveLocalFileIfPresent(@Nullable String imageUri) {
    if (imageUri == null || imageUri.trim().isEmpty()) {
      return null;
    }
    String trimmed = imageUri.trim();
    try {
      if (trimmed.startsWith("file://")) {
        File fileFromUri = new File(URI.create(trimmed));
        if (fileFromUri.exists() && fileFromUri.isFile()) {
          return fileFromUri;
        }
      } else if (!trimmed.startsWith("http://")
          && !trimmed.startsWith("https://")
          && !trimmed.startsWith("content://")) {
        File directFile = new File(trimmed);
        if (directFile.exists() && directFile.isFile()) {
          return directFile;
        }
      }
    } catch (Exception ignored) {
      // Không phải đường dẫn tệp cục bộ hợp lệ -> trả về null
    }
    return null;
  }
}
