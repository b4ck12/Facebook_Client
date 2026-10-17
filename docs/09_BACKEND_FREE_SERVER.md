# 🐍 09. KIẾN TRÚC SERVER BACKEND (PYTHON FASTAPI) & CƠ SỞ DỮ LIỆU (SQLITE)

---

## 1. TỔNG QUAN GIẢI PHÁP BACKEND: PYTHON FASTAPI + SQLITE

Để đáp ứng tối đa tiêu chí của đồ án môn học đại học: **đơn giản, gọn nhẹ, hoàn toàn miễn phí, hiệu năng cực cao và dễ dàng vận hành**, đội ngũ kỹ thuật quyết định lựa chọn kiến trúc Backend dựa trên **Python FastAPI** và **SQLite**:

### 1.1. Tại sao lựa chọn Python FastAPI?
- **Tốc độ & Hiệu năng cao (High Performance):** FastAPI được xây dựng trên nền tảng Starlette và Pydantic, là một trong những framework Python nhanh nhất hiện nay (ngang ngửa NodeJS và Go).
- **Tự động sinh tài liệu API (Auto-generated OpenAPI / Swagger UI):** Tự động tạo trang tài liệu kiểm thử trực quan tại `http://localhost:8000/docs` và `http://localhost:8000/redoc`. Giúp đội ngũ Frontend (Retrofit) đối chiếu dữ liệu JSON chuẩn xác 100% mà không sợ lệch hợp đồng (Contract Mismatch).
- **Xác thực dữ liệu chặt chẽ (Data Validation with Pydantic):** Tự động parse và validate kiểu dữ liệu của request body, query params, headers, loại bỏ tối đa lỗi kiểu dữ liệu.
- **Hỗ trợ Asynchronous (Async/Await):** Xử lý đồng thời hàng nghìn request I/O-bound mượt mà.
- **Dễ học, dễ viết:** Cú pháp Python ngắn gọn, dễ hiểu cho cả 7 thành viên.

### 1.2. Tại sao lựa chọn SQLite làm Cơ sở dữ liệu?
- **Zero-Configuration (Không cần cài đặt máy chủ CSDL phức tạp):** SQLite là hệ quản trị cơ sở dữ liệu quan hệ (RDBMS) dạng Serverless, toàn bộ dữ liệu được lưu gói gọn trong một file duy nhất (`facebook.db`). Không cần cài đặt MySQL, PostgreSQL hay Docker cồng kềnh.
- **Tương thích 100% SQL chuẩn:** Hỗ trợ đầy đủ các khóa chính (PK), khóa ngoại (FK), chỉ mục (INDEX), giao dịch ACID (Transactions).
- **Dễ dàng chia sẻ & Backup:** File database có thể dễ dàng sao chép, tạo bản seed data mẫu để nộp cho Giảng viên kiểm tra hoặc chạy demo mà không sợ chết máy chủ cloud.
- **Chi phí 0 VNĐ:** Chạy trực tiếp trên máy tính cá nhân (Localhost) hoặc deploy miễn phí lên các dịch vụ đám mây (Render, Fly.io, Railway, PythonAnywhere, Ngrok tunnel).

### 1.3. Mô hình Giao tiếp Client - Server (Android <-> FastAPI):
```
+------------------------------------+                  +--------------------------------------+
|       ANDROID CLIENT (JAVA)        |                  |       PYTHON FASTAPI SERVER          |
|  - Retrofit 2 + OkHttp 3           |                  |  - Uvicorn ASGI Server (:8000)       |
|  - Gson Converter                  |  HTTP/REST API   |  - Pydantic Schemas (Validation)     |
|  - BaseURL:                        |=================>|  - SQLAlchemy ORM / SQLite Driver    |
|    http://10.0.2.2:8000/api/       |   JSON Payload   |  - Static Files Server (/static/)    |
|    (Android Emulator host bridge)  |<=================|  - Swagger UI Interactive (/docs)    |
+------------------------------------+                  +------------------+-------------------+
                                                                           |
                                                                           v
                                                                +----------------------+
                                                                |   DATABASE (SQLITE)  |
                                                                |   facebook.db        |
                                                                +----------------------+
```

> [!IMPORTANT]
> **Lưu ý đặc biệt khi kết nối Android Emulator với Local Backend:**
> - Trên máy ảo Android Studio (Pixel 10a), địa chỉ `localhost` hay `127.0.0.1` đại diện cho chính máy ảo đó!
> - Để máy ảo Android gọi được server FastAPI đang chạy trên máy tính host, Base URL trong Retrofit phải là: **`http://10.0.2.2:8000/api/`**.
> - Nếu chạy bằng thiết bị thật qua Wi-Fi chung, thay `10.0.2.2` bằng địa chỉ IP LAN của máy tính host (ví dụ: `http://192.168.1.15:8000/api/`).

---

## 2. THIẾT KẾ CƠ SỞ DỮ LIỆU CHUẨN HÓA (SQLITE SCHEMA / ERD)

Toàn bộ hệ thống cơ sở dữ liệu quan hệ của Facebook Client được chuẩn hóa theo dạng 3NF, lưu trữ tại file `facebook.db`:

```
+------------------+             +--------------------+             +---------------------+
|      users       |             |       posts        |             |      comments       |
+------------------+             +--------------------+             +---------------------+
| id (TEXT/PK)     |<-------+    | id (TEXT/PK)       |<-------+    | id (TEXT/PK)        |
| email (TEXT)     |        |    | author_id (TEXT/FK)|----+   |    | post_id (TEXT/FK)---+
| display_name     |        |    | content (TEXT)     |    |   |    | author_id (TEXT/FK)-+
| avatar_url       |        +----| media_url (TEXT)   |    |   |    | content (TEXT)      |
| cover_url        |        |    | is_public (INTEGER)|    |   +----| created_at (INTEGER)|
| created_at       |        |    | created_at (INT)   |    |        +---------------------+
+------------------+        |    +--------------------+    |
         |                  |              |               |        +---------------------+
         |                  |              |               |        |      post_likes     |
         |                  |              |               |        +---------------------+
         |                  |              +---------------+------->| id (INTEGER/PK Auto)|
         |                  |                                       | post_id (TEXT/FK)   |
         |                  +---------------------------------------| user_id (TEXT/FK)   |
         |                                                          | created_at (INTEGER)|
         |                  +--------------------+                  +---------------------+
         |                  |    friendships     |
         |                  +--------------------+                  +---------------------+
         |                  | id (TEXT/PK)       |                  |    notifications    |
         +------------------| requester_id (FK)  |                  +---------------------+
         |                  | receiver_id (FK)   |                  | id (TEXT/PK)        |
         |                  | status (TEXT)      |                  | recipient_id (FK)---|---+
         |                  | created_at (INT)   |                  | sender_id (FK)------|---+
         |                  +--------------------+                  | type (TEXT)         |
         |                                                          | post_id (TEXT/FK)   |
         +----------------------------------------------------------| message (TEXT)      |
                                                                    | is_read (INTEGER)   |
                                                                    | created_at (INTEGER)|
                                                                    +---------------------+
```

### 2.1. Mã DDL Tạo Bảng SQLite (`schema.sql`):
```sql
-- 1. Bảng Người dùng (users)
CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    display_name TEXT NOT NULL,
    avatar_url TEXT,
    cover_url TEXT,
    created_at INTEGER NOT NULL
);

-- 2. Bảng Bài viết (posts)
CREATE TABLE IF NOT EXISTS posts (
    id TEXT PRIMARY KEY,
    author_id TEXT NOT NULL,
    content TEXT,
    media_url TEXT,
    is_public INTEGER DEFAULT 1,
    created_at INTEGER NOT NULL,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Bảng Bình luận (comments - Text only theo đặc tả PDF)
CREATE TABLE IF NOT EXISTS comments (
    id TEXT PRIMARY KEY,
    post_id TEXT NOT NULL,
    author_id TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Bảng Lượt thích bài viết (post_likes)
CREATE TABLE IF NOT EXISTS post_likes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    post_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    UNIQUE(post_id, user_id),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Bảng Quan hệ Bạn bè (friendships)
CREATE TABLE IF NOT EXISTS friendships (
    id TEXT PRIMARY KEY,
    requester_id TEXT NOT NULL,
    receiver_id TEXT NOT NULL,
    status TEXT CHECK(status IN ('PENDING', 'ACCEPTED', 'REJECTED')) NOT NULL,
    created_at INTEGER NOT NULL,
    UNIQUE(requester_id, receiver_id),
    FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 6. Bảng Thông báo (notifications)
CREATE TABLE IF NOT EXISTS notifications (
    id TEXT PRIMARY KEY,
    recipient_id TEXT NOT NULL,
    sender_id TEXT NOT NULL,
    type TEXT CHECK(type IN ('LIKE', 'COMMENT', 'FRIEND_REQUEST', 'FRIEND_ACCEPT')) NOT NULL,
    post_id TEXT,
    message TEXT NOT NULL,
    is_read INTEGER DEFAULT 0,
    created_at INTEGER NOT NULL,
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE SET NULL
);

-- Tạo Index để tăng tốc độ truy vấn
CREATE INDEX IF NOT EXISTS idx_posts_author ON posts(author_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_created ON posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comments_post ON comments(post_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_likes_post ON post_likes(post_id);
CREATE INDEX IF NOT EXISTS idx_friendships_receiver ON friendships(receiver_id, status);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications(recipient_id, created_at DESC);
```

---

## 3. ĐẶC TẢ CHI TIẾT RESTFUL API CONTRACTS

Toàn bộ phản hồi API đều ở định dạng JSON tiêu chuẩn. Mã trạng thái HTTP:
- `200 OK`: Truy vấn / cập nhật thành công.
- `201 Created`: Tạo mới tài nguyên thành công.
- `400 Bad Request`: Dữ liệu đầu vào không hợp lệ (ví dụ: đăng bài rỗng).
- `404 Not Found`: Không tìm thấy dữ liệu.
- `500 Internal Server Error`: Lỗi máy chủ.

### 3.1. Phân hệ Xác thực & Người dùng (Auth & Users)
| Phương thức | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/api/users` | Lấy danh sách tài khoản demo |
| `GET` | `/api/users/{id}` | Lấy thông tin chi tiết một người dùng |
| `PUT` | `/api/users/{id}` | Cập nhật avatar_url, cover_url, display_name |

**Ví dụ JSON Response User (`GET /api/users/usr_01`):**
```json
{
  "id": "usr_01",
  "email": "tungduong@gmail.com",
  "displayName": "Tùng Dương",
  "avatarUrl": "http://10.0.2.2:8000/static/uploads/avatar_tungduong.jpg",
  "coverUrl": "http://10.0.2.2:8000/static/uploads/cover_tungduong.jpg",
  "createdAt": 1728500000000
}
```

---

### 3.2. Phân hệ Bảng tin & Đăng bài (Feed & Posts)
| Phương thức | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/api/posts?limit=20&current_user_id={id}` | Lấy danh sách bài viết trên Feed (kèm cờ `isLiked`) |
| `POST` | `/api/posts` | Tạo bài viết mới |
| `POST` | `/api/posts/{id}/like` | Bật/tắt trạng thái Thích (Toggle Like) |
| `POST` | `/api/posts/{id}/share` | Chia sẻ bài viết lên tường nhà mình |
| `GET` | `/api/users/{id}/posts` | Lấy danh sách bài viết của một người dùng |
| `GET` | `/api/users/{id}/photos` | Lấy kho ảnh của người dùng (bài viết có `mediaUrl`) |

**Ví dụ Request Body Tạo bài viết (`POST /api/posts`):**
```json
{
  "authorId": "usr_01",
  "content": "Nhóm 7 người đã hoàn tất kết nối Android Retrofit với FastAPI và SQLite!",
  "mediaUrl": "http://10.0.2.2:8000/static/uploads/post_image_01.jpg",
  "isPublic": true
}
```

**Ví dụ Response Post Item:**
```json
{
  "id": "post_1001",
  "authorId": "usr_01",
  "authorName": "Tùng Dương",
  "authorAvatar": "http://10.0.2.2:8000/static/uploads/avatar_tungduong.jpg",
  "content": "Nhóm 7 người đã hoàn tất kết nối Android Retrofit với FastAPI và SQLite!",
  "mediaUrl": "http://10.0.2.2:8000/static/uploads/post_image_01.jpg",
  "isPublic": true,
  "likesCount": 15,
  "commentsCount": 3,
  "isLiked": false,
  "createdAt": 1728512345678
}
```

---

### 3.3. Phân hệ Bình luận (Comments - Text Only)
| Phương thức | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/api/posts/{id}/comments` | Lấy danh sách bình luận của bài viết |
| `POST` | `/api/posts/{id}/comments` | Thêm bình luận văn bản mới |

**Ví dụ Request Body Gửi bình luận (`POST /api/posts/post_1001/comments`):**
```json
{
  "authorId": "usr_02",
  "content": "Backend FastAPI chạy nhanh và mượt quá bạn ơi!"
}
```

---

### 3.4. Phân hệ Bạn bè (Friends & Search)
| Phương thức | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/api/friends/requests?user_id={id}` | Lấy danh sách lời mời kết bạn gửi đến |
| `GET` | `/api/friends?user_id={id}` | Lấy danh sách bạn bè hiện tại |
| `POST` | `/api/friends/requests` | Gửi lời mời kết bạn |
| `POST` | `/api/friends/accept` | Chấp nhận lời mời kết bạn |
| `POST` | `/api/friends/reject` | Từ chối / Xóa lời mời kết bạn |
| `GET` | `/api/friends/search?q={keyword}&user_id={id}` | Tìm kiếm bạn bè theo tên |

---

### 3.5. Phân hệ Thông báo & Upload Media (Notifications & Upload)
| Phương thức | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/api/notifications?user_id={id}` | Lấy danh sách thông báo của người dùng |
| `POST` | `/api/notifications/{id}/read` | Đánh dấu thông báo đã đọc |
| `POST` | `/api/upload` | Upload file ảnh (`multipart/form-data`) lưu vào thư mục `static/uploads/` |

---

## 4. HƯỚNG DẪN CÀI ĐẶT & CHẠY SERVER PYTHON FASTAPI

### 4.1. Cấu trúc thư mục nguồn Backend gợi ý:
```
backend/
├── main.py                  <-- Khởi tạo ứng dụng FastAPI & định tuyến API
├── database.py              <-- Khởi tạo kết nối SQLite & SQLAlchemy Session
├── models.py                <-- Định nghĩa SQLAlchemy Database Models
├── schemas.py               <-- Định nghĩa Pydantic Schemas (Request/Response DTOs)
├── crud/                    <-- Xử lý nghiệp vụ truy vấn SQLite
│   ├── crud_user.py
│   ├── crud_post.py
│   ├── crud_friend.py
│   └── crud_notif.py
├── routers/                 <-- Chia nhỏ API theo phân hệ
│   ├── auth.py
│   ├── posts.py
│   ├── comments.py
│   ├── friends.py
│   └── notifications.py
├── static/
│   └── uploads/             <-- Lưu trữ các file ảnh tải lên
├── requirements.txt         <-- Danh sách thư viện Python
└── facebook.db              <-- File CSDL SQLite (tự động tạo khi chạy)
```

### 4.2. File `requirements.txt`:
```txt
fastapi==0.110.0
uvicorn[standard]==0.29.0
sqlalchemy==2.0.29
pydantic==2.6.4
python-multipart==0.0.9
aiofiles==23.2.1
```

### 4.3. Các bước khởi chạy Server Backend trên máy:
1. **Mở terminal và di chuyển vào thư mục backend:**
   ```bash
   cd backend
   ```
2. **Tạo và kích hoạt môi trường ảo (Virtual Environment):**
   ```bash
   python -m venv venv
   # Trên Windows:
   venv\Scripts\activate
   # Trên macOS/Linux:
   source venv/bin/activate
   ```
3. **Cài đặt các gói thư viện cần thiết:**
   ```bash
   pip install -r requirements.txt
   ```
4. **Khởi chạy máy chủ FastAPI với Uvicorn:**
   ```bash
   uvicorn main:app --reload --host 0.0.0.0 --port 8000
   ```
5. **Kiểm tra hoạt động:**
   - Mở trình duyệt truy cập: `http://localhost:8000/docs` để trải nghiệm **Swagger UI** tương tác trực tiếp với API!
   - Thử nghiệm trên máy ảo Android qua địa chỉ: `http://10.0.2.2:8000/docs`.

---

## 5. TRIỂN KHAI CLOUD MIỄN PHÍ HOẶC LOCAL TUNNEL (CHO BUỔI BẢO VỆ)

Để demo đồ án, nhóm có thể linh hoạt chọn một trong các phương án sau:
1. **Chạy Localhost (Khuyên dùng số 1 - Ổn định tuyệt đối):**
   - Chạy FastAPI trên chính laptop dùng để demo.
   - Máy ảo Pixel 10a kết nối trực tiếp qua `http://10.0.2.2:8000/api/`.
   - Ưu điểm: Không phụ thuộc vào mạng Internet của phòng thi, độ trễ 0ms, không lo rớt mạng.
2. **Dùng Ngrok Tunnel (Khi cần demo qua nhiều máy tính khác nhau):**
   ```bash
   ngrok http 8000
   ```
   - Cung cấp đường dẫn HTTPS công khai tạm thời để bất kỳ máy nào cũng gọi được.
3. **Deploy Cloud Free Tier (Render / Railway / Fly.io):**
   - Đẩy mã nguồn backend lên GitHub và liên kết với Render Web Service (Free Tier).
   - Mount SQLite volume hoặc đồng bộ file `facebook.db`.
