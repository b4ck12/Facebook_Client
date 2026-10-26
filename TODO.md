# FACEBOOK CLIENT - BẢNG ĐIỀU KHIỂN DỰ ÁN 2 TUẦN (14 NGÀY THẦN TỐC)

>**Mô hình:** Android Mobile App (Java 17 + XML, MVVM Clean Architecture) 
>**Backend & Database:** Python 3.10+ (FastAPI + SQLite, RESTful API qua Retrofit 2) 
>**Nhóm thực hiện:** 7 thành viên 
>**Thời gian:** 14 ngày (2 tuần) 
>**Môi trường:** `compileSdk 37`, `targetSdk 37`, `minSdk 24`, Máy ảo chuẩn **Google Pixel 10a (API 37.2)** 
>**Bộ tài liệu kỹ thuật hoàn chỉnh:** [docs/INDEX.md](./docs/INDEX.md)

---

## DANH SÁCH 7 THÀNH VIÊN & VAI TRÒ CHUẨN CÔNG TY

| Thành viên | Vai trò chính | Vai trò kiêm nhiệm | Chức danh chuẩn |
|:--- |:--- |:--- |:--- |
| **Ánh Dương** | Back-end | Kiến trúc CSDL | **Lead Backend Engineer & Database Architect** |
| **Minh Đức** | Back-end | Quản trị Media & API | **Backend Engineer & Storage/Media Specialist** |
| **Hà Đăng Huy** | Back-end | Kiểm thử (QA) | **Backend Engineer & QA Lead / API Tester** |
| **Tạ Quang Huy** | Front-end | Kiểm thử (QA) | **Frontend Engineer & Mobile QA / UI Tester** |
| **Tùng Dương** | Front-end | Điều phối UI | **Lead Frontend Engineer & UI/UX Coordinator** |
| **Minh Lương** | Front-end | Feed & Social | **Frontend Engineer (Feed & Social Interactions)** |
| **Phúc Đức** | Front-end | Profile & Friends | **Frontend Engineer (Profile & Friends System)** |

---

## BẢNG THEO DÕI TIẾN ĐỘ TỪNG NGÀY - TỪNG THÀNH VIÊN (14 NGÀY)
*(Hướng dẫn: Khi hoàn thành task, đổi `[ ]` thành `[x]` để tick hoàn thành)*

### TUẦN 1 (SPRINT 1: SETUP, CONTRACT-FIRST, MOCK DATA & FASTAPI)

#### Ngày 01 (Thứ Hai): Khởi tạo, Cài đặt môi trường & Thống nhất DTO
- [x] `[D1-TD]` **Tùng Dương:** Khởi tạo Git Repo, cấu hình SDK 37, package `com.facebook.client`, đẩy nhánh `develop`.
- [X] `[D1-AD]` **Ánh Dương:** Thống nhất Model DTO (`User`, `Post`, `Comment`, `Notification`), thiết kế DDL SQLite (`schema.sql`) và Interface Repositories.
- [x] `[D1-MD]` **Minh Đức:** Khởi tạo project FastAPI trong `backend/`, cấu hình `requirements.txt`, kết nối SQLite `facebook.db` và static storage `/static/uploads/`.
- [x] `[D1-HDH]` **Hà Đăng Huy:** Cài đặt Java 17, Python 3.10+, Git, lập Master Test Plan.
- [x] `[D1-ML]` **Minh Lương:** Cài đặt Java 17, Android Studio, tạo máy ảo Pixel 10a, clone repo.
- [x] `[D1-PD]` **Phúc Đức:** Cài đặt Java 17, Android Studio, tạo máy ảo Pixel 10a, clone repo.
- [x] `[D1-TQH]` **Tạ Quang Huy:** Cài đặt máy ảo Pixel 10a, chuẩn bị bộ Vector Icons Facebook chuẩn.

#### Ngày 02 (Thứ Ba): Dựng Khung Base, Theme Màu & Dữ liệu Mock
- [x] `[D2-TD]` **Tùng Dương:** Xây dựng BaseActivity, BaseFragment, Theme Facebook, `colors.xml`, `dimens.xml`.
- [X] `[D2-AD]` **Ánh Dương:** Viết SQLAlchemy models (`models.py`) và Pydantic schemas (`schemas.py`), tạo các bảng SQLite.
- [x] `[D2-MD]` **Minh Đức:** Viết endpoint `POST /api/upload` tiếp nhận ảnh multipart, lưu vào `static/uploads/` và trả URL ảnh.
- [x] `[D2-HDH]` **Hà Đăng Huy:** Xây dựng script `seed_data.py` (chèn dữ liệu mẫu SQLite) và `MockDataGenerator.java` cho FE.
- [x] `[D2-ML]` **Minh Lương:** Tạo `MockPostRepository.java` trả về danh sách bài viết giả lập.
- [x] `[D2-PD]` **Phúc Đức:** Hoàn thiện `MockFriendRepository.java`, đồng bộ dữ liệu bạn bè từ `MockDataGenerator`.
- [x] `[D2-TQH]` **Tạ Quang Huy:** Tạo `MockNotificationRepository.java`, import 21 vector icons vào drawable.

#### Ngày 03 (Thứ Tư): Khung Điều hướng 4 Tab & Giao diện Thẻ Bài viết
- [x] `[D3-TD]` **Tùng Dương:** Dựng `MainActivity` với `BottomNavigationView` 4 tab (Feed, Friends, Notifications, Profile).
- [x] `[D3-ML]` **Minh Lương:** Thiết kế layout `item_post.xml` (Header avatar, text nội dung, media, action bar).
- [ ] `[D3-PD]` **Phúc Đức:** Thiết kế `fragment_friends.xml` và `item_friend_request.xml` (Confirm/Delete).
- [x] `[D3-TQH]` **Tạ Quang Huy:** Thiết kế `fragment_notifications.xml` và `item_notification.xml`.
- [X] `[D3-AD]` **Ánh Dương:** Viết router `posts.py` trên FastAPI: triển khai endpoint `GET /api/posts` truy vấn SQLite.
- [x] `[D3-MD]` **Minh Đức:** Viết router `users.py` trên FastAPI: triển khai endpoint `GET /api/users/{id}` lấy thông tin cá nhân.
- [x] `[D3-HDH]` **Hà Đăng Huy:** Viết router `friends.py` trên FastAPI: triển khai endpoint `GET /api/friends/requests`.

#### Ngày 04 (Thứ Năm): Tương tác Like/Share, Form Đăng bài & Friends UI
- [x] `[D4-TD]` **Tùng Dương:** Dựng màn hình `activity_create_post.xml` (ô nhập text, nút đính kèm ảnh duy nhất).
- [x] `[D4-ML]` **Minh Lương:** Viết `PostAdapter.java`, xử lý sự kiện click Like (Optimistic UI đổi màu xanh ngay).
- [ ] `[D4-PD]` **Phúc Đức:** Viết `FriendRequestAdapter.java`, xử lý click Chấp nhận và Xóa lời mời kết bạn.
- [x] `[D4-TQH]` **Tạ Quang Huy:** Viết `NotificationAdapter.java`, xử lý trạng thái đã đọc / chưa đọc.
- [X] `[D4-AD]` **Ánh Dương:** Viết endpoint `POST /api/posts/{id}/like` trên FastAPI xử lý toggle like trong bảng `post_likes`.
- [x] `[D4-MD]` **Minh Đức:** Viết endpoint `PUT /api/users/{id}` trên FastAPI cập nhật `avatar_url` và `cover_url`.
- [x] `[D4-HDH]` **Hà Đăng Huy:** Viết endpoint `POST /api/friends/accept` và `POST /api/friends/reject` cập nhật trạng thái trong SQLite.

#### Ngày 05 (Thứ Sáu): Validation Đăng bài, Comment Text-only & Profile Layout
- [x] `[D5-TD]` **Tùng Dương:** Xử lý chọn 1 ảnh từ thư viện bằng ActivityResultContracts, validate **khóa nút ĐĂNG nếu cả text và ảnh đều rỗng**.
- [x] `[D5-ML]` **Minh Lương:** Dựng `bottom_sheet_comments.xml` text-only (**không reply, không react, không ảnh/sticker**).
- [ ] `[D5-PD]` **Phúc Đức:** Dựng `fragment_profile.xml` (Cover 200dp, Avatar tròn 120dp, Tên, TabLayout).
- [x] `[D5-TQH]` **Tạ Quang Huy:** Thiết kế `item_photo_grid.xml` và `PhotoGridAdapter.java` lưới 3 cột cho Profile.
- [X] `[D5-AD]` **Ánh Dương:** Viết endpoint `POST /api/posts` (tạo bài viết) và `POST /api/posts/{id}/share` (chia sẻ bài viết).
- [x] `[D5-MD]` **Minh Đức:** Viết endpoint `GET /api/users/{id}/posts` và `GET /api/users/{id}/photos` lọc bài có ảnh.
- [x] `[D5-HDH]` **Hà Đăng Huy:** Viết endpoint `GET /api/friends/search?q=` tìm kiếm bạn bè theo tên trong SQLite.

#### Ngày 06 (Thứ Bảy): Hoàn thiện 100% Giao diện Mock & Test Độc lập
- [x] `[D6-TD]` **Tùng Dương:** Xây dựng màn hình Đăng nhập 1 chạm (`AccountChooserActivity`), quản lý phiên `SessionManager` và hộp thoại Đổi tài khoản Demo.
- [x] `[D6-ML]` **Minh Lương:** Kết nối `CommentViewModel`, gửi comment hiển thị ngay; làm AlertDialog Chia sẻ ngay.
- [ ] `[D6-PD]` **Phúc Đức:** Dựng màn hình `SearchFriendsActivity.xml`, ô tìm kiếm debounce 300ms.
- [X] `[D6-TQH]` **Tạ Quang Huy:** Ghép PhotoGrid vào Tab Photos của Profile; chạy test toàn bộ UI trên máy ảo Pixel 10a.
- [X] `[D6-AD]` **Ánh Dương:** Viết router `comments.py` trên FastAPI: triển khai `GET /api/posts/{id}/comments` và `POST /api/posts/{id}/comments` text-only.
- [x] `[D6-MD]` **Minh Đức:** Viết logic tự động tạo `Notification` trong SQLite khi có tương tác Like / Comment.
- [ ] `[D6-HDH]` **Hà Đăng Huy:** Rà soát Swagger UI (`/docs`), chuẩn bị dữ liệu test mẫu trong `facebook.db`.

#### Ngày 07 (Chủ Nhật): Review Checkpoint Tuần 1 & Merge vào nhánh `develop`
- [ ] `[D7-TD]` **Tùng Dương:** Review PR của nhóm FE, merge vào `develop`.
- [x] `[D7-AD]` **Ánh Dương:** Review PR của nhóm BE, kiểm tra máy chủ FastAPI và file `facebook.db`, merge vào `develop`.
- [x] `[D7-ML]` **Minh Lương:** Giải quyết xung đột giao diện, xác nhận build xanh.
- [ ] `[D7-PD]` **Phúc Đức:** Đồng bộ nhánh `develop`, kiểm tra Friends và Profile.
- [x] `[D7-MD]` **Minh Đức:** Kiểm tra thư mục upload ảnh `/static/uploads/` và file `facebook.db` trên `develop`.
- [ ] `[D7-HDH]` **Hà Đăng Huy:** Báo cáo kiểm thử Tuần 1: 100% màn hình UI hoạt động hoàn hảo và 100% API FastAPI sẵn sàng trên Swagger UI.
- [x] `[D7-TQH]` **Tạ Quang Huy:** Build APK test lần 1 trên máy ảo Pixel 10a, demo cả nhóm.

---

### TUẦN 2 (SPRINT 2: TÍCH HỢP FASTAPI + SQLITE, UNIT TEST, SỬA LỖI & NGHIỆM THU)

#### Ngày 08 (Thứ Hai): Tích hợp Real Backend vào Feed & Đăng bài
- [ ] `[D8-TD]` **Tùng Dương:** Thiết lập Core Network: thêm Retrofit/Gson/OkHttp vào `app/build.gradle`, tạo `network_security_config.xml` cho `10.0.2.2:8000`, quyền `INTERNET` trong `AndroidManifest.xml`, viết `ApiClient`, `ApiService`, `Resource`; ghép `PostRepositoryImpl` vào CreatePost (upload ảnh & tạo bài).
- [x] `[D8-ML]` **Minh Lương:** Chuyển `FeedViewModel` sang dùng `PostRepositoryImpl`, lấy bài viết thật từ FastAPI + SQLite (`GET /api/posts`); kiểm tra Like đồng bộ lên server.
- [X] `[D8-AD]` **Ánh Dương:** Hỗ trợ debug kết nối Retrofit với FastAPI, cấu hình CORS Middleware và log request Uvicorn.
- [ ] `[D8-PD]` **Phúc Đức:** Ghép `FriendRepositoryImpl` vào `FriendsViewModel`, tải danh sách lời mời kết bạn từ `GET /api/friends/requests`.
- [x] `[D8-MD]` **Minh Đức:** Hỗ trợ debug dữ liệu bạn bè và câu lệnh SQL trong SQLite.
- [ ] `[D8-HDH]` **Hà Đăng Huy:** Test độc lập API kết bạn trên 2 tài khoản demo qua Postman / Swagger UI.
- [x] `[D8-TQH]` **Tạ Quang Huy:** Ghép `NotificationRepositoryImpl` vào Notifications, gọi API `GET /api/notifications` từ FastAPI server.

#### Ngày 09 (Thứ Ba): Tích hợp Real Backend vào Profile & Bạn bè
- [ ] `[D9-PD]` **Phúc Đức:** Hoàn tất tích hợp dữ liệu thật cho Profile: ảnh bìa, avatar, bài viết cá nhân từ endpoint `/api/users/{id}` và `/api/users/{id}/posts`.
- [x] `[D9-TQH]` **Tạ Quang Huy:** Hoàn tất tích hợp Tab "Ảnh & Video" trong Profile dạng lưới 3 cột qua endpoint `/api/users/{id}/photos`.
- [x] `[D9-ML]` **Minh Lương:** Tích hợp gửi comment văn bản thật lên endpoint `POST /api/posts/{id}/comments`.
- [ ] `[D9-TD]` **Tùng Dương:** Kiểm tra luồng Đăng bài: test đăng chỉ text, chỉ ảnh, cả hai; xử lý khi FastAPI server chưa bật.
- [X] `[D9-AD]` **Ánh Dương:** Tối ưu SQLite Query Indexes cho các câu truy vấn phức tạp.
- [x] `[D9-MD]` **Minh Đức:** Nén ảnh bitmap trước khi upload lên thư mục `static/uploads/` để tiết kiệm băng thông.
- [ ] `[D9-HDH]` **Hà Đăng Huy:** Bắt đầu viết Unit Test cho tầng Backend: tạo file `test_api.py` sử dụng `pytest` và `TestClient`.

#### Ngày 10 (Thứ Tư): Viết Toàn bộ Unit Test & Xử lý Trạng thái UI
- [ ] `[D10-HDH]` **Hà Đăng Huy:** Hoàn thiện Unit Test cho Data Layer: `PostRepositoryTest` (Android Mockito) và `test_api.py` (FastAPI với pytest).
- [ ] `[D10-TQH]` **Tạ Quang Huy:** Viết Unit Test cho `CreatePostViewModelTest` với `InstantTaskExecutorRule`.
- [ ] `[D10-TD]` **Tùng Dương:** Viết `PostValidator.java` và `PostValidatorTest.java` test chặn bài rỗng.
- [ ] `[D10-ML]` **Minh Lương:** Bổ sung Empty State và Shimmer Loading cho Feed.
- [ ] `[D10-PD]` **Phúc Đức:** Bổ sung Empty State cho danh sách Lời mời kết bạn và Tìm kiếm.
- [ ] `[D10-AD]` **Ánh Dương:** Chạy `./gradlew testDebugUnitTest` (Android) và `pytest` (Backend), xuất file báo cáo.
- [ ] `[D10-MD]` **Minh Đức:** Dọn dẹp dữ liệu rác trong `facebook.db`, tạo sẵn 3 tài khoản demo có dữ liệu mẫu phục vụ chấm điểm.

#### Ngày 11 (Thứ Năm): Bug Hunting Day (Kiểm thử Toàn diện & Phân loại Bug)
- [ ] `[D11-HDH]` **Hà Đăng Huy:** Kiểm thử toàn bộ chức năng, tạo danh sách Bug Ticket kèm mã lỗi HTTP và Logcat.
- [ ] `[D11-TQH]` **Tạ Quang Huy:** Kiểm thử giao diện trên máy ảo Pixel 10a (xoay màn hình, cuộn nhanh, font chữ).
- [ ] `[D11-TD]` **Tùng Dương:** Nhận và sửa bug màn hình Tạo bài viết và Navigation.
- [ ] `[D11-ML]` **Minh Lương:** Nhận và sửa bug Feed, Like, Share, Comment Sheet.
- [ ] `[D11-PD]` **Phúc Đức:** Nhận và sửa bug Profile, hiển thị ảnh bìa, danh sách bạn bè.
- [ ] `[D11-AD]` **Ánh Dương:** Nhận và sửa bug logic FastAPI, SQLite database lock, tính sai số lượt Like.
- [ ] `[D11-MD]` **Minh Đức:** Nhận và sửa bug upload ảnh tĩnh `/static/uploads/` và gửi thông báo.

#### Ngày 12 (Thứ Sáu): Sửa Toàn bộ Lỗi & Tối ưu Hiệu năng
- [ ] `[D12-TD]` **Tùng Dương:** Sửa triệt để validation đăng bài, chống bấm đúp khi đang tải.
- [ ] `[D12-ML]` **Minh Lương:** Tối ưu RecyclerView Feed: Glide caching chống giật lag.
- [ ] `[D12-PD]` **Phúc Đức:** Sửa lỗi vỡ ảnh bìa Profile, hiển thị placeholder ảnh đẹp mắt.
- [ ] `[D12-TQH]` **Tạ Quang Huy:** Kiểm tra click notification điều hướng chính xác bài viết.
- [ ] `[D12-AD]` **Ánh Dương:** Xử lý ngoại lệ mất kết nối server (Connection Refused/Timeout) ở tầng Repository, chống crash app.
- [ ] `[D12-MD]` **Minh Đức:** Kiểm tra URL ảnh trả về từ FastAPI (`http://10.0.2.2:8000/static/uploads/...`), loại bỏ liên kết hỏng.
- [ ] `[D12-HDH]` **Hà Đăng Huy:** Verify lại các bug đã được sửa, chuyển trạng thái `Resolved`.

#### Ngày 13 (Thứ Bảy): Kiểm thử Hồi quy & Hoàn thiện Báo cáo Đồ án
- [ ] `[D13-HDH]` **Hà Đăng Huy:** Regression Test toàn diện từ Đăng nhập -> Feed -> Like/Comment -> Bạn bè -> Profile; Đóng toàn bộ bug.
- [ ] `[D13-TQH]` **Tạ Quang Huy:** Chạy lại `./gradlew testDebugUnitTest` xác nhận pass 100%.
- [ ] `[D13-TD]` **Tùng Dương:** Viết báo cáo Giao diện & Kiến trúc MVVM.
- [ ] `[D13-ML]` **Minh Lương:** Soạn Slide thuyết trình: Phần chức năng Bảng tin Home Feed.
- [ ] `[D13-PD]` **Phúc Đức:** Soạn Slide thuyết trình: Phần chức năng Profile & Bạn bè.
- [ ] `[D13-AD]` **Ánh Dương:** Viết báo cáo Kiến trúc Backend Python FastAPI, Cơ sở dữ liệu SQLite & ERD.
- [ ] `[D13-MD]` **Minh Đức:** Chuẩn bị kịch bản dữ liệu demo trực quan trong SQLite.

#### Ngày 14 (Chủ Nhật): Tổng duyệt Demo trên Pixel 10a, Xuất APK & Sẵn sàng Bảo vệ
- [ ] `[D14-TD]` **Tùng Dương:** Merge nhánh `develop` vào `master`, tạo Git Tag `v1.0.0-final`.
- [ ] `[D14-AD]` **Ánh Dương:** Đóng gói xuất file `app-release.apk` tại `app/release/`.
- [ ] `[D14-HDH]` **Hà Đăng Huy:** Cài đặt file `app-release.apk` lên máy ảo Pixel 10a test lần cuối.
- [ ] `[D14-TQH]` **Tạ Quang Huy:** Quay video màn hình demo chạy trên máy ảo Pixel 10a dự phòng.
- [ ] `[D14-ML]` **Minh Lương:** Rà soát khớp slide với đặc tả `Mobile App.pdf`.
- [ ] `[D14-PD]` **Phúc Đức:** In ấn tài liệu báo cáo nộp giảng viên.
- [ ] `[D14-MD]` **Minh Đức:** Đảm bảo server Python FastAPI và file `facebook.db` sẵn sàng khởi chạy cho buổi chấm điểm.
- [ ] `[D14-ALL]` **CẢ 7 THÀNH VIÊN:** Chạy tổng duyệt thuyết trình - **HOÀN THÀNH XUẤT SẮC ĐỒ ÁN! **

---

## HỆ THỐNG TÀI LIỆU CHI TIẾT (DOCS FOLDER)

1. [docs/01_OVERVIEW.md](./docs/01_OVERVIEW.md) - Tổng quan & Đối chiếu 100% đặc tả PDF
2. [docs/02_ARCHITECTURE.md](./docs/02_ARCHITECTURE.md) - Kiến trúc MVVM + Clean Architecture, Modular theo Feature
3. [docs/03_DESIGN_SYSTEM.md](./docs/03_DESIGN_SYSTEM.md) - Hệ thống thiết kế UI/UX & DESIGN.md
4. [docs/04_USER_FLOW.md](./docs/04_USER_FLOW.md) - Sơ đồ luồng người dùng (Mermaid Flowchart & Sequence Diagrams)
5. [docs/05_TEAM_ROLES_WORKLOAD.md](./docs/05_TEAM_ROLES_WORKLOAD.md) - Cơ cấu 7 nhân sự, cân bằng 20 Story Points mỗi người
6. [docs/06_TASKS_SPRINT_PLAN.md](./docs/06_TASKS_SPRINT_PLAN.md) - Kế hoạch 2 tuần (14 ngày) chi tiết từng người từng ngày
7. [docs/07_GIT_WORKFLOW_NAMING.md](./docs/07_GIT_WORKFLOW_NAMING.md) - Quy trình Git & Quy chuẩn đặt tên toàn diện Java/XML
8. [docs/08_CODING_CONVENTION_DOCS.md](./docs/08_CODING_CONVENTION_DOCS.md) - Tiêu chuẩn code Google Java, ViewBinding & Javadoc
9. [docs/09_BACKEND_SERVER.md](./docs/09_BACKEND_SERVER.md) - Server Backend (Python FastAPI) & Database (SQLite)
10. [docs/10_TESTING_GUIDE.md](./docs/10_TESTING_GUIDE.md) - Hướng dẫn viết Unit Test (JUnit 4, Mockito & Pytest) có code mẫu
11. [docs/11_BUG_TRIAGE_PROCESS.md](./docs/11_BUG_TRIAGE_PROCESS.md) - Quy trình quản lý lỗi Bug Triage, ma trận RACI ai fix lỗi gì
12. [docs/12_ENVIRONMENT_SETUP.md](./docs/12_ENVIRONMENT_SETUP.md) - Hướng dẫn cài đặt Java 17, Python FastAPI, Clone Git & Máy ảo Pixel 10a