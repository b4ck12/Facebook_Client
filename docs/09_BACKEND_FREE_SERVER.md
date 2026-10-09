# ☁️ 09. GIẢI PHÁP SERVER & DATABASE MIỄN PHÍ (BACKEND & FREE CLOUD INFRASTRUCTURE)

---

## 1. KHẢO SÁT & LỰA CHỌN GIẢI PHÁP SERVER / DATABASE MIỄN PHÍ

Đối với đồ án bài tập lớn đại học, mục tiêu là tìm kiếm một hệ thống Backend **ổn định 100%, miễn phí trọn đời (Free Tier), không cần thẻ tín dụng quốc tế (No Credit Card required)**, và hỗ trợ lưu trữ cả cơ sở dữ liệu lẫn hình ảnh người dùng.

### Bảng So sánh Các Giải pháp Cloud Miễn phí Hàng đầu:

| Tiêu chí | Phương án 1: Google Firebase (Khuyên dùng số 1) | Phương án 2: Supabase (PostgreSQL) | Phương án 3: Render + Neon DB |
| :--- | :--- | :--- | :--- |
| **Loại hình** | BaaS (Backend-as-a-Service) của Google | BaaS mã nguồn mở (PostgreSQL) | Tự dựng REST API (Node.js/Spring Boot) |
| **Tích hợp Android** | **Hoàn hảo 100%:** Có sẵn Plugin trong Android Studio | Tốt: Gọi qua REST API Retrofit / SDK | Trung bình: Phải tự code backend server |
| **Hạn mức miễn phí** | - 50,000 lượt đọc/ngày<br>- 20,000 lượt ghi/ngày<br>- 1 GB Cloud Storage (lưu ảnh) | - 500 MB Database<br>- 1 GB Storage<br>- 50,000 Active Users | - Miễn phí 750 giờ/tháng<br>- Bị ngủ đông (Sleep) sau 15 phút rảnh |
| **Lưu trữ ảnh/video** | Có sẵn **Firebase Storage** | Có sẵn **Supabase Storage** | Phải kết nối Cloudinary bên thứ 3 |
| **Độ trễ (Latency)** | Cực thấp, có sẵn cơ chế Offline Caching | Cực thấp (Region Singapore) | 30s delay khởi động khi bị ngủ |
| **Rủi ro đồ án** | **Gần như bằng 0** (chuẩn Android từ Google) | Thấp | Dễ nghẽn server vào ngày chấm thi |

> [!TIP]
> **ĐỀ XUẤT CHÍNH THỨC CỦA NHÓM:**
> - Sử dụng **Google Firebase (Firestore + Firebase Storage + Firebase Auth)** làm nền tảng chính.
> - Lý do: Được Google thiết kế chuyên biệt cho Android Studio, thư viện chính chủ viết bằng Java cực kỳ ổn định, không tốn tài nguyên dựng server độc lập, đáp ứng dư sức cho nhóm 7 người và buổi chấm đồ án của giảng viên!

---

## 2. THIẾT KẾ CƠ SỞ DỮ LIỆU CHUẨN HÓA (DATABASE SCHEMA / ERD)

Dưới đây là cấu trúc các bảng/collections dữ liệu phục vụ đầy đủ yêu cầu tại `Mobile App.pdf`:

```
+------------------+         +--------------------+         +---------------------+
|      USERS       |         |       POSTS        |         |      COMMENTS       |
+------------------+         +--------------------+         +---------------------+
| id (PK)          |<---+    | id (PK)            |<---+    | id (PK)             |
| email            |    |    | author_id (FK) ----+---|-+   | post_id (FK) -------+
| display_name     |    +----| content            |   | |   | author_id (FK)      |
| avatar_url       |         | media_url          |   | +---| content (text only) |
| cover_url        |         | created_at         |   |     | created_at          |
| created_at       |         | is_public (true)   |   |     +---------------------+
+------------------+         | likes_count        |   |
         |                   | comments_count     |   |     +---------------------+
         |                   +--------------------+   |     |      REACTIONS      |
         |                             |              |     +---------------------+
         |                             +--------------+---->| id (PK)             |
         |                                                  | post_id (FK)        |
         |                   +--------------------+         | user_id (FK)        |
         |                   |    FRIENDSHIPS     |         | created_at          |
         |                   +--------------------+         +---------------------+
         |                   | id (PK)            |
         +-------------------| requester_id (FK)  |         +---------------------+
         |                   | receiver_id (FK)   |         |    NOTIFICATIONS    |
         |                   | status (PEND/ACCP) |         +---------------------+
         |                   | created_at         |         | id (PK)             |
         |                   +--------------------+         | recipient_id (FK)   |
         +--------------------------------------------------| sender_id (FK)      |
                                                            | type (LIKE/CMT/FRND)|
                                                            | post_id (FK, opt)   |
                                                            | is_read (false)     |
                                                            | created_at          |
                                                            +---------------------+
```

### Chi tiết các Thực thể (Data DTOs):

#### 1. Entity `User`
```json
{
  "id": "usr_987654321",
  "email": "tungduong@gmail.com",
  "displayName": "Tùng Dương",
  "avatarUrl": "https://storage.googleapis.com/.../avatar_tungduong.jpg",
  "coverUrl": "https://storage.googleapis.com/.../cover_tungduong.jpg",
  "createdAt": 1728500000000
}
```

#### 2. Entity `Post`
```json
{
  "id": "post_11223344",
  "authorId": "usr_987654321",
  "authorName": "Tùng Dương",
  "authorAvatar": "https://storage.googleapis.com/.../avatar.jpg",
  "content": "Hôm nay thời tiết thật đẹp, nhóm chúng mình vừa code xong tính năng Feed!",
  "mediaUrl": "https://storage.googleapis.com/.../post_img_01.jpg",
  "isPublic": true,
  "likesCount": 15,
  "commentsCount": 3,
  "likedUserIds": ["usr_987654321", "usr_123456789"],
  "createdAt": 1728512345678
}
```

#### 3. Entity `Comment` (Chuẩn Text-only theo đặc tả)
```json
{
  "id": "cmt_556677",
  "postId": "post_11223344",
  "authorId": "usr_123456789",
  "authorName": "Minh Lương",
  "authorAvatar": "https://storage.googleapis.com/.../avatar2.jpg",
  "content": "Tuyệt vời quá Dương ơi, giao diện nhìn rất mượt!",
  "createdAt": 1728512400000
}
```

#### 4. Entity `Friendship` (Lời mời & Quan hệ bạn bè)
```json
{
  "id": "frnd_998877",
  "requesterId": "usr_123456789",
  "receiverId": "usr_987654321",
  "status": "ACCEPTED",
  "createdAt": 1728490000000
}
```
*Ghi chú trạng thái:* `PENDING` (Đang chờ duyệt lời mời), `ACCEPTED` (Đã là bạn bè), `REJECTED` (Đã từ chối).

#### 5. Entity `Notification`
```json
{
  "id": "notif_001",
  "recipientId": "usr_987654321",
  "senderId": "usr_123456789",
  "senderName": "Minh Lương",
  "senderAvatar": "https://storage.googleapis.com/.../avatar2.jpg",
  "type": "LIKE",
  "targetPostId": "post_11223344",
  "message": "Minh Lương đã thích bài viết của bạn.",
  "isRead": false,
  "createdAt": 1728512500000
}
```

---

## 3. HƯỚNG DẪN THIẾT LẬP VÀ BẢO MẬT CREDENTIALS

> [!CAUTION]
> **Quy tắc bảo mật tối cao:** Tuyệt đối không commit file cấu hình chứa Private Key của Cloud lên GitHub công khai!

1. **File `google-services.json`:**
   - Được Ánh Dương hoặc Minh Đức tải từ Firebase Console.
   - Thêm đường dẫn `app/google-services.json` vào file `.gitignore`.
   - Chia sẻ file này nội bộ cho các thành viên thông qua kênh Discord/Zalo của nhóm.
2. **Cấu hình Quyền bảo mật Firestore (Firestore Security Rules):**
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       // Mọi người dùng đều có thể đọc bài viết công khai
       match /posts/{postId} {
         allow read: if true;
         allow create, update: if request.auth != null;
         allow delete: if request.auth != null && request.auth.uid == resource.data.authorId;
       }
       // Bình luận chỉ cho phép người đã đăng nhập gửi
       match /comments/{commentId} {
         allow read: if true;
         allow create: if request.auth != null;
       }
       // Các collection còn lại
       match /{document=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```
3. **Cấu hình Cloud Storage Rules (Upload ảnh):**
   - Giới hạn kích thước ảnh tải lên tối đa là **5MB** để tiết kiệm băng thông miễn phí:
   ```javascript
   rules_version = '2';
   service firebase.storage {
     match /b/{bucket}/o {
       match /uploads/{userId}/{allPaths=**} {
         allow read: if true;
         allow write: if request.auth != null 
                      && request.resource.size < 5 * 1024 * 1024
                      && request.resource.contentType.matches('image/.*');
       }
     }
   }
   ```
