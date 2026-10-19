package com.facebook.client;

import android.view.LayoutInflater;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.facebook.client.core.base.BaseActivity;
import com.facebook.client.databinding.ActivityMainBinding;

/**
 * Màn hình chính (Main Shell Activity) của ứng dụng Facebook Client ([D3-TD]).
 *
 * <p>Chức năng chính:
 * <ul>
 *   <li>Kế thừa {@link BaseActivity} để tự động khởi tạo ViewBinding ({@link ActivityMainBinding}).
 *   <li>Kết nối {@code BottomNavigationView} với {@code NavController} của Jetpack Navigation
 *       để điều hướng mượt mà giữa 4 Tab: Feed, Friends, Notifications, Profile.
 *   <li>Xử lý sự kiện trên thanh Top Bar (như nút Tìm kiếm bạn bè).
 * </ul>
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {

  private NavController navController;

  @Override
  protected ActivityMainBinding inflateBinding(LayoutInflater inflater) {
    // Khởi tạo ViewBinding từ layout res/layout/activity_main.xml
    return ActivityMainBinding.inflate(inflater);
  }

  @Override
  protected void initViews() {
    setupBottomNavigation();
    setupTopBarActions();
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
    // Khi bấm nút kính lúp Tìm kiếm ở góc phải Top Bar -> Chuyển nhanh sang Tab Bạn bè (hoặc mở màn hình Search sau này)
    binding.ibTopSearch.setOnClickListener(
        v -> {
          if (navController != null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_friends);
          }
        });
  }
}
