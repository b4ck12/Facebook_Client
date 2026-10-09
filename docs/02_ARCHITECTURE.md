# 🏗️ 02. KIẾN TRÚC HỆ THỐNG & TIÊU CHUẨN THIẾT KẾ APP (SYSTEM ARCHITECTURE)

---

## 1. MỤC TIÊU KIẾN TRÚC (ARCHITECTURAL GOALS)

Hệ thống được thiết kế dựa trên các tiêu chí cốt lõi:
1. **Dễ hiểu & Dễ học (Simplicity & Learnability):** Sử dụng các mô hình tiêu chuẩn ngành, cú pháp Java trong sáng, cấu trúc thư mục rõ ràng để mọi thành viên trong nhóm 7 người đều nắm bắt nhanh.
2. **Dễ phát triển song song (Parallel Development):** Tách bạch rõ các tầng và các tính năng, cho phép 4 lập trình viên Frontend và 3 lập trình viên Backend làm việc đồng thời mà không gây xung đột mã nguồn.
3. **Dễ bảo trì & Mở rộng (Maintainability & Extensibility):** Áp dụng nguyên lý Single Responsibility Principle (SRP) và Dependency Inversion Principle (DIP). Khi thay đổi database (ví dụ từ Mock sang Firebase hoặc Supabase), tầng giao diện không bị ảnh hưởng.
4. **Dễ viết Unit Test (Testability):** Logic nghiệp vụ được cô lập trong ViewModel và Repository, không phụ thuộc vào Android Framework Lifecycle (Activity/Context).

---

## 2. MÔ HÌNH KIẾN TRÚC TỔNG THỂ (CLEAN ARCHITECTURE + MVVM)

Hệ thống áp dụng mô hình kiến trúc phân tầng chuẩn do Google khuyến nghị cho ứng dụng Android: **MVVM (Model - View - ViewModel) kết hợp Clean Architecture**.

```
+-----------------------------------------------------------------------------------+
|                            PRESENTATION LAYER (UI)                                |
|  Activities / Fragments / Adapters (XML Data Display & User Interactions)         |
+----------------------------------------+------------------------------------------+
                                         | Quan sát (Observes LiveData/State)
                                         v
+-----------------------------------------------------------------------------------+
|                            VIEWMODEL LAYER                                        |
|  PostViewModel, FriendViewModel, ProfileViewModel, NotificationViewModel          |
|  - Giữ trạng thái UI (UI State)                                                   |
|  - Xử lý tương tác của người dùng, Validate đầu vào                              |
|  - Không chứa tham chiếu tới View, Context hoặc Android Lifecycle                |
+----------------------------------------+------------------------------------------+
                                         | Gọi hàm thực thi dữ liệu
                                         v
+-----------------------------------------------------------------------------------+
|                            DOMAIN / USECASE LAYER                                 |
|  GetFeedUseCase, CreatePostUseCase, ToggleLikeUseCase, SendFriendRequestUseCase   |
|  - Đóng gói Business Logic độc lập                                                |
|  - Tùy chọn (với module đơn giản ViewModel có thể gọi trực tiếp Repository)       |
+----------------------------------------+------------------------------------------+
                                         | Truy vấn thông qua Interface
                                         v
+-----------------------------------------------------------------------------------+
|                            DATA LAYER (REPOSITORIES)                              |
|  IPostRepository  <--- PostRepositoryImpl                                         |
|  IFriendRepository <--- FriendRepositoryImpl                                      |
+--------------------+-------------------------------------+------------------------+
                     |                                     |
                     v                                     v
+------------------------------------+   +------------------------------------------+
|      REMOTE DATA SOURCE            |   |          LOCAL DATA SOURCE               |
| Firebase Firestore / Supabase API  |   | SharedPreferences (User Session, Token)  |
| Cloud Storage (Lưu trữ ảnh/video)  |   | Room DB / In-Memory Cache (nếu có)       |
+------------------------------------+   +------------------------------------------+
```

### Chi tiết các tầng:

### 2.1. Presentation Layer (Tầng hiển thị)
- **View (Activity / Fragment):** Chỉ đảm nhận nhiệm vụ vẽ giao diện từ XML, lắng nghe sự kiện người dùng (click, scroll, text input) và ủy quyền cho ViewModel xử lý.
- **ViewBinding:** Sử dụng 100% `ViewBinding` để tương tác an toàn với các thành phần trong XML, loại bỏ hoàn toàn `findViewById` nhằm tránh lỗi `NullPointerException`.
- **UI State Pattern:** Tầng View nhận dữ liệu thông qua mô hình bao bọc trạng thái (State Wrapper):
  ```java
  public class Resource<T> {
      public enum Status { SUCCESS, ERROR, LOADING }
      public final Status status;
      public final T data;
      public final String message;
      // Constructor & static factory methods: success(), error(), loading()
  }
  ```

### 2.2. ViewModel Layer (Tầng điều phối trạng thái)
- Kế thừa từ `androidx.lifecycle.ViewModel`.
- Quản lý trạng thái màn hình bằng `LiveData` (hoặc `MutableLiveData`).
- Sống sót qua các sự kiện xoay màn hình và thay đổi cấu hình (Configuration Changes).
- **Quy tắc bất di bất dịch:** Tuyệt đối không import `android.content.Context` hoặc bất kỳ view nào (`View`, `Button`, `TextView`) vào ViewModel để tránh memory leak.

### 2.3. Data Layer (Tầng dữ liệu - Repository Pattern)
- **Repository Interface:** Định nghĩa hợp đồng trừu tượng (Contract) mà tầng Presentation yêu cầu. Ví dụ: `IPostRepository`.
- **Repository Implementation:** Thực thi giao tiếp dữ liệu cụ thể (`PostRepositoryImpl`).
- **Ưu điểm vượt trội:**
  - Trong giai đoạn đầu, tạo `MockPostRepository` trả về dữ liệu mẫu (Fake data) để Frontend làm việc ngay lập tức.
  - Khi Backend hoàn thành, chỉ cần trỏ sang `FirebasePostRepository` hoặc `SupabasePostRepository` mà không phải sửa một dòng code nào ở tầng giao diện!

---

## 3. MODULAR DESIGN THEO TÍNH NĂNG (PACKAGE-BY-FEATURE)

Dự án tổ chức mã nguồn theo hướng **Package-by-Feature kết hợp Package-by-Layer** bên trong mỗi tính năng. Đây là cấu trúc được các công ty công nghệ ưa chuộng vì tính gắn kết cao (High Cohesion) và ghép nối lỏng (Loose Coupling).

### Cấu trúc thư mục nguồn (Source Tree):

```
app/src/main/java/com/facebook/client/
│
├── core/                               <-- Các thành phần dùng chung toàn app
│   ├── base/                           <-- BaseActivity, BaseFragment, BaseViewModel
│   ├── utils/                          <-- DateUtils, ImageUtils, ValidatorUtils
│   ├── common/                         <-- Resource.java, Constants.java
│   └── network/                        <-- NetworkClient, ApiResponseHandler
│
├── data/                               <-- Quản lý dữ liệu tập trung
│   ├── model/                          <-- Data Transfer Objects (DTO) / Entities
│   │   ├── User.java
│   │   ├── Post.java
│   │   ├── Comment.java
│   │   ├── Notification.java
│   │   └── FriendRequest.java
│   │
│   ├── local/                          <-- Quản lý dữ liệu máy khách
│   │   └── PreferenceManager.java      <-- Lưu UserId, trạng thái đăng nhập
│   │
│   └── repository/                     <-- Interface & Triển khai Repository
│       ├── IPostRepository.java
│       ├── PostRepositoryImpl.java
│       ├── IFriendRepository.java
│       ├── FriendRepositoryImpl.java
│       ├── IProfileRepository.java
│       ├── ProfileRepositoryImpl.java
│       ├── INotificationRepository.java
│       └── NotificationRepositoryImpl.java
│
├── features/                           <-- Module hóa theo từng tính năng độc lập
│   ├── auth/                           <-- Đăng nhập / Chọn tài khoản demo
│   │   ├── LoginActivity.java
│   │   └── AuthViewModel.java
│   │
│   ├── feed/                           <-- Tính năng Bảng tin & Đăng bài
│   │   ├── FeedFragment.java
│   │   ├── CreatePostActivity.java
│   │   ├── PostAdapter.java
│   │   ├── PostViewHolder.java
│   │   ├── FeedViewModel.java
│   │   └── CreatePostViewModel.java
│   │
│   ├── comment/                        <-- Tính năng Bình luận bài viết
│   │   ├── CommentBottomSheetDialog.java
│   │   ├── CommentAdapter.java
│   │   └── CommentViewModel.java
│   │
│   ├── friends/                        <-- Tính năng Bạn bè & Tìm kiếm
│   │   ├── FriendsFragment.java
│   │   ├── SearchFriendsActivity.java
│   │   ├── FriendRequestAdapter.java
│   │   ├── FriendListAdapter.java
│   │   └── FriendsViewModel.java
│   │
│   ├── notifications/                  <-- Tính năng Thông báo
│   │   ├── NotificationsFragment.java
│   │   ├── NotificationAdapter.java
│   │   └── NotificationViewModel.java
│   │
│   └── profile/                        <-- Tính năng Trang cá nhân & Kho ảnh
│       ├── ProfileFragment.java
│       ├── ProfilePostsTabFragment.java
│       ├── ProfilePhotosTabFragment.java
│       ├── PhotoGridAdapter.java
│       └── ProfileViewModel.java
│
└── MainActivity.java                   <-- Chứa BottomNavigationView gắn 4 Tab chính
```

---

## 4. TỔ CHỨC TÀI NGUYÊN GIAO DIỆN (RES FOLDER STRUCTURE)

Để tránh xung đột khi merge Git, tài nguyên trong thư mục `res` được chia tách khoa học:

```
app/src/main/res/
├── layout/
│   ├── activity_main.xml
│   ├── activity_login.xml
│   ├── activity_create_post.xml
│   ├── activity_search_friends.xml
│   ├── fragment_feed.xml
│   ├── fragment_friends.xml
│   ├── fragment_notifications.xml
│   ├── fragment_profile.xml
│   ├── fragment_profile_posts.xml
│   ├── fragment_profile_photos.xml
│   ├── bottom_sheet_comments.xml
│   ├── item_post.xml                   <-- Giao diện 1 thẻ bài viết trong Feed
│   ├── item_comment.xml                <-- Giao diện 1 dòng bình luận
│   ├── item_friend_request.xml         <-- Giao diện 1 dòng lời mời kết bạn
│   ├── item_friend_list.xml            <-- Giao diện 1 người bạn
│   ├── item_notification.xml           <-- Giao diện 1 thông báo
│   └── item_photo_grid.xml             <-- Giao diện 1 ô ảnh dạng lưới
│
├── drawable/                           <-- Icons vector, Background bo góc
├── values/
│   ├── colors.xml                      <-- Bảng màu chuẩn Facebook
│   ├── strings.xml                     <-- Toàn bộ chuỗi văn bản (đa ngôn ngữ)
│   ├── dimens.xml                      <-- Kích thước khoảng cách chuẩn (dp/sp)
│   └── themes.xml                      <-- Theme Material Design
└── navigation/
    └── main_navigation.xml             <-- Điều hướng các màn hình
```

---

## 5. THƯ VIỆN & CÔNG NGHỆ BỔ TRỢ (TECH STACK SPECIFICATION)

1. **Ngôn ngữ:** Java 17 LTS (JavaVersion.VERSION_17 - bắt buộc trên toàn bộ máy).
2. **Cấu hình SDK thống nhất:**
   - `compileSdk`: **37** (Android 17.0)
   - `minSdk`: **24** (Android 7.0 Nougat)
   - `targetSdk`: **37** (Android 17.0)
   - `namespace` / `applicationId`: `com.facebook.client`
3. **Thiết bị máy ảo chuẩn:** **Google Pixel 10a** (Android 17.0 - API Level 37.2 "CinnamonBun").
4. **Nền tảng build:** Gradle 9.6 + Android Gradle Plugin (AGP) 9.4+.
5. **UI Engine:** XML Layouts kết hợp Material Components (`com.google.android.material:material:1.11.0`).
6. **Hiển thị hình ảnh mượt mà:** `Glide` (`com.github.bumptech.glide:glide:4.16.0`) - Tối ưu caching và chống Out Of Memory (OOM) khi cuộn feed chứa nhiều ảnh.
7. **Kiến trúc Android Jetpack:**
   - `androidx.lifecycle:lifecycle-viewmodel:2.7.0`
   - `androidx.lifecycle:lifecycle-livedata:2.7.0`
   - `androidx.viewbinding:viewbinding:8.3.0`
8. **Backend Cloud SDK (Miễn phí):** Firebase BOM (`com.google.firebase:firebase-bom:32.8.0`) hoặc Retrofit 2 + OkHttp 3 (nếu gọi REST API Supabase).
