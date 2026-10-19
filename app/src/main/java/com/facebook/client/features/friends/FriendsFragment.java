package com.facebook.client.features.friends;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.facebook.client.core.base.BaseFragment;
import com.facebook.client.databinding.FragmentFriendsBinding;

/**
 * Fragment hiển thị danh sách Lời mời kết bạn và Bạn bè (Friends Tab).
 * Kế thừa BaseFragment để tự động quản lý ViewBinding và giải phóng bộ nhớ khi hủy View.
 *
 * <p>Thành viên phụ trách phát triển chi tiết: Phúc Đức (FE).
 */
public class FriendsFragment extends BaseFragment<FragmentFriendsBinding> {

  @Override
  protected FragmentFriendsBinding inflateBinding(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    // Khởi tạo ViewBinding từ file layout res/layout/fragment_friends.xml
    return FragmentFriendsBinding.inflate(inflater, container, false);
  }

  @Override
  protected void initViews() {
    // Khung khởi tạo giao diện Bạn bè (sẽ gắn FriendRequestAdapter ở các task tiếp theo)
  }

  @Override
  protected void initObservers() {
    // Khung đăng ký lắng nghe LiveData từ FriendsViewModel
  }
}

