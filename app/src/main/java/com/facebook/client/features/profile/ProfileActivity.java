package com.facebook.client.features.profile;

import android.view.LayoutInflater;
import com.facebook.client.R;
import com.facebook.client.core.base.BaseActivity;
import com.facebook.client.core.utils.FriendsProfileUi;
import com.facebook.client.databinding.ActivityProfileBinding;

/** Mở trang của người được chọn, không thay đổi tài khoản đang đăng nhập. */
public class ProfileActivity extends BaseActivity<ActivityProfileBinding> {
    public static final String EXTRA_USER_ID = "profile_user_id";

    @Override protected ActivityProfileBinding inflateBinding(LayoutInflater inflater) {
        return ActivityProfileBinding.inflate(inflater);
    }

    @Override protected void initViews() {
        FriendsProfileUi.applySystemInsets(this, binding.getRoot());
        binding.ibProfileBack.setOnClickListener(view -> finish());
        String userId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (userId == null || userId.trim().isEmpty()) {
            showToast(getString(R.string.fp_user_not_found));
            finish();
            return;
        }
        if (getSupportFragmentManager().findFragmentById(R.id.profile_container) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.profile_container, ProfileFragment.newInstance(userId)).commit();
        }
    }
}