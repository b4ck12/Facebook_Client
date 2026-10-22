"""
Router xử lý các API liên quan đến Bảng tin và Bài viết (Feed & Posts).
"""
from typing import List, Optional
from fastapi import APIRouter, Depends, Query, status, HTTPException, Body
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

@router.post(
    "/{id}/like",
    response_model=schemas.LikeToggleResponse,
    status_code=status.HTTP_200_OK,
    summary="Bật/tắt trạng thái thích bài viết "
)

def toggle_like(
    id: str,
    user_id: Optional[str] = Query(None, description="Mã ID người dùng thích bài viết (snake_case)"),
    userId: Optional[str] = Query(None, description="Mã ID người dùng thích bài viết (camelCase)"),
    db: Session = Depends(get_db)
):
    """
    Toggle Like: Thêm hoặc xóa lượt thích của người dùng cho bài viết.
    Trả về isLiked và likesCount sau khi xử lý.
    """
    target_user_id = user_id or userId
    if not target_user_id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Thiếu mã người dùng(user_id). Vui lòng truyền qua query param ?user_id=..."
        )
    return crud_post.toggle_like(db=db, post_id=id, user_id=target_user_id)