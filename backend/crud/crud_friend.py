"""
CRUD operations cho phân hệ Bạn bè (Friends) và Tìm kiếm bạn bè (Search).
Phụ trách: Hà Đăng Huy (Backend Engineer & QA Lead / API Tester)
"""
import uuid
import time
from typing import Optional, List
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import desc, or_, and_
from fastapi import HTTPException, status

try:
    import models
    import schemas
except ImportError:
    from backend import models
    from backend import schemas


def get_friend_requests(
    db: Session,
    user_id: Optional[str] = None,
    skip: int = 0,
    limit: int = 20
) -> List[schemas.FriendRequestResponse]:
    """
    Lấy danh sách lời mời kết bạn đang chờ duyệt (status = 'PENDING').
    Sử dụng joinedload để lấy thông tin người gửi (avatar, display_name) không bị N+1 query.
    """
    query = (
        db.query(models.Friendship)
        .options(joinedload(models.Friendship.requester))
        .filter(models.Friendship.status == "PENDING")
    )

    if user_id and str(user_id).strip():
        query = query.filter(models.Friendship.receiver_id == str(user_id).strip())

    friendships = (
        query.order_by(desc(models.Friendship.created_at))
        .offset(skip)
        .limit(limit)
        .all()
    )

    result = []
    for f in friendships:
        requester_name = f.requester.display_name if f.requester else "Người dùng Facebook"
        requester_avatar = f.requester.avatar_url if f.requester else None

        result.append(schemas.FriendRequestResponse(
            id=f.id,
            requesterId=f.requester_id,
            requesterName=requester_name,
            requesterAvatar=requester_avatar,
            receiverId=f.receiver_id,
            status=f.status,
            createdAt=f.created_at
        ))
    return result


def accept_friend_request(
    db: Session,
    request_id: Optional[str] = None,
    user_id: Optional[str] = None,
    current_user_id: Optional[str] = None
) -> dict:
    """
    Chấp nhận lời mời kết bạn (status -> ACCEPTED).
    Đồng thời tạo thông báo FRIEND_ACCEPT gửi tới người yêu cầu.
    """
    friendship = None
    if request_id:
        friendship = db.query(models.Friendship).filter(models.Friendship.id == request_id).first()

    effective_curr_id = current_user_id or "user_current"
    if not friendship and user_id:
        friendship = db.query(models.Friendship).filter(
            models.Friendship.requester_id == user_id,
            models.Friendship.receiver_id == effective_curr_id,
            models.Friendship.status == "PENDING"
        ).first()

    if not friendship:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Lời mời kết bạn không tồn tại"
        )

    if friendship.status == "ACCEPTED":
        return {
            "success": True,
            "message": "Lời mời kết bạn đã được chấp nhận trước đó",
            "status": "ACCEPTED"
        }

    friendship.status = "ACCEPTED"

    # Tạo thông báo cho người gửi lời mời
    receiver = db.query(models.User).filter(models.User.id == friendship.receiver_id).first()
    sender_name = receiver.display_name if receiver else "Một người bạn"

    notif = models.Notification(
        id=f"notif_{uuid.uuid4().hex[:8]}",
        recipient_id=friendship.requester_id,
        sender_id=friendship.receiver_id,
        type="FRIEND_ACCEPT",
        post_id=None,
        message=f"{sender_name} đã chấp nhận lời mời kết bạn của bạn.",
        is_read=False,
        created_at=int(time.time() * 1000)
    )
    db.add(notif)
    db.commit()

    return {
        "success": True,
        "message": "Đã chấp nhận lời mời kết bạn",
        "status": "ACCEPTED"
    }


def reject_friend_request(
    db: Session,
    request_id: Optional[str] = None,
    user_id: Optional[str] = None,
    current_user_id: Optional[str] = None
) -> dict:
    """
    Từ chối hoặc xóa lời mời kết bạn (status -> REJECTED).
    """
    friendship = None
    if request_id:
        friendship = db.query(models.Friendship).filter(models.Friendship.id == request_id).first()

    effective_curr_id = current_user_id or "user_current"
    if not friendship and user_id:
        friendship = db.query(models.Friendship).filter(
            models.Friendship.requester_id == user_id,
            models.Friendship.receiver_id == effective_curr_id
        ).first()

    if not friendship:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Lời mời kết bạn không tồn tại"
        )

    if friendship.status == "REJECTED":
        return {
            "success": True,
            "message": "Lời mời kết bạn đã được từ chối trước đó",
            "status": "REJECTED"
        }

    friendship.status = "REJECTED"
    db.commit()

    return {
        "success": True,
        "message": "Đã từ chối lời mời kết bạn",
        "status": "REJECTED"
    }


def search_friends(
    db: Session,
    query: str,
    user_id: Optional[str] = None,
    limit: int = 20
) -> List[schemas.UserResponse]:
    """
    Tìm kiếm bạn bè theo tên (SQL LIKE không phân biệt chữ hoa thường).
    Nếu truyền user_id, tự động loại trừ chính người đó khỏi kết quả tìm kiếm.
    """
    if not query or not query.strip():
        return []

    search_term = f"%{query.strip()}%"
    q = db.query(models.User).filter(models.User.display_name.ilike(search_term))

    if user_id:
        q = q.filter(models.User.id != user_id)

    users = q.limit(limit).all()
    return [schemas.UserResponse.model_validate(u) for u in users]


def get_friends_list(
    db: Session,
    user_id: str,
    skip: int = 0,
    limit: int = 50
) -> List[schemas.UserResponse]:
    """
    Lấy danh sách tất cả bạn bè hiện tại (status = 'ACCEPTED') của một người dùng.
    """
    friendships = (
        db.query(models.Friendship)
        .filter(
            models.Friendship.status == "ACCEPTED",
            or_(
                models.Friendship.requester_id == user_id,
                models.Friendship.receiver_id == user_id
            )
        )
        .offset(skip)
        .limit(limit)
        .all()
    )

    friend_ids = []
    for f in friendships:
        if f.requester_id == user_id:
            friend_ids.append(f.receiver_id)
        else:
            friend_ids.append(f.requester_id)

    if not friend_ids:
        return []

    friends = db.query(models.User).filter(models.User.id.in_(friend_ids)).all()
    return [schemas.UserResponse.model_validate(u) for u in friends]


def send_friend_request(
    db: Session,
    request_data: schemas.FriendRequestCreate
) -> schemas.FriendRequestResponse:
    """
    Gửi lời mời kết bạn mới.
    Kiểm tra không tự kết bạn, kiểm tra quan hệ trùng lặp, và tạo thông báo FRIEND_REQUEST.
    """
    if request_data.requesterId == request_data.receiverId:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Không thể gửi lời mời kết bạn cho chính mình"
        )

    requester = db.query(models.User).filter(models.User.id == request_data.requesterId).first()
    receiver = db.query(models.User).filter(models.User.id == request_data.receiverId).first()

    if not requester:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Người gửi yêu cầu '{request_data.requesterId}' không tồn tại"
        )
    if not receiver:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Người nhận yêu cầu '{request_data.receiverId}' không tồn tại"
        )

    # Kiểm tra quan hệ đã tồn tại trong bảng friendships
    existing = db.query(models.Friendship).filter(
        or_(
            and_(
                models.Friendship.requester_id == request_data.requesterId,
                models.Friendship.receiver_id == request_data.receiverId
            ),
            and_(
                models.Friendship.requester_id == request_data.receiverId,
                models.Friendship.receiver_id == request_data.requesterId
            )
        )
    ).first()

    now = int(time.time() * 1000)

    if existing:
        if existing.status == "ACCEPTED":
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Hai người đã là bạn bè của nhau"
            )
        elif existing.status == "PENDING":
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Lời mời kết bạn đã tồn tại và đang chờ duyệt"
            )
        else:
            # Nếu đã từng bị REJECTED -> kích hoạt lại thành PENDING
            existing.requester_id = request_data.requesterId
            existing.receiver_id = request_data.receiverId
            existing.status = "PENDING"
            existing.created_at = now
            db_friendship = existing
    else:
        db_friendship = models.Friendship(
            id=f"frnd_{uuid.uuid4().hex[:8]}",
            requester_id=request_data.requesterId,
            receiver_id=request_data.receiverId,
            status="PENDING",
            created_at=now
        )
        db.add(db_friendship)

    # Tạo thông báo cho người nhận
    notif = models.Notification(
        id=f"notif_{uuid.uuid4().hex[:8]}",
        recipient_id=request_data.receiverId,
        sender_id=request_data.requesterId,
        type="FRIEND_REQUEST",
        post_id=None,
        message=f"{requester.display_name} đã gửi cho bạn một lời mời kết bạn.",
        is_read=False,
        created_at=now
    )
    db.add(notif)
    db.commit()
    db.refresh(db_friendship)

    return schemas.FriendRequestResponse(
        id=db_friendship.id,
        requesterId=db_friendship.requester_id,
        requesterName=requester.display_name,
        requesterAvatar=requester.avatar_url,
        receiverId=db_friendship.receiver_id,
        status=db_friendship.status,
        createdAt=db_friendship.created_at
    )
