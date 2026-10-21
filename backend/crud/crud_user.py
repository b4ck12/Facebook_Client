"""
Nghiệp vụ truy vấn CSDL SQLite cho phân hệ Người dùng (Users / Profile).
"""
from typing import Optional, List
from sqlalchemy.orm import Session
import models


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
