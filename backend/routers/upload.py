import os
import uuid
import aiofiles
from fastapi import APIRouter, UploadFile, File, HTTPException, status

try:
    from schemas import UploadResponse
except ImportError:
    from backend.schemas import UploadResponse

router = APIRouter()

# Thư mục lưu trữ ảnh tĩnh: backend/static/uploads
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
UPLOAD_DIR = os.path.join(BASE_DIR, "static", "uploads")
ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp"}
MAX_FILE_SIZE = 5 * 1024 * 1024  # 5MB


@router.post("", response_model=UploadResponse, status_code=status.HTTP_201_CREATED)
@router.post("/", response_model=UploadResponse, status_code=status.HTTP_201_CREATED, include_in_schema=False)
async def upload_image(file: UploadFile = File(...)):
    """
    Tiếp nhận ảnh upload dạng multipart/form-data.
    Lưu file vào thư mục static/uploads/ và trả về URL ảnh tĩnh phục vụ Android client.
    """
    # Đảm bảo thư mục static/uploads tồn tại
    os.makedirs(UPLOAD_DIR, exist_ok=True)

    # 1. Kiểm tra phần mở rộng tệp tin
    if not file.filename:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Tên tệp tin không hợp lệ!"
        )

    _, ext = os.path.splitext(file.filename.lower())
    if ext not in ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Định dạng tệp không hỗ trợ! Chỉ chấp nhận JPG, PNG, WEBP."
        )

    # 2. Đọc nội dung và kiểm tra kích thước
    content = await file.read()
    if len(content) == 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Tệp tin rỗng không có nội dung!"
        )

    if len(content) > MAX_FILE_SIZE:
        raise HTTPException(
            status_code=status.HTTP_413_CONTENT_TOO_LARGE,
            detail="Kích thước tệp tin vượt quá 5MB!"
        )

    # 3. Tạo tên tệp ngẫu nhiên chống trùng lặp
    filename = f"img_{uuid.uuid4().hex[:12]}{ext}"
    filepath = os.path.join(UPLOAD_DIR, filename)

    # 4. Ghi tệp tin bất đồng bộ
    async with aiofiles.open(filepath, "wb") as out_file:
        await out_file.write(content)

    # 5. Trả về đường dẫn tĩnh có thể truy cập qua HTTP cho Android Retrofit
    url = f"http://10.0.2.2:8000/static/uploads/{filename}"
    return {"url": url, "filename": filename}
