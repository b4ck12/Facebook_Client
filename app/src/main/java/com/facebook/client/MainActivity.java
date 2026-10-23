package com.facebook.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.facebook.client.core.base.BaseActivity;
import com.facebook.client.core.utils.SessionManager;
import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.User;
import com.facebook.client.databinding.ActivityMainBinding;
import com.facebook.client.features.auth.AccountChooserActivity;

import java.util.List;

/**
 * Màn hình chính (Main Shell Activity) của ứng dụng Facebook Client ([D3-TD]).
 *
 * <p>Chức năng chính:
 * <ul>
 *   <li>Kế thừa {@link BaseActivity} để tự động khởi tạo ViewBinding ({@link ActivityMainBinding}).
 *   <li>Kết nối {@code BottomNavigationView} với {@code NavController} của Jetpack Navigation
 *       để điều hướng mượt mà giữa 4 Tab: Feed, Friends, Notifications, Profile.
 *   <li>Xử lý sự kiện trên thanh Top Bar (nút Tìm kiếm và nút Đổi tài khoản Demo).
 * </ul>
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {

  private NavController navController;
  private SessionManager sessionManager;

  @Override
  protected ActivityMainBinding inflateBinding(LayoutInflater inflater) {
    // Khởi tạo ViewBinding từ layout res/layout/activity_main.xml
    return ActivityMainBinding.inflate(inflater);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    sessionManager = SessionManager.getInstance(this);

    // Kiểm tra phiên đăng nhập, nếu chưa chọn tài khoản thì điều hướng tới AccountChooserActivity
    if (!sessionManager.isLoggedIn()) {
      Intent intent = new Intent(this, AccountChooserActivity.class);
      startActivity(intent);
      finish();
      super.onCreate(savedInstanceState);
      return;
    }

    // Đồng bộ người dùng hiện tại vào MockDataGenerator
    MockDataGenerator.getInstance().setCurrentUserId(sessionManager.getCurrentUserId());

    super.onCreate(savedInstanceState);
  }

  @Override
  protected void initViews() {
    setupWindowInsets();
    setupBottomNavigation();
    setupTopBarActions();
  }

  /**
   * Áp dụng WindowInsets cho thanh Top Bar để hỗ trợ chế độ tràn viền (Edge-to-Edge):
   * Thêm padding đỉnh tương ứng với độ cao Status Bar và Display Cutout (tai thỏ),
   * đồng thời mở rộng tổng chiều cao của Top Bar để nội dung không bị đè lấp ([D7-TD]).
   */
  private void setupWindowInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(
        binding.layoutTopBar,
        (v, windowInsets) -> {
          Insets insets =
              windowInsets.getInsets(
                  WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout());

          int baseTopBarHeight = getResources().getDimensionPixelSize(R.dimen.top_bar_height);
          ViewGroup.LayoutParams params = v.getLayoutParams();
          if (params != null) {
            params.height = baseTopBarHeight + insets.top;
            v.setLayoutParams(params);
          }
          v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), v.getPaddingBottom());

          return windowInsets;
        });
    ViewCompat.requestApplyInsets(binding.layoutTopBar);
  }

  /**
   * Thiết lập điều hướng 4 Tab bằng Jetpack Navigation Component:
   * Tìm NavHostFragment trong layout và gắn kết NavController với BottomNavigationView.
   */
  private void setupBottomNavigation() {
    NavHostFragment navHostFragment =
        (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

    if (navHostFragment != null) {
      navController = navHostFragment.getNavController();
      // Tự động đồng bộ sự kiện bấm chọn Tab dưới đáy với việc chuyển đổi Fragment tương ứng
      NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

      // Chặn việc khởi tạo lại (recreate) Fragment nếu người dùng bấm lại vào đúng Tab đang mở
      binding.bottomNavigation.setOnItemReselectedListener(
          item -> {
            // Không làm gì khi bấm lại Tab hiện tại để giữ nguyên vị trí cuộn trên màn hình
          });
    }
  }

  /**
   * Thiết lập sự kiện click cho các nút chức năng trên thanh Top Bar.
   */
  private void setupTopBarActions() {
    // Khi bấm nút kính lúp Tìm kiếm ở Top Bar -> Chuyển nhanh sang Tab Bạn bè
    binding.ibTopSearch.setOnClickListener(
        v -> {
          if (navController != null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_friends);
          }
        });

    // Khi bấm nút Menu/Đổi tài khoản ở góc phải Top Bar -> Hiển thị hộp thoại chuyển đổi tài khoản
    ImageButton ibSwitchAccount = binding.getRoot().findViewById(R.id.ib_top_switch_account);
    if (ibSwitchAccount != null) {
      ibSwitchAccount.setOnClickListener(v -> showSwitchAccountDialog());
    }
  }

  /**
   * Hộp thoại chuyển nhanh giữa các tài khoản Demo hoặc Đăng xuất.
   */
  private void showSwitchAccountDialog() {
    List<User> accounts = MockDataGenerator.getInstance().getDemoAccounts();
    String[] items = new String[accounts.size() + 1];
    String currentId = MockDataGenerator.getInstance().getCurrentUserId();
    int checkedItem = -1;

    for (int i = 0; i < accounts.size(); i++) {
      User u = accounts.get(i);
      boolean isCurrent = u.getId().equals(currentId);
      items[i] = u.getDisplayName() + (isCurrent ? " (Đang chọn)" : "");
      if (isCurrent) {
        checkedItem = i;
      }
    }
    items[accounts.size()] = "Đăng xuất ra màn hình chính";

    new MaterialAlertDialogBuilder(this)
        .setTitle(R.string.switch_account_title)
        .setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
          dialog.dismiss();
          if (which < accounts.size()) {
            User selectedUser = accounts.get(which);
            sessionManager.saveLoginSession(selectedUser.getId());
            MockDataGenerator.getInstance().setCurrentUserId(selectedUser.getId());
            showToast("Đã chuyển sang tài khoản: " + selectedUser.getDisplayName());
            recreate();
          } else {
            // Đăng xuất về AccountChooserActivity
            sessionManager.clearSession();
            Intent intent = new Intent(this, AccountChooserActivity.class);
            intent.putExtra("EXTRA_FORCE_CHOOSER", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
          }
        })
        .setNegativeButton(R.string.btn_cancel, null)
        .show();
  }
}
