# 🧪 10. HƯỚNG DẪN VIẾT VÀ CHẠY UNIT TEST (TESTING GUIDE)

---

## 1. CHIẾN LƯỢC KIỂM THỬ (TESTING STRATEGY)

Dự án áp dụng mô hình **Kim tự tháp kiểm thử (Testing Pyramid)** chuẩn ngành:
- **70% Unit Tests (Kiểm thử đơn vị):** Kiểm tra các hàm logic độc lập, ViewModel, Validator, UseCase. Chạy trực tiếp trên JVM của máy tính, thời gian chạy dưới vài giây, không cần bật máy ảo.
- **20% Integration Tests (Kiểm thử tích hợp):** Kiểm tra sự liên kết giữa ViewModel và Repository hoặc giữa Repository và Database.
- **10% UI / Manual Tests (Kiểm thử giao diện):** Do 2 bạn QA (Hà Đăng Huy, Tạ Quang Huy) chạy trực tiếp trên máy ảo Android để kiểm tra trải nghiệm thực tế.

---

## 2. CẤU HÌNH THƯ VIỆN KIỂM THỬ (TEST DEPENDENCIES)

Khai báo trong file `app/build.gradle`:

```groovy
dependencies {
    // Thư viện Unit Test chuẩn trên JVM
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:5.11.0'
    testImplementation 'org.mockito:mockito-inline:5.2.0' // Hỗ trợ mock final class

    // Hỗ trợ kiểm thử LiveData chạy đồng bộ (InstantTaskExecutorRule)
    testImplementation 'androidx.arch.core:core-testing:2.2.0'
}
```

---

## 3. HƯỚNG DẪN VIẾT UNIT TEST THỰC CHIẾN (KÈM CODE MẪU JAVA)

### 3.1. Bài toán 1: Kiểm thử Logic nghiệp vụ (Validator Test)
Theo đặc tả `Mobile App.pdf`, bài viết chỉ hợp lệ khi **nội dung text có chữ HOẶC có đính kèm ảnh**.

#### File cần test: `com/facebook/client/core/utils/PostValidator.java`
```java
package com.facebook.client.core.utils;

public class PostValidator {
    public static boolean isValidPost(String content, String imageUri) {
        boolean hasText = content != null && !content.trim().isEmpty();
        boolean hasMedia = imageUri != null && !imageUri.trim().isEmpty();
        return hasText || hasMedia;
    }
}
```

#### File Unit Test: `app/src/test/java/com/facebook/client/utils/PostValidatorTest.java`
```java
package com.facebook.client.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.facebook.client.core.utils.PostValidator;

import org.junit.Test;

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
        boolean result = PostValidator.isValidPost("Ảnh đi chơi", "content://media/external/images/123");
        assertTrue("Có cả chữ và ảnh phải cho phép đăng", result);
    }
}
```

---

### 3.2. Bài toán 2: Kiểm thử ViewModel với LiveData & Mockito
Kiểm tra xem khi người dùng bấm Tạo bài viết, `CreatePostViewModel` có phát ra trạng thái `Resource.LOADING` rồi chuyển sang `Resource.SUCCESS` hay không.

#### File Unit Test: `app/src/test/java/com/facebook/client/features/feed/CreatePostViewModelTest.java`
```java
package com.facebook.client.features.feed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.facebook.client.core.common.Resource;
import com.facebook.client.data.model.Post;
import com.facebook.client.data.repository.IPostRepository;
import com.facebook.client.data.repository.RepositoryCallback;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreatePostViewModelTest {

    // Quy tắc bắt buộc để LiveData thực thi ngay trên luồng gọi (Synchronous)
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private IPostRepository mockPostRepository;

    private CreatePostViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new CreatePostViewModel(mockPostRepository);
    }

    @Test
    public void createPost_success_updatesLiveDataWithSuccess() {
        // Giả lập Repository trả về kết quả thành công
        Post fakePost = new Post("p_1", "usr_1", "Tùng Dương", null, "Hello World", null, true, 0, 0, System.currentTimeMillis());
        
        doAnswer(invocation -> {
            RepositoryCallback<Post> callback = invocation.getArgument(3);
            callback.onSuccess(fakePost);
            return null;
        }).when(mockPostRepository).createPost(eq("usr_1"), eq("Hello World"), any(), any());

        // Thực thi hành động trên ViewModel
        viewModel.submitPost("usr_1", "Hello World", null);

        // Kiểm tra kết quả LiveData
        Resource<Post> result = viewModel.getCreatePostStatus().getValue();
        assertNotNull("LiveData không được null", result);
        assertEquals("Trạng thái phải là SUCCESS", Resource.Status.SUCCESS, result.status);
        assertEquals("Nội dung bài viết phải trùng khớp", "Hello World", result.data.getContent());
    }

    @Test
    public void createPost_emptyInput_updatesLiveDataWithError() {
        // Gửi bài viết không có chữ và không có ảnh
        viewModel.submitPost("usr_1", "", null);

        // Kiểm tra kết quả báo lỗi validation
        Resource<Post> result = viewModel.getCreatePostStatus().getValue();
        assertNotNull(result);
        assertEquals(Resource.Status.ERROR, result.status);
        assertEquals("Vui lòng nhập nội dung hoặc chọn ảnh!", result.message);
    }
}
```

---

### 3.3. Bài toán 3: Kiểm thử Backend Python FastAPI & SQLite với Pytest
Kiểm tra endpoint `POST /api/posts` tạo bài viết mới và kiểm tra validation từ chối bài viết rỗng trên FastAPI server.

#### Cài đặt thư viện kiểm thử Python:
```bash
pip install pytest httpx
```

#### File kiểm thử: `backend/tests/test_posts_api.py`
```python
import pytest
from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def test_create_post_success():
    """Kiểm tra tạo bài viết hợp lệ thành công"""
    payload = {
        "authorId": "usr_01",
        "content": "Kiểm thử tự động API tạo bài viết thành công!",
        "mediaUrl": None,
        "isPublic": True
    }
    response = client.post("/api/posts", json=payload)
    assert response.status_code == 201 or response.status_code == 200
    data = response.json()
    assert data["content"] == payload["content"]
    assert "id" in data
    assert data["likesCount"] == 0

def test_create_post_empty_fails():
    """Kiểm tra API từ chối bài viết khi cả text và media rỗng (chuẩn PDF)"""
    payload = {
        "authorId": "usr_01",
        "content": "",
        "mediaUrl": None,
        "isPublic": True
    }
    response = client.post("/api/posts", json=payload)
    assert response.status_code == 400
    assert "Vui lòng nhập nội dung hoặc chọn ảnh" in response.json()["detail"]

def test_toggle_like_success():
    """Kiểm tra toggle like tăng/giảm trong SQLite"""
    # Like lần 1 -> isLiked = True
    res1 = client.post("/api/posts/post_1001/like?user_id=usr_01")
    assert res1.status_code == 200
    assert res1.json()["isLiked"] is True

    # Like lần 2 -> isLiked = False (unlike)
    res2 = client.post("/api/posts/post_1001/like?user_id=usr_01")
    assert res2.status_code == 200
    assert res2.json()["isLiked"] is False
```

---

## 4. HƯỚNG DẪN CHẠY TEST TRONG DỰ ÁN

### Cách 1: Chạy Android Unit Test bằng Terminal Gradle
Chạy toàn bộ Unit Test Android trên terminal:
```bash
./gradlew testDebugUnitTest
```
- Kết quả tổng hợp báo cáo dạng Web HTML trực quan:  
  📁 `app/build/reports/tests/testDebugUnitTest/index.html`

### Cách 2: Chạy trực tiếp trên giao diện Android Studio
1. Mở cửa sổ **Project** bên trái màn hình.
2. Điều hướng tới thư mục: `app/src/test/java`.
3. Bấm chuột phải vào thư mục `java` hoặc một file test cụ thể (ví dụ: `PostValidatorTest.java`).
4. Chọn **Run 'Tests in java'** (hoặc phím tắt `Ctrl + Shift + F10` trên Windows).
5. Android Studio sẽ hiển thị thanh tiến trình màu xanh lá cây (**Green Bar**).

### Cách 3: Chạy Kiểm thử Backend FastAPI với Pytest & Swagger UI
1. **Chạy Pytest tự động trong terminal:**
   ```bash
   cd backend
   pytest tests/ -v
   ```
   *(Toàn bộ các test cases endpoint và SQLite query sẽ hiển thị `PASSED` màu xanh)*
2. **Kiểm thử trực quan trên Swagger UI:**
   - Khởi động server: `uvicorn main:app --reload`
   - Truy cập `http://localhost:8000/docs` trên trình duyệt.
   - Bấm vào từng endpoint -> Chọn **Try it out** -> Nhập tham số -> Bấm **Execute** để xem response code `200` và dữ liệu JSON trực tiếp!

