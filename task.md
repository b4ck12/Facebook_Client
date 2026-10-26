# BẢNG NHIỆM VỤ THỰC THI & KIỂM THỬ (TASK TRACKER)
## DÀNH CHO: HÀ ĐĂNG HUY (`HuyHaDang` / `HDH`)
### VAI TRÒ: BACKEND ENGINEER & QA LEAD / API TESTER
### DỰ ÁN: FACEBOOK MOBILE CLIENT (ANDROID JAVA + FASTAPI & SQLITE)
### GIAI ĐOẠN: SPRINT 1 CHECKPOINT & SPRINT 2 INTEGRATION (NGÀY 06 - NGÀY 09)

---

## 1. BẢNG THEO DÕI TIẾN ĐỘ TỔNG QUAN (PROGRESS MATRIX)

| Ngày | Mã Task | Tên Nhiệm Vụ Cốt Lõi | Sản Phẩm Bàn Giao (Deliverables) | Trạng Thái |
|:---:|:---:|:---|:---|:---:|
| **Ngày 06** | `[D6-HDH]` | Rà soát Swagger UI, Đối chiếu DTO camelCase & Kiểm tra Seed Data | `backend/tests/test_contract_dto.py`, `backend/seed_data.py` | `[x]` Hoàn thành |
| **Ngày 07** | `[D7-HDH]` | Tổng hợp Báo cáo Kiểm thử Tuần 1 (Quality Gate Checkpoint) | `docs/reports/WEEK_1_QA_REPORT.md`, Cập nhật `TODO.md` | `[x]` Hoàn thành |
| **Ngày 08** | `[D8-HDH]` | Kiểm thử độc lập API kết bạn trên 2 tài khoản qua Postman/Swagger | `backend/tests/test_multi_account_friends.py`, cURL logs | `[x]` Hoàn thành |
| **Ngày 09** | `[D9-HDH]` | Viết Unit Test toàn diện cho tầng Backend (`test_api.py`) | `backend/tests/test_api.py` (≥ 15 test cases) | `[x]` Hoàn thành |

```
Tiến độ thực hiện:
[==================================================] 100% Hoàn thành (4/4 Tasks)
```

---

## 2. CHECKLIST CHI TIẾT TỪNG NGÀY (ACTIONABLE CHECKLIST)

---

### NGÀY 06 (Thứ Bảy): `[D6-HDH]` - RÀ SOÁT SWAGGER UI, ĐỐI CHIẾU DTO & KIỂM TRA SEED DATA

> **Mục tiêu:** Đảm bảo 100% API phản hồi đúng chuẩn `camelCase` tương thích tuyệt đối với Android Retrofit Gson DTO; cơ sở dữ liệu `facebook.db` có đầy đủ dữ liệu mẫu chuẩn nghiệp vụ.

#### Checklist công việc:
- [x] **1.1. Khởi động máy chủ FastAPI & Mở Swagger UI:**
  - Chạy lệnh khởi động:
    ```bash
    cd backend
    uvicorn main:app --reload --port 8000
    ```
  - Truy cập trình duyệt: `http://localhost:8000/docs`.
- [x] **1.2. Rà soát danh sách 15 REST Endpoints trên Swagger UI:**
  - [x] `GET /api/users`: Lấy danh sách tài khoản demo
  - [x] `GET /api/users/{id}`: Lấy thông tin cá nhân chi tiết
  - [x] `PUT /api/users/{id}`: Cập nhật thông tin cá nhân
  - [x] `GET /api/users/{id}/posts`: Lấy bài viết của một người dùng
  - [x] `GET /api/users/{id}/photos`: Lấy bài viết có ảnh dạng lưới
  - [x] `GET /api/posts`: Lấy bảng tin (Home Feed)
  - [x] `POST /api/posts`: Đăng bài viết mới
  - [x] `POST /api/posts/{id}/like`: Thích / Bỏ thích bài viết
  - [x] `POST /api/posts/{id}/share`: Chia sẻ bài viết
  - [x] `GET /api/posts/{id}/comments`: Lấy bình luận text-only
  - [x] `POST /api/posts/{id}/comments`: Gửi bình luận mới
  - [x] `GET /api/friends/requests`: Lấy danh sách lời mời kết bạn
  - [x] `POST /api/friends/requests`: Gửi lời mời kết bạn mới
  - [x] `POST /api/friends/accept`: Chấp nhận kết bạn
  - [x] `POST /api/friends/reject`: Từ chối kết bạn
  - [x] `GET /api/friends/search`: Tìm kiếm bạn bè theo tên
  - [x] `GET /api/friends`: Lấy danh sách bạn bè hiện tại
  - [x] `GET /api/notifications`: Lấy danh sách thông báo
  - [x] `POST /api/notifications/{id}/read`: Đánh dấu thông báo đã đọc
  - [x] `POST /api/notifications/read-all`: Đánh dấu toàn bộ đã đọc
  - [x] `POST /api/upload`: Upload ảnh tĩnh multipart
- [x] **1.3. Đối chiếu các trường JSON Response (camelCase vs. Android DTO):**
  - [x] `User`: `id`, `email`, `displayName`, `avatarUrl`, `coverUrl`, `createdAt` $\rightarrow$ Khớp `User.java`.
  - [x] `Post`: `id`, `authorId`, `authorName`, `authorAvatar`, `content`, `mediaUrl`, `isPublic`, `likesCount`, `commentsCount`, `isLiked`, `createdAt` $\rightarrow$ Khớp `Post.java`.
  - [x] `Comment`: `id`, `postId`, `authorId`, `authorName`, `authorAvatar`, `content`, `createdAt` $\rightarrow$ Khớp `Comment.java`.
  - [x] `FriendRequest`: `id`, `requesterId`, `requesterName`, `requesterAvatar`, `receiverId`, `status`, `createdAt` $\rightarrow$ Khớp `FriendRequest.java`.
  - [x] `Notification`: `id`, `recipientId`, `senderId`, `senderName`, `senderAvatar`, `type`, `targetPostId`, `message`, `isRead`, `createdAt` $\rightarrow$ Khớp `Notification.java`.
- [x] **1.4. Tạo và chạy test kiểm tra tự động hợp đồng DTO (`test_contract_dto.py`):**
  ```bash
  pytest backend/tests/test_contract_dto.py -v
  ```
- [x] **1.5. Nạp lại và kiểm tra toàn vẹn CSDL `facebook.db`:**
  ```bash
  python backend/seed_data.py --reset
  ```
  - Xác nhận PRAGMA Integrity Check = `ok`.
  - Xác nhận PRAGMA Foreign Key Check = `0 lỗi`.
  - Kiểm tra có đủ 6 Users, 10 Posts, 7 Comments, 5 Likes, 4 Friendships, 5 Notifications.
- [x] **1.6. Git Commit Ngày 6:**
  ```bash
  git add backend/tests/test_contract_dto.py backend/seed_data.py backend/requirements.txt
  git commit -m "test(qa): audit swagger UI contracts and verify camelCase DTO parity (D6-HDH)"
  ```

---

### NGÀY 07 (Chủ Nhật): `[D7-HDH]` - BÁO CÁO KIỂM THỬ TUẦN 1 & KÝ DUYỆT QUALITY GATE

> **Mục tiêu:** Đóng gói kết quả kiểm thử toàn diện của Sprint 1, lập văn bản Báo cáo nghiệm thu Tuần 1, xác nhận toàn bộ PR được merge an toàn vào nhánh `develop`.

#### Checklist công việc:
- [x] **2.1. Thu thập kết quả kiểm thử từ nhóm Frontend:**
  - [x] Xác nhận màn hình Đăng nhập 1 chạm (`AccountChooserActivity`) chạy tốt trên máy ảo Pixel 10a.
  - [x] Xác nhận màn hình Feed, Like (Optimistic UI), Share bài viết hoạt động mượt mà.
  - [x] Xác nhận Hộp thoại bình luận (`CommentBottomSheetDialog`) đúng chuẩn text-only.
  - [x] Xác nhận Màn hình tạo bài viết (`CreatePostActivity`) có validation chặn bài rỗng.
  - [x] Xác nhận Màn hình Profile hiển thị đúng ảnh bìa, avatar, và Tab ảnh lưới 3 cột.
  - [x] Xác nhận Màn hình Bạn bè và Tìm kiếm bạn bè có debounce 300ms.
  - [x] Xác nhận Màn hình Thông báo hiển thị đúng danh sách tương tác.
- [x] **2.2. Kiểm tra trạng thái Git & Merge Checkpoint:**
  - Chạy `git status` và `git log --oneline -n 10` trên nhánh `develop`.
  - Xác nhận không có conflict, không có branch rác chưa merge.
- [x] **2.3. Soạn thảo văn bản `docs/reports/WEEK_1_QA_REPORT.md`:**
  - Bao gồm: Thông tin tổng quan, Ma trận kết quả kiểm thử UI (7 màn hình), Ma trận kiểm thử API (15 endpoints), Nhật ký lỗi (Defect Log), và Quyết định Quality Gate.
- [x] **2.4. Cập nhật bảng tiến độ tổng thể:**
  - Đánh dấu `[x]` cho `[D6-HDH]` và `[D7-HDH]` trên `TODO.md` và `docs/06_TASKS_SPRINT_PLAN.md`.
- [x] **2.5. Git Commit Ngày 7:**
  ```bash
  git add docs/reports/WEEK_1_QA_REPORT.md TODO.md docs/06_TASKS_SPRINT_PLAN.md task.md
  git commit -m "docs(qa): publish week 1 QA verification report and sign off sprint 1 gate (D7-HDH)"
  ```

---

### NGÀY 08 (Thứ Hai): `[D8-HDH]` - KIỂM THỬ ĐỘC LẬP API KẾT BẠN TRÊN 2 TÀI KHOẢN DEMO

> **Mục tiêu:** Kiểm thử thực tế tương tác mạng xã hội giữa 2 tài khoản độc lập (Người gửi A và Người nhận B); xác thực tính chính xác của lời mời kết bạn, ngăn chặn gửi trùng, và đồng bộ thông báo tự động.

#### Checklist công việc:
- [x] **3.1. Thiết lập 2 tài khoản thử nghiệm độc lập:**
  - Tài khoản A: `user_101` (Nguyễn Văn An)
  - Tài khoản B: `user_102` (Trần Thị Mai)
- [x] **3.2. Thực thi Kịch bản 1: Gửi lời mời kết bạn (A $\rightarrow$ B):**
  - Gửi request qua cURL:
    ```bash
    curl -X POST "http://localhost:8000/api/friends/requests" \
         -H "Content-Type: application/json" \
         -d '{"requesterId": "user_101", "receiverId": "user_102"}'
    ```
  - Kiểm tra kết quả: HTTP `201 Created`, trả về bản ghi có `status = "PENDING"`.
  - Gọi `GET /api/friends/requests?user_id=user_102`: Xác nhận thấy lời mời từ `user_101`.
  - Gọi `GET /api/notifications?user_id=user_102`: Xác nhận có thông báo loại `FRIEND_REQUEST` từ `user_101`.
- [x] **3.3. Thực thi Kịch bản 2: Kiểm tra Chống trùng lặp (Duplicate Request):**
  - Tiếp tục gửi lại lời mời từ `user_101` sang `user_102`:
    ```bash
    curl -X POST "http://localhost:8000/api/friends/requests" \
         -H "Content-Type: application/json" \
         -d '{"requesterId": "user_101", "receiverId": "user_102"}'
    ```
  - Kiểm tra kết quả: HTTP `400 Bad Request`, thông báo lỗi: `"Yêu cầu kết bạn đã tồn tại hoặc đã là bạn bè"`.
  - Xác nhận trong bảng `friendships` không bị tạo 2 dòng giống nhau.
- [x] **3.4. Thực thi Kịch bản 3: Chặn tự gửi kết bạn cho chính mình:**
  - Gửi lời mời với `requesterId = user_101` và `receiverId = user_101`.
  - Kiểm tra kết quả: HTTP `400 Bad Request`, chi tiết: `"Không thể gửi lời mời kết bạn cho chính mình"`.
- [x] **3.5. Thực thi Kịch bản 4: Chấp nhận kết bạn (B đồng ý A):**
  - B gọi `POST /api/friends/accept` với `requestId` vừa tạo.
  - Kiểm tra: HTTP `200 OK`, `status = "ACCEPTED"`.
  - Kiểm tra thông báo của A (`GET /api/notifications?user_id=user_101`): Có thông báo `FRIEND_ACCEPT` từ `user_102`.
  - Kiểm tra danh sách bạn bè 2 chiều:
    - `GET /api/friends?user_id=user_101` $\rightarrow$ Chứa `user_102`.
    - `GET /api/friends?user_id=user_102` $\rightarrow$ Chứa `user_101`.
- [x] **3.6. Tự động hóa kịch bản bằng file test (`test_multi_account_friends.py`):**
  ```bash
  pytest backend/tests/test_multi_account_friends.py -v
  ```
- [x] **3.7. Git Commit Ngày 8:**
  ```bash
  git add backend/tests/test_multi_account_friends.py TODO.md docs/06_TASKS_SPRINT_PLAN.md task.md
  git commit -m "test(friends): verify multi-account friend requests and duplicate prevention (D8-HDH)"
  ```

---

### NGÀY 09 (Thứ Ba): `[D9-HDH]` - BẮT ĐẦU VIẾT UNIT TEST TOÀN DIỆN CHO FASTAPI (`test_api.py`)

> **Mục tiêu:** Xây dựng tệp kiểm thử tự động `backend/tests/test_api.py` sử dụng `pytest` và `TestClient`, bao phủ toàn diện 100% các endpoint của máy chủ FastAPI trên môi trường In-Memory SQLite độc lập.

#### Checklist công việc:
- [x] **4.1. Cấu hình môi trường In-Memory Test Fixture:**
  - Tạo `test_engine` trỏ tới `sqlite:///:memory:` với `StaticPool`.
  - Viết fixture `client` tự động nạp Base metadata và dữ liệu mẫu sạch trước mỗi test function.
- [x] **4.2. Viết Test Suite phân hệ Người dùng (Users):**
  - [x] `test_get_all_users`: Kiểm tra trả về danh sách tài khoản hợp lệ (200 OK).
  - [x] `test_get_user_profile_success`: Lấy thông tin user có trong CSDL (200 OK).
  - [x] `test_get_user_profile_not_found`: ID không tồn tại $\rightarrow$ 404 Not Found.
  - [x] `test_update_user_profile`: Cập nhật `displayName` và kiểm tra phản hồi.
- [x] **4.3. Viết Test Suite phân hệ Bài viết & Like (Posts & Likes):**
  - [x] `test_get_feed_posts_pagination`: Kiểm tra phân trang `limit` và `skip`.
  - [x] `test_create_post_success`: Đăng bài viết hợp lệ (201 Created).
  - [x] `test_create_post_empty_bad_request`: Đăng bài rỗng cả text và media $\rightarrow$ 400 Bad Request.
  - [x] `test_toggle_like_cycle`: Bấm like $\rightarrow$ `isLiked = True`; bấm lại $\rightarrow$ `isLiked = False`.
  - [x] `test_share_post_creates_new_post`: Chia sẻ bài viết $\rightarrow$ Tạo bài mới thành công.
- [x] **4.4. Viết Test Suite phân hệ Bình luận (Comments):**
  - [x] `test_get_post_comments_sorted_asc`: Lấy bình luận sắp xếp thời gian tăng dần (cũ trước, mới sau).
  - [x] `test_add_comment_success`: Thêm bình luận text-only hợp lệ (201 Created).
  - [x] `test_add_comment_empty_bad_request`: Nội dung rỗng $\rightarrow$ 400 Bad Request.
  - [x] `test_add_comment_nonexistent_post`: ID bài viết sai $\rightarrow$ 404 Not Found.
- [x] **4.5. Viết Test Suite phân hệ Thông báo (Notifications):**
  - [x] `test_get_user_notifications`: Lấy danh sách thông báo theo `user_id`.
  - [x] `test_mark_single_notification_read`: Đánh dấu 1 thông báo là đã đọc (`/read`).
  - [x] `test_mark_all_notifications_read`: Đánh dấu toàn bộ thông báo đã đọc (`/read-all`).
- [x] **4.6. Viết Test Suite phân hệ Tải ảnh (Upload):**
  - [x] `test_upload_valid_image`: Upload file ảnh PNG/JPEG hợp lệ $\rightarrow$ 201 Created.
  - [x] `test_upload_invalid_file_type`: Upload file không phải ảnh $\rightarrow$ 400 Bad Request.
- [x] **4.7. Chạy kiểm thử và nghiệm thu kết quả:**
  ```bash
  # Chạy riêng file test_api.py
  pytest backend/tests/test_api.py -v

  # Chạy toàn bộ test suites trong backend/tests/
  pytest backend/tests/ -v --durations=5
  ```
  - Xác nhận 100% test cases đều XANH (PASS).
  - Thời gian chạy < 5 giây (2.14s thực tế).
- [x] **4.8. Cập nhật `TODO.md` và Git Commit Ngày 9:**
  ```bash
  git add backend/tests/test_api.py TODO.md docs/06_TASKS_SPRINT_PLAN.md task.md
  git commit -m "test(backend): implement comprehensive pytest test client suite test_api.py (D9-HDH)"
  ```

---

## 3. CÁC LỆNH TERMINAL HÀNG NGÀY (DAILY CHEAT SHEET CHO HUY)

```bash
# 1. Khởi động server FastAPI phục vụ kiểm thử
cd backend
.venv\Scripts\activate
uvicorn main:app --reload --port 8000

# 2. Reset và seed lại CSDL sạch
python seed_data.py --reset

# 3. Chạy kiểm thử tự động toàn bộ Backend
pytest tests/ -v

# 4. Kiểm tra Git branch và trạng thái
git status
git branch -v
```

---

## 4. TIÊU CHÍ HOÀN THÀNH (DEFINITION OF DONE - DoD)

Một nhiệm vụ của Hà Đăng Huy chỉ được xem là hoàn thành khi:
1. **Mã nguồn sạch:** Code tuân thủ PEP 8, có chú thích tiếng Việt rõ ràng, type hints đầy đủ.
2. **Kiểm thử tự động:** Test suite `pytest` chạy pass 100% không có warning hoặc error.
3. **Đối chiếu thực tế:** Đã đối chiếu thực tế với Swagger UI và Android Java DTO.
4. **Tài liệu & Tiến độ:** Đã cập nhật trạng thái `[x]` trên `TODO.md` và `docs/06_TASKS_SPRINT_PLAN.md`.
5. **Git sạch sẽ:** Commit đúng quy chuẩn đặt tên và merge an toàn vào `develop`.
