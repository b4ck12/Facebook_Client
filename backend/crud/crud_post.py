from typing import Optional, List
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import select, func, desc, literal

try:
    import models
    import schemas
except ImportError:
    from backend import models
    from backend import schemas


def get_feed_posts(
    db: Session,
    current_user_id: Optional[str] = None,
    skip: int = 0,
    limit: int = 20
) -> List[schemas.PostResponse]:
    """
    Lấy danh sách bài viết hiển thị trên Bảng tin (Feed):
    - Dùng joinedload để Eager Load bảng User (loại bỏ N query lấy tác giả).
    - Dùng scalar subquery gom đếm likes, comments và kiểm tra isLiked trực tiếp trong SELECT.
    """
    # 1. Subquery đếm số like cho từng bài
    likes_sub = (
        select(func.count(models.PostLike.id))
        .where(models.PostLike.post_id == models.Post.id)
        .correlate(models.Post)
        .scalar_subquery()
    )

    # 2. Subquery đếm số bình luận cho từng bài
    comments_sub = (
        select(func.count(models.Comment.id))
        .where(models.Comment.post_id == models.Post.id)
        .correlate(models.Post)
        .scalar_subquery()
    )

    # 3. Subquery kiểm tra user hiện tại đã like chưa (EXISTS trả về True/False)
    if current_user_id:
        is_liked_sub = (
            select(1)
            .where(
                models.PostLike.post_id == models.Post.id,
                models.PostLike.user_id == current_user_id
            )
            .correlate(models.Post)
            .exists()
        )
    else:
        is_liked_sub = literal(False)

    # 4. Câu truy vấn tổng hợp duy nhất 
    stmt = (
        select(
            models.Post,
            likes_sub.label("likes_count"),
            comments_sub.label("comments_count"),
            is_liked_sub.label("is_liked")
        )
        .options(joinedload(models.Post.author))  # LEFT OUTER JOIN lấy luôn tác giả
        .where(models.Post.is_public == True)
        .order_by(desc(models.Post.created_at))
        .offset(skip)
        .limit(limit)
    )

    results = db.execute(stmt).all()

    # 5. Đóng gói kết quả trả về
    feed_items = []
    for row in results:
        post = row[0]
        likes_count = row[1] or 0
        comments_count = row[2] or 0
        is_liked = bool(row[3])

        author_name = post.author.display_name if post.author else "Người dùng Facebook"
        author_avatar = post.author.avatar_url if post.author else None

        feed_items.append(
            schemas.PostResponse(
                id=post.id,
                authorId=post.author_id,
                authorName=author_name,
                authorAvatar=author_avatar,
                content=post.content,
                mediaUrl=post.media_url,
                isPublic=post.is_public,
                likesCount=likes_count,
                commentsCount=comments_count,
                isLiked=is_liked,
                createdAt=post.created_at
            )
        )

    return feed_items