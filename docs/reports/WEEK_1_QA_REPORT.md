# BÁO CÁO KIỂM THỬ CHẤT LƯỢNG TUẦN 1 (WEEK 1 QA REPORT)
## DỰ ÁN: FACEBOOK MOBILE CLIENT (ANDROID JAVA + FASTAPI & SQLITE)
### GIAI ĐOẠN: SPRINT 1 CHECKPOINT & QUALITY GATE SIGN-OFF

---

## 1. THÔNG TIN CHUNG (REPORT METADATA)

- **Tên dự án:** Facebook Mobile Client
- **Phiên bản nghiệm thu:** `v0.1.0-alpha` (Kết thúc Sprint 1)
- **Thời gian đánh giá:** Ngày 07 (Chủ Nhật, 2026-10-25)
- **Người lập báo cáo:** Hà Đăng Huy (`HuyHaDang` / `HDH`) - Backend Engineer & QA Lead / API Tester
- **Người đồng kiểm tra & phê duyệt:** 
  - Tùng Dương (`tungduong` / `TD`) - Lead Frontend Engineer & UI/UX Coordinator
  - Ánh Dương (`anhduong` / `AD`) - Lead Backend Engineer & Database Architect
- **Môi trường thử nghiệm tiêu chuẩn:**
  - **Mobile Client:** Máy ảo Android Google Pixel 10a (Chuẩn `Pixel_10a_API_37`, API Level 37.2, Android 15), Java 17, ViewBinding, Retrofit 2.9.0 + Gson.
  - **Backend Server:** Python 3.10+, FastAPI 0.100+, Uvicorn 0.23+, SQLAlchemy 2.0 ORM, Pydantic v2.
  - **Database:** SQLite 3.x (WAL Journaling Mode, `PRAGMA foreign_keys = ON;`).
  - **Tools kiểm thử:** Pytest 9.x, Starlette TestClient, Swagger UI (`http://localhost:8000/docs`), Postman, cURL.

---

## 2. MA TRẬN ĐÁNH GIÁ GIAO DIỆN MOBILE (FRONTEND MOCK TESTING)

Trong Sprint 1, toàn bộ 7 màn hình và thành phần giao diện người dùng cốt lõi được chạy thử nghiệm trên máy ảo Pixel 10a với dữ liệu giả lập (`MockDataGenerator.java`):

| STT | Màn hình / Thành phần UI | Thành viên phụ trách | Tiêu chí nghiệm thu chuẩn PDF | Kết quả | Ghi chú |
|:---:|:---|:---:|:---|:---:|:---|
| 1 | **Đăng nhập 1 chạm** (`AccountChooserActivity`) | Tùng Dương (`TD`) | Chọn tài khoản không cần mật khẩu, lưu phiên bằng `SessionManager`, chuyển đổi tài khoản demo. | **PASS** | Hoạt động mượt mà |
| 2 | **Bảng tin Home Feed** (`FeedFragment`, `PostAdapter`) | Minh Lương (`ML`) | Hiển thị bài viết, avatar tròn, tên tác giả, thời gian, Optimistic UI nút Like, dialog Chia sẻ bài. | **PASS** | Phản hồi tức thì |
| 3 | **Hộp thoại Bình luận** (`CommentBottomSheetDialog`) | Minh Lương (`ML`) | Chuẩn text-only (bong bóng xám, không reaction, không reply lồng nhau, không ảnh/sticker theo PDF). | **PASS** | Đúng đặc tả PDF |
| 4 | **Tạo bài viết mới** (`CreatePostActivity`) | Tùng Dương (`TD`) | Nhập văn bản, chọn 1 ảnh từ thư viện; **nút ĐĂNG bị khóa nếu cả chữ và ảnh đều rỗng**. | **PASS** | Validation chặt chẽ |
| 5 | **Trang cá nhân** (`ProfileFragment`, `PhotoGridAdapter`) | Phúc Đức (`PD`) & Tạ Quang Huy (`TQH`) | Ảnh bìa 200dp, avatar 120dp đè 50%, Tab All Posts và Tab Photos/Videos lưới 3 cột tỉ lệ 1:1. | **PASS** | Layout chuẩn Facebook |
| 6 | **Bạn bè & Tìm kiếm** (`FriendsFragment`, `SearchFriendsActivity`) | Phúc Đức (`PD`) | Danh sách lời mời kết bạn (Xác nhận / Xóa), ô tìm kiếm bạn bè có cơ chế debounce 300ms. | **PASS** | Tương tác tốt |
| 7 | **Hệ thống Thông báo** (`NotificationsFragment`, `NotificationAdapter`) | Tạ Quang Huy (`TQH`) | Danh sách thông báo, phân biệt đã đọc / chưa đọc, hiển thị đúng avatar và biểu tượng tương tác. | **PASS** | Đúng cấu trúc DTO |

**Tỷ lệ đạt giao diện Mobile:** **7/7 màn hình (100% PASS)**.

---

## 3. ĐÁNH GIÁ MÁY CHỦ FASTAPI & ĐỐI CHIẾU HỢP ĐỒNG API (BACKEND VERIFICATION)

### 3.1. Rà soát 15 REST Endpoints trên Swagger UI
Đã thực hiện kiểm tra thủ công và tự động toàn bộ 15 REST endpoints qua Swagger UI (`http://localhost:8000/docs`):

1. `GET /api/users`: Lấy danh sách tài khoản demo -> **200 OK**
2. `GET /api/users/{id}`: Chi tiết người dùng theo ID -> **200 OK** (404 khi không tồn tại)
3. `PUT /api/users/{id}`: Cập nhật thông tin trang cá nhân -> **200 OK**
4. `GET /api/users/{id}/posts`: Lấy bài viết của người dùng -> **200 OK**
5. `GET /api/users/{id}/photos`: Lấy bài viết có hình ảnh (dạng lưới) -> **200 OK**
6. `GET /api/posts`: Lấy bảng tin (hỗ trợ phân trang limit/skip) -> **200 OK**
7. `POST /api/posts`: Đăng bài viết mới (chặn bài rỗng) -> **201 Created** (400 khi rỗng)
8. `POST /api/posts/{id}/like`: Toggle trạng thái Thích -> **200 OK** (`isLiked`: boolean)
9. `POST /api/posts/{id}/share`: Chia sẻ bài viết -> **200 OK**
10. `GET /api/posts/{id}/comments`: Lấy bình luận text-only -> **200 OK**
11. `POST /api/posts/{id}/comments`: Gửi bình luận mới -> **200 OK** (tự động tạo Notification)
12. `GET /api/friends/requests`: Lấy danh sách lời mời kết bạn -> **200 OK**
13. `POST /api/friends/requests`: Gửi lời mời kết bạn -> **201 Created** (chặn trùng, chặn tự gửi)
14. `POST /api/friends/accept` & `/reject`: Xử lý lời mời kết bạn -> **200 OK**
15. `GET /api/friends/search`: Tìm kiếm bạn bè -> **200 OK**
16. `GET /api/notifications`: Lấy danh sách thông báo -> **200 OK**
17. `POST /api/notifications/{id}/read` & `/read-all`: Đánh dấu đã đọc -> **200 OK**
18. `POST /api/upload`: Tải lên ảnh tĩnh multipart -> **200 OK**

### 3.2. Đối chiếu Hợp đồng DTO camelCase (Data Parity với Android Retrofit)
Hệ thống Pydantic Schemas sử dụng cấu hình `ORMBaseConfig` để chuyển đổi tự động giữa `snake_case` (CSDL SQLite) và `camelCase` (JSON trả về cho Android). Đã kiểm thử bằng tệp `backend/tests/test_contract_dto.py`:

- **User Model:** `displayName`, `avatarUrl`, `coverUrl`, `createdAt` (không chứa `display_name`).
- **Post Model:** `authorId`, `authorName`, `authorAvatar`, `mediaUrl`, `isPublic`, `likesCount`, `commentsCount`, `isLiked`, `createdAt` (không chứa `author_id`, `likes_count`).
- **Comment Model:** `postId`, `authorId`, `authorName`, `authorAvatar`, `content`, `createdAt` (không chứa `post_id`).
- **FriendRequest Model:** `requesterId`, `requesterName`, `requesterAvatar`, `receiverId`, `status`, `createdAt`.
- **Notification Model:** `recipientId`, `senderId`, `senderName`, `senderAvatar`, `type`, `targetPostId`, `message`, `isRead`, `createdAt`.

**Kết quả kiểm tra tự động hợp đồng:**
- File: `backend/tests/test_contract_dto.py`
- Số ca kiểm thử: **6/6 test cases PASS (100%)**
- Thời gian thực thi: **1.58s**

### 3.3. Xác thực Tính toàn vẹn Cơ sở dữ liệu (`facebook.db`)
Thực thi lệnh kiểm tra và nạp dữ liệu mẫu sạch qua `seed_data.py --reset`:
- `PRAGMA integrity_check` = **`ok`**
- `PRAGMA foreign_key_check` = **HỢP LỆ (0 lỗi vi phạm ràng buộc)**
- Dữ liệu mẫu chuẩn:
  - 6 Người dùng (`user_current`, `user_101` đến `user_105`)
  - 10 Bài viết (có cả bài có ảnh và bài text-only)
  - 7 Bình luận text-only
  - 5 Lượt thích bài viết
  - 4 Quan hệ bạn bè (2 Pending requests + 2 Accepted friendships)
  - 5 Thông báo đủ 4 phân loại (`LIKE`, `COMMENT`, `FRIEND_REQUEST`, `FRIEND_ACCEPT`)

---

## 4. NHẬT KÝ LỖI ĐÃ PHÁT HIỆN & XỬ LÝ (SPRINT 1 DEFECT LOG)

| Mã Bug | Phân hệ | Mô tả lỗi | Mức độ | Trạng thái | Giải pháp khắc phục |
|:---:|:---:|:---|:---:|:---:|:---|
| **BUG-01** | Backend / Test | In-memory SQLite bị lỗi `no such table` khi chạy song song luồng test | Critical | **Resolved** | Bổ sung `from sqlalchemy.pool import StaticPool` và gán `poolclass=StaticPool` cho test engine. |
| **BUG-02** | Backend / API | Query parameter `/api/friends/requests` chỉ nhận `userId` gây lỗi 422 khi Android gửi `user_id` | Major | **Resolved** | Cấu hình router tiếp nhận cả `user_id` và `userId` (`target_user_id = user_id or userId`). |
| **BUG-03** | Frontend / UI | Nút ĐĂNG bài viết không bị khóa khi người dùng xóa sạch văn bản và không có ảnh | Medium | **Resolved** | Thêm validation listener kiểm tra `text.trim().isEmpty() && selectedImageUri == null` để disable nút. |
| **BUG-04** | Backend / Logic | Người dùng gửi 2 lần lời mời kết bạn cho cùng 1 người gây lỗi 500 vi phạm UNIQUE | Major | **Resolved** | Bắt ngoại lệ và kiểm tra trước trong `crud_friend.py`, trả về mã `400 Bad Request` thân thiện. |
| **BUG-05** | Backend / Logic | Tự like hoặc tự comment bài của chính mình vẫn kích hoạt gửi Notification cho bản thân | Minor | **Resolved** | Bổ sung điều kiện chặn trong `crud_notif.py`: `if recipient_id == sender_id: return None`. |

**Thống kê:**
- Tổng số bug phát hiện: **5**
- Số bug đã khắc phục triệt để: **5 (100%)**
- Số bug tồn đọng: **0** (Không có blocker nào trước thềm Sprint 2)

---

## 5. KẾT LUẬN & KÝ DUYỆT QUALITY GATE (QUALITY GATE SIGN-OFF)

Căn cứ vào kết quả kiểm thử thực tế trên cả 2 tầng Frontend và Backend:
1. **Giao diện người dùng:** 100% 7 màn hình hoàn thành xuất sắc, không lỗi giật lag, đạt chuẩn UI/UX Facebook trên máy ảo `Pixel_10a_API_37`.
2. **Hệ thống API Backend:** 100% endpoints sẵn sàng trên Swagger UI, định dạng JSON camelCase đồng bộ tuyệt đối với Android Retrofit DTOs.
3. **Mã nguồn & CSDL:** Nhánh `develop` được tích hợp sạch sẽ, không có xung đột mã nguồn (no merge conflicts), CSDL SQLite đạt chuẩn toàn vẹn dữ liệu.

> ### QUYẾT ĐỊNH CỦA QA LEAD & BAN ĐIỀU PHỐI DỰ ÁN:
> **CHÍNH THỨC KÝ DUYỆT QUALITY GATE SPRINT 1 - ĐẠT CHUẨN (PASS)**  
> Cho phép toàn bộ đội ngũ bước vào **Sprint 2: Ghép nối thực tế Real Backend qua Retrofit, Viết Unit Test tự động toàn diện và Tối ưu hiệu năng**.

**Chữ ký xác nhận:**
- Hà Đăng Huy (`HuyHaDang` - Backend Engineer & QA Lead)
- Tùng Dương (`tungduong` - Lead Frontend Engineer)
- Ánh Dương (`anhduong` - Lead Backend Engineer)
