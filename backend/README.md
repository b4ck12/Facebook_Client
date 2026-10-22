# Facebook Mobile Client - Backend Server

Máy chủ Backend RESTful API được xây dựng bằng **Python FastAPI** và cơ sở dữ liệu **SQLite** (`facebook.db`), phục vụ cho ứng dụng Android Java Retrofit 2.

## Cấu trúc thư mục

```text
backend/
├── main.py              # Khởi tạo FastAPI App, CORS, static files, health check
├── database.py          # Kết nối CSDL SQLite facebook.db (kèm chế độ WAL và Foreign Keys)
├── models.py            # SQLAlchemy ORM Models (User, Post, Comment, Friendship, PostLike, Notification)
├── schemas.py           # Pydantic Schemas xác thực dữ liệu đầu vào và định dạng đầu ra
├── crud/
│   ├── __init__.py      # CRUD package init
│   └── crud_user.py     # Nghiệp vụ truy vấn CSDL phân hệ User
├── routers/
│   ├── __init__.py      # Routers package init
│   ├── upload.py        # Router xử lý POST /api/upload tiếp nhận ảnh multipart
│   └── users.py         # Router xử lý GET /api/users/{id} và PUT /api/users/{id} (cập nhật avatar/cover)
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
