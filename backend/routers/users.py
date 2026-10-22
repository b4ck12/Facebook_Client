"""
Router xử lý các API liên quan đến Người dùng và Trang cá nhân (Users / Profile).
"""
from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

try:
    from database import get_db
    import models
    import schemas
    from crud import crud_user
except ImportError:
    from backend.database import get_db
    from backend import models
    from backend import schemas
    from backend.crud import crud_user

router = APIRouter()


@router.get("", response_model=List[schemas.UserResponse], status_code=status.HTTP_200_OK)
@router.get("/", response_model=List[schemas.UserResponse], status_code=status.HTTP_200_OK, include_in_schema=False)
def get_all_users(db: Session = Depends(get_db)):
    """
    Lấy danh sách các tài khoản người dùng demo trong hệ thống.
    """
    return crud_user.get_all_users(db)


@router.get("/{id}", response_model=schemas.UserResponse, status_code=status.HTTP_200_OK)
def get_user_profile(id: str, db: Session = Depends(get_db)):
    """
    Lấy thông tin cá nhân chi tiết của người dùng từ CSDL SQLite theo mã ID.
    Trả về thông tin bao gồm id, email, displayName, avatarUrl, coverUrl, createdAt.
    """
    user = crud_user.get_user_by_id(db, id)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Không tìm thấy người dùng có ID '{id}'"
        )
    return user


@router.put("/{id}", response_model=schemas.UserResponse, status_code=status.HTTP_200_OK)
def update_user_profile(
    id: str,
    update_data: schemas.UserUpdate,
    db: Session = Depends(get_db)
):
    """
    Cập nhật thông tin người dùng (avatar_url, cover_url, display_name) trong CSDL SQLite.
    Trả về thông tin người dùng sau khi đã cập nhật thành công.
    """
    user = crud_user.update_user(db, id, update_data)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Không tìm thấy người dùng có ID '{id}'"
        )
    return user
