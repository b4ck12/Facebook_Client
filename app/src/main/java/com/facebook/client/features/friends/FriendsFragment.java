package com.facebook.client.features.friends;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.facebook.client.R;
import com.facebook.client.core.base.BaseFragment;
import com.facebook.client.core.utils.FriendsProfileUi;
import com.facebook.client.data.model.FriendRequest;
import com.facebook.client.data.repository.IFriendRepository;
import com.facebook.client.data.repository.MockFriendRepository;
import com.facebook.client.data.repository.RepositoryCallback;
import com.facebook.client.databinding.FragmentFriendsBinding;
import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends BaseFragment<FragmentFriendsBinding> {
  private final IFriendRepository repository = new MockFriendRepository();
  private FriendRequestAdapter adapter;
  private int generation;
  private String userId;

  @Override protected FragmentFriendsBinding inflateBinding(
          @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    return FragmentFriendsBinding.inflate(inflater, container, false);
  }

  @Override protected void initViews() {
    adapter = new FriendRequestAdapter(new ArrayList<>(),
            new FriendRequestAdapter.OnActionListener() {
              @Override public void onAccept(FriendRequest request) { respond(request, true); }
              @Override public void onReject(FriendRequest request) { respond(request, false); }
            });
    getBinding().rvFriendRequests.setAdapter(adapter);
    getBinding().ibSearchFriends.setOnClickListener(view -> openFriends(false));
    getBinding().btnYourFriends.setOnClickListener(view -> openFriends(true));
    getBinding().btnFriendRetry.setOnClickListener(view -> loadRequests());
  }

  @Override public void onResume() {
    super.onResume();
    userId = FriendsProfileUi.activeUserId(requireContext());
    loadRequests();
  }

  private void openFriends(boolean showAll) {
    Intent intent = new Intent(requireContext(), SearchFriendsActivity.class);
    intent.putExtra(SearchFriendsActivity.EXTRA_SHOW_ALL, showAll);
    startActivity(intent);
  }

  private boolean active(int token) {
    return token == generation && isAdded() && getView() != null;
  }

  private void loadRequests() {
    int token = ++generation;
    getBinding().pbFriendLoading.setVisibility(View.VISIBLE);
    getBinding().tvFriendState.setVisibility(View.GONE);
    getBinding().btnFriendRetry.setVisibility(View.GONE);
    getBinding().rvFriendRequests.setVisibility(View.GONE);
    repository.getFriendRequests(userId, new RepositoryCallback<List<FriendRequest>>() {
      @Override public void onSuccess(List<FriendRequest> items) {
        if (!active(token)) return;
        adapter.updateRequests(items);
        getBinding().tvFriendRequestCount.setText(String.valueOf(adapter.getItemCount()));
        getBinding().pbFriendLoading.setVisibility(View.GONE);
        boolean empty = adapter.getItemCount() == 0;
        getBinding().rvFriendRequests.setVisibility(empty ? View.GONE : View.VISIBLE);
        getBinding().tvFriendState.setText(R.string.fp_no_requests);
        getBinding().tvFriendState.setVisibility(empty ? View.VISIBLE : View.GONE);
      }

      @Override public void onError(String message) {
        if (!active(token)) return;
        getBinding().pbFriendLoading.setVisibility(View.GONE);
        getBinding().tvFriendState.setText(message);
        getBinding().tvFriendState.setVisibility(View.VISIBLE);
        getBinding().btnFriendRetry.setVisibility(View.VISIBLE);
      }
    });
  }

  private void respond(FriendRequest request, boolean accept) {
    final int token = generation;
    adapter.setRequestBusy(request.getId(), true);
    RepositoryCallback<Boolean> callback = new RepositoryCallback<Boolean>() {
      @Override public void onSuccess(Boolean result) {
        if (!active(token)) return;
        if (!Boolean.TRUE.equals(result)) {
          onError(getString(R.string.fp_operation_failed));
          return;
        }
        showToast(accept ? getString(R.string.fp_accept_success, request.getRequesterName())
                : getString(R.string.fp_reject_success));
        loadRequests();
      }

      @Override public void onError(String message) {
        if (!active(token)) return;
        adapter.setRequestBusy(request.getId(), false);
        showToast(message);
      }
    };
    if (accept) repository.acceptFriendRequest(request.getId(), callback);
    else repository.rejectFriendRequest(request.getId(), callback);
  }

  @Override public void onDestroyView() {
    generation++;
    getBinding().rvFriendRequests.setAdapter(null);
    adapter = null;
    super.onDestroyView();
  }
}