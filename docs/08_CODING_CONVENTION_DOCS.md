# 📝 08. TIÊU CHUẨN MÃ NGUỒN & TÀI LIỆU HÓA (CODING CONVENTION & DOCUMENTATION)

---

## 1. QUY CHUẨN LẬP TRÌNH JAVA (JAVA CODING CONVENTION)

Toàn bộ dự án áp dụng biến thể tinh gọn của **Google Java Style Guide** dành cho Android:

### 1.1. Định dạng mã nguồn (Formatting)
- **Thụt đầu dòng (Indentation):** Sử dụng **4 khoảng trắng (4 spaces)**, tuyệt đối không dùng Tab.
- **Độ dài dòng (Line length):** Tối đa **120 ký tự**. Nếu dòng quá dài, xuống dòng trước các toán tử hoặc dấu phẩy.
- **Khối lệnh ngoặc nhọn (Braces):**
  - Mở ngoặc `{` nằm cùng dòng với câu lệnh điều kiện/hàm.
  - Đóng ngoặc `}` nằm trên dòng riêng.
  - Bắt buộc dùng ngoặc `{}` cho mọi câu lệnh `if`, `else`, `for`, `while` ngay cả khi chỉ có 1 dòng lệnh.
  ```java
  // Đúng:
  if (isLiked) {
      decrementLikeCount();
  } else {
      incrementLikeCount();
  }

  // SAI (tuyệt đối không viết):
  if (isLiked) decrementLikeCount();
  ```

---

## 2. QUY CHUẨN SỬ DỤNG VIEWBINDING (CHỐNG MEMORY LEAK)

Dự án cấm hoàn toàn `findViewById` hoặc thư viện ButterKnife cũ. Tất cả giao diện bắt buộc dùng **Android ViewBinding**.

### 2.1. Chuẩn ViewBinding trong Activity
```java
public class CreatePostActivity extends AppCompatActivity {
    private ActivityCreatePostBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViews();
        observeViewModel();
    }
}
```

### 2.2. Chuẩn ViewBinding trong Fragment (Bắt buộc hủy binding ở onDestroyView)
> [!WARNING]
> Trong Fragment, View có vòng đời ngắn hơn Fragment. Nếu không gán `binding = null` trong `onDestroyView()`, ứng dụng sẽ bị **Memory Leak** nghiêm trọng!

```java
public class FeedFragment extends Fragment {
    private FragmentFeedBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        observeViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // BẮT BUỘC để giải phóng bộ nhớ
    }
}
```

---

## 3. QUẢN LÝ TRẠNG THÁI VỚI RESOURCE WRAPPER PATTERN

Để tầng Giao diện (UI) dễ dàng hiển thị đúng 3 trạng thái: Đang tải (Loading), Thành công (Success), Thất bại (Error), toàn bộ kết quả từ Repository trả về ViewModel phải được đóng gói bằng lớp `Resource<T>`:

```java
package com.facebook.client.core.common;

public class Resource<T> {
    public enum Status { SUCCESS, ERROR, LOADING }

    public final Status status;
    public final T data;
    public final String message;

    private Resource(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(String message, T data) {
        return new Resource<>(Status.ERROR, data, message);
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }
}
```

### Cách quan sát ở tầng View:
```java
feedViewModel.getPostsLiveData().observe(getViewLifecycleOwner(), resource -> {
    if (resource == null) return;
    switch (resource.status) {
        case LOADING:
            binding.pbLoading.setVisibility(View.VISIBLE);
            binding.rvFeedPosts.setVisibility(View.GONE);
            break;
        case SUCCESS:
            binding.pbLoading.setVisibility(View.GONE);
            binding.rvFeedPosts.setVisibility(View.VISIBLE);
            postAdapter.submitList(resource.data);
            break;
        case ERROR:
            binding.pbLoading.setVisibility(View.GONE);
            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            break;
    }
});
```

---

## 4. XỬ LÝ NGOẠI LỆ & GHI NHẬT KÝ (LOGGING & ERROR HANDLING)

1. **Tuyệt đối không bỏ trống khối `catch`:** Không bao giờ viết `catch (Exception e) {}`. Tối thiểu phải ghi Log hoặc ném lên tầng trên.
2. **Sử dụng `Log.d`, `Log.e` có định danh TAG:**
   ```java
   private static final String TAG = "FeedViewModel";
   Log.e(TAG, "Lỗi khi tải bảng tin: " + e.getMessage(), e);
   ```
3. **Tuyệt đối không dùng `System.out.println()`:** Làm giảm hiệu năng ứng dụng trên thiết bị di động thật.

---

## 5. TIÊU CHUẨN CHÚ THÍCH MÃ NGUỒN (JAVADOC STANDARDS)

Mọi Interface, Repository, ViewModel và phương thức public xử lý logic nghiệp vụ bắt buộc phải có tài liệu Javadoc:

```java
/**
 * Quản lý các thao tác dữ liệu liên quan đến bảng tin và bài viết người dùng.
 * Cung cấp cơ chế phân trang và tương tác like/comment.
 */
public interface IPostRepository {

    /**
     * Lấy danh sách các bài viết mới nhất trên bảng tin.
     *
     * @param limit Số lượng bài viết tối đa cần lấy.
     * @param callback Callback trả về kết quả dưới dạng Resource chứa danh sách Post.
     */
    void getFeedPosts(int limit, RepositoryCallback<List<Post>> callback);

    /**
     * Tạo bài viết mới lên hệ thống.
     *
     * @param authorId Mã định danh của người đăng bài.
     * @param content Nội dung văn bản (có thể null nếu đã có ảnh).
     * @param imageUrl Đường dẫn URL của ảnh đã upload lên Cloud Storage (có thể null nếu có text).
     * @param callback Nhận thông báo kết quả bài viết vừa được tạo.
     */
    void createPost(String authorId, String content, String imageUrl, RepositoryCallback<Post> callback);
}
```

---

## 6. BẢNG KIỂM TRA TRƯỚC KHI TẠO PULL REQUEST (CODE REVIEW CHECKLIST)

Trước khi gửi PR cho Tech Lead duyệt, lập trình viên phải tự kiểm tra danh sách sau:
- [ ] Code có build thành công không (`./gradlew assembleDebug`)?
- [ ] Có chạy qua toàn bộ Unit Test hiện tại không (`./gradlew testDebugUnitTest`)?
- [ ] Đã xóa toàn bộ `System.out.println` và code thừa bị comment không dùng?
- [ ] Đã giải phóng `binding = null` trong `onDestroyView()` của Fragment?
- [ ] Tên biến, class, layout, id có đúng theo [07_GIT_WORKFLOW_NAMING.md](./07_GIT_WORKFLOW_NAMING.md)?
- [ ] Các chuỗi văn bản UI đã đưa vào `strings.xml` chưa (không hardcode tiếng Việt trong file Java/XML)?
