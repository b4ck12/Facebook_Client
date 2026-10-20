from sqlalchemy import Column, String, Integer, Boolean, ForeignKey, UniqueConstraint, Text
from sqlalchemy.orm import relationship
from database import Base

"""Tạo bảng lưu trữ dữ liệu"""

class User(Base):
    """ thông tin tài khoản người dùng """
    __tablename__ = "users"

    # --- Thông tin cơ bản ---
    id = Column(String, primary_key=True)            
    email = Column(String, unique=True, nullable=False, index=True)
    display_name = Column(String, nullable=False)
    avatar_url = Column(String, nullable=True)
    cover_url = Column(String, nullable=True)
    created_at = Column(Integer, nullable=False)        

    # --- Mối quan hệ ---
    posts = relationship("Post", back_populates="author", cascade="all, delete-orphan")
    comments = relationship("Comment", back_populates="author", cascade="all, delete-orphan")
    likes = relationship("PostLike", back_populates="user", cascade="all, delete-orphan")

class Post(Base):
    """ bài viết của người dùng """
    __tablename__ = "posts"

    # --- Thông tin bài viết ---
    id = Column(String, primary_key=True)               
    author_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    content = Column(Text, nullable=True)              
    media_url = Column(String, nullable=True)         
    is_public = Column(Boolean, default=True, nullable=False)
    created_at = Column(Integer, nullable=False, index=True)

    # --- Mối quan hệ ---
    author = relationship("User", back_populates="posts")
    comments = relationship("Comment", back_populates="post", cascade="all, delete-orphan")
    likes = relationship("PostLike", back_populates="post", cascade="all, delete-orphan")

class Comment(Base):
    """ bình luận dưới các bài viết """
    __tablename__ = "comments"

    id = Column(String, primary_key=True)            
    post_id = Column(String, ForeignKey("posts.id", ondelete="CASCADE"), nullable=False, index=True)
    author_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    content = Column(Text, nullable=False)             
    created_at = Column(Integer, nullable=False)

    post = relationship("Post", back_populates="comments")
    author = relationship("User", back_populates="comments")

class PostLike(Base):
    """ trung gian quản lý lượt thích bài viết (M-N)"""
    __tablename__ = "post_likes"

    id = Column(Integer, primary_key=True, autoincrement=True)
    post_id = Column(String, ForeignKey("posts.id", ondelete="CASCADE"), nullable=False, index=True)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    created_at = Column(Integer, nullable=False)

    # Đảm bảo mỗi người dùng chỉ bấm thích 1 bài viết duy nhất 1 lần
    __table_args__ = (
        UniqueConstraint("post_id", "user_id", name="uq_post_user_like"),
    )

    post = relationship("Post", back_populates="likes")
    user = relationship("User", back_populates="likes")

class Friendship(Base):
    """ quản lý mối quan hệ và yêu cầu kết bạn """
    __tablename__ = "friendships"

    id = Column(String, primary_key=True)               # Ví dụ: "fr_01"
    requester_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    receiver_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    # Trạng thái: PENDING (Chờ duyệt) | ACCEPTED (Đã đồng ý) | REJECTED (TỪ chối)
    status = Column(String, nullable=False, default="PENDING")  
    created_at = Column(Integer, nullable=False)

    __table_args__ = (
        UniqueConstraint("requester_id", "receiver_id", name="uq_friendship_requester_receiver"),
    )

    requester = relationship("User", foreign_keys=[requester_id])
    receiver = relationship("User", foreign_keys=[receiver_id])

class Notification(Base):
    """ lưu thông báo gửi tới người dùng """
    __tablename__ = "notifications"

    id = Column(String, primary_key=True)
    recipient_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    sender_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    type = Column(String, nullable=False)
    post_id = Column(String, ForeignKey("posts.id", ondelete="SET NULL"), nullable=True)
    message = Column(String, nullable=False)
    is_read = Column(Boolean, default=False, nullable=False)
    created_at = Column(Integer, nullable=False)

    recipient = relationship("User", foreign_keys=[recipient_id])
    sender = relationship("User", foreign_keys=[sender_id])
    post = relationship("Post")