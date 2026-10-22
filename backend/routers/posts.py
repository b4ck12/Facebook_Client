"""
Router xử lý các API liên quan đến Bảng tin và Bài viết (Feed & Posts).
"""
from typing import List, Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session

try:
    from database import get_db
    import schemas
    from crud import crud_post
except ImportError:
    from backend.database import get_db
    from backend import schemas
    from backend.crud import crud_post

router = APIRouter()

@router.get("", response_model=List[schemas.PostResponse], status_code=status.HTTP_200_OK)
@router.get("/", response_model=List[schemas.PostResponse], status_code=status.HTTP_200_OK, include_in_schema=False)
def get_feed_posts(
    current_user_id: Optional[str] = Query(None, description="Mã ID người dùng hiện tại để kiểm tra isLiked"),
    skip: int = Query(0, ge=0, description="Vị trí bắt đầu lấy dữ liệu (phân trang)"),
    limit: int = Query(20, ge=1, le=100, description="Số lượng bài viết tối đa cần lấy"),
    db: Session = Depends(get_db)
):
    """
    Lấy danh sách bài viết trên Bảng tin (Home Feed).
    """
    return crud_post.get_feed_posts(
        db=db,
        current_user_id=current_user_id,
        skip=skip,
        limit=limit
    )