package com.facebook.client.features.profile;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.facebook.client.core.base.BaseFragment;
import com.facebook.client.databinding.FragmentProfileBinding;

/**
 * Fragment hiển thị Trang cá nhân người dùng (Profile Tab: Ảnh bìa, Avatar, Tab Bài viết & Ảnh).
 * Kế thừa BaseFragment để tự động quản lý ViewBinding và giải phóng bộ nhớ khi hủy View.
 *
 * <p>Thành viên phụ trách phát triển chi tiết: Phúc Đức (FE) & Tạ Quang Huy (FE + QA).
 */
public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

  @Override
  protected FragmentProfileBinding inflateBinding(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    // Khởi tạo ViewBinding từ file layout res/layout/fragment_profile.xml
    return FragmentProfileBinding.inflate(inflater, container, false);
  }

  @Override
  protected void initViews() {
    // Khung khởi tạo giao diện Profile (sẽ gắn Header, TabLayout & PhotoGridAdapter ở các task tiếp theo)
  }

  @Override
  protected void initObservers() {
    // Khung đăng ký lắng nghe LiveData từ ProfileViewModel
  }
}

