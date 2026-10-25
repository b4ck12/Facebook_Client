# BÁO CÁO KIỂM THỬ TUẦN 1 & KỊCH BẢN DEMO TRÊN MÁY ẢO PIXEL 10A
## PHÂN HỆ MOBILE QA & UI TESTING — SPRINT 1 CHECKPOINT ([D7-TQH])

> **Người thực hiện:** Tạ Quang Huy  
> **Chức danh:** Frontend Engineer & Mobile QA / UI Tester  
> **Nhiệm vụ:** `[D7-TQH]` Build APK test lần 1 trên máy ảo Pixel 10a, demo cả nhóm.  
> **Môi trường thử nghiệm:** Máy ảo chuẩn **Google Pixel 10a (API 37.2, Android 16 / UpsideDownCake)**  
> **Thời điểm:** Ngày 07 — Kết thúc Sprint 1 (Tuần 1: Dựng khung, Mock Data, Giao diện 4 Tab)  
> **Trạng thái Build:** PASS 100% — Sẵn sàng demo nghiệm thu Tuần 1.

---

## 1. HƯỚNG DẪN XUẤT FILE APK VÀ CÀI ĐẶT LÊN MÁY ẢO

### 1.1. Cách xuất file APK Debug trong Android Studio:
1. Mở dự án `Facebook_Client` trong **Android Studio**.
2. Trên thanh menu đỉnh, chọn: **Build ➔ Build Bundle(s) / APK(s) ➔ Build APK(s)**.
3. Chờ Gradle biên dịch xong (khoảng 30 giây - 1 phút). Khi có thông báo ở góc dưới bên phải màn hình:
   > *"APK(s) generated successfully for 1 module"*, bấm vào liên kết **locate**.
4. Đường dẫn file APK thành phẩm:
   ```text
   Facebook_Client/app/build/outputs/apk/debug/app-debug.apk
   ```

### 1.2. Cách cài đặt file APK lên máy ảo Pixel 10a:
- **Cách 1 (Kéo thả trực tiếp):** Kéo file `app-debug.apk` và thả thẳng vào cửa sổ máy ảo `Pixel 10a`. Hệ điều hành Android sẽ tự động cài đặt trong 2 giây.
- **Cách 2 (Dùng lệnh Terminal):**
  ```powershell
  adb install -r app/build/outputs/apk/debug/app-debug.apk
  ```

---

## 2. KỊCH BẢN DEMO CHI TIẾT CHO BUỔI HỌP NHÓM (DEMO SCRIPT)

Kịch bản demo được thiết kế theo luồng trải nghiệm người dùng thực tế (User Flow) qua 6 phân hệ hoàn chỉnh của Tuần 1:

```
[1. Chọn tài khoản Demo] ➔ [2. Lướt Feed & Tương tác] ➔ [3. Tạo bài viết mới]
           │                                                    │
           ▼                                                    ▼
[6. Đổi tài khoản & Nghiệm thu] ◄── [5. Trang cá nhân] ◄── [4. Bạn bè & Thông báo]
```

### Kịch bản 1: Màn hình Đăng nhập 1 chạm & Quản lý phiên (`AccountChooserActivity`)
- **Mục tiêu demo:** Chứng minh ứng dụng hỗ trợ chuyển đổi linh hoạt giữa các tài khoản thử nghiệm của nhóm.
- **Thao tác:**
  1. Khởi chạy ứng dụng từ màn hình Home của máy ảo Pixel 10a.
  2. Màn hình chọn tài khoản hiển thị danh sách người dùng mẫu (Alex Johnson, Nguyễn Văn An, Trần Thị Mai...).
  3. Bấm vào tài khoản "Alex Johnson" ➔ Ứng dụng tự động lưu phiên vào `SessionManager` và chuyển thẳng vào Bảng tin.

### Kịch bản 2: Bảng tin Trang chủ (`FeedFragment` - Minh Lương & Tùng Dương)
- **Mục tiêu demo:** Kiểm tra danh sách bài viết và các tương tác mạng xã hội cơ bản.
- **Thao tác:**
  1. Cuộn danh sách bài viết: Kiểm tra ảnh đại diện, ảnh bài viết (Unsplash), tên tác giả, thời gian đăng.
  2. **Thử nghiệm Optimistic Like UI:** Bấm nút **Thích (Like)** ➔ Icon lập tức chuyển sang màu xanh Facebook (`#1877F2`) và số like tăng lên 1 đơn vị ngay lập tức mà không có độ trễ. Bấm lần nữa để bỏ thích.
  3. **Thử nghiệm Comment Sheet:** Bấm nút **Bình luận** ➔ Hộp thoại `CommentBottomSheetDialog` trượt mượt mà từ đáy màn hình lên, hiển thị danh sách comment Text-only (chuẩn đặc tả PDF).
  4. **Thử nghiệm Chia sẻ:** Bấm nút **Chia sẻ** ➔ Mở hộp thoại "Chia sẻ ngay" lên trang cá nhân.

### Kịch bản 3: Màn hình Tạo bài viết (`CreatePostActivity` - Tùng Dương)
- **Mục tiêu demo:** Xác minh tính năng chặn bài viết rỗng theo đúng đặc tả PDF.
- **Thao tác:**
  1. Từ Bảng tin, bấm vào thanh *"What's on your mind?"*.
  2. Màn hình `CreatePostActivity` xuất hiện với nút **POST** đang bị mờ/khóa (Disabled).
  3. Gõ thử vài ký tự chữ ➔ Nút **POST** lập tức sáng xanh (Enabled). Xóa hết chữ ➔ Nút POST lại bị khóa.
  4. Bấm nút đính kèm ảnh ➔ Chọn 1 ảnh từ thư viện ➔ Ảnh hiển thị trong khung xem trước kèm nút Xóa (Remove).
  5. Bấm **POST** ➔ Đăng bài thành công, bài viết mới xuất hiện ngay đầu Bảng tin.

### Kịch bản 4: Màn hình Bạn bè (`FriendsFragment` - Phúc Đức)
- **Mục tiêu demo:** Kiểm tra danh sách lời mời kết bạn và quản lý quan hệ bạn bè.
- **Thao tác:**
  1. Bấm vào Tab thứ 2 (**Friends**) trên BottomNavigationView.
  2. Kiểm tra danh sách Lời mời kết bạn: bấm **Confirm** để đồng ý hoặc **Delete** để xóa lời mời.
  3. Bấm nút Tìm kiếm bạn bè ở góc trên để mở giao diện tìm kiếm với cơ chế Debounce 300ms.

### Kịch bản 5: Màn hình Thông báo (`NotificationsFragment` - Tạ Quang Huy)
- **Mục tiêu demo:** Kiểm tra phân loại thông báo và tương tác trực quan.
- **Thao tác:**
  1. Bấm vào Tab thứ 3 (**Notifications**) trên BottomNavigationView.
  2. Quan sát các huy hiệu badge tròn nhỏ đè góc dưới avatar:
     - Icon màu xanh Facebook: Thông báo Thích bài viết (`ic_notif_like`).
     - Icon màu xanh lá cây: Thông báo Bình luận bài viết (`ic_notif_comment`).
     - Icon bạn bè: Lời mời kết bạn (`ic_notif_friend`).
  3. **Thử nghiệm Optimistic Read UI:** Bấm vào một thông báo chưa đọc (có nền màu xanh nhạt và chấm xanh) ➔ Nền lập tức đổi sang màu trắng và chấm tròn biến mất, báo hiệu đã đọc thành công.
  4. Bấm vào nút menu 3 chấm ở mép phải dòng thông báo để xem các tùy chọn ẩn/tắt thông báo.

### Kịch bản 6: Trang cá nhân & Lưới ảnh 3 cột (`ProfileFragment` - Phúc Đức & Tạ Quang Huy)
- **Mục tiêu demo:** Kiểm tra bố cục chuẩn Facebook và tính năng Lưới ảnh 3 cột (Photo Grid [D5-TQH]/[D6-TQH]).
- **Thao tác:**
  1. Bấm vào Tab thứ 4 (**Profile**) trên BottomNavigationView.
  2. Quan sát Header chuẩn: Ảnh bìa 200dp, Avatar tròn 120dp đè 50% lên mép dưới ảnh bìa viền trắng 4dp.
  3. **Kiểm tra Tab "Photos" (Lưới ảnh 3 cột):**
     - Các ô ảnh vuông vức tỷ lệ 1:1, sắp xếp đều 3 cột.
     - Dùng chuột/ngón tay cuộn: Toàn bộ Header và Lưới ảnh cuộn mượt mà cùng nhau nhờ `NestedScrollView`.
     - Chạm vào một ô ảnh bất kỳ ➔ Hộp thoại `MaterialAlertDialogBuilder` bật lên hiển thị ảnh phóng to chi tiết sắc nét.
  4. **Kiểm tra Tab "Posts":** Bấm sang Tab "Posts" ➔ Lưới ảnh ẩn đi, hiển thị danh sách bài viết cá nhân.

---

## 3. BẢNG TỔNG HỢP KẾT QUẢ KIỂM THỬ GIAO DIỆN TUẦN 1

| STT | Màn hình / Phân hệ | Thành viên phụ trách | Kết quả trên Pixel 10a | Ghi chú đánh giá |
|:---:|:---|:---|:---:|:---|
| 1 | **Theme & Design System** | Tùng Dương `[D2-TD]` | **PASS** | Màu sắc chuẩn Facebook (`#1877F2`, `#F0F2F5`), font chữ đồng nhất. |
| 2 | **Bottom Navigation Bar** | Tùng Dương `[D3-TD]` | **PASS** | Chuyển đổi mượt 4 Tab, chống recreate Fragment khi bấm lại tab đang mở. |
| 3 | **Home Feed UI & Like** | Minh Lương `[D3-ML]`, `[D4-ML]` | **PASS** | Like đổi màu xanh tức thì (Optimistic UI), bố cục thẻ bài viết chuẩn. |
| 4 | **Comment Bottom Sheet** | Minh Lương `[D5-ML]`, `[D6-ML]` | **PASS** | Chuẩn Text-only theo đặc tả PDF, trượt từ đáy mượt mà. |
| 5 | **Create Post Validation** | Tùng Dương `[D4-TD]`, `[D5-TD]` | **PASS** | Nút POST khóa khi rỗng; chọn 1 ảnh đính kèm thành công. |
| 6 | **Friends & Friend Requests** | Phúc Đức `[D3-PD]`, `[D4-PD]` | **PASS** | Lời mời kết bạn bấm Confirm/Delete mượt, giao diện rõ ràng. |
| 7 | **Notifications & Badges** | Tạ Quang Huy `[D3-TQH]`, `[D4-TQH]` | **PASS** | 4 loại badge hiển thị chính xác; Optimistic click đổi trạng thái đã đọc. |
| 8 | **Profile Header & Photo Grid** | Tạ Quang Huy `[D5-TQH]`, `[D6-TQH]` | **PASS** | Lưới 3 cột vuông 1:1, cuộn không giật lag, click mở ảnh xem chi tiết. |
| 9 | **Demo Account Switcher** | Tùng Dương `[D6-TD]` | **PASS** | Đổi tài khoản demo nhanh chóng từ nút menu Top Bar. |

---

## 4. KẾT LUẬN & SẴN SÀNG CHO TUẦN 2 (SPRINT 2)

- **Kết luận:** 100% các màn hình giao diện phía Client (Android Java 17 + XML) đã hoạt động trơn tru, không có lỗi crash, không có cảnh báo nghiêm trọng trên máy ảo chuẩn Google Pixel 10a (API 37.2).
- **Kế hoạch tiếp theo:** Toàn đội sẵn sàng bước vào **Tuần 2 (Sprint 2: Tích hợp Retrofit gọi API thật từ FastAPI + SQLite, viết Unit Test và bảo vệ đồ án)**.
