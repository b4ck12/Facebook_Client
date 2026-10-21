import os
from fastapi import FastAPI, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from database import engine, Base
import models

# Đảm bảo thư mục upload tĩnh tồn tại
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
STATIC_DIR = os.path.join(BASE_DIR, "static")
UPLOAD_DIR = os.path.join(STATIC_DIR, "uploads")
os.makedirs(UPLOAD_DIR, exist_ok=True)

# Tự động tạo bảng SQLite nếu chưa tồn tại
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Facebook Mobile Client Backend API",
    description="RESTful API cung cấp dữ liệu cho ứng dụng di động Facebook Client (Android Java)",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Cấu hình CORS Middleware cho phép mọi client truy cập
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount thư mục static phục vụ file ảnh tĩnh
app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")

# Xử lý ngoại lệ Validation Pydantic đồng bộ JSON cho Android Retrofit
@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    error_msg = exc.errors()[0]["msg"] if exc.errors() else "Dữ liệu không hợp lệ"
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={"detail": error_msg}
    )

# Đăng ký các router
try:
    from routers import upload, users, posts
except ImportError:
    from backend.routers import upload, users, posts

app.include_router(upload.router, prefix="/api/upload", tags=["Media Upload"])
app.include_router(users.router, prefix="/api/users", tags=["Profile & Photos"])

app.include_router(posts.router, prefix="/api/posts", tags=["Feed & Posts"])
# Các router tiếp theo (D3-D6)
# from routers import auth, posts, comments, friends, notifications
# app.include_router(auth.router, prefix="/api/auth", tags=["Authentication & Demo Users"])
# app.include_router(comments.router, prefix="/api/posts", tags=["Comments"])
# app.include_router(friends.router, prefix="/api/friends", tags=["Friends & Search"])
# app.include_router(notifications.router, prefix="/api/notifications", tags=["Notifications"])

@app.get("/", tags=["Health Check"])
def health_check():
    """
    Endpoint kiểm tra trạng thái máy chủ Backend và kết nối CSDL SQLite.
    """
    return {
        "status": "healthy",
        "framework": "FastAPI",
        "database": "SQLite (facebook.db)",
        "message": "Facebook Client Backend is running smoothly"
    }
