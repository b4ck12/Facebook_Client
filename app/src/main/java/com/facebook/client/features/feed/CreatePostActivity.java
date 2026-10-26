package com.facebook.client.features.feed;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.facebook.client.R;
import com.facebook.client.core.base.BaseActivity;
import com.facebook.client.core.common.Resource;
import com.facebook.client.core.utils.BitmapCompressor;
import com.facebook.client.core.utils.PostValidator;
import com.facebook.client.core.utils.SessionManager;
import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.User;
import com.facebook.client.databinding.ActivityCreatePostBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Màn hình tạo bài viết mới (Create Post Screen - [D4-TD], [D5-TD], [D8-TD]).
 *
 * <p>Chức năng hoàn thiện trong task [D8-TD]:
 * <ul>
 *   <li>Chọn 1 ảnh từ thư viện máy bằng {@link ActivityResultContracts.GetContent}.
 *   <li>Hiển thị ảnh xem trước và nút [X] để gỡ ảnh.
 *   <li>Kiểm tra validation qua {@link PostValidator}: Khóa nút POST (alpha=0.4, enabled=false)
 *       nếu cả text và ảnh đều rỗng; bật sáng (alpha=1.0, enabled=true) khi có ít nhất 1 yếu tố.
 *   <li>Kết nối {@link CreatePostViewModel} và {@code PostRepositoryImpl}: Nén ảnh qua
 *       {@link BitmapCompressor}, upload ảnh multipart ({@code POST /api/upload}) và tạo bài viết
 *       mới ({@code POST /api/posts}) trên máy chủ FastAPI + SQLite.
 * </ul>
 */
public class CreatePostActivity extends BaseActivity<ActivityCreatePostBinding> {

  private String selectedImageUri = null;
  private boolean isSubmitting = false;
  private ActivityResultLauncher<String> imagePickerLauncher;
  private CreatePostViewModel viewModel;
  private final ExecutorService imagePrepExecutor = Executors.newSingleThreadExecutor();

  @Override
  protected ActivityCreatePostBinding inflateBinding(LayoutInflater inflater) {
    return ActivityCreatePostBinding.inflate(inflater);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    // Đăng ký trình chọn ảnh trước khi Activity chuyển sang trạng thái STARTED
    imagePickerLauncher =
        registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
              if (uri != null) {
                selectedImageUri = uri.toString();
                displaySelectedImage(uri);
              }
              validatePost();
            });

    viewModel = new ViewModelProvider(this).get(CreatePostViewModel.class);
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void initViews() {
    setupWindowInsets();
    setupAuthorHeader();
    setupTextWatcher();
    setupImagePicker();
    setupSubmitButton();

    // Nút quay lại Top Bar
    binding.ibBack.setOnClickListener(
        v -> {
          if (!isSubmitting) {
            finish();
          }
        });

    // Kiểm tra validation ban đầu (mặc định khóa nút ĐĂNG)
    validatePost();
  }

  @Override
  protected void initObservers() {
    viewModel
        .getCreatePostStatus()
        .observe(
            this,
            resource -> {
              if (resource == null) {
                return;
              }
              if (resource.status == Resource.Status.LOADING) {
                setSubmittingUiState(true);
              } else if (resource.status == Resource.Status.SUCCESS) {
                setSubmittingUiState(false);
                showToast("Đã đăng bài viết thành công!");
                setResult(RESULT_OK);
                finish();
              } else if (resource.status == Resource.Status.ERROR) {
                setSubmittingUiState(false);
                showToast(
                    resource.message != null
                        ? resource.message
                        : "Không thể đăng bài viết. Vui lòng thử lại!");
              }
            });
  }

  /**
   * Áp dụng WindowInsets cho màn hình Tạo bài viết ([D7-TD]):
   * 1. Top Bar: Thêm padding đỉnh và tăng chiều cao bằng insets status bar + cutout
   *    để không bị che lấp bởi thanh trạng thái của hệ thống.
   * 2. Bottom Toolbar (Nút Photo/video): Đẩy thanh công cụ lên trên Navigation Bar
   *    hoặc bàn phím ảo (IME) khi người dùng nhập văn bản.
   */
  private void setupWindowInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(
        binding.getRoot(),
        (v, windowInsets) -> {
          Insets statusBarInsets =
              windowInsets.getInsets(
                  WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout());
          Insets bottomInsets =
              windowInsets.getInsets(
                  WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.ime());

          // 1. Cập nhật Top Bar
          int baseTopBarHeight = getResources().getDimensionPixelSize(R.dimen.top_bar_height);
          ViewGroup.LayoutParams topParams = binding.layoutTopBar.getLayoutParams();
          if (topParams != null) {
            topParams.height = baseTopBarHeight + statusBarInsets.top;
            binding.layoutTopBar.setLayoutParams(topParams);
          }
          binding.layoutTopBar.setPadding(
              binding.layoutTopBar.getPaddingLeft(),
              statusBarInsets.top,
              binding.layoutTopBar.getPaddingRight(),
              binding.layoutTopBar.getPaddingBottom());

          // 2. Cập nhật thanh công cụ đáy (layout_add_photo_button)
          ViewGroup.MarginLayoutParams bottomParams =
              (ViewGroup.MarginLayoutParams) binding.layoutAddPhotoButton.getLayoutParams();
          if (bottomParams != null) {
            bottomParams.bottomMargin = bottomInsets.bottom;
            binding.layoutAddPhotoButton.setLayoutParams(bottomParams);
          }

          return windowInsets;
        });
    ViewCompat.requestApplyInsets(binding.getRoot());
  }

  /**
   * Hiển thị thông tin người đăng bài từ tài khoản đang đăng nhập trong SessionManager.
   */
  private void setupAuthorHeader() {
    String sessionUserId = SessionManager.getInstance(this).getCurrentUserId();
    if (sessionUserId != null && !sessionUserId.trim().isEmpty()) {
      MockDataGenerator.getInstance().setCurrentUserId(sessionUserId.trim());
    }

    User currentUser = MockDataGenerator.getInstance().getCurrentUser();
    if (currentUser != null) {
      binding.tvAuthorName.setText(currentUser.getDisplayName());

      Glide.with(this)
          .load(currentUser.getAvatarUrl())
          .circleCrop()
          .placeholder(R.drawable.bg_avatar_placeholder)
          .error(R.drawable.bg_avatar_placeholder)
          .into(binding.ivAuthorAvatar);
    }
  }

  /**
   * Lắng nghe sự kiện thay đổi văn bản để cập nhật trạng thái nút ĐĂNG theo thời gian thực.
   */
  private void setupTextWatcher() {
    binding.etPostContent.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            validatePost();
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });
  }

  /**
   * Thiết lập sự kiện chọn ảnh từ thư viện và nút xóa ảnh xem trước.
   */
  private void setupImagePicker() {
    // Bấm nút "Photo/video" ở thanh công cụ đáy -> Mở Photo Picker của hệ thống
    binding.layoutAddPhotoButton.setOnClickListener(
        v -> {
          if (!isSubmitting) {
            imagePickerLauncher.launch("image/*");
          }
        });

    // Bấm nút [X] ở góc trên bên phải khung xem trước -> Xóa ảnh đã chọn
    binding.ibRemoveImage.setOnClickListener(
        v -> {
          if (isSubmitting) {
            return;
          }
          selectedImageUri = null;
          binding.cvMediaPreview.setVisibility(View.GONE);
          binding.ivPostImage.setImageDrawable(null);
          validatePost();
        });
  }

  /**
   * Hiển thị ảnh được chọn lên khung xem trước bằng Glide.
   */
  private void displaySelectedImage(Uri uri) {
    binding.cvMediaPreview.setVisibility(View.VISIBLE);
    Glide.with(this).load(uri).centerCrop().into(binding.ivPostImage);
  }

  /**
   * Kiểm tra điều kiện hợp lệ qua PostValidator và cập nhật nút ĐĂNG.
   * Ràng buộc cứng chuẩn Mobile App.pdf: Nếu chưa có gắn ảnh hay viết gì thì chưa được đăng.
   */
  private void validatePost() {
    if (isSubmitting) {
      binding.btnSubmitPost.setEnabled(false);
      binding.btnSubmitPost.setAlpha(0.4f);
      return;
    }
    String content =
        binding.etPostContent.getText() != null
            ? binding.etPostContent.getText().toString()
            : "";
    boolean isValid = PostValidator.isValidPost(content, selectedImageUri);

    binding.btnSubmitPost.setEnabled(isValid);
    binding.btnSubmitPost.setAlpha(isValid ? 1.0f : 0.4f);
  }

  /**
   * Thiết lập sự kiện bấm nút ĐĂNG bài viết mới qua CreatePostViewModel ([D8-TD]).
   */
  private void setupSubmitButton() {
    binding.btnSubmitPost.setOnClickListener(
        v -> {
          if (isSubmitting) {
            return;
          }

          String content =
              binding.etPostContent.getText() != null
                  ? binding.etPostContent.getText().toString().trim()
                  : "";

          if (!PostValidator.isValidPost(content, selectedImageUri)) {
            showToast("Vui lòng nhập nội dung hoặc chọn ảnh!");
            return;
          }

          String authorId = resolveCurrentAuthorId();

          // Nếu không có ảnh đính kèm -> Gửi bài viết văn bản ngay
          if (selectedImageUri == null || selectedImageUri.trim().isEmpty()) {
            viewModel.submitPost(authorId, content, null);
            return;
          }

          // Nếu có ảnh đính kèm -> Nén ảnh thành File trên luồng phụ rồi gọi ViewModel upload & đăng bài
          final String currentUriString = selectedImageUri.trim();
          setSubmittingUiState(true);
          imagePrepExecutor.execute(
              () -> {
                File preparedFile = prepareImageFileForUpload(Uri.parse(currentUriString));
                runOnUiThread(
                    () -> viewModel.submitPost(authorId, content, preparedFile, currentUriString));
              });
        });
  }

  @NonNull
  private String resolveCurrentAuthorId() {
    String sessionUserId = SessionManager.getInstance(this).getCurrentUserId();
    if (sessionUserId != null && !sessionUserId.trim().isEmpty()) {
      return sessionUserId.trim();
    }
    User currentUser = MockDataGenerator.getInstance().getCurrentUser();
    return currentUser != null ? currentUser.getId() : "user_101";
  }

  /**
   * Nén ảnh từ Uri sang File tạm bằng {@link BitmapCompressor}; nếu không giải mã được Bitmap
   * thì sao chép trực tiếp luồng dữ liệu từ ContentResolver sang File tạm để đảm bảo upload thành công.
   */
  @Nullable
  private File prepareImageFileForUpload(@NonNull Uri uri) {
    File compressed = BitmapCompressor.compressImageFile(getApplicationContext(), uri);
    if (compressed != null && compressed.exists() && compressed.length() > 0) {
      return compressed;
    }

    try (InputStream is = getContentResolver().openInputStream(uri)) {
      if (is == null) {
        return null;
      }
      File cacheDir = new File(getCacheDir(), "compressed_uploads");
      if (!cacheDir.exists()) {
        //noinspection ResultOfMethodCallIgnored
        cacheDir.mkdirs();
      }
      File fallbackFile =
          new File(cacheDir, "upload_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg");
      try (FileOutputStream fos = new FileOutputStream(fallbackFile)) {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = is.read(buffer)) != -1) {
          fos.write(buffer, 0, read);
        }
        fos.flush();
      }
      return fallbackFile.length() > 0 ? fallbackFile : null;
    } catch (Exception ignored) {
      return null;
    }
  }

  private void setSubmittingUiState(boolean submitting) {
    this.isSubmitting = submitting;
    binding.etPostContent.setEnabled(!submitting);
    binding.layoutAddPhotoButton.setEnabled(!submitting);
    binding.ibRemoveImage.setEnabled(!submitting);
    validatePost();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    imagePrepExecutor.shutdownNow();
  }
}
