package com.facebook.client.features.feed;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.facebook.client.core.base.BaseFragment;
import com.facebook.client.databinding.FragmentFeedBinding;

/**
 * Fragment hiển thị Bảng tin Trang chủ (Home Feed).
 * Kế thừa BaseFragment để tự động quản lý ViewBinding và giải phóng bộ nhớ khi hủy View.
 *
 * <p>Thành viên phụ trách phát triển chi tiết: Minh Lương (FE).
 */
public class FeedFragment extends BaseFragment<FragmentFeedBinding> {

  @Override
  protected FragmentFeedBinding inflateBinding(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    // Khởi tạo ViewBinding từ file layout res/layout/fragment_feed.xml
    return FragmentFeedBinding.inflate(inflater, container, false);
  }

  @Override
  protected void initViews() {
    // Khung khởi tạo giao diện Bảng tin (sẽ gắn RecyclerView & PostAdapter ở các task tiếp theo)
  }

  @Override
  protected void initObservers() {
    // Khung đăng ký lắng nghe LiveData từ FeedViewModel
  }
}

