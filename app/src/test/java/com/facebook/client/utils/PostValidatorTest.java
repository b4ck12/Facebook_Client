package com.facebook.client.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.facebook.client.core.utils.PostValidator;
import org.junit.Test;

/**
 * Unit Test kiểm thử logic xác thực bài viết mới ([D5-TD]).
 * Đảm bảo 100% tuân thủ đặc tả Mobile App.pdf: Khóa nút Đăng khi bài viết hoàn toàn rỗng.
 */
public class PostValidatorTest {

  @Test
  public void isValidPost_emptyContentAndNoImage_returnsFalse() {
    // Cả text và ảnh đều rỗng -> Không được đăng
    boolean result = PostValidator.isValidPost("", null);
    assertFalse("Bài viết rỗng không được phép đăng", result);
  }

  @Test
  public void isValidPost_whitespaceContentAndNoImage_returnsFalse() {
    // Chỉ toàn dấu cách -> Không được đăng
    boolean result = PostValidator.isValidPost("     ", "");
    assertFalse("Bài viết chỉ có khoảng trắng không được đăng", result);
  }

  @Test
  public void isValidPost_nullContentAndNullImage_returnsFalse() {
    // Cả hai tham số null -> Không được đăng
    boolean result = PostValidator.isValidPost(null, null);
    assertFalse("Bài viết với tham số null không được đăng", result);
  }

  @Test
  public void isValidPost_hasContentOnly_returnsTrue() {
    // Chỉ có text, không có ảnh -> Hợp lệ
    boolean result = PostValidator.isValidPost("Hôm nay trời đẹp quá!", null);
    assertTrue("Có chữ thì phải cho phép đăng", result);
  }

  @Test
  public void isValidPost_hasImageOnly_returnsTrue() {
    // Không có chữ, chỉ có ảnh -> Hợp lệ
    boolean result = PostValidator.isValidPost("", "content://media/external/images/123");
    assertTrue("Có ảnh thì phải cho phép đăng", result);
  }

  @Test
  public void isValidPost_hasBothContentAndImage_returnsTrue() {
    // Có cả chữ và ảnh -> Hợp lệ
    boolean result = PostValidator.isValidPost("Ảnh đi chơi cuối tuần", "content://media/external/images/123");
    assertTrue("Có cả chữ và ảnh phải cho phép đăng", result);
  }
}
