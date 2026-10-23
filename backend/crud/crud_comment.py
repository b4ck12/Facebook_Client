"""
Nghiệp vụ truy vấn và tạo Bình luận (Comments) trong CSDL SQLite.
Tự động kích hoạt tạo Notification gửi đến chủ bài viết khi có bình luận mới.
"""
from typing import List, Optional
import uuid
import time
from fastapi import HTTPException, status
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import desc, asc

try:
    import models
    import schemas
    from crud import crud_notif
except ImportError:
    from backend import models
    from backend import schemas
    from backend.crud import crud_notif


def create_comment(
    db: Session,
    post_id: str,
    comment_data: schemas.CommentCreate
) -> schemas.CommentResponse:
    """
    Tạo bình luận mới cho bài viết (Text-only theo chuẩn PDF):
    - Kiểm tra bài viết và người bình luận tồn tại.
    - Kiểm tra nội dung văn bản không được để trống.
    - Tự động tạo bản ghi trong bảng notifications gửi tới tác giả bài viết.
    """
    # 1. Kiểm tra bài viết tồn tại
    post = db.query(models.Post).filter(models.Post.id == post_id).first()
    if not post:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Không tìm thấy bài viết có ID '{post_id}'"
        )

    # 2. Kiểm tra tác giả bình luận tồn tại
    author = db.query(models.User).filter(models.User.id == comment_data.authorId).first()
    if not author:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Không tìm thấy người dùng có ID '{comment_data.authorId}'"
        )

    # 3. Kiểm tra nội dung bình luận
    clean_content = comment_data.content.strip() if comment_data.content else ""
    if not clean_content:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Nội dung bình luận không được để trống!"
        )

    # 4. Tạo bản ghi bình luận mới
    cmt_id = f"cmt_{uuid.uuid4().hex[:8]}"
    created_at = int(time.time() * 1000)

    new_comment = models.Comment(
        id=cmt_id,
        post_id=post_id,
        author_id=comment_data.authorId,
        content=clean_content,
        created_at=created_at
    )
    db.add(new_comment)

    # 5. Tự động tạo Notification cho tác giả bài viết (nếu không phải tự bình luận bài mình)
    crud_notif.create_comment_notification(
        db=db,
        post_id=post_id,
        author_id=comment_data.authorId,
        content=clean_content
    )

    db.commit()
    db.refresh(new_comment)

    author_name = author.display_name if author else "Người dùng Facebook"
    author_avatar = author.avatar_url if author else None

    return schemas.CommentResponse(
        id=new_comment.id,
        postId=new_comment.post_id,
        authorId=new_comment.author_id,
        authorName=author_name,
        authorAvatar=author_avatar,
        content=new_comment.content,
        createdAt=new_comment.created_at
    )


def get_post_comments(
    db: Session,
    post_id: str,
    skip: int = 0,
    limit: int = 50
) -> List[schemas.CommentResponse]:
    """
    Lấy danh sách bình luận của bài viết theo thứ tự thời gian tăng dần (cũ trước, mới sau).
    """
    post = db.query(models.Post).filter(models.Post.id == post_id).first()
    if not post:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Không tìm thấy bài viết có ID '{post_id}'"
        )

    comments = (
        db.query(models.Comment)
        .options(joinedload(models.Comment.author))
        .filter(models.Comment.post_id == post_id)
        .order_by(asc(models.Comment.created_at))
        .offset(skip)
        .limit(limit)
        .all()
    )

    results = []
    for c in comments:
        author_name = c.author.display_name if c.author else "Người dùng Facebook"
        author_avatar = c.author.avatar_url if c.author else None
        results.append(
            schemas.CommentResponse(
                id=c.id,
                postId=c.post_id,
                authorId=c.author_id,
                authorName=author_name,
                authorAvatar=author_avatar,
                content=c.content,
                createdAt=c.created_at
            )
        )
    return results
