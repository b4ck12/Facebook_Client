
"""
Router xử lý các API liên quan đến Bình luận bài viết (Comments - Text only).
Tự động kích hoạt tạo Notification trong CSDL SQLite khi có bình luận mới.
"""
from typing import List
from fastapi import APIRouter, Depends, Path, Query, status, HTTPException
from sqlalchemy.orm import Session

try:
    from database import get_db
    import schemas
    from crud import crud_comment
except ImportError:
    from backend.database import get_db
    from backend import schemas
    from backend.crud import crud_comment

router = APIRouter()


@router.get(
    "/{id}/comments",
    response_model=List[schemas.CommentResponse],
    status_code=status.HTTP_200_OK,
    summary="Lấy danh sách bình luận của bài viết (Get Comments)"
)
def get_post_comments(
    id: str = Path(..., description="Mã ID bài viết cần lấy bình luận"),
    skip: int = Query(0, ge=0, description="Vị trí bắt đầu lấy danh sách (phân trang)"),
    limit: int = Query(50, ge=1, le=100, description="Số lượng bình luận tối đa"),
    db: Session = Depends(get_db)
):
    """
    Lấy danh sách tất cả bình luận của bài viết có ID tương ứng.
    Sắp xếp theo thứ tự thời gian tăng dần (cũ trước, mới sau) chuẩn Facebook.
    """
    return crud_comment.get_post_comments(db=db, post_id=id, skip=skip, limit=limit)


@router.post(
    "/{id}/comments",
    response_model=schemas.CommentResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Thêm bình luận văn bản mới (Add Comment - Text only)"
)
def add_comment(
    id: str = Path(..., description="Mã ID bài viết được bình luận"),
    comment_data: schemas.CommentCreate = ...,
    db: Session = Depends(get_db)
):
    """
    Thêm bình luận văn bản mới cho bài viết (Text only theo chuẩn đặc tả PDF).
    Tự động tạo bản ghi Notification trong SQLite gửi đến tác giả bài viết.
    """
    return crud_comment.create_comment(db=db, post_id=id, comment_data=comment_data)