package com.facebook.client.features.friends;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.client.core.base.BaseFragment;
import com.facebook.client.data.model.FriendRequest;
import com.facebook.client.data.repository.IFriendRepository;
import com.facebook.client.data.repository.MockFriendRepository;
import com.facebook.client.data.repository.RepositoryCallback;
import com.facebook.client.databinding.FragmentFriendsBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị danh sách Lời mời kết bạn và Bạn bè (Friends Tab).
 * Kế thừa BaseFragment để tự động quản lý ViewBinding và giải phóng bộ nhớ khi hủy View.
 *
 * <p>Thành viên phụ trách phát triển chi tiết: Phúc Đức (FE).
 */
public class FriendsFragment extends BaseFragment<FragmentFriendsBinding> {

  private IFriendRepository friendRepository;
  private FriendRequestAdapter friendRequestAdapter;
  private int currentRequestCount = 0;

  @Override
  protected FragmentFriendsBinding inflateBinding(
          @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    return FragmentFriendsBinding.inflate(inflater, container, false);
  }

  @Override
  protected void initViews() {
    friendRepository = new MockFriendRepository();

    // 1. Khởi tạo Adapter và gán lắng nghe sự kiện Chấp nhận / Xóa lời mời
    friendRequestAdapter = new FriendRequestAdapter(new ArrayList<>(), new FriendRequestAdapter.OnActionListener() {
      @Override
      public void onAccept(FriendRequest request) {
        friendRepository.acceptFriendRequest(request.getId(), new RepositoryCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean result) {
            friendRequestAdapter.removeRequest(request.getId());
            updateRequestCount(-1);
            showToast("Đã chấp nhận lời mời của " + request.getRequesterName());
          }

          @Override
          public void onError(String errorMessage) {
            showToast(errorMessage);
          }
        });
      }

      @Override
      public void onReject(FriendRequest request) {
        friendRepository.rejectFriendRequest(request.getId(), new RepositoryCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean result) {
            friendRequestAdapter.removeRequest(request.getId());
            updateRequestCount(-1);
            showToast("Đã xóa lời mời của " + request.getRequesterName());
          }

          @Override
          public void onError(String errorMessage) {
            showToast(errorMessage);
          }
        });
      }
    });

    // 2. Gắn Adapter vào RecyclerView (rv_FriendRequest) qua getBinding()
    getBinding().rvFriendRequest.setAdapter(friendRequestAdapter);

    // 3. Tải danh sách lời mời kết bạn từ MockFriendRepository
    loadFriendRequests();
  }

  @Override
  protected void initObservers() {
    // Khung đăng ký lắng nghe LiveData ở Sprint 2 khi ghép ViewModel
  }

  private void loadFriendRequests() {
    friendRepository.getFriendRequests("user_current", new RepositoryCallback<List<FriendRequest>>() {
      @Override
      public void onSuccess(List<FriendRequest> requests) {
        if (requests != null) {
          friendRequestAdapter.updateRequests(requests);
          currentRequestCount = requests.size();
          getBinding().FriendRequestCount.setText(String.valueOf(currentRequestCount));
        }
      }

      @Override
      public void onError(String errorMessage) {
        showToast(errorMessage);
      }
    });
  }

  private void updateRequestCount(int delta) {
    currentRequestCount = Math.max(0, currentRequestCount + delta);
    if (getView() != null) {
      getBinding().FriendRequestCount.setText(String.valueOf(currentRequestCount));
    }
  }
}