# Facebook Mobile Client - Backend Server

Máy chủ Backend RESTful API được xây dựng bằng **Python FastAPI** và cơ sở dữ liệu **SQLite** (`facebook.db`), phục vụ cho ứng dụng Android Java Retrofit 2.

## Cấu trúc thư mục

```text
backend/
├── main.py              # Khởi tạo FastAPI App, CORS, static files, health check
├── database.py          # Kết nối CSDL SQLite facebook.db (kèm chế độ WAL và Foreign Keys)
├── models.py            # SQLAlchemy ORM Models (User, Post, Comment, Friendship, PostLike, Notification)
├── schemas.py           # Pydantic Schemas xác thực dữ liệu đầu vào và định dạng đầu ra
├── schema.sql           # DDL cấu trúc CSDL chuẩn hóa 3NF và hệ thống Indexes
├── seed_data.py         # Script nạp dữ liệu mẫu ban đầu vào facebook.db
├── debug_friends.py     # Script kiểm tra, debug dữ liệu bạn bè và câu lệnh SQL trong SQLite (D8-MD)
├── test_friends_crud.py # Bộ kiểm thử logic CRUD và quan hệ bạn bè
├── crud/
│   ├── __init__.py      # CRUD package init
│   ├── crud_user.py     # Nghiệp vụ truy vấn CSDL phân hệ User
│   ├── crud_post.py     # Nghiệp vụ bài viết, toggle like, posts & photos
│   ├── crud_friend.py   # Nghiệp vụ bạn bè, lời mời kết bạn & tìm kiếm
│   ├── crud_notif.py    # Tự động tạo và quản lý thông báo (Like, Comment, Friends)
│   └── crud_comment.py  # Nghiệp vụ bình luận bài viết (Text only)
├── routers/
│   ├── __init__.py      # Routers package init
│   ├── upload.py        # Router xử lý POST /api/upload tiếp nhận ảnh multipart
│   ├── users.py         # Router xử lý User/Profile: GET/PUT /api/users/{id}, GET /posts, GET /photos
│   ├── posts.py         # Router xử lý Feed & Posts: GET /api/posts, POST /like
│   ├── comments.py      # Router xử lý Comments: GET/POST /api/posts/{id}/comments
│   ├── friends.py       # Router xử lý Friends & Friend Requests
│   └── notifications.py # Router xử lý Notifications: GET /api/notifications, POST /read
├── requirements.txt     # Danh sách thư viện Python cần thiết
├── static/
│   └── uploads/         # Thư mục lưu trữ hình ảnh tải lên (/static/uploads/)
└── README.md
```

## Hướng dẫn cài đặt & Khởi chạy

### 1. Tạo và kích hoạt môi trường ảo (Virtual Environment)
```bash
cd backend
python -m venv venv

# Windows PowerShell:
.\venv\Scripts\Activate.ps1

# Windows Command Prompt:
venv\Scripts\activate.bat

# macOS / Linux:
source venv/bin/activate
```

### 2. Cài đặt các thư viện phụ thuộc
```bash
pip install -r requirements.txt
```

### 3. Khởi chạy máy chủ FastAPI với Uvicorn
```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### 4. Kiểm tra hoạt động
- **Swagger UI (Interactive Docs):** Mở trình duyệt tại [http://localhost:8000/docs](http://localhost:8000/docs)
- **Health Check Endpoint:** [http://localhost:8000/](http://localhost:8000/)
- **Địa chỉ kết nối từ máy ảo Android (Pixel 10a):** `http://10.0.2.2:8000/api/`
- **Địa chỉ phục vụ tệp tĩnh tải lên:** `http://10.0.2.2:8000/static/uploads/`

---

## Công cụ Debug & Kiểm tra Dữ liệu SQLite

### Chạy script gỡ lỗi phân hệ Bạn bè (Task [D8-MD] - Minh Đức):
```bash
python backend/debug_friends.py
```
Script tự động thực hiện:
1. **Kiểm tra PRAGMA SQLite:** `journal_mode=WAL`, `foreign_keys=ON`, `integrity_check`, `foreign_key_check`.
2. **Tối ưu hóa Indexes trên bảng `friendships`:**
   - `idx_friendships_receiver_created(receiver_id, status, created_at DESC)`: Tối ưu lấy danh sách lời mời kết bạn, triệt tiêu hoàn toàn `USE TEMP B-TREE FOR ORDER BY`.
   - `idx_friendships_requester(requester_id, status)`: Tối ưu tra cứu quan hệ bạn bè hai chiều.
   - `idx_friendships_status(status)`: Lọc nhanh các bản ghi bạn bè đã được chấp nhận (`ACCEPTED`).
3. **Phân tích hiệu năng SQL:** Sử dụng `EXPLAIN QUERY PLAN` để chứng minh 100% câu truy vấn đều dùng chỉ mục.
4. **Kiểm tra hợp đồng dữ liệu Android:** Xác thực định dạng JSON từ FastAPI khớp tuyệt đối với DTO Java (`FriendRequest.java`, `User.java`).
5. **Kịch bản kiểm thử vòng đời kết bạn:** Mô phỏng gửi lời mời -> sinh thông báo -> chấp nhận lời mời -> đồng bộ danh sách bạn bè.
