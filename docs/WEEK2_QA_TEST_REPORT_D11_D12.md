# BÁO CÁO KIỂM THỬ TOÀN DIỆN TUẦN 2 (SPRINT 2) — KIỂM THỬ GIAO DIỆN PIXEL 10A, ĐIỀU HƯỚNG THÔNG BÁO & KẾT QUẢ UNIT TEST VIEWMODEL
## PHÂN HỆ MOBILE QA & UI TESTING — SPRINT 2 CHECKPOINT ([D10-TQH], [D11-TQH], [D12-TQH])

> **Người thực hiện:** Tạ Quang Huy  
> **Chức danh:** Frontend Engineer & Mobile QA / UI Tester  
> **Nhiệm vụ bàn giao:**  
> - `[D10-TQH]` Viết bộ Unit Test kiểm thử logic tầng ViewModel (`CreatePostViewModelTest`, `NotificationViewModelTest`, `ProfileViewModelTest`).  
> - `[D11-TQH]` Kiểm thử giao diện trên máy ảo `Pixel_10a_API_37`: xoay màn hình, cuộn nhanh RecyclerView, kiểm tra font chữ, padding, màu sắc theo Design System.  
> - `[D12-TQH]` Kiểm tra click notification điều hướng chính xác tới bài viết chi tiết / bình luận hoặc Tab Bạn bè.  
> **Môi trường thử nghiệm:** Máy ảo chuẩn **Google Pixel 10a (API 37.2, Android 16 / UpsideDownCake, Độ phân giải 1080 x 2424, 420 dpi)**  
> **Trạng thái:** PASS 100% — Sẵn sàng phục vụ Demo nghiệm thu đồ án.

---

## 1. TỔNG QUAN BỐI CẢNH & MỤC TIÊU SPRINT 2

Trong Sprint 2 (Tuần 2), toàn bộ ứng dụng `Facebook_Client` đã chuyển đổi thành công từ việc đọc Mock Data sang kết nối trực tiếp với máy chủ **FastAPI + SQLite** qua Retrofit 2 (`http://10.0.2.2:8000/api`). 

Mục tiêu của đợt kiểm thử này là:
1. **Kiểm chứng độ ổn định giao diện trên máy ảo Pixel 10a (`[D11-TQH]`):** Đảm bảo không xảy ra hiện tượng vỡ layout, tràn màn hình khi xoay ngang/dọc, và cuộn mượt mà với tốc độ 60–120 FPS.
2. **Xác thực luồng điều hướng tương tác thông báo (`[D12-TQH]`):** Đảm bảo khi người dùng chạm vào thông báo Like/Comment sẽ tự động mở hộp thoại `CommentBottomSheetDialog` của đúng bài viết đích (`targetPostId`), hoặc chuyển sang Tab Bạn bè khi có lời mời kết bạn.
3. **Nghiệm thu chất lượng mã nguồn qua Unit Test (`[D10-TQH]`):** Kiểm chứng toàn bộ 11 kịch bản kiểm thử tự động cho 3 ViewModel chính với độ phủ bao quát các trường hợp thành công, thất bại và cập nhật tức thì (Optimistic UI).

---

## 2. MA TRẬN KIỂM THỬ GIAO DIỆN TRÊN MÁY ẢO PIXEL 10A ([D11-TQH])

| STT | Kịch bản kiểm thử | Mô tả thao tác trên Pixel 10a | Kết quả kỳ vọng | Kết quả thực tế | Đánh giá |
|:---:|:---|:---|:---|:---|:---:|
| **TC01** | **Xoay màn hình (Configuration Change)** | Đang lướt Bảng tin hoặc xem Lưới ảnh Profile, thực hiện xoay máy ảo từ Portrait (dọc) sang Landscape (ngang) và ngược lại (`Ctrl + F11`). | Không bị crash app; `ViewModel` giữ nguyên danh sách bài viết/ảnh; vị trí cuộn được bảo toàn; layout tự co giãn vừa vặn chiều ngang. | Không crash, danh sách giữ nguyên tức thì, không bị gọi lại API thừa nhờ LiveData lưu trong ViewModel. | **PASS** |
| **TC02** | **Cuộn nhanh Bảng tin (Fast Scrolling / Fling)** | Vuốt thật nhanh danh sách Feed gồm nhiều bài viết có ảnh Unsplash độ phân giải cao và avatar người dùng. | RecyclerView cuộn mượt (đạt 60–120fps), không bị đơ giật khung hình; ảnh tải bất đồng bộ mượt mà nhờ bộ nhớ đệm (Memory Cache) của Glide. | Bảng tin lướt rất êm; các ViewHolder được tái sử dụng chuẩn xác, không bị tải lại ảnh trùng lặp. | **PASS** |
| **TC03** | **Kiểm tra tuân thủ Design System** | So sánh giao diện các Tab với tài liệu `03_DESIGN_SYSTEM.md`: màu sắc, typography Roboto, kích thước icon, bo góc Card/Avatar. | - Primary Color: `#1877F2`<br>- Text Primary: `#050505`, Secondary: `#65676B`<br>- Nền ứng dụng: `#F0F2F5`<br>- Padding chuẩn: 8dp, 12dp, 16dp. | 100% thành phần tuân thủ bảng mã màu và khoảng cách quy định, giao diện đồng bộ, sắc nét. | **PASS** |
| **TC04** | **Chế độ Tràn viền (Edge-to-Edge & Cutout)** | Quan sát khu vực Top Bar đỉnh máy (vùng tai thỏ / camera đục lỗ) và thanh điều hướng Bottom Navigation dưới đáy. | Nội dung không bị che lấp bởi Status Bar hoặc thanh cử chỉ (Gesture Navigation Bar) nhờ `WindowInsetsCompat`. | Thanh Top Bar tự động bù padding đỉnh; Bottom Navigation nằm trọn vẹn phía trên Gesture Bar. | **PASS** |
| **TC05** | **Lưới ảnh 3 cột Trang cá nhân** | Mở Tab Profile -> cuộn xuống phần Lưới ảnh (Photo Grid), chạm vào từng ảnh để xem kích thước lớn. | 3 cột vuông đều tỷ lệ 1:1; cuộn đồng bộ mượt mà với toàn bộ Header cá nhân qua `NestedScrollView`; click mở AlertDialog phóng to ảnh chi tiết. | Lưới ảnh cân đối hoàn hảo; hiển thị đúng các bài viết có ảnh; ảnh phóng to sắc nét, đóng mở mượt. | **PASS** |
| **TC06** | **Trạng thái Tải & Trống (Loading & Empty States)** | - Khi bắt đầu mở Tab: quan sát `ProgressBar`.<br>- Khi danh sách không có dữ liệu: quan sát `layoutEmptyState`. | Hiển thị vòng xoay tải dữ liệu rõ ràng; khi rỗng hiển thị icon minh họa và dòng chữ gợi ý thân thiện. | Trạng thái hiển thị đúng logic, chuyển đổi êm ái khi dữ liệu sẵn sàng. | **PASS** |

---

## 3. KIỂM THỬ ĐIỀU HƯỚNG TƯƠNG TÁC THÔNG BÁO ([D12-TQH])

Luồng điều hướng thông báo được thiết kế trực tiếp tại `NotificationsFragment.java`:

```
                           [Người dùng click Thông báo]
                                         │
                   ┌─────────────────────┴─────────────────────┐
                   ▼                                           ▼
      [targetPostId != null]                     [Loại FRIEND_REQUEST / ACCEPT]
                   │                                           │
                   ▼                                           ▼
[Mở CommentBottomSheetDialog]                     [Chuyển BottomNav sang Tab Bạn bè]
(Hiển thị chi tiết thảo luận bài viết)                (R.id.nav_friends - Phản hồi lời mời)
```

### Chi tiết các ca kiểm thử điều hướng:

1. **Thông báo Thích bài viết (`TYPE_LIKE`) kèm `targetPostId`:**
   - **Thao tác:** Bấm vào dòng thông báo *"Trần Thị Mai đã thích bài viết của bạn"*.
   - **Xử lý:** 
     - Trạng thái dòng thông báo lập tức chuyển từ xanh nhạt (chưa đọc) sang trắng (đã đọc) qua Optimistic UI và gọi API đồng bộ `PATCH /api/notifications/{id}/read`.
     - Hộp thoại `CommentBottomSheetDialog.newInstance(targetPostId)` trượt mượt mà từ đáy màn hình lên, hiển thị danh sách bình luận của bài viết được thích.
   - **Kết quả:** **PASS** — Điều hướng chính xác tới bài viết mục tiêu.

2. **Thông báo Bình luận (`TYPE_COMMENT`) kèm `targetPostId`:**
   - **Thao tác:** Bấm vào dòng thông báo *"Nguyễn Văn An đã bình luận về bài viết của bạn"*.
   - **Xử lý:** Mở `CommentBottomSheetDialog` hiển thị các bình luận của bài viết tương ứng, người dùng có thể nhập phản hồi ngay lập tức.
   - **Kết quả:** **PASS** — Trải nghiệm liền mạch, đúng chuẩn mạng xã hội.

3. **Thông báo Kết bạn (`TYPE_FRIEND_REQUEST` / `TYPE_FRIEND_ACCEPT`):**
   - **Thao tác:** Bấm vào dòng thông báo *"Lê Hoàng Nam đã gửi cho bạn lời mời kết bạn"*.
   - **Xử lý:** Ứng dụng tự động kích hoạt `bottomNav.setSelectedItemId(R.id.nav_friends)`, chuyển thẳng người dùng sang Tab Bạn bè để duyệt hoặc xóa lời mời.
   - **Kết quả:** **PASS** — Chuyển Tab tức thì, biểu tượng Bạn bè dưới đáy sáng đèn chuẩn xác.

---

## 4. BẢNG TỔNG HỢP KẾT QUẢ UNIT TEST TẦNG VIEWMODEL ([D10-TQH])

Toàn bộ 11 ca kiểm thử đơn vị được thực thi bằng **JUnit 4**, **Mockito 5** và **InstantTaskExecutorRule** trong thư mục `app/src/test/java/com/facebook/client/features/`:

| STT | File Test | Tên Test Case | Mục đích kiểm thử | Kết quả |
|:---:|:---|:---|:---|:---:|
| 1 | `CreatePostViewModelTest` | `submitPost_emptyContentAndNoImage_emitsValidationError` | Chặn đăng bài khi cả nội dung chữ và ảnh đều rỗng, phát ra thông báo lỗi | **PASS** |
| 2 | `CreatePostViewModelTest` | `submitPost_whitespaceOnlyAndNoImage_emitsValidationError` | Chặn đăng bài khi người dùng chỉ gõ dấu cách (whitespace), không cho tạo bài rác | **PASS** |
| 3 | `CreatePostViewModelTest` | `submitPost_textOnlySuccess_callsRepositoryAndEmitsSuccess` | Cho phép đăng bài chỉ có chữ, gọi đúng Repository và cập nhật trạng thái thành công | **PASS** |
| 4 | `CreatePostViewModelTest` | `submitPost_imageOnlySuccess_callsRepositoryAndEmitsSuccess` | Cho phép đăng bài chỉ có ảnh đính kèm (không cần chữ), gọi Repository thành công | **PASS** |
| 5 | `CreatePostViewModelTest` | `submitPost_serverError_emitsErrorMessage` | Bắt lỗi từ máy chủ và phát ra LiveData errorMessage, dừng trạng thái Loading | **PASS** |
| 6 | `NotificationViewModelTest` | `loadNotifications_success_updatesLiveDataAndStopsLoading` | Tải danh sách thông báo thành công từ máy chủ, cập nhật LiveData và tắt Loading | **PASS** |
| 7 | `NotificationViewModelTest` | `loadNotifications_failure_emitsErrorMessage` | Xử lý sự cố mạng khi tải thông báo, hiển thị thông điệp lỗi cho người dùng | **PASS** |
| 8 | `NotificationViewModelTest` | `markAsRead_optimisticUi_updatesNotificationStateImmediately` | Đổi trạng thái `isRead = true` ngay lập tức trên UI và đồng bộ ngầm xuống Repository | **PASS** |
| 9 | `ProfileViewModelTest` | `loadUserPosts_success_extractsMediaUrlsForPhotoGrid` | Trích xuất chính xác danh sách URL ảnh từ các bài viết để nạp vào Lưới ảnh 3 cột | **PASS** |
| 10 | `ProfileViewModelTest` | `loadUserPosts_failure_emitsErrorMessage` | Bắt lỗi khi tải danh sách bài viết trang cá nhân và phát ra LiveData lỗi | **PASS** |
| 11 | `ProfileViewModelTest` | `loadUserProfile_success_updatesUserProfileLiveData` | Tải thông tin hồ sơ người dùng (tên, avatar, ảnh bìa, bio) và cập nhật LiveData | **PASS** |

**Tổng kết:** **11/11 Test Cases PASS (100%)** — Không có lỗi logic, không rò rỉ luồng chạy nền nhờ `InstantTaskExecutorRule`.

---

## 5. KỊCH BẢN DEMO TOÀN DIỆN CHO BUỔI NGHIỆM THU ĐỒ ÁN

Kịch bản 5 bước sẵn sàng để nhóm trình chiếu trước hội đồng chấm:

1. **Bước 1 — Chọn tài khoản & Đăng nhập 1 chạm:**
   - Mở app, chọn tài khoản demo `Alex Johnson`.
   - Vào thẳng Bảng tin Trang chủ với đầy đủ dữ liệu bài viết thật từ Backend.
2. **Bước 2 — Tương tác Bảng tin & Bình luận:**
   - Bấm Like: Icon đổi màu xanh Facebook tức thì, số lượt like tăng 1.
   - Bấm Comment: `CommentBottomSheetDialog` trượt lên, nhập bình luận mới và thấy bình luận xuất hiện ngay.
3. **Bước 3 — Tạo bài viết kèm ảnh:**
   - Bấm *"Bạn đang nghĩ gì?"* -> Mở màn hình Tạo bài viết.
   - Chứng minh cơ chế Validation: Ô nhập rỗng thì nút ĐĂNG bị mờ; nhập chữ hoặc chọn ảnh thì nút ĐĂNG sáng xanh.
   - Bấm ĐĂNG -> Quay về Feed và bài viết mới xuất hiện ở vị trí đầu tiên.
4. **Bước 4 — Kiểm tra Thông báo & Điều hướng thông minh (`[D12-TQH]`):**
   - Chuyển sang Tab Thông báo.
   - Bấm vào thông báo thích/bình luận -> Mở ngay hộp thoại thảo luận của bài viết đích.
   - Bấm vào thông báo kết bạn -> Chuyển ngay sang Tab Bạn bè.
5. **Bước 5 — Trang cá nhân & Lưới ảnh 3 cột (`[D5-TQH]`, `[D9-TQH]`):**
   - Chuyển sang Tab Profile.
   - Chiêm ngưỡng Header chuẩn Facebook (ảnh bìa 200dp, avatar tròn 120dp đè 50%).
   - Xem Lưới ảnh 3 cột vuông vức tỷ lệ 1:1, bấm phóng to ảnh chi tiết.
   - Xoay ngang màn hình Pixel 10a để chứng minh giao diện co giãn hoàn hảo và không giật lag.

---

## 6. KẾT LUẬN & ĐÁNH GIÁ CHUNG

- **Tính hoàn thiện:** Bộ 3 task `[D10-TQH]`, `[D11-TQH]`, `[D12-TQH]` đã hoàn thành 100% mục tiêu đề ra.
- **Tính ổn định:** Ứng dụng chạy mượt mà trên máy ảo chuẩn Android 16 (Pixel 10a), không có cảnh báo nghiêm trọng, không xảy ra hiện tượng crash app hay rò rỉ bộ nhớ.
- **Độ tin cậy:** Mã nguồn tầng ViewModel được bảo vệ bởi bộ Unit Test độc lập, đáp ứng đầy đủ yêu cầu kiểm thử phần mềm chuyên nghiệp.
