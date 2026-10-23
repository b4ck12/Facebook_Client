"""
Router xử lý các API liên quan đến Hệ thống Thông báo (Notifications).
"""
from typing import List, Optional
from fastapi import APIRouter, Depends, Query, HTTPException, status
from sqlalchemy.orm import Session

try:
    from database import get_db
    import schemas
    from crud import crud_notif
except ImportError:
    from backend.database import get_db
    from backend import schemas
    from backend.crud import crud_notif

router = APIRouter()


@router.get("", response_model=List[schemas.NotificationResponse], status_code=status.HTTP_200_OK)
@router.get("/", response_model=List[schemas.NotificationResponse], status_code=status.HTTP_200_OK, include_in_schema=False)
def get_user_notifications(
    user_id: Optional[str] = Query(None, description="Mã ID người dùng cần lấy thông báo (snake_case)"),
    userId: Optional[str] = Query(None, description="Mã ID người dùng cần lấy thông báo (camelCase)"),
    skip: int = Query(0, ge=0, description="Vị trí bắt đầu phân trang"),
    limit: int = Query(50, ge=1, le=100, description="Số lượng thông báo tối đa"),
    db: Session = Depends(get_db)
):
    """
    Lấy danh sách thông báo của người dùng:
    - Sắp xếp mới nhất lên đầu.
    - Trả về chi tiết người gửi (tên, avatar) và loại thông báo (LIKE, COMMENT, FRIEND_REQUEST, FRIEND_ACCEPT).
    """
    target_user_id = user_id or userId
    if not target_user_id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Vui lòng cung cấp user_id qua query parameter (?user_id=...)"
        )
    return crud_notif.get_user_notifications(db=db, user_id=target_user_id, skip=skip, limit=limit)


@router.post("/{id}/read", status_code=status.HTTP_200_OK)
def mark_notification_as_read(
    id: str,
    db: Session = Depends(get_db)
):
    """
    Đánh dấu một thông báo cụ thể là đã đọc (is_read = true).
    """
    notif = crud_notif.mark_notification_as_read(db=db, notif_id=id)
    if not notif:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Không tìm thấy thông báo có ID '{id}'"
        )
    return {"success": True, "message": "Thông báo đã được đánh dấu đã đọc", "notifId": id}


@router.post("/read-all", status_code=status.HTTP_200_OK)
def mark_all_notifications_as_read(
    user_id: Optional[str] = Query(None, description="Mã ID người dùng (snake_case)"),
    userId: Optional[str] = Query(None, description="Mã ID người dùng (camelCase)"),
    db: Session = Depends(get_db)
):
    """
    Đánh dấu tất cả thông báo của người dùng là đã đọc.
    """
    target_user_id = user_id or userId
    if not target_user_id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Vui lòng cung cấp user_id qua query parameter (?user_id=...)"
        )
    count = crud_notif.mark_all_notifications_as_read(db=db, user_id=target_user_id)
    return {"success": True, "message": f"Đã đánh dấu {count} thông báo là đã đọc"}
