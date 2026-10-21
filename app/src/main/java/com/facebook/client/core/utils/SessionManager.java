package com.facebook.client.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Quản lý phiên đăng nhập của người dùng Demo (Session Management).
 * Sử dụng SharedPreferences để lưu trữ và duy trì tài khoản đang hoạt động.
 */
public class SessionManager {

  private static final String PREF_NAME = "facebook_client_session";
  private static final String KEY_USER_ID = "current_user_id";
  private static final String KEY_IS_LOGGED_IN = "is_logged_in";

  private static volatile SessionManager instance;
  private final SharedPreferences pref;

  private SessionManager(Context context) {
    this.pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
  }

  /**
   * Singleton Pattern an toàn đa luồng (Double-Checked Locking).
   */
  public static SessionManager getInstance(Context context) {
    if (instance == null) {
      synchronized (SessionManager.class) {
        if (instance == null) {
          instance = new SessionManager(context);
        }
      }
    }
    return instance;
  }

  /**
   * Lưu phiên đăng nhập với ID người dùng được chọn.
   *
   * @param userId Mã định danh người dùng (ví dụ: user_101).
   */
  public void saveLoginSession(String userId) {
    if (userId == null) return;
    pref.edit()
        .putString(KEY_USER_ID, userId)
        .putBoolean(KEY_IS_LOGGED_IN, true)
        .apply();
  }

  /**
   * Lấy mã định danh người dùng đang đăng nhập trong phiên.
   *
   * @return Mã userId hoặc null nếu chưa có phiên.
   */
  public String getCurrentUserId() {
    return pref.getString(KEY_USER_ID, null);
  }

  /**
   * Kiểm tra người dùng đã chọn tài khoản đăng nhập hay chưa.
   *
   * @return true nếu đã có phiên hợp lệ.
   */
  public boolean isLoggedIn() {
    return pref.getBoolean(KEY_IS_LOGGED_IN, false) && getCurrentUserId() != null;
  }

  /**
   * Đăng xuất / Xóa toàn bộ phiên hiện tại để quay về màn hình chọn tài khoản.
   */
  public void clearSession() {
    pref.edit()
        .clear()
        .apply();
  }
}

