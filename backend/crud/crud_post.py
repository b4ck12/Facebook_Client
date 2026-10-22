from typing import Optional, List
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import select, func, desc, literal
import uuid
import time
from fastapi import HTTPException, status

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

def toggle_like(db: Session, post_id: str, user_id: str) -> schemas.LikeToggleResponse:
    """
    Xử lý bật/tắt trạng thái Thích của bài viết trong SQLite:
    - Nếu đã like -> Xóa khỏi bảng post_likes
    - Nếu chưa like -> Thêm vào bảng post_likes + gửi thông báo cho tác giả
    """

    #1. Kiểm tra bài viết tồn tại
    post = db.query(models.Post).filter(models.Post.id == post_id).first()
    if not post:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Không tìm thấy bài viết ID'{post_id}'"
        )

    #2. Kiểm tra người dùng tồn tại
    user = db.query(models.User).filter(models.User.id == user_id).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Không tìm thấy người dùng ID'{user_id}'"
        )

    #3. Kiểm tra xem người dùng đã like bài này chưa
    existing_like = db.query(models.PostLike).filter(
        models.PostLike.post_id == post_id,
        models.PostLike.user_id == user_id
    ).first()

    if existing_like:
        db.delete(existing_like)
        db.commit()
        is_liked = False
    else:
        new_like = models.PostLike(
            post_id = post_id,
            user_id = user_id,
            created_at = int(time.time() * 1000)
        )
        db.add(new_like)

        #Tạo thông báo cho tác giả bài viết (nếu không phải tự like)
        if post.author_id != user_id:
            sender_name = user.display_name if user.display_name else "Một người bạn"
            notif = models.Notification(
                id=f"notif_{uuid.uuid4().hex[:8]}",
                recipient_id=post.author_id,
                sender_id=user_id,
                type="LIKE",
                post_id=post_id,
                message=f"{sender_name} đã thích bài viết của bạn.",
                is_read=False,
                created_at=int(time.time() * 1000)
            )
            db.add(notif)
        is_liked = True

    db.commit()
    
    #4. Đếm tổng số Like hiện tại
    likes_count = db.query(models.PostLike).filter(models.PostLike.post_id == post_id).count()

    return schemas.LikeToggleResponse(
        isLiked=is_liked,
        likesCount=likes_count
    )



def get_user_posts(
    db: Session,
    user_id: str,
    current_user_id: Optional[str] = None,
    skip: int = 0,
    limit: int = 50
) -> List[schemas.PostResponse]:
    """
    Lấy danh sách bài viết của một người dùng cụ thể trên trang cá nhân:
    - Sắp xếp bài viết mới nhất trước (created_at DESC).
    - Tính toán likes_count, comments_count và is_liked theo current_user_id.
    """
    likes_sub = (
        select(func.count(models.PostLike.id))
        .where(models.PostLike.post_id == models.Post.id)
        .correlate(models.Post)
        .scalar_subquery()
    )

    comments_sub = (
        select(func.count(models.Comment.id))
        .where(models.Comment.post_id == models.Post.id)
        .correlate(models.Post)
        .scalar_subquery()
    )

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

    stmt = (
        select(
            models.Post,
            likes_sub.label("likes_count"),
            comments_sub.label("comments_count"),
            is_liked_sub.label("is_liked")
        )
        .options(joinedload(models.Post.author))
        .where(models.Post.author_id == user_id)
        .order_by(desc(models.Post.created_at))
        .offset(skip)
        .limit(limit)
    )

    results = db.execute(stmt).all()
    user_posts = []
    for row in results:
        post = row[0]
        likes_count = row[1] or 0
        comments_count = row[2] or 0
        is_liked = bool(row[3])

        author_name = post.author.display_name if post.author else "Người dùng Facebook"
        author_avatar = post.author.avatar_url if post.author else None

        user_posts.append(
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

    return user_posts


def get_user_photos(
    db: Session,
    user_id: str,
    current_user_id: Optional[str] = None,
    skip: int = 0,
    limit: int = 50
) -> List[schemas.PostResponse]:
    """
    Lấy danh sách các bài viết có hình ảnh của một người dùng (Tab Ảnh trang cá nhân):
    - Lọc các bài viết có media_url IS NOT NULL và media_url != ''.
    - Sắp xếp ảnh mới nhất trước (created_at DESC).
    - Tính toán likes_count, comments_count và is_liked theo current_user_id.
    """
    likes_sub = (
        select(func.count(models.PostLike.id))
        .where(models.PostLike.post_id == models.Post.id)
        .correlate(models.Post)
        .scalar_subquery()
    )

    comments_sub = (
        select(func.count(models.Comment.id))
        .where(models.Comment.post_id == models.Post.id)
        .correlate(models.Post)
        .scalar_subquery()
    )

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

    stmt = (
        select(
            models.Post,
            likes_sub.label("likes_count"),
            comments_sub.label("comments_count"),
            is_liked_sub.label("is_liked")
        )
        .options(joinedload(models.Post.author))
        .where(
            models.Post.author_id == user_id,
            models.Post.media_url.isnot(None),
            models.Post.media_url != ""
        )
        .order_by(desc(models.Post.created_at))
        .offset(skip)
        .limit(limit)
    )

    results = db.execute(stmt).all()
    user_photos = []
    for row in results:
        post = row[0]
        likes_count = row[1] or 0
        comments_count = row[2] or 0
        is_liked = bool(row[3])

        author_name = post.author.display_name if post.author else "Người dùng Facebook"
        author_avatar = post.author.avatar_url if post.author else None

        user_photos.append(
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

    return user_photos
