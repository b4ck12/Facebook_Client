package com.facebook.client.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.facebook.client.MainActivity;
import com.facebook.client.core.base.BaseActivity;
import com.facebook.client.core.utils.SessionManager;
import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.User;
import com.facebook.client.databinding.ActivityAccountChooserBinding;

import java.util.List;

/**
 * Màn hình Đăng nhập 1 chạm (Account Chooser / Landing Screen) chuẩn Facebook.
 * Cho phép người dùng hoặc giảng viên chọn nhanh tài khoản demo để trải nghiệm ứng dụng.
 *
 * <p>Thành viên phụ trách: Tùng Dương (Lead FE).
 */
public class AccountChooserActivity extends BaseActivity<ActivityAccountChooserBinding> {

  private SessionManager sessionManager;

  @Override
  protected ActivityAccountChooserBinding inflateBinding(LayoutInflater inflater) {
    return ActivityAccountChooserBinding.inflate(inflater);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    sessionManager = SessionManager.getInstance(this);

    // Nếu đã đăng nhập trước đó và không phải từ hành động "Đổi tài khoản" thì vào thẳng MainActivity
    boolean forceChooser = getIntent().getBooleanExtra("EXTRA_FORCE_CHOOSER", false);
    if (!forceChooser && sessionManager.isLoggedIn()) {
      String currentUserId = sessionManager.getCurrentUserId();
      MockDataGenerator.getInstance().setCurrentUserId(currentUserId);
      navigateToMain();
      super.onCreate(savedInstanceState);
      return;
    }

    super.onCreate(savedInstanceState);
  }

  @Override
  protected void initViews() {
    setupAccountsList();
    setupActions();
  }

  private void setupAccountsList() {
    binding.rvAccounts.setLayoutManager(new LinearLayoutManager(this));

    List<User> demoAccounts = MockDataGenerator.getInstance().getDemoAccounts();
    AccountChooserAdapter adapter = new AccountChooserAdapter(demoAccounts, this::onAccountSelected);
    binding.rvAccounts.setAdapter(adapter);
  }

  private void setupActions() {
    binding.btnLoginOther.setOnClickListener(v -> {
      showToast("Chế độ Demo: Vui lòng chạm vào một trong các tài khoản có sẵn ở trên để đăng nhập.");
    });
  }

  private void onAccountSelected(User user) {
    if (user == null) return;

    // Lưu session và cập nhật MockDataGenerator
    sessionManager.saveLoginSession(user.getId());
    MockDataGenerator.getInstance().setCurrentUserId(user.getId());

    showToast("Đăng nhập với tư cách: " + user.getDisplayName());
    navigateToMain();
  }

  private void navigateToMain() {
    Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
  }
}

