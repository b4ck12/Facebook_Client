# 📌 FACEBOOK CLIENT - BẢNG ĐIỀU KHIỂN DỰ ÁN 2 TUẦN (14 NGÀY THẦN TỐC)

> **Mô hình:** Android Mobile App (Java 17 + XML, MVVM Clean Architecture)  
> **Nhóm thực hiện:** 7 thành viên  
> **Thời gian:** 14 ngày (2 tuần)  
> **Môi trường:** `compileSdk 37`, `targetSdk 37`, `minSdk 24`, Máy ảo chuẩn **Google Pixel 10a (API 37.2)**  
> **Bộ tài liệu kỹ thuật hoàn chỉnh:** [docs/INDEX.md](./docs/INDEX.md)

---

## 👥 DANH SÁCH 7 THÀNH VIÊN & VAI TRÒ CHUẨN CÔNG TY

| Thành viên | Vai trò chính | Vai trò kiêm nhiệm | Chức danh chuẩn |
| :--- | :--- | :--- | :--- |
| **Ánh Dương** | Back-end | Kiến trúc CSDL | **Lead Backend Engineer & Database Architect** |
| **Minh Đức** | Back-end | Quản trị Cloud | **Backend Engineer & Cloud/Storage Specialist** |
| **Hà Đăng Huy** | Back-end | Kiểm thử (QA) | **Backend Engineer & QA Lead / API Tester** |
| **Tạ Quang Huy** | Front-end | Kiểm thử (QA) | **Frontend Engineer & Mobile QA / UI Tester** |
| **Tùng Dương** | Front-end | Điều phối UI | **Lead Frontend Engineer & UI/UX Coordinator** |
| **Minh Lương** | Front-end | Feed & Social | **Frontend Engineer (Feed & Social Interactions)** |
| **Phúc Đức** | Front-end | Profile & Friends | **Frontend Engineer (Profile & Friends System)** |

---

## 📅 BẢNG THEO DÕI TIẾN ĐỘ TỪNG NGÀY - TỪNG THÀNH VIÊN (14 NGÀY)
*(Hướng dẫn: Khi hoàn thành task, đổi `[ ]` thành `[x]` để tick hoàn thành)*

### 🗓️ TUẦN 1 (SPRINT 1: SETUP, CONTRACT-FIRST & MOCK DATA)

#### 📍 Ngày 01 (Thứ Hai): Khởi tạo, Cài đặt môi trường & Thống nhất DTO
- [ ] `[D1-TD]` **Tùng Dương:** Khởi tạo Git Repo, cấu hình SDK 37, package `com.facebook.client`, đẩy nhánh `develop`.
- [ ] `[D1-AD]` **Ánh Dương:** Thống nhất Model DTO (`User`, `Post`, `Comment`, `Notification`) và Interface Repositories.
- [ ] `[D1-MD]` **Minh Đức:** Khởi tạo Firebase Project, kích hoạt Auth, Firestore và Cloud Storage.
- [ ] `[D1-HDH]` **Hà Đăng Huy:** Cài đặt Java 17, Git, lập Master Test Plan.
- [ ] `[D1-ML]` **Minh Lương:** Cài đặt Java 17, Android Studio, tạo máy ảo Pixel 10a, clone repo.
- [ ] `[D1-PD]` **Phúc Đức:** Cài đặt Java 17, Android Studio, tạo máy ảo Pixel 10a, clone repo.
- [ ] `[D1-TQH]` **Tạ Quang Huy:** Cài đặt máy ảo Pixel 10a, chuẩn bị bộ Vector Icons Facebook chuẩn.

#### 📍 Ngày 02 (Thứ Ba): Dựng Khung Base, Theme Màu & Dữ liệu Mock
- [ ] `[D2-TD]` **Tùng Dương:** Xây dựng BaseActivity, BaseFragment, Theme Facebook, `colors.xml`, `dimens.xml`.
- [ ] `[D2-AD]` **Ánh Dương:** Viết file `firestore.rules`, cấu trúc các collection Firestore.
- [ ] `[D2-MD]` **Minh Đức:** Viết `storage.rules`, cấu hình upload ảnh đại diện / bài viết.
- [ ] `[D2-HDH]` **Hà Đăng Huy:** Xây dựng `MockDataGenerator.java` sinh sẵn dữ liệu mẫu cho FE.
- [ ] `[D2-ML]` **Minh Lương:** Tạo `MockPostRepository.java` trả về danh sách bài viết giả lập.
- [ ] `[D2-PD]` **Phúc Đức:** Tạo `MockFriendRepository.java` trả về danh sách bạn bè mẫu.
- [ ] `[D2-TQH]` **Tạ Quang Huy:** Tạo `MockNotificationRepository.java`, import icon vào drawable.

#### 📍 Ngày 03 (Thứ Tư): Khung Điều hướng 4 Tab & Giao diện Thẻ Bài viết
- [ ] `[D3-TD]` **Tùng Dương:** Dựng `MainActivity` với `BottomNavigationView` 4 tab (Feed, Friends, Notifications, Profile).
- [ ] `[D3-ML]` **Minh Lương:** Thiết kế layout `item_post.xml` (Header avatar, text nội dung, media, action bar).
- [ ] `[D3-PD]` **Phúc Đức:** Thiết kế `fragment_friends.xml` và `item_friend_request.xml` (Confirm/Delete).
- [ ] `[D3-TQH]` **Tạ Quang Huy:** Thiết kế `fragment_notifications.xml` và `item_notification.xml`.
- [ ] `[D3-AD]` **Ánh Dương:** Viết `PostRepositoryImpl.java`: triển khai `getFeedPosts()`.
- [ ] `[D3-MD]` **Minh Đức:** Viết `ProfileRepositoryImpl.java`: triển khai `getUserProfile()`.
- [ ] `[D3-HDH]` **Hà Đăng Huy:** Viết `FriendRepositoryImpl.java`: triển khai `getFriendRequests()`.

#### 📍 Ngày 04 (Thứ Năm): Tương tác Like/Share, Form Đăng bài & Friends UI
- [ ] `[D4-TD]` **Tùng Dương:** Dựng màn hình `activity_create_post.xml` (ô nhập text, nút đính kèm ảnh duy nhất).
- [ ] `[D4-ML]` **Minh Lương:** Viết `PostAdapter.java`, xử lý sự kiện click Like (Optimistic UI đổi màu xanh ngay).
- [ ] `[D4-PD]` **Phúc Đức:** Viết `FriendRequestAdapter.java`, xử lý click Chấp nhận và Xóa lời mời kết bạn.
- [ ] `[D4-TQH]` **Tạ Quang Huy:** Viết `NotificationAdapter.java`, xử lý trạng thái đã đọc / chưa đọc.
- [ ] `[D4-AD]` **Ánh Dương:** Viết hàm `toggleLike()` trong `PostRepositoryImpl`.
- [ ] `[D4-MD]` **Minh Đức:** Viết hàm tải ảnh lên Storage và lưu link ảnh đại diện.
- [ ] `[D4-HDH]` **Hà Đăng Huy:** Viết hàm `acceptFriendRequest()` và `rejectFriendRequest()`.

#### 📍 Ngày 05 (Thứ Sáu): Validation Đăng bài, Comment Text-only & Profile Layout
- [ ] `[D5-TD]` **Tùng Dương:** Xử lý chọn 1 ảnh từ thư viện, validate **khóa nút ĐĂNG nếu cả text và ảnh đều rỗng**.
- [ ] `[D5-ML]` **Minh Lương:** Dựng `bottom_sheet_comments.xml` text-only (**không reply, không react, không ảnh/sticker**).
- [ ] `[D5-PD]` **Phúc Đức:** Dựng `fragment_profile.xml` (Cover 200dp, Avatar tròn 120dp, Tên, TabLayout).
- [ ] `[D5-TQH]` **Tạ Quang Huy:** Thiết kế `item_photo_grid.xml` và `PhotoGridAdapter.java` lưới 3 cột cho Profile.
- [ ] `[D5-AD]` **Ánh Dương:** Viết hàm `createPost()` và hàm `sharePost()` trong `PostRepositoryImpl`.
- [ ] `[D5-MD]` **Minh Đức:** Viết hàm `getUserPosts()` và `getUserPhotos()` lọc bài có ảnh.
- [ ] `[D5-HDH]` **Hà Đăng Huy:** Viết hàm `searchFriends()` tìm kiếm bạn bè theo tên.

#### 📍 Ngày 06 (Thứ Bảy): Hoàn thiện 100% Giao diện Mock & Test Độc lập
- [ ] `[D6-TD]` **Tùng Dương:** Rà soát chuyển tab, hiển thị bài viết mới tạo lên đầu Feed.
- [ ] `[D6-ML]` **Minh Lương:** Kết nối `CommentViewModel`, gửi comment hiển thị ngay; làm AlertDialog Chia sẻ ngay.
- [ ] `[D6-PD]` **Phúc Đức:** Dựng màn hình `SearchFriendsActivity.xml`, ô tìm kiếm debounce 300ms.
- [ ] `[D6-TQH]` **Tạ Quang Huy:** Ghép PhotoGrid vào Tab Photos của Profile; chạy test toàn bộ UI trên máy ảo Pixel 10a.
- [ ] `[D6-AD]` **Ánh Dương:** Viết hàm `addComment()` text-only trong `PostRepositoryImpl`.
- [ ] `[D6-MD]` **Minh Đức:** Viết trigger tạo `Notification` khi có tương tác Like / Comment.
- [ ] `[D6-HDH]` **Hà Đăng Huy:** Rà soát Firestore queries, chuẩn bị dữ liệu test trên Firebase Console.

#### 📍 Ngày 07 (Chủ Nhật): Review Checkpoint Tuần 1 & Merge vào nhánh `develop`
- [ ] `[D7-TD]` **Tùng Dương:** Review PR của nhóm FE, merge vào `develop`.
- [ ] `[D7-AD]` **Ánh Dương:** Review PR của nhóm BE, merge vào `develop`.
- [ ] `[D7-ML]` **Minh Lương:** Giải quyết xung đột giao diện, xác nhận build xanh.
- [ ] `[D7-PD]` **Phúc Đức:** Đồng bộ nhánh `develop`, kiểm tra Friends và Profile.
- [ ] `[D7-MD]` **Minh Đức:** Kiểm tra kết nối Firebase trên nhánh `develop`.
- [ ] `[D7-HDH]` **Hà Đăng Huy:** Báo cáo kiểm thử Tuần 1: 100% màn hình UI hoạt động hoàn hảo với Mock Data.
- [ ] `[D7-TQH]` **Tạ Quang Huy:** Build APK test lần 1 trên máy ảo Pixel 10a, demo cả nhóm.

---

### 🗓️ TUẦN 2 (SPRINT 2: TÍCH HỢP CLOUD, UNIT TEST, SỬA LỖI & NGHIỆM THU)

#### 📍 Ngày 08 (Thứ Hai): Tích hợp Real Backend vào Feed & Đăng bài
- [ ] `[D8-TD]` **Tùng Dương:** Ghép `PostRepositoryImpl` vào CreatePost, upload ảnh Cloud Storage thật.
- [ ] `[D8-ML]` **Minh Lương:** Chuyển `FeedViewModel` sang dùng `PostRepositoryImpl`, lấy bài viết thật từ Firestore.
- [ ] `[D8-AD]` **Ánh Dương:** Hỗ trợ debug kết nối Firestore cho Feed và CreatePost.
- [ ] `[D8-PD]` **Phúc Đức:** Ghép `FriendRepositoryImpl` vào `FriendsViewModel`.
- [ ] `[D8-MD]` **Minh Đức:** Hỗ trợ debug dữ liệu bạn bè và phân quyền security rules.
- [ ] `[D8-HDH]` **Hà Đăng Huy:** Test độc lập API kết bạn trên 2 tài khoản demo.
- [ ] `[D8-TQH]` **Tạ Quang Huy:** Ghép `NotificationRepositoryImpl` vào Notifications.

#### 📍 Ngày 09 (Thứ Ba): Tích hợp Real Backend vào Profile & Bạn bè
- [ ] `[D9-PD]` **Phúc Đức:** Hoàn tất tích hợp dữ liệu thật cho Profile: ảnh bìa, avatar, bài viết cá nhân.
- [ ] `[D9-TQH]` **Tạ Quang Huy:** Hoàn tất tích hợp Tab "Ảnh & Video" trong Profile dạng lưới 3 cột.
- [ ] `[D9-ML]` **Minh Lương:** Tích hợp gửi comment văn bản thật lên Firestore.
- [ ] `[D9-TD]` **Tùng Dương:** Kiểm tra luồng Đăng bài: test đăng chỉ text, chỉ ảnh, cả hai; xử lý mất mạng.
- [ ] `[D9-AD]` **Ánh Dương:** Tối ưu Firestore Query Indexes cho các câu truy vấn phức tạp.
- [ ] `[D9-MD]` **Minh Đức:** Nén ảnh bitmap trước khi upload Cloud Storage để tiết kiệm dung lượng.
- [ ] `[D9-HDH]` **Hà Đăng Huy:** Bắt đầu viết Unit Test cho `PostRepository` với Mockito.

#### 📍 Ngày 10 (Thứ Tư): Viết Toàn bộ Unit Test & Xử lý Trạng thái UI
- [ ] `[D10-HDH]` **Hà Đăng Huy:** Hoàn thiện Unit Test cho Data Layer (`PostRepositoryTest`, `FriendRepositoryTest`).
- [ ] `[D10-TQH]` **Tạ Quang Huy:** Viết Unit Test cho `CreatePostViewModelTest` với `InstantTaskExecutorRule`.
- [ ] `[D10-TD]` **Tùng Dương:** Viết `PostValidator.java` và `PostValidatorTest.java` test chặn bài rỗng.
- [ ] `[D10-ML]` **Minh Lương:** Bổ sung Empty State và Shimmer Loading cho Feed.
- [ ] `[D10-PD]` **Phúc Đức:** Bổ sung Empty State cho danh sách Lời mời kết bạn và Tìm kiếm.
- [ ] `[D10-AD]` **Ánh Dương:** Chạy `./gradlew testDebugUnitTest`, xuất file báo cáo HTML.
- [ ] `[D10-MD]` **Minh Đức:** Tạo sẵn 3 tài khoản demo có dữ liệu phục vụ chấm điểm.

#### 📍 Ngày 11 (Thứ Năm): Bug Hunting Day (Kiểm thử Toàn diện & Phân loại Bug)
- [ ] `[D11-HDH]` **Hà Đăng Huy:** Kiểm thử toàn bộ chức năng, tạo danh sách Bug Ticket kèm logcat.
- [ ] `[D11-TQH]` **Tạ Quang Huy:** Kiểm thử giao diện trên máy ảo Pixel 10a (xoay màn hình, cuộn nhanh, font chữ).
- [ ] `[D11-TD]` **Tùng Dương:** Nhận và sửa bug màn hình Tạo bài viết và Navigation.
- [ ] `[D11-ML]` **Minh Lương:** Nhận và sửa bug Feed, Like, Share, Comment Sheet.
- [ ] `[D11-PD]` **Phúc Đức:** Nhận và sửa bug Profile, hiển thị ảnh bìa, danh sách bạn bè.
- [ ] `[D11-AD]` **Ánh Dương:** Nhận và sửa bug dữ liệu Firestore, security rules.
- [ ] `[D11-MD]` **Minh Đức:** Nhận và sửa bug upload ảnh Cloud Storage và gửi thông báo.

#### 📍 Ngày 12 (Thứ Sáu): Sửa Toàn bộ Lỗi & Tối ưu Hiệu năng
- [ ] `[D12-TD]` **Tùng Dương:** Sửa triệt để validation đăng bài, chống bấm đúp khi đang tải.
- [ ] `[D12-ML]` **Minh Lương:** Tối ưu RecyclerView Feed: Glide caching chống giật lag.
- [ ] `[D12-PD]` **Phúc Đức:** Sửa lỗi vỡ ảnh bìa Profile, hiển thị placeholder ảnh đẹp mắt.
- [ ] `[D12-TQH]` **Tạ Quang Huy:** Kiểm tra click notification điều hướng chính xác bài viết.
- [ ] `[D12-AD]` **Ánh Dương:** Xử lý ngoại lệ mất kết nối mạng ở tầng Repository, chống crash app.
- [ ] `[D12-MD]` **Minh Đức:** Kiểm tra URL ảnh trả về từ Firebase Storage.
- [ ] `[D12-HDH]` **Hà Đăng Huy:** Verify lại các bug đã được sửa, chuyển trạng thái `Resolved`.

#### 📍 Ngày 13 (Thứ Bảy): Kiểm thử Hồi quy & Hoàn thiện Báo cáo Đồ án
- [ ] `[D13-HDH]` **Hà Đăng Huy:** Regression Test toàn diện từ Đăng nhập -> Feed -> Like/Comment -> Bạn bè -> Profile; Đóng toàn bộ bug.
- [ ] `[D13-TQH]` **Tạ Quang Huy:** Chạy lại `./gradlew testDebugUnitTest` xác nhận pass 100%.
- [ ] `[D13-TD]` **Tùng Dương:** Viết báo cáo Giao diện & Kiến trúc MVVM.
- [ ] `[D13-ML]` **Minh Lương:** Soạn Slide thuyết trình: Phần chức năng Bảng tin Home Feed.
- [ ] `[D13-PD]` **Phúc Đức:** Soạn Slide thuyết trình: Phần chức năng Profile & Bạn bè.
- [ ] `[D13-AD]` **Ánh Dương:** Viết báo cáo Kiến trúc CSDL Cloud Firestore & ERD.
- [ ] `[D13-MD]` **Minh Đức:** Chuẩn bị kịch bản dữ liệu demo trực quan.

#### 📍 Ngày 14 (Chủ Nhật): Tổng duyệt Demo trên Pixel 10a, Xuất APK & Sẵn sàng Bảo vệ
- [ ] `[D14-TD]` **Tùng Dương:** Merge nhánh `develop` vào `master`, tạo Git Tag `v1.0.0-final`.
- [ ] `[D14-AD]` **Ánh Dương:** Đóng gói xuất file `app-release.apk` tại `app/release/`.
- [ ] `[D14-HDH]` **Hà Đăng Huy:** Cài đặt file `app-release.apk` lên máy ảo Pixel 10a test lần cuối.
- [ ] `[D14-TQH]` **Tạ Quang Huy:** Quay video màn hình demo chạy trên máy ảo Pixel 10a dự phòng.
- [ ] `[D14-ML]` **Minh Lương:** Rà soát khớp slide với đặc tả `Mobile App.pdf`.
- [ ] `[D14-PD]` **Phúc Đức:** In ấn tài liệu báo cáo nộp giảng viên.
- [ ] `[D14-MD]` **Minh Đức:** Đảm bảo Cloud Server sẵn sàng cho buổi chấm điểm.
- [ ] `[D14-ALL]` **CẢ 7 THÀNH VIÊN:** Chạy tổng duyệt thuyết trình - **HOÀN THÀNH XUẤT SẮC ĐỒ ÁN! 🎉**

---

## 📖 HỆ THỐNG TÀI LIỆU CHI TIẾT (DOCS FOLDER)

1. [docs/01_OVERVIEW.md](./docs/01_OVERVIEW.md) - Tổng quan & Đối chiếu 100% đặc tả PDF
2. [docs/02_ARCHITECTURE.md](./docs/02_ARCHITECTURE.md) - Kiến trúc MVVM + Clean Architecture, Modular theo Feature
3. [docs/03_DESIGN_SYSTEM.md](./docs/03_DESIGN_SYSTEM.md) - Hệ thống thiết kế UI/UX & DESIGN.md
4. [docs/04_USER_FLOW.md](./docs/04_USER_FLOW.md) - Sơ đồ luồng người dùng (Mermaid Flowchart & Sequence Diagrams)
5. [docs/05_TEAM_ROLES_WORKLOAD.md](./docs/05_TEAM_ROLES_WORKLOAD.md) - Cơ cấu 7 nhân sự, cân bằng 20 Story Points mỗi người
6. [docs/06_TASKS_SPRINT_PLAN.md](./docs/06_TASKS_SPRINT_PLAN.md) - Kế hoạch 2 tuần (14 ngày) chi tiết từng người từng ngày
7. [docs/07_GIT_WORKFLOW_NAMING.md](./docs/07_GIT_WORKFLOW_NAMING.md) - Quy trình Git & Quy chuẩn đặt tên toàn diện Java/XML
8. [docs/08_CODING_CONVENTION_DOCS.md](./docs/08_CODING_CONVENTION_DOCS.md) - Tiêu chuẩn code Google Java, ViewBinding & Javadoc
9. [docs/09_BACKEND_FREE_SERVER.md](./docs/09_BACKEND_FREE_SERVER.md) - Server & Cloud Database miễn phí (Firebase)
10. [docs/10_TESTING_GUIDE.md](./docs/10_TESTING_GUIDE.md) - Hướng dẫn viết Unit Test (JUnit 4 + Mockito) có code mẫu Java
11. [docs/11_BUG_TRIAGE_PROCESS.md](./docs/11_BUG_TRIAGE_PROCESS.md) - Quy trình quản lý lỗi Bug Triage, ma trận RACI ai fix lỗi gì
12. [docs/12_ENVIRONMENT_SETUP.md](./docs/12_ENVIRONMENT_SETUP.md) - Hướng dẫn cài đặt Java 17, Clone Git, Package & Máy ảo Pixel 10a