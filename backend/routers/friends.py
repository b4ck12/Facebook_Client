"""
Router xử lý các API liên quan đến Bạn bè, Lời mời kết bạn và Tìm kiếm bạn bè.
Phụ trách: Hà Đăng Huy (Backend Engineer & QA Lead / API Tester)
"""
from typing import List, Optional
from fastapi import APIRouter, Depends, Query, Body, status
from sqlalchemy.orm import Session

try:
    from database import get_db
    import schemas
    from crud import crud_friend
except ImportError:
    from backend.database import get_db
    from backend import schemas
    from backend.crud import crud_friend

router = APIRouter()


# ============================================================================
# NGÀY 03 (D3-HDH): LẤY DANH SÁCH LỜI MỜI KẾT BẠN
# ============================================================================

@router.get(
    "/requests",
    response_model=List[schemas.FriendRequestResponse],
    status_code=status.HTTP_200_OK,
    summary="Lấy danh sách lời mời kết bạn gửi đến"
)
def get_friend_requests(
    user_id: Optional[str] = Query(None, description="Mã ID người nhận lời mời kết bạn (snake_case)"),
    userId: Optional[str] = Query(None, description="Mã ID người nhận lời mời kết bạn (camelCase)"),
    skip: int = Query(0, ge=0, description="Vị trí bắt đầu (phân trang)"),
    limit: int = Query(20, ge=1, le=100, description="Số lượng lời mời tối đa cần lấy"),
    db: Session = Depends(get_db)
):
    """
    Lấy danh sách các lời mời kết bạn đang ở trạng thái PENDING gửi đến người dùng.
    """
    target_user_id = user_id or userId
    return crud_friend.get_friend_requests(
        db=db,
        user_id=target_user_id,
        skip=skip,
        limit=limit
    )


# ============================================================================
# NGÀY 04 (D4-HDH): CHẤP NHẬN & TỪ CHỐI LỜI MỜI KẾT BẠN
# ============================================================================

@router.post(
    "/accept",
    response_model=schemas.FriendActionResponse,
    status_code=status.HTTP_200_OK,
    summary="Chấp nhận lời mời kết bạn"
)
def accept_friend_request(
    action_data: Optional[schemas.FriendActionRequest] = Body(None),
    request_id: Optional[str] = Query(None, description="Mã lời mời kết bạn (snake_case)"),
    requestId: Optional[str] = Query(None, description="Mã lời mời kết bạn (camelCase)"),
    user_id: Optional[str] = Query(None, description="Mã người gửi lời mời (snake_case)"),
    userId: Optional[str] = Query(None, description="Mã người gửi lời mời (camelCase)"),
    current_user_id: Optional[str] = Query(None, description="Mã người dùng hiện tại (snake_case)"),
    currentUserId: Optional[str] = Query(None, description="Mã người dùng hiện tại (camelCase)"),
    db: Session = Depends(get_db)
):
    """
    Chấp nhận lời mời kết bạn:
    - Cập nhật trạng thái status sang 'ACCEPTED' trong bảng friendships.
    - Tự động sinh thông báo 'FRIEND_ACCEPT' trong bảng notifications gửi tới người yêu cầu.
    """
    resolved_req_id = (action_data.requestId if (action_data and action_data.requestId) else None) or request_id or requestId
    resolved_user_id = (action_data.userId if (action_data and action_data.userId) else None) or user_id or userId
    resolved_curr_id = current_user_id or currentUserId

    return crud_friend.accept_friend_request(
        db=db,
        request_id=resolved_req_id,
        user_id=resolved_user_id,
        current_user_id=resolved_curr_id
    )


@router.post(
    "/reject",
    response_model=schemas.FriendActionResponse,
    status_code=status.HTTP_200_OK,
    summary="Từ chối hoặc xóa lời mời kết bạn"
)
def reject_friend_request(
    action_data: Optional[schemas.FriendActionRequest] = Body(None),
    request_id: Optional[str] = Query(None, description="Mã lời mời kết bạn (snake_case)"),
    requestId: Optional[str] = Query(None, description="Mã lời mời kết bạn (camelCase)"),
    user_id: Optional[str] = Query(None, description="Mã người gửi lời mời (snake_case)"),
    userId: Optional[str] = Query(None, description="Mã người gửi lời mời (camelCase)"),
    current_user_id: Optional[str] = Query(None, description="Mã người dùng hiện tại (snake_case)"),
    currentUserId: Optional[str] = Query(None, description="Mã người dùng hiện tại (camelCase)"),
    db: Session = Depends(get_db)
):
    """
    Từ chối lời mời kết bạn:
    - Cập nhật trạng thái status sang 'REJECTED' trong bảng friendships.
    """
    resolved_req_id = (action_data.requestId if (action_data and action_data.requestId) else None) or request_id or requestId
    resolved_user_id = (action_data.userId if (action_data and action_data.userId) else None) or user_id or userId
    resolved_curr_id = current_user_id or currentUserId

    return crud_friend.reject_friend_request(
        db=db,
        request_id=resolved_req_id,
        user_id=resolved_user_id,
        current_user_id=resolved_curr_id
    )

@router.post(
    "/{request_id}/accept",
    response_model=schemas.FriendActionResponse,
    status_code=status.HTTP_200_OK,
    summary="Chấp nhận lời mời kết bạn qua Path",
    include_in_schema=False
)
@router.post(
    "/requests/{request_id}/accept",
    response_model=schemas.FriendActionResponse,
    status_code=status.HTTP_200_OK,
    summary="Chấp nhận lời mời kết bạn qua Requests Path",
    include_in_schema=False
)
def accept_friend_request_by_path(
    request_id: str,
    current_user_id: Optional[str] = Query(None),
    currentUserId: Optional[str] = Query(None),
    db: Session = Depends(get_db)
):
    resolved_curr_id = current_user_id or currentUserId
    return crud_friend.accept_friend_request(
        db=db,
        request_id=request_id,
        current_user_id=resolved_curr_id
    )


@router.post(
    "/{request_id}/reject",
    response_model=schemas.FriendActionResponse,
    status_code=status.HTTP_200_OK,
    summary="Từ chối hoặc xóa lời mời kết bạn qua Path",
    include_in_schema=False
)
@router.post(
    "/requests/{request_id}/reject",
    response_model=schemas.FriendActionResponse,
    status_code=status.HTTP_200_OK,
    summary="Từ chối hoặc xóa lời mời kết bạn qua Requests Path",
    include_in_schema=False
)
def reject_friend_request_by_path(
    request_id: str,
    current_user_id: Optional[str] = Query(None),
    currentUserId: Optional[str] = Query(None),
    db: Session = Depends(get_db)
):
    resolved_curr_id = current_user_id or currentUserId
    return crud_friend.reject_friend_request(
        db=db,
        request_id=request_id,
        current_user_id=resolved_curr_id
    )



# ============================================================================
# NGÀY 05 (D5-HDH): TÌM KIẾM BẠN BÈ & HOÀN THIỆN HỢP ĐỒNG API
# ============================================================================

@router.get(
    "/search",
    response_model=List[schemas.UserResponse],
    status_code=status.HTTP_200_OK,
    summary="Tìm kiếm bạn bè theo tên (SQL LIKE)"
)
def search_friends(
    q: str = Query("", description="Từ khóa tìm kiếm theo tên người dùng"),
    user_id: Optional[str] = Query(None, description="Mã người dùng hiện tại để loại trừ chính mình (snake_case)"),
    userId: Optional[str] = Query(None, description="Mã người dùng hiện tại để loại trừ chính mình (camelCase)"),
    limit: int = Query(20, ge=1, le=50, description="Số lượng kết quả tối đa"),
    db: Session = Depends(get_db)
):
    """
    Tìm kiếm người dùng/bạn bè theo tên không phân biệt chữ hoa chữ thường.
    Loại trừ chính mình khỏi kết quả nếu truyền user_id hoặc userId.
    """
    target_user_id = user_id or userId
    return crud_friend.search_friends(
        db=db,
        query=q,
        user_id=target_user_id,
        limit=limit
    )


@router.get(
    "",
    response_model=List[schemas.UserResponse],
    status_code=status.HTTP_200_OK,
    summary="Lấy danh sách bạn bè hiện tại"
)
@router.get(
    "/",
    response_model=List[schemas.UserResponse],
    status_code=status.HTTP_200_OK,
    include_in_schema=False
)
def get_friends(
    user_id: Optional[str] = Query(None, description="Mã người dùng cần lấy danh sách bạn bè (snake_case)"),
    userId: Optional[str] = Query(None, description="Mã người dùng cần lấy danh sách bạn bè (camelCase)"),
    skip: int = Query(0, ge=0),
    limit: int = Query(50, ge=1, le=100),
    db: Session = Depends(get_db)
):
    """
    Lấy danh sách toàn bộ bạn bè hiện tại (đã ACCEPTED) của người dùng.
    """
    target_user_id = user_id or userId
    if not target_user_id:
        from fastapi import HTTPException
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Tham số user_id hoặc userId là bắt buộc!"
        )
    return crud_friend.get_friends_list(
        db=db,
        user_id=target_user_id,
        skip=skip,
        limit=limit
    )


@router.post(
    "/requests",
    response_model=schemas.FriendRequestResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Gửi lời mời kết bạn mới"
)
def send_friend_request(
    request_data: schemas.FriendRequestCreate,
    db: Session = Depends(get_db)
):
    """
    Gửi lời mời kết bạn mới đến một người dùng khác:
    - Tạo bản ghi mới trong bảng friendships với status='PENDING'.
    - Tự động sinh thông báo 'FRIEND_REQUEST' cho người nhận.
    """
    return crud_friend.send_friend_request(
        db=db,
        request_data=request_data
    )
