# 06. KẾ HOẠCH CHI TIẾT 2 TUẦN (14 NGÀY) - TỪNG THÀNH VIÊN, TỪNG TASK CÓ CHECKBOX TICK

---

## NGUYÊN TẮC VẬN HÀNH 2 TUẦN (14 NGÀY THẦN TỐC)

- **Thời gian triển khai:** Đúng 14 ngày (2 tuần) chuẩn chu kỳ 2 Sprints ngắn.
- **Mục tiêu tối thượng:** Bám sát 100% đặc tả `Mobile App.pdf`, không làm ngoài phạm vi, bàn giao ứng dụng chạy mượt mà trên máy ảo **Pixel 10a (API 37.2)** kết nối với backend **Python FastAPI** và **SQLite**.
- **Cơ chế song song:** Tuần 1 Frontend dùng **Mock Data** chạy 100% giao diện, Backend xây dựng FastAPI Server, SQLite Schema và API Endpoints; Tuần 2 ghép nối thực tế qua Retrofit, viết Unit Test, sửa lỗi và đóng gói APK.
- **Đánh dấu tiến độ:** Mỗi công việc có checkbox `[ ]`. Khi thành viên hoàn thành task, đổi thành `[x]`.

---

## TỔNG HỢP TIẾN ĐỘ THEO 7 THÀNH VIÊN (CHECKLIST TỔNG HỢP)

- [ ] **Tùng Dương (FE Lead):** Base Shell, Navigation, Auth, Màn hình Tạo bài viết `CreatePostActivity`.
- [ ] **Minh Lương (FE):** Giao diện Feed, `PostAdapter`, Thao tác Like, Share, Hộp thoại Comment text-only.
- [ ] **Phúc Đức (FE):** Giao diện Bạn bè, Duyệt kết bạn, Tìm kiếm bạn bè, Màn hình Profile Header & All Posts.
- [ ] **Tạ Quang Huy (FE + QA/Test):** Giao diện Notifications, Tab ảnh lưới 3 cột Profile, Unit Test ViewModel, Manual Test UI.
- [ ] **Ánh Dương (BE Lead/Main):** Kiến trúc CSDL SQLite (`facebook.db`), Core FastAPI Server, CRUD Bài viết, Thao tác Like/Share, Review code BE.
- [ ] **Minh Đức (BE):** Media Upload API (lưu trữ ảnh tĩnh `/static/uploads/`), API Profile cá nhân, API Hệ thống Thông báo.
- [ ] **Hà Đăng Huy (BE + QA/Test):** API Bạn bè & Tìm kiếm trên FastAPI + SQLite, Unit Test Backend (`pytest`) & Android Data Layer, Master Test Plan.

---

# TUẦN 1 (SPRINT 1: NGÀY 01 - NGÀY 07)
### *Mục tiêu: Dựng nền tảng, Thống nhất DTO, Code độc lập song song với Mock Data & FastAPI Endpoints*

---

### NGÀY 01 (Thứ Hai): Khởi tạo Dự án, Chuẩn hóa Môi trường & Thống nhất Contract DTO
*Mục tiêu ngày 1: Cả 7 người cài đặt máy đồng nhất, có repo chung, chốt toàn bộ Model DTO và SQLite Schema.*

- [ ] **Tùng Dương (FE Lead):** `[D1-TD]` Khởi tạo Git Repo, cấu hình `.gitignore`, thiết lập `namespace "com.facebook.client"`, `compileSdk 37`, `targetSdk 37`, `minSdk 24`, đẩy nhánh `develop` lên GitHub.
- [ ] **Ánh Dương (BE Lead):** `[D1-AD]` Họp với Tùng Dương, thống nhất 100% cấu trúc các lớp DTO (`User`, `Post`, `Comment`, `FriendRequest`, `Notification`), định nghĩa DDL SQLite (`schema.sql`) và Interface `IPostRepository`, `IFriendRepository`.
- [x] **Minh Đức (BE):** `[D1-MD]` Khởi tạo project Python FastAPI trong thư mục `backend/`, thiết lập file `requirements.txt` (FastAPI, Uvicorn, SQLAlchemy, Pydantic), cấu hình kết nối SQLite `facebook.db` và thư mục `static/uploads/`.
- [x] **Hà Đăng Huy (BE + QA):** `[D1-HDH]` Cài đặt Java 17, Python 3.10+, cấu hình Git cá nhân, tạo tài liệu Kế hoạch Kiểm thử tổng thể (Master Test Plan).
- [ ] **Minh Lương (FE):** `[D1-ML]` Cài đặt Java 17, Android Studio, cấu hình máy ảo chuẩn `Pixel_10a_API_37`, clone repo dự án về máy.
- [ ] **Phúc Đức (FE):** `[D1-PD]` Cài đặt Java 17, Android Studio, kiểm tra chạy thử máy ảo `Pixel_10a_API_37`, clone repo dự án về máy.
- [x] **Tạ Quang Huy (FE + QA):** `[D1-TQH]` Cài đặt Java 17, Android Studio, máy ảo `Pixel_10a_API_37`, clone repo, chuẩn bị bộ Vector Icons chuẩn Facebook (like, comment, share, friends, bell).

---

### NGÀY 02 (Thứ Ba): Dựng Khung Base Architecture, Theme Màu & Dữ liệu Mock
*Mục tiêu ngày 2: Frontend có dữ liệu giả lập để hiển thị; Backend khởi tạo SQLAlchemy Models và Upload Endpoint.*

- [ ] **Tùng Dương (FE Lead):** `[D2-TD]` Xây dựng `BaseActivity`, `BaseFragment`, khai báo Bảng màu Facebook (`colors.xml`), Font chữ Roboto (`dimens.xml`) và Theme Material.
- [ ] **Ánh Dương (BE Lead):** `[D2-AD]` Viết SQLAlchemy models (`models.py`) và Pydantic schemas (`schemas.py`), tạo các bảng trên SQLite: `users`, `posts`, `comments`, `friendships`, `post_likes`, `notifications`.
- [x] **Minh Đức (BE):** `[D2-MD]` Viết endpoint `POST /api/upload` trên FastAPI tiếp nhận ảnh upload dạng multipart, lưu file vào `static/uploads/` và trả về URL ảnh tĩnh phục vụ Android client.
- [x] **Hà Đăng Huy (BE + QA):** `[D2-HDH]` Xây dựng script `seed_data.py` (chèn dữ liệu mẫu vào SQLite) và class `MockDataGenerator.java` sinh sẵn 10 bài viết mẫu, 5 bạn bè, 5 thông báo trong bộ nhớ để FE dùng ngay.
- [ ] **Minh Lương (FE):** `[D2-ML]` Tạo class `MockPostRepository.java` thực thi `IPostRepository`, trả về danh sách bài viết giả lập từ `MockDataGenerator`.
- [ ] **Phúc Đức (FE):** `[D2-PD]` Tạo class `MockFriendRepository.java` thực thi `IFriendRepository`, trả về danh sách bạn bè và lời mời kết bạn mẫu.
- [x] **Tạ Quang Huy (FE + QA):** `[D2-TQH]` Tạo class `MockNotificationRepository.java`, dựng sẵn bộ dữ liệu thông báo mẫu; import đầy đủ 21 vector icons Facebook vào `res/drawable`.

---

### NGÀY 03 (Thứ Tư): Bắt đầu Code Giao diện Độc lập & Backend Feed API
*Mục tiêu ngày 3: Khung điều hướng 4 Tab hoàn thiện, thẻ bài viết item_post lên hình, Feed API sẵn sàng.*

- [ ] **Tùng Dương (FE Lead):** `[D3-TD]` Dựng `MainActivity.xml`, gắn `BottomNavigationView` với 4 Tab: Feed, Friends, Notifications, Profile; tạo `NavigationComponent` chuyển tab mượt mà.
- [ ] **Minh Lương (FE):** `[D3-ML]` Thiết kế layout `item_post.xml` (Header avatar tròn, tên, thời gian, text nội dung, khung ảnh đính kèm, stats lượt like/comment).
- [ ] **Phúc Đức (FE):** `[D3-PD]` Thiết kế layout `fragment_friends.xml` và `item_friend_request.xml` (Avatar, tên người gửi, nút Confirm màu xanh, nút Delete màu xám).
- [x] **Tạ Quang Huy (FE + QA):** `[D3-TQH]` Thiết kế layout `fragment_notifications.xml` và `item_notification.xml` (Avatar người tương tác, nội dung thông báo, icon loại tương tác).
- [ ] **Ánh Dương (BE Lead):** `[D3-AD]` Viết router `posts.py` trên FastAPI: Triển khai endpoint `GET /api/posts` truy vấn bảng `posts` trong SQLite kèm số like, số comment sắp xếp theo `created_at DESC`.
- [ ] **Minh Đức (BE):** `[D3-MD]` Viết router `users.py` trên FastAPI: Triển khai endpoint `GET /api/users/{id}` lấy thông tin cá nhân từ SQLite.
- [ ] **Hà Đăng Huy (BE + QA):** `[D3-HDH]` Viết router `friends.py` trên FastAPI: Triển khai endpoint `GET /api/friends/requests` lấy danh sách lời mời kết bạn từ SQLite.

---

### NGÀY 04 (Thứ Năm): Post Interaction (Like/Share), Create Post Form & Friends UI
*Mục tiêu ngày 4: Bấm like đổi màu ngay lập tức, mở form tạo bài viết chọn ảnh từ thư viện, API Like hoàn tất.*

- [ ] **Tùng Dương (FE Lead):** `[D4-TD]` Dựng giao diện `activity_create_post.xml` (Top bar có nút ĐĂNG, Header người dùng Public, ô nhập text không viền, nút đính kèm ảnh duy nhất có icon xanh).
- [ ] **Minh Lương (FE):** `[D4-ML]` Viết `PostAdapter.java` kết nối RecyclerView cho Feed; gắn sự kiện click nút Thích (Optimistic UI: đổi icon xanh và tăng số like ngay lập tức mà không chờ mạng).
- [ ] **Phúc Đức (FE):** `[D4-PD]` Viết `FriendRequestAdapter.java`, xử lý sự kiện bấm nút "Chấp nhận" (Confirm) và "Xóa" (Delete) trên danh sách lời mời kết bạn.
- [ ] **Tạ Quang Huy (FE + QA):** `[D4-TQH]` Viết `NotificationAdapter.java`, xử lý hiển thị trạng thái đã đọc / chưa đọc (nền xám nhạt) và thời gian tương đối ("5 phút trước").
- [ ] **Ánh Dương (BE Lead):** `[D4-AD]` Viết endpoint `POST /api/posts/{id}/like` trên FastAPI xử lý thêm/xóa bản ghi trong bảng `post_likes` (Toggle Like transaction trong SQLite).
- [ ] **Minh Đức (BE):** `[D4-MD]` Viết endpoint `PUT /api/users/{id}` trên FastAPI cập nhật `avatar_url` và `cover_url` vào bảng `users`.
- [ ] **Hà Đăng Huy (BE + QA):** `[D4-HDH]` Viết endpoint `POST /api/friends/accept` và `POST /api/friends/reject` cập nhật trạng thái trong bảng `friendships` của SQLite.

---

### NGÀY 05 (Thứ Sáu): Validation Đăng bài, Comment Text-only & Profile Layout
*Mục tiêu ngày 5: Khóa nút Đăng khi rỗng (chuẩn PDF), mở bottom sheet comment text-only, API Create Post & Search.*

- [ ] **Tùng Dương (FE Lead):** `[D5-TD]` Xử lý logic chọn 1 ảnh từ thư viện máy bằng `ActivityResultContracts.GetContent()`; viết hàm kiểm tra validation: **nếu text rỗng VÀ chưa chọn ảnh thì nút ĐĂNG bị mờ/khóa (disable)**.
- [ ] **Minh Lương (FE):** `[D5-ML]` Dựng `bottom_sheet_comments.xml` và `item_comment.xml` (Bong bóng xám text-only, **không reply, không reaction, không đính kèm ảnh/video, không emoji picker** theo đúng đặc tả PDF).
- [ ] **Phúc Đức (FE):** `[D5-PD]` Dựng `fragment_profile.xml`: Hiển thị ảnh bìa 200dp, avatar tròn 120dp đè 50% lên ảnh bìa, tên hiển thị, và `TabLayout` (Tab All Posts, Tab Photos/Videos).
- [ ] **Tạ Quang Huy (FE + QA):** `[D5-TQH]` Thiết kế `item_photo_grid.xml` (ảnh tỷ lệ 1:1) và `PhotoGridAdapter.java` dạng lưới 3 cột (`GridLayoutManager`) phục vụ Tab ảnh của Profile.
- [ ] **Ánh Dương (BE Lead):** `[D5-AD]` Viết endpoint `POST /api/posts` (tạo bài viết) và `POST /api/posts/{id}/share` (chia sẻ bài viết) trên FastAPI.
- [ ] **Minh Đức (BE):** `[D5-MD]` Viết endpoint `GET /api/users/{id}/posts` và `GET /api/users/{id}/photos` (lọc các bài viết có `media_url IS NOT NULL`).
- [ ] **Hà Đăng Huy (BE + QA):** `[D5-HDH]` Viết endpoint `GET /api/friends/search?q=` trên FastAPI tìm kiếm bạn bè theo tên trong SQLite (câu lệnh SQL `LIKE %query%`).

---

### NGÀY 06 (Thứ Bảy): Hoàn thiện 100% Giao diện Mock & Test Độc lập từng Module
*Mục tiêu ngày 6: Tất cả các màn hình bấm được, chạy mượt mà trên máy ảo Pixel 10a; API Swagger UI test xanh 100%.*

- [ ] **Tùng Dương (FE Lead):** `[D6-TD]` Rà soát toàn bộ luồng Auth giả lập, chuyển tab tại `MainActivity`, kết nối hiển thị bài viết mới tạo vào đầu danh sách Feed.
- [ ] **Minh Lương (FE):** `[D6-ML]` Kết nối `CommentViewModel` với `CommentBottomSheetDialog`, gửi comment text và hiển thị ngay vào RecyclerView; làm AlertDialog "Chia sẻ ngay" khi bấm nút Share.
- [ ] **Phúc Đức (FE):** `[D6-PD]` Dựng màn hình `activity_search_friends.xml`, gắn ô tìm kiếm với danh sách bạn bè mẫu, xử lý debounce nhập liệu 300ms.
- [ ] **Tạ Quang Huy (FE + QA):** `[D6-TQH]` Ghép `PhotoGridAdapter` vào Tab Photos của Profile; chạy thử nghiệm toàn bộ giao diện trên máy ảo `Pixel_10a_API_37`, chụp ảnh các màn hình báo cáo.
- [ ] **Ánh Dương (BE Lead):** `[D6-AD]` Viết router `comments.py` trên FastAPI: Triển khai endpoint `GET /api/posts/{id}/comments` và `POST /api/posts/{id}/comments` (chỉ nhận text thuần túy).
- [ ] **Minh Đức (BE):** `[D6-MD]` Viết logic tự động tạo bản ghi trong bảng `notifications` khi có tương tác Thích bài viết hoặc Bình luận bài viết.
- [ ] **Hà Đăng Huy (BE + QA):** `[D6-HDH]` Rà soát toàn bộ Swagger UI (`http://localhost:8000/docs`), chuẩn bị bộ dữ liệu test mẫu trong `facebook.db` sẵn sàng cho tuần tích hợp.

---

### NGÀY 07 (Chủ Nhật): Review Checkpoint Tuần 1 & Hợp nhất Code Nhánh `develop`
*Mục tiêu ngày 7: 100% code tuần 1 được review sạch sẽ và merge vào develop, không có conflict.*

- [ ] **Tùng Dương (FE Lead):** `[D7-TD]` Review toàn bộ Pull Request của 3 bạn FE (`Minh Lương`, `Phúc Đức`, `Tạ Quang Huy`), phê duyệt và merge vào nhánh `develop`.
- [ ] **Ánh Dương (BE Lead):** `[D7-AD]` Review toàn bộ Pull Request của 2 bạn BE (`Minh Đức`, `Hà Đăng Huy`), kiểm tra máy chủ FastAPI và file `facebook.db`, phê duyệt và merge vào nhánh `develop`.
- [ ] **Minh Lương (FE):** `[D7-ML]` Giải quyết xung đột giao diện (nếu có), đảm bảo app build xanh không có cảnh báo.
- [ ] **Phúc Đức (FE):** `[D7-PD]` Đồng bộ nhánh `develop` về máy, test luồng Friends và Profile chạy trơn tru.
- [ ] **Minh Đức (BE):** `[D7-MD]` Kiểm tra thư mục upload ảnh `/static/uploads/` và file `facebook.db` trên nhánh `develop`.
- [ ] **Hà Đăng Huy (BE + QA):** `[D7-HDH]` Tổng hợp báo cáo kiểm thử Tuần 1: Xác nhận 100% màn hình UI hoạt động hoàn hảo với Mock Data và 100% API FastAPI đã sẵn sàng trên Swagger UI.
- [ ] **Tạ Quang Huy (FE + QA):** `[D7-TQH]` Chạy build APK thử nghiệm lần 1 trên máy ảo Pixel 10a, demo ngắn cho cả nhóm trong buổi họp tuần 1.

---

# TUẦN 2 (SPRINT 2: NGÀY 08 - NGÀY 14)
### *Mục tiêu: Tích hợp Real FastAPI Backend, Viết Unit Test, Sửa lỗi & Nghiệm thu*

---

### NGÀY 08 (Thứ Hai): Tích hợp Real Backend vào Feed & Đăng bài (Integration Day 1)
*Mục tiêu ngày 8: Thay thế MockPostRepository bằng PostRepositoryImpl, load bài viết thật từ FastAPI server.*

- [ ] **Tùng Dương (FE Lead):** `[D8-TD]` **Xây dựng Tầng Mạng Cốt lõi (Core Network Setup):**
  - Thêm thư viện Retrofit 2 (`2.9.0`), Converter Gson (`2.9.0`), OkHttp Logging Interceptor (`4.12.0`) vào `app/build.gradle`.
  - Tạo cấu hình mạng `res/xml/network_security_config.xml` cho phép HTTP cleartext tới `10.0.2.2` và `localhost`.
  - Khai báo quyền `INTERNET`, `ACCESS_NETWORK_STATE` và gán `networkSecurityConfig` trong `AndroidManifest.xml`.
  - Viết `ApiClient.java` (Retrofit Singleton trỏ `http://10.0.2.2:8000/api/`), `ApiService.java` (Interface toàn bộ REST Endpoints), `ApiResponseHandler.java`, `Resource.java`.
  - Ghép `PostRepositoryImpl` vào `CreatePostViewModel`, gọi API upload ảnh multipart (`POST /api/upload`) và tạo bài viết (`POST /api/posts`).
- [ ] **Minh Lương (FE):** `[D8-ML]` Chuyển `FeedViewModel` sang dùng `PostRepositoryImpl`, lấy dữ liệu bài viết thật từ FastAPI + SQLite (`GET /api/posts`); kiểm tra thao tác Like (`POST /api/posts/{id}/like`) đồng bộ lên server.
- [ ] **Ánh Dương (BE Lead):** `[D8-AD]` Hỗ trợ Tùng Dương và Minh Lương gỡ lỗi (debug) kết nối Retrofit với FastAPI, cấu hình CORS Middleware và log request Uvicorn.
- [ ] **Phúc Đức (FE):** `[D8-PD]` Bắt đầu ghép `FriendRepositoryImpl` vào `FriendsViewModel`, gọi API tải danh sách lời mời kết bạn từ endpoint `GET /api/friends/requests`.
- [ ] **Minh Đức (BE):** `[D8-MD]` Hỗ trợ Phúc Đức gỡ lỗi dữ liệu bạn bè; kiểm tra câu lệnh SQL trong SQLite.
- [ ] **Hà Đăng Huy (BE + QA):** `[D8-HDH]` Kiểm thử độc lập API kết bạn bằng cách test trên 2 tài khoản demo khác nhau xem lời mời có đến đúng người không.
- [ ] **Tạ Quang Huy (FE + QA):** `[D8-TQH]` Ghép `NotificationRepositoryImpl` vào `NotificationViewModel`, gọi API `GET /api/notifications` từ FastAPI server.

---

### NGÀY 09 (Thứ Ba): Tích hợp Real Backend vào Profile & Thao tác Bạn bè (Integration Day 2)
*Mục tiêu ngày 9: Toàn bộ 4 tab của ứng dụng đều đọc/ghi dữ liệu thật từ FastAPI + SQLite.*

- [ ] **Phúc Đức (FE):** `[D9-PD]` Hoàn tất tích hợp dữ liệu thật cho Màn hình Trang cá nhân (`ProfileFragment`): hiển thị ảnh bìa, avatar và danh sách bài viết từ endpoint `/api/users/{id}` và `/api/users/{id}/posts`.
- [ ] **Tạ Quang Huy (FE + QA):** `[D9-TQH]` Hoàn tất tích hợp Tab "Ảnh & Video" trong Profile: gọi endpoint `/api/users/{id}/photos` hiển thị lên lưới 3 cột.
- [ ] **Minh Lương (FE):** `[D9-ML]` Tích hợp gửi comment văn bản thật lên endpoint `POST /api/posts/{id}/comments`, kiểm tra danh sách comment tự động cập nhật sau khi gửi.
- [ ] **Tùng Dương (FE Lead):** `[D9-TD]` Kiểm tra luồng Đăng bài: test đăng bài chỉ có chữ, test đăng bài chỉ có ảnh, test bài có cả chữ và ảnh; bắt lỗi khi FastAPI server chưa bật.
- [ ] **Ánh Dương (BE Lead):** `[D9-AD]` Tối ưu SQLite Query Indexes cho các câu truy vấn phức tạp (lọc bài viết theo `author_id` sắp xếp theo `created_at DESC`).
- [ ] **Minh Đức (BE):** `[D9-MD]` Kiểm tra dung lượng ảnh tải lên thư mục `static/uploads/`, bổ sung xử lý nén ảnh bitmap trước khi upload để tiết kiệm băng thông.
- [ ] **Hà Đăng Huy (BE + QA):** `[D9-HDH]` Bắt đầu viết Unit Test cho tầng Backend: tạo file `test_api.py` sử dụng `pytest` và `TestClient` để kiểm thử toàn diện các endpoint FastAPI.

---

### NGÀY 10 (Thứ Tư): Viết Toàn bộ Unit Test & Xử lý Trạng thái UI (Loading/Empty/Error)
*Mục tiêu ngày 10: Đạt chuẩn Unit Test đồ án (Android JUnit + Backend pytest), app xử lý mượt mà khi rỗng hoặc lỗi mạng.*

- [ ] **Hà Đăng Huy (BE + QA Lead):** `[D10-HDH]` Hoàn thiện bộ Unit Test cho Data Layer: `PostRepositoryTest` (Android Mockito) và `test_api.py` (FastAPI endpoints với pytest, đạt ít nhất 8 test cases bao gồm success/error).
- [ ] **Tạ Quang Huy (FE + QA):** `[D10-TQH]` Viết bộ Unit Test cho tầng ViewModel: `CreatePostViewModelTest` (test chặn bài rỗng, test submit thành công) với `InstantTaskExecutorRule`.
- [ ] **Tùng Dương (FE Lead):** `[D10-TD]` Viết class tiện ích `PostValidator.java` và viết Unit Test `PostValidatorTest.java` kiểm tra logic đăng bài chuẩn xác 100%.
- [ ] **Minh Lương (FE):** `[D10-ML]` Bổ sung giao diện Empty State ("Chưa có bài viết nào") và Shimmer Loading cho bảng tin Feed.
- [ ] **Phúc Đức (FE):** `[D10-PD]` Bổ sung giao diện Empty State cho tab Lời mời kết bạn ("Không có lời mời nào") và tab Tìm kiếm.
- [ ] **Ánh Dương (BE Lead):** `[D10-AD]` Chạy toàn bộ test trên terminal bằng lệnh `./gradlew testDebugUnitTest` (Android) và `pytest` (Backend), xuất file báo cáo.
- [ ] **Minh Đức (BE):** `[D10-MD]` Dọn dẹp dữ liệu rác trong `facebook.db`, tạo sẵn 3 tài khoản demo đầy đủ bài viết và ảnh mẫu phục vụ chấm điểm.

---

### NGÀY 11 (Thứ Năm): Bug Hunting Day (Kiểm thử Toàn diện & Phân loại Bug)
*Mục tiêu ngày 11: Tìm ra toàn bộ lỗi còn tồn đọng, phân loại P0-P3 theo ma trận RACI và gán người fix.*

- [ ] **Hà Đăng Huy (QA Lead):** `[D11-HDH]` Thực hiện kiểm thử toàn bộ chức năng theo Master Test Plan; tạo danh sách Bug Tickets trên GitHub/Zalo kèm HTTP status code và Logcat.
- [ ] **Tạ Quang Huy (QA):** `[D11-TQH]` Kiểm thử giao diện trên máy ảo `Pixel_10a_API_37`: xoay màn hình, cuộn nhanh RecyclerView, kiểm tra font chữ, padding, màu sắc theo Design System.
- [ ] **Tùng Dương (FE Lead):** `[D11-TD]` Tiếp nhận các bug liên quan đến màn hình Tạo bài viết và Navigation, bắt đầu xử lý ngay các lỗi P0/P1.
- [ ] **Minh Lương (FE):** `[D11-ML]` Tiếp nhận các bug liên quan đến Feed, nút Like, nút Share và Comment Sheet.
- [ ] **Phúc Đức (FE):** `[D11-PD]` Tiếp nhận các bug liên quan đến Profile, hiển thị ảnh bìa, danh sách bạn bè và thanh tìm kiếm.
- [ ] **Ánh Dương (BE Lead):** `[D11-AD]` Tiếp nhận các bug liên quan đến dữ liệu FastAPI, SQLite lock, và tính toán sai số lượt Like.
- [ ] **Minh Đức (BE):** `[D11-MD]` Tiếp nhận các bug liên quan đến upload ảnh tĩnh `/static/uploads/` và gửi thông báo.

---

### NGÀY 12 (Thứ Sáu): Sửa Toàn bộ Lỗi & Tối ưu Hiệu năng (Bug Fixing Sprint)
*Mục tiêu ngày 12: Đóng sạch 100% bug Blocker (P0) và Critical (P1), không còn hiện tượng giật lag.*

- [ ] **Tùng Dương (FE Lead):** `[D12-TD]` Sửa triệt để lỗi giao diện tạo bài viết, đảm bảo nút ĐĂNG chỉ sáng khi có chữ hoặc ảnh; vô hiệu hóa nút trong khi đang tải (tránh bấm 2 lần).
- [ ] **Minh Lương (FE):** `[D12-ML]` Tối ưu RecyclerView Feed: cấu hình `setHasFixedSize(true)`, tích hợp Glide caching để cuộn mượt mà không bị giật lag.
- [ ] **Phúc Đức (FE):** `[D12-PD]` Sửa lỗi vỡ ảnh bìa trên Profile, xử lý trường hợp người dùng chưa có ảnh đăng thì hiển thị placeholder đẹp mắt.
- [ ] **Tạ Quang Huy (FE + QA):** `[D12-TQH]` Kiểm tra lại thông báo click: đảm bảo bấm vào thông báo nhảy chính xác tới màn hình chi tiết bài viết.
- [ ] **Ánh Dương (BE Lead):** `[D12-AD]` Kiểm tra xử lý ngoại lệ mất kết nối server (Connection Refused / Timeout) ở tầng Repository, đảm bảo app không bị crash khi FastAPI chưa bật.
- [ ] **Minh Đức (BE):** `[D12-MD]` Kiểm tra đường dẫn URL ảnh trả về từ FastAPI (`http://10.0.2.2:8000/static/uploads/...`), loại bỏ các liên kết hỏng.
- [ ] **Hà Đăng Huy (BE + QA):** `[D12-HDH]` Kiểm tra lại từng bug đã được dev sửa, xác nhận kết quả và chuyển trạng thái sang `Resolved`.

---

### NGÀY 13 (Thứ Bảy): Kiểm thử Hồi quy (Regression Test) & Hoàn thiện Báo cáo Đồ án
*Mục tiêu ngày 13: 100% Bug được đóng (Closed), hoàn thành tài liệu báo cáo Word/PDF và Slide thuyết trình.*

- [ ] **Hà Đăng Huy (QA Lead):** `[D13-HDH]` Thực hiện Regression Test toàn diện từ bước Đăng nhập -> Lướt Feed -> Like/Comment/Share -> Đăng bài -> Kết bạn -> Xem Profile; Đóng toàn bộ Bug tickets.
- [ ] **Tạ Quang Huy (QA):** `[D13-TQH]` Chạy lại lệnh `./gradlew testDebugUnitTest` xác nhận toàn bộ Unit Test pass 100%; chụp ảnh kết quả đưa vào báo cáo.
- [ ] **Tùng Dương (FE Lead):** `[D13-TD]` Viết phần Báo cáo Giao diện & Kiến trúc MVVM cho tài liệu nộp giảng viên.
- [ ] **Minh Lương (FE):** `[D13-ML]` Soạn Slide thuyết trình: Phần giới thiệu chức năng Bảng tin Home Feed và các tương tác xã hội.
- [ ] **Phúc Đức (FE):** `[D13-PD]` Soạn Slide thuyết trình: Phần chức năng Trang cá nhân và Quản lý bạn bè.
- [ ] **Ánh Dương (BE Lead):** `[D13-AD]` Viết phần Báo cáo Kiến trúc Backend Python FastAPI, Cơ sở dữ liệu SQLite và ERD Diagram.
- [ ] **Minh Đức (BE):** `[D13-MD]` Chuẩn bị kịch bản dữ liệu demo với tài khoản mẫu có sẵn nhiều bài viết và hình ảnh trực quan trong SQLite.

---

### NGÀY 14 (Chủ Nhật): Tổng duyệt Demo trên Pixel 10a, Đóng gói APK Release & Sẵn sàng Bảo vệ
*Mục tiêu ngày 14: Xuất file app-release.apk, merge master, sẵn sàng 100% cho buổi chấm điểm.*

- [ ] **Tùng Dương (FE Lead):** `[D14-TD]` Tạo Pull Request cuối cùng merge toàn bộ nhánh `develop` vào nhánh `master`; tạo Git Tag `v1.0.0-final`.
- [ ] **Ánh Dương (BE Lead):** `[D14-AD]` Thực hiện đóng gói ứng dụng (Generate Signed Bundle / APK) tạo file `app-release.apk` tại `app/release/`.
- [ ] **Hà Đăng Huy (BE + QA Lead):** `[D14-HDH]` Cài đặt file `app-release.apk` lên máy ảo `Pixel_10a_API_37` và thiết bị thật để kiểm tra lần cuối.
- [ ] **Tạ Quang Huy (FE + QA):** `[D14-TQH]` Quay video màn hình demo chạy ứng dụng trên máy ảo Pixel 10a (dự phòng trường hợp máy chiếu bị lỗi).
- [ ] **Minh Lương (FE):** `[D14-ML]` Rà soát lại slide thuyết trình, khớp từng tính năng với đặc tả `Mobile App.pdf`.
- [ ] **Phúc Đức (FE):** `[D14-PD]` Kiểm tra in ấn tài liệu báo cáo đồ án (nếu giảng viên yêu cầu nộp bản cứng).
- [ ] **Minh Đức (BE):** `[D14-MD]` Đảm bảo server Python FastAPI và file `facebook.db` sẵn sàng khởi chạy mượt mà ngay khi giảng viên yêu cầu demo.
- [ ] **CẢ 7 THÀNH VIÊN:** `[D14-ALL]` Chạy tổng duyệt kịch bản thuyết trình nhóm (Rehearsal) - **HOÀN THÀNH 100% ĐỒ ÁN! **
