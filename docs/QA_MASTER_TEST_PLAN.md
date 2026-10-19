# KẾ HOẠCH KIỂM THỬ TỔNG THỂ (QA MASTER TEST PLAN)

## DỰ ÁN FACEBOOK MOBILE CLIENT (ANDROID JAVA & PYTHON FASTAPI)

> **Người lập kế hoạch (QA Lead):** Hà Đăng Huy (`HuyHaDang`)  
> **Chức danh:** Backend Engineer & QA Lead / API Tester  
> **Đồng phụ trách QA:** Tạ Quang Huy (Frontend Engineer & Mobile QA / UI Tester)  
> **Phiên bản:** 1.0 (Ban hành Sprint 1 - Ngày 01)  
> **Đối chiếu đặc tả:** Bám sát 100% tài liệu gốc `Mobile App.pdf`  
> **Tiêu chuẩn kiểm thử:** IEEE 829 Standard for Software Test Documentation & Testing Pyramid

---

## 1. TỔNG QUAN & PHẠM VI KIỂM THỬ (EXECUTIVE SUMMARY & SCOPE)

### 1.1. Mục tiêu Kiểm thử

Đảm bảo chất lượng toàn diện của sản phẩm **Facebook Mobile Client**, xác minh rằng toàn bộ tính năng hoạt động ổn định, chính xác theo hợp đồng API, không xảy ra xung đột dữ liệu giữa Client (Android Java 17) và Backend (Python FastAPI + SQLite), đồng thời đáp ứng nghiêm ngặt các giới hạn đặc tả trong `Mobile App.pdf`.

### 1.2. Ranh giới Phạm vi (Scope Boundaries)

#### A. Trong phạm vi kiểm thử (In-Scope):

1. **Phân hệ Bảng tin (Home Feed):**
   - Tải danh sách bài viết sắp xếp theo thời gian mới nhất (`createdAt DESC`).
   - Tương tác Thích (Like/Unlike) với cơ chế Optimistic UI (đổi màu xanh tức thì).
   - Chia sẻ bài viết (Share Dialog) với dòng cảm nghĩ ngắn.
2. **Phân hệ Đăng bài viết mới (Create Post):**
   - Validation nghiêm ngặt: **Khóa nút ĐĂNG nếu cả nội dung chữ và ảnh đều rỗng**.
   - Chỉ cho phép đính kèm tối đa **01 ảnh hoặc video** từ thư viện thiết bị.
   - Quyền riêng tư mặc định: **Công khai (Public)**.
   - Cắt bỏ triệt để: Âm nhạc (Music), Gắn thẻ (Tag), Vị trí (Location), bộ lọc ảnh/sticker.
3. **Phân hệ Bình luận (Comments):**
   - Hộp thoại bình luận (Bottom Sheet Dialog) chuẩn **Text-only**.
   - Tuyệt đối **không có reply lồng nhau (no threading)**, **không có reaction trên comment**, **không đính kèm ảnh/video**, **không có bộ chọn sticker/emoji**.
4. **Phân hệ Bạn bè (Friends):**
   - Xem danh sách Lời mời kết bạn gửi đến, xử lý Chấp nhận (Confirm) và Xóa/Từ chối (Delete).
   - Xem danh sách Bạn bè hiện tại (Your Friends).
   - Tìm kiếm bạn bè (Search Friends) theo tên, áp dụng cơ chế chống spam gọi mạng (Debounce 300ms).
5. **Phân hệ Thông báo (Notifications):**
   - Danh sách thông báo với 4 loại tương tác: `LIKE`, `COMMENT`, `FRIEND_REQUEST`, `FRIEND_ACCEPT`.
   - Phân biệt trạng thái đã đọc (`isRead = 1`) và chưa đọc (`isRead = 0`).
   - Điều hướng người dùng tới bài viết hoặc hồ sơ tương ứng khi click.
6. **Phân hệ Trang cá nhân (Profile):**
   - Hiển thị đúng tỷ lệ: Ảnh bìa 200dp, Avatar tròn 120dp đè 50% lên ảnh bìa.
   - Tab 1: Tất cả bài viết cá nhân (All Posts).
   - Tab 2: Kho ảnh & video dạng lưới 3 cột (Photo Grid 1:1 aspect ratio), lọc các bài viết có `mediaUrl`.
7. **Hạ tầng Tầng Mạng & Cơ sở dữ liệu:**
   - Client Retrofit 2 + OkHttp gọi đúng `http://10.0.2.2:8000/api/` trên máy ảo Pixel 10a.
   - FastAPI RESTful API endpoints trả đúng HTTP status codes (200, 201, 400, 404, 422, 500).
   - SQLite WAL mode không bị lỗi `database is locked`, đảm bảo tính toàn vẹn khóa ngoại (Foreign Keys).

#### B. Ngoài phạm vi kiểm thử (Out-of-Scope - Cắt giảm chuẩn BTL):

- Nhắn tin trò chuyện (Messenger / Chat 1-1 / Group Chat).
- Tin 24h (Stories), Reels video ngắn, Livestream trực tiếp.
- Fanpage, Nhóm (Groups), Marketplace, Quảng cáo, Thanh toán trực tuyến.

---

## 2. CHIẾN LƯỢC KIỂM THỬ (TESTING PYRAMID STRATEGY)

```
              / \
             /   \
            / 10% \  --> E2E & Manual UI Testing (Pixel 10a API 37.2)
           /-------\
          /   20%   \  --> Integration Testing (Retrofit <-> FastAPI <-> SQLite)
         /-----------\
        /     70%     \  --> Unit Testing (JVM JUnit 4, Mockito, Pytest TestClient)
       /---------------\
```

### 2.1. Tầng 1: Unit Testing (70% tổng test cases)

- **Mục tiêu:** Kiểm thử độc lập từng hàm logic, validation, state transformation mà không cần khởi động thiết bị thật hay máy ảo.
- **Công cụ:**
  - Android Client: **JUnit 4.13.2**, **Mockito 5.11.0**, **androidx.arch.core:core-testing 2.2.0** (`InstantTaskExecutorRule`).
  - Backend Server: **pytest 8.x**, **httpx**, **FastAPI TestClient**.
- **Tiêu chuẩn:** Thời gian chạy toàn bộ bộ Unit Test phải < 15 giây.

### 2.2. Tầng 2: Integration Testing (20% tổng test cases)

- **Mục tiêu:** Kiểm tra sự tương thích hợp đồng dữ liệu (API Contracts) giữa JSON Response của Pydantic/FastAPI và Model DTO của Retrofit/Gson.
- **Công cụ:** Postman Collection, Swagger UI (`http://localhost:8000/docs`), Pytest database transaction rollback.

### 2.3. Tầng 3: Manual & System UI Testing (10% tổng test cases)

- **Mục tiêu:** Đánh giá trải nghiệm người dùng thực tế, giao diện hiển thị, phản hồi cảm ứng và tính mượt mà.
- **Môi trường chuẩn duy nhất:** Máy ảo Android Studio **Google Pixel 10a (Android 17.0, API Level 37.2, 420 dpi, màn hình 1080x2424)**.

---

## 3. MA TRẬN KỊCH BẢN KIỂM THỬ CHI TIẾT (TEST CASE MATRIX)

### Phân hệ 1: Bảng tin Home Feed & Đăng bài viết (Feed & Create Post)

| Mã Test Case   | Tên Kịch Bản                                          | Dữ Liệu Đầu Vào                                   | Các Bước Thực Hiện                                                                            | Kết Quả Kỳ Vọng                                                                                                | Mức Độ |
| :------------- | :---------------------------------------------------- | :------------------------------------------------ | :-------------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------- | :----: |
| **TC-FEED-01** | Tải danh sách bài viết Feed                           | `limit=20`, `currentUserId=usr_04`                | 1. Mở app vào Tab Feed.<br>2. Quan sát danh sách bài viết.                                    | Hiển thị danh sách bài viết mới nhất xếp theo `createdAt DESC`, avatar tròn, tên tác giả, nội dung, thời gian. | **P1** |
| **TC-FEED-02** | Thao tác Like bài viết (Optimistic UI)                | Click icon Like trên bài viết `post_1001`         | 1. Bấm nút "Thích".<br>2. Quan sát nút Thích.<br>3. Kiểm tra số lượng Like.                   | Icon đổi sang màu xanh `#1877F2` ngay lập tức, số like tăng +1 mà không bị giật/lag.                           | **P1** |
| **TC-FEED-03** | Thao tác Bỏ thích (Unlike)                            | Click icon Like trên bài đã like                  | 1. Bấm nút "Thích" đang ở màu xanh.                                                           | Icon trở về màu xám `#65676B`, số like giảm -1 ngay lập tức.                                                   | **P2** |
| **TC-FEED-04** | Mở dialog Chia sẻ bài viết                            | Click nút Share trên bài viết                     | 1. Bấm nút "Chia sẻ".                                                                         | Hiển thị Dialog cho phép nhập cảm nghĩ ngắn và nút "Share now".                                                | **P2** |
| **TC-POST-01** | Validation: Chặn đăng bài khi cả Text và Ảnh đều rỗng | `content = ""`, `imageUri = null`                 | 1. Mở màn hình Tạo bài viết.<br>2. Không nhập chữ, không chọn ảnh.<br>3. Quan sát nút "ĐĂNG". | Nút "ĐĂNG" ở trạng thái **Disabled (bị mờ/khóa)**, người dùng không thể bấm gửi.                               | **P0** |
| **TC-POST-02** | Cho phép đăng bài chỉ có Text                         | `content = "Hôm nay trời đẹp"`, `imageUri = null` | 1. Nhập văn bản vào ô nội dung.<br>2. Quan sát nút "ĐĂNG".                                    | Nút "ĐĂNG" chuyển sang trạng thái **Enabled (sáng màu xanh)**, bấm đăng thành công.                            | **P1** |
| **TC-POST-03** | Cho phép đăng bài chỉ có Ảnh                          | `content = ""`, `imageUri = "content://..."`      | 1. Bấm nút đính kèm ảnh.<br>2. Chọn 1 ảnh từ thư viện.                                        | Khung ảnh preview hiển thị, nút "ĐĂNG" tự động **Enabled**, bấm đăng thành công.                               | **P1** |
| **TC-POST-04** | Đính kèm ảnh tối đa duy nhất 1 ảnh                    | Chọn ảnh lần 2                                    | 1. Chọn ảnh thứ nhất.<br>2. Chọn tiếp ảnh thứ 2.                                              | Ảnh mới thay thế ảnh cũ trong preview, chỉ duy nhất 1 ảnh được đính kèm.                                       | **P2** |
| **TC-POST-05** | Hiển thị bài viết mới tạo lên đầu Feed                | Đăng bài thành công                               | 1. Bấm ĐĂNG.<br>2. Màn hình đóng lại, quay về Feed.                                           | Bài viết mới xuất hiện ngay tại vị trí đầu tiên của RecyclerView Feed.                                         | **P1** |

---

### Phân hệ 2: Bình luận bài viết (Comments - Text Only)

| Mã Test Case  | Tên Kịch Bản                                            | Dữ Liệu Đầu Vào                       | Các Bước Thực Hiện                                        | Kết Quả Kỳ Vọng                                                                          | Mức Độ |
| :------------ | :------------------------------------------------------ | :------------------------------------ | :-------------------------------------------------------- | :--------------------------------------------------------------------------------------- | :----: |
| **TC-CMT-01** | Mở hộp thoại bình luận dạng Bottom Sheet                | Click nút Bình luận ở bài viết        | 1. Bấm nút "Bình luận" trên bài viết `post_1001`.         | Hộp thoại Bottom Sheet trượt lên từ đáy màn hình, hiển thị danh sách comment text.       | **P1** |
| **TC-CMT-02** | Gửi bình luận chữ thành công                            | `content = "Chúc mừng nhóm 7 người!"` | 1. Nhập nội dung vào ô text.<br>2. Bấm nút Gửi (Send).    | Bình luận mới xuất hiện ngay cuối danh sách, số lượng `commentsCount` trên bài tăng +1.  | **P1** |
| **TC-CMT-03** | Chặn bình luận rỗng hoặc toàn dấu cách                  | `content = "     "`                   | 1. Nhập dấu cách.<br>2. Bấm nút Gửi.                      | Không gửi request, nút Gửi bị disable hoặc thông báo yêu cầu nhập chữ.                   | **P2** |
| **TC-CMT-04** | Ràng buộc đặc tả PDF: Không có Reply lồng nhau          | Quan sát giao diện item comment       | 1. Kiểm tra từng comment item.                            | Không có nút "Trả lời / Reply", toàn bộ comment hiển thị phẳng (Flat list).              | **P1** |
| **TC-CMT-05** | Ràng buộc đặc tả PDF: Không có Reaction và Đính kèm ảnh | Quan sát ô nhập và item comment       | 1. Kiểm tra thanh nhập liệu.<br>2. Kiểm tra comment item. | Không có nút đính kèm ảnh/video, không có nút chọn sticker/emoji, không có nút like cmt. | **P1** |

---

### Phân hệ 3: Quản lý Bạn bè & Tìm kiếm (Friends & Search)

| Mã Test Case | Tên Kịch Bản                           | Dữ Liệu Đầu Vào                            | Các Bước Thực Hiện                                             | Kết Quả Kỳ Vọng                                                                                                  | Mức Độ |
| :----------- | :------------------------------------- | :----------------------------------------- | :------------------------------------------------------------- | :--------------------------------------------------------------------------------------------------------------- | :----: |
| **TC-FR-01** | Tải danh sách Lời mời kết bạn          | Tab Friends (`currentUserId=usr_04`)       | 1. Chuyển sang Tab Bạn bè.<br>2. Quan sát mục Lời mời kết bạn. | Hiển thị các lời mời ở trạng thái `PENDING` gồm avatar, tên người gửi, nút Chấp nhận và Xóa.                     | **P1** |
| **TC-FR-02** | Chấp nhận lời mời kết bạn (Confirm)    | Bấm "Chấp nhận" tại lời mời của `usr_01`   | 1. Bấm nút "Chấp nhận" màu xanh.                               | Lời mời biến mất khỏi danh sách lời mời, người này được chuyển vào danh sách bạn bè "Your Friends".              | **P1** |
| **TC-FR-03** | Xóa/Từ chối lời mời kết bạn (Delete)   | Bấm "Xóa" tại lời mời                      | 1. Bấm nút "Xóa" màu xám.                                      | Lời mời biến mất khỏi danh sách, trạng thái chuyển thành `REJECTED` trong database.                              | **P2** |
| **TC-FR-04** | Tìm kiếm bạn bè với Debounce 300ms     | Nhập từ khóa `"Tùng"`                      | 1. Gõ từng ký tự vào ô tìm kiếm.<br>2. Quan sát log network.   | API chỉ được kích hoạt sau 300ms kể từ khi người dùng ngừng gõ, kết quả lọc đúng người dùng có tên chứa từ khóa. | **P2** |
| **TC-FR-05** | Hiển thị trạng thái rỗng (Empty State) | User không có bạn bè hoặc không có lời mời | 1. Xem danh sách rỗng.                                         | Hiển thị thông báo "Không có lời mời kết bạn nào" thân thiện, không bị crash app.                                | **P2** |

---

### Phân hệ 4: Hệ thống Thông báo (Notifications)

| Mã Test Case    | Tên Kịch Bản                           | Dữ Liệu Đầu Vào              | Các Bước Thực Hiện                 | Kết Quả Kỳ Vọng                                                                                                       | Mức Độ |
| :-------------- | :------------------------------------- | :--------------------------- | :--------------------------------- | :-------------------------------------------------------------------------------------------------------------------- | :----: |
| **TC-NOTIF-01** | Hiển thị danh sách thông báo đa dạng   | Tab Notifications            | 1. Chuyển sang Tab Thông báo.      | Hiển thị đủ 4 loại tương tác với icon tương ứng: LIKE (icon like), COMMENT (icon cmt), FRIEND_REQUEST, FRIEND_ACCEPT. | **P1** |
| **TC-NOTIF-02** | Phân biệt trạng thái Đã đọc / Chưa đọc | Danh sách thông báo          | 1. Quan sát màu nền các item.      | Thông báo chưa đọc có nền màu xám nhạt/chấm xanh; thông báo đã đọc có nền màu trắng.                                  | **P2** |
| **TC-NOTIF-03** | Đánh dấu thông báo đã đọc khi click    | Click vào thông báo chưa đọc | 1. Bấm vào một thông báo chưa đọc. | Nền item đổi sang nền trắng, trạng thái `isRead` cập nhật thành 1.                                                    | **P2** |

---

### Phân hệ 5: Trang cá nhân (Profile & Photo Grid)

| Mã Test Case   | Tên Kịch Bản                               | Dữ Liệu Đầu Vào       | Các Bước Thực Hiện                | Kết Quả Kỳ Vọng                                                                                             | Mức Độ |
| :------------- | :----------------------------------------- | :-------------------- | :-------------------------------- | :---------------------------------------------------------------------------------------------------------- | :----: |
| **TC-PROF-01** | Kiểm tra bố cục Header Profile chuẩn PDF   | Mở Tab Profile        | 1. Quan sát Header trang cá nhân. | Ảnh bìa cao đúng 200dp, Avatar tròn đường kính 120dp đè 50% lên ảnh bìa, tên hiển thị căn giữa/dưới avatar. | **P1** |
| **TC-PROF-02** | Kiểm tra Tab "Tất cả bài viết" (All Posts) | Click Tab "Bài viết"  | 1. Xem danh sách bài viết.        | Chỉ hiển thị các bài viết do chính tài khoản này tạo ra (`authorId == currentUserId`).                      | **P1** |
| **TC-PROF-03** | Kiểm tra Tab "Ảnh & Video" lưới 3 cột      | Click Tab "Ảnh/Video" | 1. Xem danh sách ảnh.             | Hiển thị lưới đúng 3 cột (`GridLayoutManager`), các ảnh có tỷ lệ vuông 1:1, lọc toàn bộ bài có `mediaUrl`.  | **P1** |

---

### Phân hệ 6: Backend API, SQLite & Tính Toàn Vẹn Dữ Liệu

| Mã Test Case | Tên Kịch Bản                              | Dữ Liệu Đầu Vào                                        | Các Bước Thực Hiện                                                                           | Kết Quả Kỳ Vọng                                                                                                            | Mức Độ |
| :----------- | :---------------------------------------- | :----------------------------------------------------- | :------------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------- | :----: |
| **TC-BE-01** | SQLite Seeder Idempotency                 | `python seed_data.py --reset`                          | 1. Chạy lệnh reset database.<br>2. Kiểm tra dữ liệu.                                         | Tạo đủ 6 bảng, nạp đủ 7 users, 10 posts, 12 comments, 15 likes, 8 friendships, 7 notifications mà không có lỗi khóa ngoại. | **P0** |
| **TC-BE-02** | FastAPI Endpoint `GET /api/posts`         | `GET /api/posts?limit=20`                              | 1. Gửi request qua TestClient / Curl.                                                        | Trả về HTTP 200 OK, JSON array chứa danh sách bài viết đầy đủ `likesCount`, `commentsCount`, `isLiked`.                    | **P0** |
| **TC-BE-03** | Validation FastAPI `POST /api/posts` rỗng | `{"authorId":"usr_01", "content":"", "mediaUrl":null}` | 1. Gửi request tạo bài rỗng.                                                                 | Trả về HTTP 400 Bad Request hoặc 422 Unprocessable Entity, không chèn bản ghi rác vào SQLite.                              | **P1** |
| **TC-BE-04** | Toggle Like Transaction trong SQLite      | `POST /api/posts/post_1001/like`                       | 1. Gọi toggle like lần 1 -> chèn vào `post_likes`.<br>2. Gọi lần 2 -> xóa khỏi `post_likes`. | Lần 1 trả `isLiked: true`, lần 2 trả `isLiked: false`, không bị lỗi trùng lặp khóa chính.                                  | **P1** |
| **TC-BE-05** | Đồng bộ Model DTO giữa Client và Server   | Schema Pydantic vs Model Java                          | 1. Đối chiếu field names giữa JSON và Java class.                                            | Tên thuộc tính khớp 100% (camelCase trên JSON: `authorId`, `displayName`, `mediaUrl`, `createdAt`).                        | **P0** |

---

## 4. QUY TRÌNH QUẢN LÝ VÀ PHÂN LOẠI LỖI (BUG TRIAGE PROTOCOL)

Khi phát hiện lỗi trong quá trình kiểm thử tự động hoặc thủ công, QA Lead (Hà Đăng Huy) và UI QA (Tạ Quang Huy) tiến hành lập ticket và phân công theo quy chuẩn:

### 4.1. Phân định Cấp độ Nghiêm trọng (Severity Levels) & SLA

| Cấp Độ | Tên Mức Độ   | Định Nghĩa & Tác Động                                                                                                                         |   Thời Gian Xử Lý Tối Đa (SLA)   |
| :----: | :----------- | :-------------------------------------------------------------------------------------------------------------------------------------------- | :------------------------------: |
| **P0** | **Blocker**  | App bị Crash (văng ứng dụng), ANR (đơ màn hình), server FastAPI sập hoặc database bị lock không truy vấn được. Chặn hoàn toàn luồng kiểm thử. | **Sửa ngay lập tức trong 4 giờ** |
| **P1** | **Critical** | Tính năng chính bị hỏng: Không đăng được bài, không bấm được Like, không gửi được comment, không xem được danh sách bạn bè.                   |    **Sửa trong vòng 24 giờ**     |
| **P2** | **Major**    | Tính năng chạy được nhưng sai nghiệp vụ: Đăng được bài rỗng (vi phạm PDF), tính sai số lượng like khi cuộn trang, avatar méo tỷ lệ.           |    **Sửa trong vòng 48 giờ**     |
| **P3** | **Minor**    | Lỗi hiển thị nhỏ: Lệch padding vài dp, sai chính tả văn bản, màu icon chưa khớp 100% mã màu trong `colors.xml`.                               |  **Sửa trước ngày đóng Sprint**  |

### 4.2. Ma trận Trách nhiệm Phân công Sửa lỗi (RACI Matrix)

| Module / Hiện tượng lỗi                                                  | Lập trình viên chịu trách nhiệm xử lý (Assignee) | Người kiểm tra lại (Tester) |
| :----------------------------------------------------------------------- | :----------------------------------------------- | :-------------------------- |
| Lỗi Form Tạo bài viết (Create Post), Validation chặn bài rỗng            | **Tùng Dương** (Lead Frontend)                   | Hà Đăng Huy                 |
| Lỗi Khung Base, Navigation 4 Tab, Mạng cốt lõi (Core Retrofit)           | **Tùng Dương** (Lead Frontend)                   | Tạ Quang Huy                |
| Lỗi Bảng tin Feed, RecyclerView cuộn lag, Like/Share UI, Comment Dialog  | **Minh Lương** (Frontend)                        | Hà Đăng Huy                 |
| Lỗi Màn hình Bạn bè, Tìm kiếm bạn bè Debounce, Bố cục Profile Header     | **Phúc Đức** (Frontend)                          | Tạ Quang Huy                |
| Lỗi Màn hình Thông báo, Lưới ảnh 3 cột Tab Photos Profile                | **Tạ Quang Huy** (Frontend & QA)                 | Hà Đăng Huy                 |
| Lỗi API Upload ảnh multipart, Lưu trữ static `/static/uploads/`, URL ảnh | **Minh Đức** (Backend)                           | Hà Đăng Huy                 |
| Lỗi Core FastAPI Server, SQLite Queries Feed & Like/Share, Database Lock | **Ánh Dương** (Lead Backend)                     | Hà Đăng Huy                 |
| Lỗi API Bạn bè, API Lời mời kết bạn, Tìm kiếm SQLite                     | **Hà Đăng Huy** (Backend & QA Lead)              | Ánh Dương                   |

---

## 5. HƯỚNG DẪN THỰC THI KIỂM THỬ (TEST EXECUTION COMMANDS)

### 5.1. Chạy Toàn bộ Unit Test Android trên JVM:

```bash
# Chạy toàn bộ Unit Test của dự án Android
./gradlew testDebugUnitTest

# Chạy riêng bộ test kiểm tra dữ liệu Mock
./gradlew testDebugUnitTest --tests "com.facebook.client.data.mock.MockDataGeneratorTest"

# Chạy riêng bộ test kiểm tra logic đăng bài
./gradlew testDebugUnitTest --tests "com.facebook.client.utils.PostValidatorTest"
```

### 5.2. Chạy Kiểm tra CSDL SQLite Backend:

```bash
# Khởi tạo và kiểm tra tính toàn vẹn của dữ liệu mẫu SQLite
python backend/seed_data.py --reset --check
```

### 5.3. Chạy Toàn bộ Unit Test Backend với Pytest:

```bash
# Cài đặt thư viện kiểm thử backend (nếu chưa có)
pip install pytest httpx

# Chạy kiểm thử các endpoints API
pytest backend/tests/ -v
```

---

## 6. TIÊU CHÍ NGHIỆM THU THEO TỪNG CỘT MỐC (QUALITY GATES)

### 6.1. Cột mốc Checkpoint Tuần 1 (Ngày 07):

- [ ] 100% màn hình UI hoạt động hoàn hảo trên máy ảo Pixel 10a sử dụng Mock Data từ `MockDataGenerator.java`.
- [ ] 100% API endpoints của FastAPI phản hồi chuẩn xác theo đặc tả trên Swagger UI (`/docs`).
- [ ] Không còn tồn đọng lỗi mức **P0** hoặc **P1**.
- [ ] Toàn bộ code tuần 1 được review và merge sạch vào nhánh `develop`.

### 6.2. Cột mốc Nghiệm thu Cuối cùng Tuần 2 (Ngày 14):

- [ ] 100% tính năng kết nối thực tế thành công giữa Android Client (Retrofit) và Backend (FastAPI + SQLite).
- [ ] `./gradlew testDebugUnitTest` đạt tỷ lệ Pass 100% (Không có bất kỳ test case nào fail).
- [ ] `pytest backend/tests/` đạt tỷ lệ Pass 100%.
- [ ] Xuất file `app-release.apk` thành công, cài đặt và demo trơn tru trên máy ảo **Google Pixel 10a (API 37.2)** trước Giảng viên.

---

_Tài liệu được phê chuẩn bởi QA Lead Hà Đăng Huy & Đội ngũ Kỹ thuật Facebook Client._
