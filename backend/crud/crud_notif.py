"""
Nghiệp vụ truy vấn và tạo Thông báo (Notifications) trong SQLite.
Tự động tạo thông báo khi có tương tác Like, Comment, Friend Request, Friend Accept.
"""
from typing import Optional, List
import uuid
import time
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import desc

try:
    import models
    import schemas
except ImportError:
    from backend import models
    from backend import schemas


def create_notification(
    db: Session,
    recipient_id: str,
    sender_id: str,
    notif_type: str,
    post_id: Optional[str],
    message: str
) -> Optional[models.Notification]:
    """
    Hàm nền tảng tạo bản ghi thông báo trong CSDL SQLite:
    - Chặn tự gửi thông báo cho chính mình (recipient_id == sender_id).
    - Kiểm tra người nhận có tồn tại trong CSDL không.
    """
    # 1. Không gửi thông báo khi tự tương tác với bài viết của chính mình
    if recipient_id == sender_id:
        return None

    # 2. Kiểm tra người nhận tồn tại
    recipient = db.query(models.User).filter(models.User.id == recipient_id).first()
    if not recipient:
        return None

    notif_id = f"notif_{uuid.uuid4().hex[:8]}"
    created_at = int(time.time() * 1000)

    notif = models.Notification(
        id=notif_id,
        recipient_id=recipient_id,
        sender_id=sender_id,
        type=notif_type,
        post_id=post_id,
        message=message,
        is_read=False,
        created_at=created_at
    )
    db.add(notif)
    return notif


def create_like_notification(
    db: Session,
    post_id: str,
    user_id: str
) -> Optional[models.Notification]:
    """
    Tự động tạo Notification khi người dùng thả Like vào bài viết:
    - Lấy thông tin bài viết và tác giả.
    - Nếu tác giả khác người thả like -> tạo thông báo dạng LIKE.
    """
    post = db.query(models.Post).filter(models.Post.id == post_id).first()
    if not post or post.author_id == user_id:
        return None

    sender = db.query(models.User).filter(models.User.id == user_id).first()
    sender_name = sender.display_name if sender and sender.display_name else "Một người bạn"
    message = f"{sender_name} đã thích bài viết của bạn."

    return create_notification(
        db=db,
        recipient_id=post.author_id,
        sender_id=user_id,
        notif_type="LIKE",
        post_id=post_id,
        message=message
    )


def create_comment_notification(
    db: Session,
    post_id: str,
    author_id: str,
    content: str = ""
) -> Optional[models.Notification]:
    """
    Tự động tạo Notification khi người dùng bình luận vào bài viết:
    - Lấy thông tin bài viết và tác giả bài viết.
    - Nếu tác giả khác người bình luận -> tạo thông báo dạng COMMENT.
    """
    post = db.query(models.Post).filter(models.Post.id == post_id).first()
    if not post or post.author_id == author_id:
        return None

    sender = db.query(models.User).filter(models.User.id == author_id).first()
    sender_name = sender.display_name if sender and sender.display_name else "Một người bạn"
    message = f"{sender_name} đã bình luận về bài viết của bạn."

    return create_notification(
        db=db,
        recipient_id=post.author_id,
        sender_id=author_id,
        notif_type="COMMENT",
        post_id=post_id,
        message=message
    )


def get_user_notifications(
    db: Session,
    user_id: str,
    skip: int = 0,
    limit: int = 50
) -> List[schemas.NotificationResponse]:
    """
    Lấy danh sách thông báo gửi đến người dùng:
    - Eager load quan hệ sender để lấy thông tin avatar và tên hiển thị.
    - Sắp xếp mới nhất lên đầu (created_at DESC).
    """
    notifs = (
        db.query(models.Notification)
        .options(joinedload(models.Notification.sender))
        .filter(models.Notification.recipient_id == user_id)
        .order_by(desc(models.Notification.created_at))
        .offset(skip)
        .limit(limit)
        .all()
    )

    results = []
    for n in notifs:
        sender_name = n.sender.display_name if n.sender else "Người dùng Facebook"
        sender_avatar = n.sender.avatar_url if n.sender else None
        results.append(
            schemas.NotificationResponse(
                id=n.id,
                recipientId=n.recipient_id,
                senderId=n.sender_id,
                senderName=sender_name,
                senderAvatar=sender_avatar,
                type=n.type,
                targetPostId=n.post_id,
                message=n.message,
                isRead=bool(n.is_read),
                createdAt=n.created_at
            )
        )
    return results


def mark_notification_as_read(
    db: Session,
    notif_id: str,
    user_id: Optional[str] = None
) -> Optional[models.Notification]:
    """
    Đánh dấu một thông báo cụ thể là đã đọc (is_read = True).
    """
    query = db.query(models.Notification).filter(models.Notification.id == notif_id)
    if user_id:
        query = query.filter(models.Notification.recipient_id == user_id)

    notif = query.first()
    if notif:
        notif.is_read = True
        db.commit()
        db.refresh(notif)
    return notif


def mark_all_notifications_as_read(
    db: Session,
    user_id: str
) -> int:
    """
    Đánh dấu tất cả thông báo của một người dùng là đã đọc.
    """
    updated_count = (
        db.query(models.Notification)
        .filter(
            models.Notification.recipient_id == user_id,
            models.Notification.is_read == False
        )
        .update({"is_read": True})
    )
    db.commit()
    return updated_count
