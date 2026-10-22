package com.facebook.client.features.feed;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.facebook.client.R;
import com.facebook.client.core.base.BaseActivity;
import com.facebook.client.core.utils.PostValidator;
import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.User;
import com.facebook.client.databinding.ActivityCreatePostBinding;

/**
 * Màn hình tạo bài viết mới (Create Post Screen - [D4-TD] & [D5-TD]).
 *
 * <p>Chức năng hoàn thiện trong task [D5-TD]:
 * <ul>
 *   <li>Chọn 1 ảnh từ thư viện máy bằng {@link ActivityResultContracts.GetContent}.
 *   <li>Hiển thị ảnh xem trước và nút [X] để gỡ ảnh.
 *   <li>Kiểm tra validation qua {@link PostValidator}: Khóa nút POST (alpha=0.4, enabled=false)
 *       nếu cả text và ảnh đều rỗng; bật sáng (alpha=1.0, enabled=true) khi có ít nhất 1 yếu tố.
 *   <li>Lấy thông tin người dùng hiện tại từ {@link MockDataGenerator#getCurrentUser()}.
 *   <li>Lưu bài viết mới vào {@link MockDataGenerator#createPost} và trả về {@code RESULT_OK}.
 * </ul>
 */
public class CreatePostActivity extends BaseActivity<ActivityCreatePostBinding> {

  private String selectedImageUri = null;
  private ActivityResultLauncher<String> imagePickerLauncher;

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

    super.onCreate(savedInstanceState);
  }

  @Override
  protected void initViews() {
    setupAuthorHeader();
    setupTextWatcher();
    setupImagePicker();
    setupSubmitButton();

    // Nút quay lại Top Bar
    binding.ibBack.setOnClickListener(v -> finish());

    // Kiểm tra validation ban đầu (mặc định khóa nút ĐĂNG)
    validatePost();
  }

  /**
   * Hiển thị thông tin người đăng bài từ tài khoản đang đăng nhập.
   */
  private void setupAuthorHeader() {
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
    binding.layoutAddPhotoButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

    // Bấm nút [X] ở góc trên bên phải khung xem trước -> Xóa ảnh đã chọn
    binding.ibRemoveImage.setOnClickListener(
        v -> {
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
    Glide.with(this)
        .load(uri)
        .centerCrop()
        .into(binding.ivPostImage);
  }

  /**
   * Kiểm tra điều kiện hợp lệ qua PostValidator và cập nhật nút ĐĂNG.
   * Ràng buộc cứng chuẩn Mobile App.pdf: Nếu chưa có gắn ảnh hay viết gì thì chưa được đăng.
   */
  private void validatePost() {
    String content =
        binding.etPostContent.getText() != null
            ? binding.etPostContent.getText().toString()
            : "";
    boolean isValid = PostValidator.isValidPost(content, selectedImageUri);

    binding.btnSubmitPost.setEnabled(isValid);
    binding.btnSubmitPost.setAlpha(isValid ? 1.0f : 0.4f);
  }

  /**
   * Thiết lập sự kiện bấm nút ĐĂNG bài viết mới.
   */
  private void setupSubmitButton() {
    binding.btnSubmitPost.setOnClickListener(
        v -> {
          String content =
              binding.etPostContent.getText() != null
                  ? binding.etPostContent.getText().toString().trim()
                  : "";

          // Kiểm tra an toàn trước khi lưu
          if (!PostValidator.isValidPost(content, selectedImageUri)) {
            showToast("Vui lòng nhập nội dung hoặc chọn ảnh để đăng bài.");
            return;
          }

          User currentUser = MockDataGenerator.getInstance().getCurrentUser();
          String authorId = currentUser != null ? currentUser.getId() : "user_current";

          // Lưu bài viết mới vào bộ nhớ MockDataGenerator
          MockDataGenerator.getInstance().createPost(authorId, content, selectedImageUri, true);

          showToast("Đã đăng bài viết thành công!");
          setResult(RESULT_OK);
          finish();
        });
  }
}
