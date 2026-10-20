package com.facebook.client.features.feed;

import android.view.LayoutInflater;
import com.facebook.client.core.base.BaseActivity;
import com.facebook.client.databinding.ActivityCreatePostBinding;

/**
 * Màn hình tạo bài viết mới (Create Post Screen - [D4-TD]).
 *
 * <p>Chức năng trong task [D4-TD]:
 * <ul>
 *   <li>Kế thừa {@link BaseActivity} và liên kết với giao diện {@code activity_create_post.xml}
 *       thông qua ViewBinding ({@link ActivityCreatePostBinding}).
 *   <li>Xử lý sự kiện nút Back ({@code ibBack}) trên thanh Top Bar để đóng màn hình.
 *   <li>Chuẩn bị sẵn cấu trúc cho task [D5-TD] (chọn ảnh từ thư viện và kiểm tra khóa/mở nút POST).
 * </ul>
 */
public class CreatePostActivity extends BaseActivity<ActivityCreatePostBinding> {

  @Override
  protected ActivityCreatePostBinding inflateBinding(LayoutInflater inflater) {
    // Khởi tạo ViewBinding từ layout res/layout/activity_create_post.xml
    return ActivityCreatePostBinding.inflate(inflater);
  }

  @Override
  protected void initViews() {
    // Sự kiện bấm nút mũi tên quay lại trên Top Bar -> Đóng màn hình hiện tại
    binding.ibBack.setOnClickListener(v -> finish());
  }

  @Override
  protected void initObservers() {
    // Khung lắng nghe trạng thái từ CreatePostViewModel (sẽ mở rộng ở các bước tiếp theo)
  }
}

