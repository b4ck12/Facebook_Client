"""
Router xử lý các API liên quan đến Người dùng và Trang cá nhân (Users / Profile).
"""
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

try:
    from database import get_db
    import models
    import schemas
    from crud import crud_user, crud_post
except ImportError:
    from backend.database import get_db
    from backend import models
    from backend import schemas
    from backend.crud import crud_user, crud_post

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


@router.get("/{id}/posts", response_model=List[schemas.PostResponse], status_code=status.HTTP_200_OK)
def get_user_posts(
    id: str,
    current_user_id: Optional[str] = Query(None, description="Mã ID người dùng hiện tại để kiểm tra isLiked (snake_case)"),
    currentUserId: Optional[str] = Query(None, description="Mã ID người dùng hiện tại để kiểm tra isLiked (camelCase)"),
    skip: int = Query(0, ge=0, description="Vị trí bắt đầu phân trang"),
    limit: int = Query(50, ge=1, le=100, description="Số lượng bài viết tối đa cần lấy"),
    db: Session = Depends(get_db)
):
    """
    Lấy danh sách bài viết của một người dùng từ SQLite (Tab Tất cả bài viết trên Profile).
    Sắp xếp bài viết mới nhất lên đầu.
    """
    user = crud_user.get_user_by_id(db, id)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Không tìm thấy người dùng có ID '{id}'"
        )
    target_current_user_id = current_user_id or currentUserId
    return crud_post.get_user_posts(
        db=db,
        user_id=id,
        current_user_id=target_current_user_id,
        skip=skip,
        limit=limit
    )


@router.get("/{id}/photos", response_model=List[schemas.PostResponse], status_code=status.HTTP_200_OK)
def get_user_photos(
    id: str,
    current_user_id: Optional[str] = Query(None, description="Mã ID người dùng hiện tại để kiểm tra isLiked (snake_case)"),
    currentUserId: Optional[str] = Query(None, description="Mã ID người dùng hiện tại để kiểm tra isLiked (camelCase)"),
    skip: int = Query(0, ge=0, description="Vị trí bắt đầu phân trang"),
    limit: int = Query(50, ge=1, le=100, description="Số lượng ảnh tối đa cần lấy"),
    db: Session = Depends(get_db)
):
    """
    Lấy kho ảnh của người dùng từ SQLite (Tab Ảnh & Video trên Profile).
    Lọc các bài viết có media_url IS NOT NULL và media_url != ''. Sắp xếp mới nhất lên đầu.
    """
    user = crud_user.get_user_by_id(db, id)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Không tìm thấy người dùng có ID '{id}'"
        )
    target_current_user_id = current_user_id or currentUserId
    return crud_post.get_user_photos(
        db=db,
        user_id=id,
        current_user_id=target_current_user_id,
        skip=skip,
        limit=limit
    )
