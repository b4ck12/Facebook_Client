"""
Router xử lý tiếp nhận tải lên hình ảnh (Media Upload) và nén ảnh Bitmap.
Phụ trách: Minh Đức (BE) - Backend Engineer & Storage/Media Specialist (Task D9-MD).
"""

import os
import io
import uuid
import logging
from typing import Tuple, Dict, Any
import aiofiles
from fastapi import APIRouter, UploadFile, File, HTTPException, status
from PIL import Image, ImageOps

try:
    from schemas import UploadResponse
except ImportError:
    from backend.schemas import UploadResponse

logger = logging.getLogger("UploadService")

router = APIRouter()

# Thư mục lưu trữ ảnh tĩnh: backend/static/uploads
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
UPLOAD_DIR = os.path.join(BASE_DIR, "static", "uploads")
ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp"}
MAX_FILE_SIZE = 10 * 1024 * 1024  # 10MB tối đa khi tải lên
MAX_IMAGE_DIMENSION = 1920        # Kích thước chiều dài/rộng tối đa (Full HD)
JPEG_QUALITY = 82                 # Mức chất lượng tối ưu giữa kích thước và độ nét


def compress_image_content(content: bytes, original_ext: str) -> Tuple[bytes, str, int, int]:
    """
    Nén ảnh Bitmap bằng Pillow trước khi ghi vào đĩa:
    1. Tự động xoay ảnh theo EXIF Orientation (chống ảnh chụp dọc bị xoay ngang).
    2. Resize ảnh tỉ lệ chuẩn nếu chiều lớn nhất vượt quá MAX_IMAGE_DIMENSION (1920px).
    3. Nén ảnh với chất lượng JPEG 82% kèm optimize và progressive encoding.
    4. Giữ lại ảnh gốc nếu sau khi nén dung lượng không giảm.

    Trả về: (output_bytes, final_extension, original_size, compressed_size)
    """
    original_size = len(content)

    try:
        image = Image.open(io.BytesIO(content))

        # 1. Tự động chuyển đổi hướng ảnh dựa trên dữ liệu EXIF
        image = ImageOps.exif_transpose(image)

        # 2. Thu nhỏ kích thước ảnh nếu vượt quá giới hạn tối đa
        orig_width, orig_height = image.size
        max_dim = max(orig_width, orig_height)
        if max_dim > MAX_IMAGE_DIMENSION:
            scale = MAX_IMAGE_DIMENSION / float(max_dim)
            new_width = max(1, int(orig_width * scale))
            new_height = max(1, int(orig_height * scale))
            image = image.resize((new_width, new_height), Image.Resampling.LANCZOS)

        # 3. Lựa chọn định dạng xuất và nén
        out_buf = io.BytesIO()
        ext_lower = original_ext.lower()

        if ext_lower in {".jpg", ".jpeg"}:
            if image.mode != "RGB":
                image = image.convert("RGB")
            image.save(out_buf, format="JPEG", quality=JPEG_QUALITY, optimize=True, progressive=True)
            final_ext = ".jpg"
        elif ext_lower == ".png":
            # Nếu ảnh PNG không chứa kênh trong suốt (Alpha), chuyển sang JPEG để nén hiệu quả hơn
            if image.mode in ("RGBA", "LA") or (image.mode == "P" and "transparency" in image.info):
                image.save(out_buf, format="PNG", optimize=True)
                final_ext = ".png"
            else:
                image = image.convert("RGB")
                image.save(out_buf, format="JPEG", quality=JPEG_QUALITY, optimize=True, progressive=True)
                final_ext = ".jpg"
        elif ext_lower == ".webp":
            image.save(out_buf, format="WEBP", quality=JPEG_QUALITY, method=6)
            final_ext = ".webp"
        else:
            if image.mode != "RGB":
                image = image.convert("RGB")
            image.save(out_buf, format="JPEG", quality=JPEG_QUALITY, optimize=True)
            final_ext = ".jpg"

        compressed_bytes = out_buf.getvalue()
        compressed_size = len(compressed_bytes)

        # Nếu dung lượng nén nhỏ hơn ảnh gốc -> lấy ảnh nén
        if compressed_size < original_size:
            return compressed_bytes, final_ext, original_size, compressed_size

        # Nếu ảnh gốc đã được tối ưu sẵn -> giữ nguyên
        return content, original_ext, original_size, original_size

    except Exception as e:
        logger.warning(f"Không thể nén ảnh qua Pillow: {e}. Sử dụng dữ liệu gốc.")
        return content, original_ext, original_size, original_size


@router.get("/stats", summary="Kiểm tra dung lượng và số lượng ảnh trong thư mục static/uploads/")
def get_upload_storage_stats() -> Dict[str, Any]:
    """
    Thống kê dung lượng lưu trữ thư mục static/uploads/ [D9-MD].
    """
    os.makedirs(UPLOAD_DIR, exist_ok=True)
    total_size = 0
    file_count = 0

    for entry in os.scandir(UPLOAD_DIR):
        if entry.is_file() and not entry.name.startswith("."):
            total_size += entry.stat().st_size
            file_count += 1

    total_mb = round(total_size / (1024 * 1024), 2)
    return {
        "uploadDirectory": UPLOAD_DIR,
        "totalFiles": file_count,
        "totalSizeBytes": total_size,
        "totalSizeMB": total_mb
    }


@router.post("", response_model=UploadResponse, status_code=status.HTTP_201_CREATED)
@router.post("/", response_model=UploadResponse, status_code=status.HTTP_201_CREATED, include_in_schema=False)
async def upload_image(file: UploadFile = File(...)):
    """
    Tiếp nhận ảnh upload dạng multipart/form-data.
    Áp dụng nén Bitmap để tiết kiệm băng thông và dung lượng lưu trữ [D9-MD].
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
            detail="Kích thước tệp tin vượt quá 10MB!"
        )

    # 3. Nén ảnh Bitmap trước khi lưu vào thư mục static/uploads/
    final_content, final_ext, orig_size, comp_size = compress_image_content(content, ext)

    saved_pct = round((1.0 - (comp_size / float(orig_size))) * 100.0, 2) if orig_size > 0 else 0.0

    # 4. Tạo tên tệp ngẫu nhiên chống trùng lặp
    filename = f"img_{uuid.uuid4().hex[:12]}{final_ext}"
    filepath = os.path.join(UPLOAD_DIR, filename)

    # 5. Ghi tệp tin bất đồng bộ
    async with aiofiles.open(filepath, "wb") as out_file:
        await out_file.write(final_content)

    logger.info(
        f"[UPLOAD COMPRESSION] Đã nén {filename}: "
        f"{orig_size / 1024:.1f}KB -> {comp_size / 1024:.1f}KB (Tiết kiệm {saved_pct}%)"
    )

    # 6. Trả về đường dẫn tĩnh có thể truy cập qua HTTP cho Android Retrofit
    url = f"http://10.0.2.2:8000/static/uploads/{filename}"
    return {
        "url": url,
        "filename": filename,
        "originalSize": orig_size,
        "compressedSize": comp_size,
        "savedPercentage": saved_pct
    }
