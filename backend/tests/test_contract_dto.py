"""
=============================================================================
Dự án: Facebook Mobile Client (Android Java + Python FastAPI & SQLite)
Module: backend/tests/test_contract_dto.py
Tác giả: Hà Đăng Huy (Git user: HuyHaDang) - Backend Engineer & QA Lead
Nhiệm vụ: [D6-HDH] Rà soát Swagger UI, Đối chiếu DTO camelCase & Kiểm tra Seed Data
Mục tiêu: Đảm bảo 100% API phản hồi đúng chuẩn camelCase tương thích tuyệt đối
          với Android Retrofit Gson DTO models (User, Post, Comment, FriendRequest,
          Notification, LikeToggleResponse).
=============================================================================
"""

import os
import sys
import time
import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

# Đảm bảo import được module backend
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
if BASE_DIR not in sys.path:
    sys.path.insert(0, BASE_DIR)

from main import app
from database import Base, get_db
import models

# Cấu hình in-memory SQLite với StaticPool cô lập hoàn toàn môi trường test
TEST_DATABASE_URL = "sqlite:///:memory:"
test_engine = create_engine(
    TEST_DATABASE_URL,
    connect_args={"check_same_thread": False},
    poolclass=StaticPool
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=test_engine)


@pytest.fixture(scope="function")
def db_session():
    """Khởi tạo schema và dữ liệu mẫu kiểm thử DTO contract trước mỗi test case."""
    Base.metadata.create_all(bind=test_engine)
    db = TestingSessionLocal()
    now = int(time.time() * 1000)

    # 1. Tạo 2 người dùng mẫu
    user1 = models.User(
        id="usr_contract_01",
        email="huy.hadang@facebook.test",
        display_name="Hà Đăng Huy",
        avatar_url="http://localhost:8000/static/uploads/avatar_huy.jpg",
        cover_url="http://localhost:8000/static/uploads/cover_huy.jpg",
        created_at=now - 100000
    )
    user2 = models.User(
        id="usr_contract_02",
        email="tung.duong@facebook.test",
        display_name="Tùng Dương",
        avatar_url="http://localhost:8000/static/uploads/avatar_duong.jpg",
        cover_url=None,
        created_at=now - 90000
    )
    db.add_all([user1, user2])
    db.commit()

    # 2. Tạo 1 bài viết mẫu
    post1 = models.Post(
        id="post_contract_01",
        author_id="usr_contract_01",
        content="Hôm nay trời nắng đẹp, kiểm thử API DTO camelCase thôi!",
        media_url="http://localhost:8000/static/uploads/post_sample.jpg",
        is_public=True,
        created_at=now - 50000
    )
    db.add(post1)
    db.commit()

    # 3. Tạo like mẫu
    like1 = models.PostLike(
        post_id="post_contract_01",
        user_id="usr_contract_02",
        created_at=now - 40000
    )
    db.add(like1)

    # 4. Tạo comment mẫu
    cmt1 = models.Comment(
        id="cmt_contract_01",
        post_id="post_contract_01",
        author_id="usr_contract_02",
        content="Bài viết kiểm thử rất trực quan!",
        created_at=now - 30000
    )
    db.add(cmt1)

    # 5. Tạo lời mời kết bạn mẫu: user2 gửi tới user1
    req1 = models.Friendship(
        id="frnd_contract_01",
        requester_id="usr_contract_02",
        receiver_id="usr_contract_01",
        status="PENDING",
        created_at=now - 20000
    )
    db.add(req1)

    # 6. Tạo thông báo mẫu: user2 tương tác gửi tới user1
    notif1 = models.Notification(
        id="notif_contract_01",
        recipient_id="usr_contract_01",
        sender_id="usr_contract_02",
        type="COMMENT",
        post_id="post_contract_01",
        message="Tùng Dương đã bình luận về bài viết của bạn.",
        is_read=False,
        created_at=now - 10000
    )
    db.add(notif1)
    db.commit()

    try:
        yield db
    finally:
        db.close()
        Base.metadata.drop_all(bind=test_engine)


@pytest.fixture(scope="function")
def client(db_session):
    """FastAPI TestClient với dependency override trỏ vào in-memory db_session."""
    def override_get_db():
        try:
            yield db_session
        finally:
            pass

    app.dependency_overrides[get_db] = override_get_db
    with TestClient(app) as test_client:
        yield test_client
    app.dependency_overrides.clear()


def test_user_dto_camel_case_keys(client):
    """Xác minh UserResponse serialization trả về đúng camelCase cho Android Gson."""
    # 1. Kiểm tra endpoint GET /api/users
    response = client.get("/api/users")
    assert response.status_code == 200
    users = response.json()
    assert isinstance(users, list)
    assert len(users) >= 2

    for user in users:
        # Bắt buộc có các khóa chuẩn camelCase
        assert "id" in user
        assert "email" in user
        assert "displayName" in user
        assert "avatarUrl" in user
        assert "coverUrl" in user
        assert "createdAt" in user

        # Tuyệt đối không để lọt khóa snake_case
        assert "display_name" not in user
        assert "avatar_url" not in user
        assert "cover_url" not in user
        assert "created_at" not in user

    # 2. Kiểm tra endpoint GET /api/users/{id}
    res_single = client.get("/api/users/usr_contract_01")
    assert res_single.status_code == 200
    single_user = res_single.json()
    assert single_user["id"] == "usr_contract_01"
    assert single_user["displayName"] == "Hà Đăng Huy"
    assert "display_name" not in single_user


def test_post_dto_camel_case_keys(client):
    """Xác minh PostResponse serialization trả về đúng camelCase cho Android Gson."""
    # 1. Kiểm tra endpoint GET /api/posts
    response = client.get("/api/posts?limit=5")
    assert response.status_code == 200
    posts = response.json()
    assert isinstance(posts, list)
    assert len(posts) >= 1

    post = posts[0]
    # Khóa camelCase theo đúng Android Post.java
    assert "id" in post
    assert "authorId" in post
    assert "authorName" in post
    assert "authorAvatar" in post
    assert "content" in post
    assert "mediaUrl" in post
    assert "isPublic" in post
    assert "likesCount" in post
    assert "commentsCount" in post
    assert "isLiked" in post
    assert "createdAt" in post

    # Tuyệt đối không để lọt snake_case
    assert "author_id" not in post
    assert "author_name" not in post
    assert "author_avatar" not in post
    assert "media_url" not in post
    assert "is_public" not in post
    assert "likes_count" not in post
    assert "comments_count" not in post
    assert "is_liked" not in post
    assert "created_at" not in post

    # 2. Kiểm tra endpoint GET /api/users/{id}/posts
    res_user_posts = client.get("/api/users/usr_contract_01/posts")
    assert res_user_posts.status_code == 200
    u_posts = res_user_posts.json()
    assert len(u_posts) >= 1
    assert "authorId" in u_posts[0]
    assert "author_id" not in u_posts[0]


def test_comment_dto_camel_case_keys(client):
    """Xác minh CommentResponse serialization trả về đúng camelCase cho Android Gson."""
    response = client.get("/api/posts/post_contract_01/comments")
    assert response.status_code == 200
    comments = response.json()
    assert isinstance(comments, list)
    assert len(comments) >= 1

    cmt = comments[0]
    assert "id" in cmt
    assert "postId" in cmt
    assert "authorId" in cmt
    assert "authorName" in cmt
    assert "authorAvatar" in cmt
    assert "content" in cmt
    assert "createdAt" in cmt

    # Không để lọt snake_case
    assert "post_id" not in cmt
    assert "author_id" not in cmt
    assert "author_name" not in cmt
    assert "created_at" not in cmt


def test_friend_request_dto_camel_case_keys(client):
    """Xác minh FriendRequestResponse serialization trả về đúng camelCase cho Android Gson."""
    # Kiểm tra hỗ trợ cả user_id và userId query param
    response = client.get("/api/friends/requests?user_id=usr_contract_01")
    assert response.status_code == 200
    requests = response.json()
    assert isinstance(requests, list)
    assert len(requests) >= 1

    req = requests[0]
    assert "id" in req
    assert "requesterId" in req
    assert "requesterName" in req
    assert "requesterAvatar" in req
    assert "receiverId" in req
    assert "status" in req
    assert "createdAt" in req

    # Không để lọt snake_case
    assert "requester_id" not in req
    assert "requester_name" not in req
    assert "receiver_id" not in req
    assert "created_at" not in req


def test_notification_dto_camel_case_keys(client):
    """Xác minh NotificationResponse serialization trả về đúng camelCase cho Android Gson."""
    response = client.get("/api/notifications?user_id=usr_contract_01")
    assert response.status_code == 200
    notifs = response.json()
    assert isinstance(notifs, list)
    assert len(notifs) >= 1

    notif = notifs[0]
    assert "id" in notif
    assert "recipientId" in notif
    assert "senderId" in notif
    assert "senderName" in notif
    assert "senderAvatar" in notif
    assert "type" in notif
    assert "targetPostId" in notif
    assert "message" in notif
    assert "isRead" in notif
    assert "createdAt" in notif

    # Không để lọt snake_case
    assert "recipient_id" not in notif
    assert "sender_id" not in notif
    assert "sender_name" not in notif
    assert "target_post_id" not in notif
    assert "is_read" not in notif
    assert "created_at" not in notif


def test_like_toggle_dto_camel_case_keys(client):
    """Xác minh LikeToggleResponse serialization trả về đúng camelCase cho Android Gson."""
    response = client.post("/api/posts/post_contract_01/like?user_id=usr_contract_02")
    assert response.status_code == 200
    data = response.json()
    assert "isLiked" in data
    assert "likesCount" in data
    assert "is_liked" not in data
    assert "likes_count" not in data
