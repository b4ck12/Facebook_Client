import os
import sys
import time

# Đảm bảo thư mục backend luôn có trong sys.path để import các module nội bộ
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
if BASE_DIR not in sys.path:
    sys.path.insert(0, BASE_DIR)

from fastapi import FastAPI, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from database import engine, Base
import models
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

# Middleware ghi log chi tiết request phục vụ Android Retrofit Debugging 
@app.middleware("http")
async def log_requests_middleware(request: Request, call_next):
    start_time = time.time()
    client_ip = request.client.host if request.client else "unknow"
    method = request.method
    path = request.url.path
    query = request.url.query
    full_path = f"{path}?{query}" if query else path

    #Thực thi request và tính thời gian xử lí (ms)
    response = await call_next(request)
    process_time_ms = round((time.time() - start_time) * 1000, 2)
    status_code = response.status_code

    #Biểu tượng trạng thái trực quan 
    status_symbol = "✓" if status_code < 400 else "X"
    print(f"[{status_symbol}] {method} {full_path} | Status: {status_code} | Time: {process_time_ms}ms | Client: {client_ip}")

    #Đính kèm header thời gian xử lí vào response
    response.headers["X-Process-Time-Ms"] = str(process_time_ms)
    return response


# Cấu hình CORS Middleware cho phép mọi client truy cập
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
    expose_headers=["*"],
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
    from routers import upload, users, posts, friends, comments, notifications
except ImportError:
    from backend.routers import upload, users, posts, friends, comments, notifications

app.include_router(upload.router, prefix="/api/upload", tags=["Media Upload"])
app.include_router(users.router, prefix="/api/users", tags=["Profile & Photos"])
app.include_router(posts.router, prefix="/api/posts", tags=["Feed & Posts"])
app.include_router(comments.router, prefix="/api/posts", tags=["Comments"])
app.include_router(friends.router, prefix="/api/friends", tags=["Friends & Search"])
app.include_router(notifications.router, prefix="/api/notifications", tags=["Notifications"])

# Các router tiếp theo
# from routers import auth
# app.include_router(auth.router, prefix="/api/auth", tags=["Authentication & Demo Users"])

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
@app.get("/api/network-test", tags=["Health Check"])
def network_connectivity_test(request: Request):
    """
    Endpoint kiểm tra kết nối mạng chuyên dụng dành cho Android Retrofit Client (D8-AD).
    Client từ máy ảo Google Pixel 10a (10.0.2.2:8000) có thể gọi vào đây để test kết nối.
    """
    client_ip = request.client.host if request.client else "unknow"
    return {
        "status" : "connected",
        "message" : "Kết nối thành công từ Androi CLient tới FastAPI Backend !",
        "client_ip" : client_ip,
        "server_time" : int(time.time() * 1000),
        "target_api" : "Facebook Mobile Client RESTful API"
    }