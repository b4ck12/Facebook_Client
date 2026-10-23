from typing import Optional, List
from pydantic import BaseModel, ConfigDict, AliasGenerator, AliasChoices
from pydantic.alias_generators import to_snake

# Cấu hình tự động chuyển đổi giữa snake_case (SQLAlchemy) và camelCase (JSON/Android)
ORMBaseConfig = ConfigDict(
    from_attributes=True,
    populate_by_name=True,
    alias_generator=AliasGenerator(
        validation_alias=lambda field_name: AliasChoices(field_name, to_snake(field_name)),
        serialization_alias=lambda field_name: field_name
    )
)

#Xác thực dữ liệu đầu vào và định dạng đầu ra 

#1.USER SCHEMA

class UserBase(BaseModel):
    email: str
    displayName: str
    avatarUrl: Optional[str] = None
    coverUrl: Optional[str] = None

class UserCreated(UserBase):
    id: str

class UserUpdate(BaseModel):
    displayName: Optional[str] = None
    avatarUrl: Optional[str] = None
    coverUrl: Optional[str] = None

    model_config = ORMBaseConfig

class UserResponse(UserBase):
    id: str
    createdAt: int

    # Cho phép đọc dữ liệu trực tiếp từ SQLAlchemy ORM Model
    model_config = ORMBaseConfig

#2.POST SCHEMAS

class PostCreate(BaseModel):
    authorId: str
    content: Optional[str] = None
    mediaUrl: Optional[str] = None
    isPublic: bool = True

class PostResponse(BaseModel):
    id: str
    authorId: str
    authorName: str
    authorAvatar: Optional[str] = None
    content: Optional[str] = None
    mediaUrl: Optional[str] = None
    isPublic: bool
    likesCount: int = 0
    commentsCount: int = 0
    isLiked: bool = False
    createdAt: int

    model_config = ConfigDict(from_attributes=True)

#3.COMMENT SCHEMAS
class CommentCreate(BaseModel):
    authorId: str
    content: str

    model_config = ORMBaseConfig

class CommentResponse(BaseModel):
    id: str
    postId: str
    authorId: str
    authorName: str
    authorAvatar: Optional[str] = None
    content: str
    createdAt: int

    model_config = ORMBaseConfig

#4.LIKE SCHEMAS
class LikeToggleResponse(BaseModel):
    isLiked: bool
    likesCount: int

#5.FRIENDSHIP SCHEMAS
class FriendRequestCreate(BaseModel):
    requesterId: str
    receiverId: str

class FriendActionRequest(BaseModel):
    requestId: Optional[str] = None
    userId: Optional[str] = None

class FriendActionResponse(BaseModel):
    success: bool
    message: str
    status: str

class FriendRequestResponse(BaseModel):
    id: str
    requesterId: str
    requesterName: str
    requesterAvatar: Optional[str] = None
    receiverId: str
    status: str
    createdAt: int

    model_config = ConfigDict(from_attributes=True)

#6.NOTIFICATION SCHEMAS
class NotificationResponse(BaseModel):
    id: str
    recipientId: str
    senderId: str
    senderName: str
    senderAvatar: Optional[str] = None
    type: str
    targetPostId: Optional[str] = None
    message: str
    isRead: bool
    createdAt: int

    model_config = ORMBaseConfig


# 7. UPLOAD SCHEMAS
class UploadResponse(BaseModel):
    url: str
    filename: str
