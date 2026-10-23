"""
Router xử lý các API liên quan đến Bình luận bài viết (Comments - Text only).
Tự động kích hoạt tạo Notification trong CSDL SQLite khi có bình luận mới.
"""
from typing import List
from fastapi import APIRouter, Depends, status, HTTPException
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


@router.get("/{id}/comments", response_model=List[schemas.CommentResponse], status_code=status.HTTP_200_OK)
def get_post_comments(
    id: str,
    skip: int = 0,
    limit: int = 50,
    db: Session = Depends(get_db)
):
    """
    Lấy danh sách tất cả bình luận của bài viết có ID tương ứng.
    Sắp xếp theo thứ tự thời gian tăng dần (cũ trước, mới sau).
    """
    return crud_comment.get_post_comments(db=db, post_id=id, skip=skip, limit=limit)


@router.post("/{id}/comments", response_model=schemas.CommentResponse, status_code=status.HTTP_201_CREATED)
def add_comment(
    id: str,
    comment_data: schemas.CommentCreate,
    db: Session = Depends(get_db)
):
    """
    Thêm bình luận văn bản mới cho bài viết (Text only theo chuẩn PDF).
    Tự động tạo bản ghi Notification trong SQLite gửi đến tác giả bài viết.
    """
    return crud_comment.create_comment(db=db, post_id=id, comment_data=comment_data)
