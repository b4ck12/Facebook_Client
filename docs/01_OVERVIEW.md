# 📋 01. TỔNG QUAN DỰ ÁN & ĐỐI CHIẾU CHÍNH XÁC ĐẶC TẢ PDF (OVERVIEW & PDF SCOPE)

---

## 1. GIỚI THIỆU DỰ ÁN & BỐI CẢNH

- **Tên dự án:** Facebook Mobile Client
- **Loại hình:** Đồ án môn học / Bài tập lớn (BTL) ứng dụng di động Android
- **Ngôn ngữ & Nền tảng:** Android (Java + XML), Android Studio
- **Môi trường thống nhất:**
  - `compileSdk`: **34** (Android 14)
  - `targetSdk`: **34** (Android 14)
  - `minSdk`: **24** (Android 7.0)
  - `Java Version`: **JDK 17 LTS**
  - `Thiết bị máy ảo chuẩn`: **Pixel 10a** (Android 14 - API 34)
- **Quy mô nhóm:** 7 thành viên (phân vai chuẩn công ty theo đúng danh sách trong `Mobile App.pdf`).

---

## 2. ĐỐI CHIẾU 100% NGUYÊN BẢN VỚI TÀI LIỆU `Mobile App.pdf`

Toàn bộ các tính năng dưới đây được đối chiếu và xây dựng bám sát từng dòng trong file đặc tả gốc `Mobile App.pdf`:

### 2.1. Phân hệ 1: Home Feed (Trang 1 PDF)

#### ● New post (Đăng bài viết mới):
- **Cắt giảm triệt để (Bỏ):** Bỏ toàn bộ các nút chức năng: **Music (Âm nhạc), Tag/collaborate (Gắn thẻ/Cộng tác), Location (Vị trí),...** và các nút phụ khác.
- **○ Text - Viết nội dung:**
  - Ô nhập văn bản chia sẻ trạng thái.
  - **Ràng buộc cứng:** *Nếu chưa có gắn ảnh hay viết gì thì chưa được đăng* (Nút Đăng bị khóa/mờ cho tới khi có ít nhất 1 trong 2 yếu tố: chữ hoặc ảnh/video).
- **○ Add image or video (Thêm ảnh hoặc video):**
  - **Có nút bấm để thêm ảnh** (Chỉ cần duy nhất nút này, các nút còn lại bỏ hoàn toàn).
  - Sau khi bấm vào thì chỉ cần chọn ảnh từ thư viện thiết bị, **không cần tùy biến thêm** (bỏ hết các icons chỉnh sửa, filter, sticker, cắt ảnh).
- **○ Post privacy (Quyền riêng tư):** Mặc định là **Public (Công khai)**.

#### ● Post display (Hiển thị bài viết trên Bảng tin):
- **○ React:** Bấm thả Like / React cơ bản vào bài viết (thay đổi trạng thái icon và tăng/giảm số lượng).
- **○ Comment section (Phần bình luận):**
  - **No react:** Không có tính năng thả cảm xúc vào bình luận.
  - **No reply:** Không có trả lời bình luận lồng nhau (không có phân cấp cha - con / thread).
  - **Text only, no attaching image or video:** Chỉ hỗ trợ văn bản thuần túy, tuyệt đối không cho đính kèm ảnh hoặc video.
  - **No icons, emoji:** Không có bộ chọn icon, emoji sticker phức tạp.
- **○ Share (Chia sẻ bài viết):**
  - **Chỉ cần viết gì và share now:** Nhập dòng cảm nghĩ ngắn và bấm nút "Share now" để đăng bài viết chia sẻ lên trang cá nhân.

---

### 2.2. Phân hệ 2: Friend tab (Trang 2 PDF)
- **○ See friend requests:** Xem danh sách lời mời kết bạn gửi đến, có nút Chấp nhận (Confirm) và Xóa/Từ chối (Delete).
- **○ Your friends:** Xem danh sách bạn bè hiện tại của người dùng.
- **○ Search friend:** Ô tìm kiếm bạn bè theo tên người dùng.

---

### 2.3. Phân hệ 3: Notification (Trang 2 PDF)
- Xem danh sách thông báo hoạt động tương tác: có người thích bài viết, có người bình luận, có người gửi lời mời kết bạn.
- Bấm vào thông báo điều hướng tới đúng nội dung liên quan.

---

### 2.4. Phân hệ 4: Profile (Trang 2 PDF)
- **○ Cover photo:** Ảnh bìa cá nhân (tải ảnh và hiển thị).
- **○ Profile picture:** Ảnh đại diện hình tròn.
- **○ Name:** Tên hiển thị người dùng.
- **● All -> Posts:** Tab hiển thị toàn bộ bài viết do chính tài khoản này đăng theo dòng thời gian.
- **● Photo / video:** Tab hiển thị kho ảnh & video:
  - **Show photo that have been posted on FB:** Hiển thị dạng lưới toàn bộ các bức ảnh/video đã từng được người này đăng lên Facebook Client.

---

### 2.5. Phân chia vai trò 7 người (Trang 2 PDF)
Bám sát nguyên văn phân chia nhân sự trong `Mobile App.pdf`:

1. **Front end (4 thành viên):**
   - **Tùng Dương**
   - **Minh Lương**
   - **Phúc Đức**
   - **Tạ Quang Huy**
2. **Back-end (3 thành viên):**
   - **Ánh Dương (main)**
   - **Minh Đức**
   - **Hà Đăng Huy**
3. **Testing (2 thành viên kiêm nhiệm):**
   - **Hà Đăng Huy** (Backend + Testing)
   - **Tạ Quang Huy** (Frontend + Testing)

---

## 3. RANH GIỚI PHẠM VI (SCOPE BOUNDARY) - BÀI TẬP LỚN ĐẠI HỌC

> [!IMPORTANT]
> **Tuyệt đối KHÔNG LÀM ngoài phạm vi (No Scope Creep):**
> 1. Không làm nhắn tin trò chuyện Messenger, chat 1-1, gọi thoại/video.
> 2. Không làm Tin 24h (Stories), Reels video ngắn, Livestream.
> 3. Không làm Fanpage, Nhóm (Groups), Marketplace buôn bán, Quảng cáo, Thanh toán.
> 4. Không cho bình luận ảnh/sticker trong comment, không làm trả lời lồng nhau.
> 5. Giữ ứng dụng chạy nhẹ nhàng, mượt mà trên máy ảo chuẩn **Pixel 10a**.
