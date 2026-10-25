package com.facebook.client.features.friends;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import com.facebook.client.R;
import com.facebook.client.core.base.BaseActivity;
import com.facebook.client.core.utils.FriendsProfileUi;
import com.facebook.client.data.model.User;
import com.facebook.client.data.repository.IFriendRepository;
import com.facebook.client.data.repository.MockFriendRepository;
import com.facebook.client.data.repository.RepositoryCallback;
import com.facebook.client.databinding.ActivitySearchFriendsBinding;
import com.facebook.client.features.profile.ProfileActivity;
import java.util.List;

public class SearchFriendsActivity extends BaseActivity<ActivitySearchFriendsBinding> {
    public static final String EXTRA_SHOW_ALL = "show_all_friends";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final IFriendRepository repository = new MockFriendRepository();
    private FriendSearchAdapter adapter;
    private Runnable pendingSearch;
    private int generation;
    private boolean destroyed;

    @Override protected ActivitySearchFriendsBinding inflateBinding(LayoutInflater inflater) {
        return ActivitySearchFriendsBinding.inflate(inflater);
    }

    @Override protected void initViews() {
        FriendsProfileUi.applySystemInsets(this, binding.getRoot());
        binding.tvSearchTitle.setText(getIntent().getBooleanExtra(EXTRA_SHOW_ALL, false)
                ? R.string.fp_your_friends : R.string.fp_search_title);
        binding.ibBack.setOnClickListener(view -> finish());
        binding.ibClear.setOnClickListener(view -> binding.etSearchQuery.setText(""));
        binding.btnSearchRetry.setOnClickListener(view -> scheduleSearch(false));
        adapter = new FriendSearchAdapter(user -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_USER_ID, user.getId());
            startActivity(intent);
        });
        binding.rvSearchResults.setAdapter(adapter);
        binding.etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                scheduleSearch(true);
            }
            @Override public void afterTextChanged(Editable text) {}
        });
        binding.etSearchQuery.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return false;
            scheduleSearch(false);
            return true;
        });
    }

    @Override protected void onResume() {
        super.onResume();
        scheduleSearch(false);
    }

    private void scheduleSearch(boolean debounce) {
        int token = ++generation;
        if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
        String query = binding.etSearchQuery.getText().toString().trim();
        String userId = FriendsProfileUi.activeUserId(this);
        binding.ibClear.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
        binding.pbSearchLoading.setVisibility(View.VISIBLE);
        binding.tvSearchState.setVisibility(View.GONE);
        binding.btnSearchRetry.setVisibility(View.GONE);
        binding.rvSearchResults.setVisibility(View.GONE);
        pendingSearch = () -> search(query, userId, token);
        if (debounce && !query.isEmpty()) handler.postDelayed(pendingSearch, 300L);
        else pendingSearch.run();
    }

    private boolean active(int token) {
        return !destroyed && !isFinishing() && token == generation;
    }

    private void search(String query, String userId, int token) {
        RepositoryCallback<List<User>> callback = new RepositoryCallback<List<User>>() {
            @Override public void onSuccess(List<User> users) {
                if (!active(token)) return;
                adapter.setUsers(users);
                binding.pbSearchLoading.setVisibility(View.GONE);
                boolean empty = adapter.getItemCount() == 0;
                binding.rvSearchResults.setVisibility(empty ? View.GONE : View.VISIBLE);
                binding.tvSearchState.setText(query.isEmpty()
                        ? R.string.fp_no_friends : R.string.fp_no_results);
                binding.tvSearchState.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(String message) {
                if (!active(token)) return;
                adapter.setUsers(null);
                binding.pbSearchLoading.setVisibility(View.GONE);
                binding.tvSearchState.setText(message);
                binding.tvSearchState.setVisibility(View.VISIBLE);
                binding.btnSearchRetry.setVisibility(View.VISIBLE);
            }
        };
        if (query.isEmpty()) repository.getFriends(userId, callback);
        else repository.searchFriend(query, userId, callback);
    }

    @Override protected void onStop() {
        generation++;
        if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
        super.onStop();
    }

    @Override protected void onDestroy() {
        destroyed = true;
        handler.removeCallbacksAndMessages(null);
        binding.rvSearchResults.setAdapter(null);
        super.onDestroy();
    }
}