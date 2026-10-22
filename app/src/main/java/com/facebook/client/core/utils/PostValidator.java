package com.facebook.client.core.utils;

/**
 * Lớp tiện ích kiểm tra tính hợp lệ của bài viết mới ([D5-TD]).
 * Tuân thủ 100% đặc tả Mobile App.pdf: bài viết chỉ được phép đăng khi có ít nhất chữ HOẶC có ảnh.
 */
public class PostValidator {

  /**
   * Kiểm tra nội dung văn bản và URI media đính kèm có đủ điều kiện để đăng hay không.
   *
   * @param content  Nội dung văn bản người dùng nhập vào ô soạn thảo.
   * @param imageUri Đường dẫn/URI ảnh hoặc video đính kèm (nếu có).
   * @return true nếu có chữ (không rỗng sau khi trim) HOẶC có ảnh/media hợp lệ; false nếu cả 2 đều rỗng.
   */
  public static boolean isValidPost(String content, String imageUri) {
    boolean hasText = content != null && !content.trim().isEmpty();
    boolean hasMedia = imageUri != null && !imageUri.trim().isEmpty();
    return hasText || hasMedia;
  }
}
