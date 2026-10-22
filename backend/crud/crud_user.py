"""
Nghiệp vụ truy vấn CSDL SQLite cho phân hệ Người dùng (Users / Profile).
"""
from typing import Optional, List
from sqlalchemy.orm import Session
import models

try:
    import schemas
except ImportError:
    from backend import schemas


def get_user_by_id(db: Session, user_id: str) -> Optional[models.User]:
    """
    Tìm người dùng theo khóa chính ID từ SQLite.
    """
    return db.query(models.User).filter(models.User.id == user_id).first()


def get_user_by_email(db: Session, email: str) -> Optional[models.User]:
    """
    Tìm người dùng theo địa chỉ email duy nhất.
    """
    return db.query(models.User).filter(models.User.email == email).first()


def get_all_users(db: Session, skip: int = 0, limit: int = 100) -> List[models.User]:
    """
    Lấy danh sách các tài khoản người dùng demo.
    """
    return db.query(models.User).offset(skip).limit(limit).all()


def update_user(db: Session, user_id: str, update_data: schemas.UserUpdate) -> Optional[models.User]:
    """
    Cập nhật thông tin người dùng (avatar_url, cover_url, display_name) trong SQLite.
    """
    user = get_user_by_id(db, user_id)
    if not user:
        return None

    if update_data.displayName is not None:
        user.display_name = update_data.displayName
    if update_data.avatarUrl is not None:
        user.avatar_url = update_data.avatarUrl
    if update_data.coverUrl is not None:
        user.cover_url = update_data.coverUrl

    db.commit()
    db.refresh(user)
    return user



def get_user_posts(
    db: Session,
    user_id: str,
    current_user_id: Optional[str] = None,
    skip: int = 0,
    limit: int = 50
) -> List[schemas.PostResponse]:
    """
    Lấy danh sách bài viết của người dùng (gọi sang crud_post).
    """
    try:
        from crud import crud_post
    except ImportError:
        from backend.crud import crud_post
    return crud_post.get_user_posts(db, user_id=user_id, current_user_id=current_user_id, skip=skip, limit=limit)


def get_user_photos(
    db: Session,
    user_id: str,
    current_user_id: Optional[str] = None,
    skip: int = 0,
    limit: int = 50
) -> List[schemas.PostResponse]:
    """
    Lấy danh sách ảnh của người dùng (gọi sang crud_post).
    """
    try:
        from crud import crud_post
    except ImportError:
        from backend.crud import crud_post
    return crud_post.get_user_photos(db, user_id=user_id, current_user_id=current_user_id, skip=skip, limit=limit)
