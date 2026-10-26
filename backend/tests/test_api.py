"""
=============================================================================
Dự án: Facebook Mobile Client (Android Java + Python FastAPI & SQLite)
Module: backend/tests/test_api.py
Tác giả: Hà Đăng Huy (Git user: HuyHaDang) - Backend Engineer & QA Lead
Nhiệm vụ: [D9-HDH] Viết Unit Test toàn diện cho tầng Backend (test_api.py)
Mục tiêu: Xây dựng bộ kiểm thử tự động toàn diện bao quát 100% 6 phân hệ cốt lõi
          của máy chủ FastAPI (Users, Posts & Likes, Comments, Notifications,
          Upload, Friends Search) trên môi trường In-Memory SQLite cô lập.
=============================================================================
"""

import io
import os
import sys
import time
import pytest
from fastapi.testclient import TestClient
from PIL import Image
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

# Cấu hình in-memory SQLite với StaticPool độc lập 100%
TEST_DB_URL = "sqlite:///:memory:"
engine = create_engine(
    TEST_DB_URL,
    connect_args={"check_same_thread": False},
    poolclass=StaticPool
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


@pytest.fixture(scope="function")
def db_session():
    """Khởi tạo schema CSDL test và nạp dữ liệu mẫu sạch trước mỗi test function."""
    Base.metadata.create_all(bind=engine)
    db = TestingSessionLocal()
    now = int(time.time() * 1000)

    # 1. Tạo 2 người dùng mẫu
    user1 = models.User(
        id="usr_test_01",
        email="taquanghuy@facebook.test",
        display_name="Tạ Quang Huy",
        avatar_url="http://localhost:8000/static/uploads/avatar_tqh.jpg",
        cover_url="http://localhost:8000/static/uploads/cover_tqh.jpg",
        created_at=now - 150000
    )
    user2 = models.User(
        id="usr_test_02",
        email="hadanghuy@facebook.test",
        display_name="Hà Đăng Huy",
        avatar_url="http://localhost:8000/static/uploads/avatar_hdh.jpg",
        cover_url=None,
        created_at=now - 120000
    )
    db.add_all([user1, user2])
    db.commit()

    # 2. Tạo 1 bài viết mẫu của user1
    post1 = models.Post(
        id="post_test_01",
        author_id="usr_test_01",
        content="Chào mừng các bạn đến với Facebook Client Test Suite!",
        media_url="http://localhost:8000/static/uploads/banner_test.jpg",
        is_public=True,
        created_at=now - 80000
    )
    db.add(post1)
    db.commit()

    # 3. Tạo 1 lượt thích mẫu: user2 thích bài viết post1
    like1 = models.PostLike(
        post_id="post_test_01",
        user_id="usr_test_02",
        created_at=now - 60000
    )
    db.add(like1)

    # 4. Tạo 1 bình luận mẫu: user2 bình luận bài post1
    cmt1 = models.Comment(
        id="cmt_test_01",
        post_id="post_test_01",
        author_id="usr_test_02",
        content="Tuyệt vời! Test suite chạy rất nhanh.",
        created_at=now - 40000
    )
    db.add(cmt1)

    # 5. Tạo 1 thông báo mẫu gửi tới user1 từ user2
    notif1 = models.Notification(
        id="notif_test_01",
        recipient_id="usr_test_01",
        sender_id="usr_test_02",
        type="LIKE",
        post_id="post_test_01",
        message="Hà Đăng Huy đã thích bài viết của bạn.",
        is_read=False,
        created_at=now - 20000
    )
    db.add(notif1)
    db.commit()

    try:
        yield db
    finally:
        db.close()
        Base.metadata.drop_all(bind=engine)


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


# ============================================================================
# 1. USERS SUITE (Phân hệ Người dùng & Trang cá nhân)
# ============================================================================

def test_get_all_users(client):
    """Kiểm tra lấy danh sách tài khoản người dùng demo (200 OK)."""
    response = client.get("/api/users")
    assert response.status_code == 200
    users = response.json()
    assert isinstance(users, list)
    assert len(users) >= 2
    assert any(u["id"] == "usr_test_01" for u in users)


def test_get_user_profile_success(client):
    """Kiểm tra lấy chi tiết trang cá nhân của người dùng tồn tại (200 OK)."""
    response = client.get("/api/users/usr_test_01")
    assert response.status_code == 200
    user = response.json()
    assert user["id"] == "usr_test_01"
    assert user["displayName"] == "Tạ Quang Huy"
    assert user["email"] == "taquanghuy@facebook.test"


def test_get_user_profile_not_found(client):
    """Truy vấn mã ID người dùng không tồn tại trả về lỗi 404 Not Found."""
    response = client.get("/api/users/usr_non_existent")
    assert response.status_code == 404
    assert "không tìm thấy" in response.json()["detail"].lower()


def test_update_user_profile(client):
    """Cập nhật thông tin cá nhân (displayName, avatarUrl) thành công (200 OK)."""
    payload = {
        "displayName": "Tạ Quang Huy (QA Lead)",
        "avatarUrl": "http://localhost:8000/static/uploads/avatar_updated.jpg"
    }
    response = client.put("/api/users/usr_test_01", json=payload)
    assert response.status_code == 200
    updated_user = response.json()
    assert updated_user["displayName"] == "Tạ Quang Huy (QA Lead)"
    assert updated_user["avatarUrl"] == "http://localhost:8000/static/uploads/avatar_updated.jpg"


# ============================================================================
# 2. POSTS & LIKES SUITE (Phân hệ Bài viết, Thích & Chia sẻ)
# ============================================================================

def test_get_feed_posts_pagination(client):
    """Lấy danh sách bài viết trên Bảng tin có phân trang limit và skip (200 OK)."""
    response = client.get("/api/posts?limit=1&skip=0")
    assert response.status_code == 200
    posts = response.json()
    assert isinstance(posts, list)
    assert len(posts) <= 1


def test_create_post_success(client):
    """Đăng bài viết mới hợp lệ với text và hình ảnh (201 Created)."""
    payload = {
        "authorId": "usr_test_01",
        "content": "Bài viết mới thử nghiệm tự động!",
        "mediaUrl": "http://localhost:8000/static/uploads/test_upload.jpg",
        "isPublic": True
    }
    response = client.post("/api/posts", json=payload)
    assert response.status_code == 201
    new_post = response.json()
    assert new_post["authorId"] == "usr_test_01"
    assert new_post["content"] == payload["content"]
    assert new_post["mediaUrl"] == payload["mediaUrl"]
    assert new_post["likesCount"] == 0
    assert new_post["commentsCount"] == 0


def test_create_post_empty_bad_request(client):
    """Khóa/chặn đăng bài viết nếu cả text và media đều rỗng (400 Bad Request)."""
    payload = {
        "authorId": "usr_test_01",
        "content": "   ",
        "mediaUrl": ""
    }
    response = client.post("/api/posts", json=payload)
    assert response.status_code == 400
    assert "không để trống" in response.json()["detail"].lower()


def test_toggle_like_cycle(client):
    """
    Chu kỳ Toggle Like:
    - usr_test_02 ban đầu đã like post_test_01 -> Gọi like sẽ thành Unlike (isLiked=False).
    - Gọi tiếp lần nữa -> Trở lại trạng thái Like (isLiked=True).
    """
    # 1. Lần 1: Bỏ like (Unlike)
    res_unlike = client.post("/api/posts/post_test_01/like?user_id=usr_test_02")
    assert res_unlike.status_code == 200
    data_unlike = res_unlike.json()
    assert data_unlike["isLiked"] is False
    assert data_unlike["likesCount"] == 0

    # 2. Lần 2: Thả like lại (Like)
    res_like = client.post("/api/posts/post_test_01/like?user_id=usr_test_02")
    assert res_like.status_code == 200
    data_like = res_like.json()
    assert data_like["isLiked"] is True
    assert data_like["likesCount"] == 1


def test_share_post_creates_new_post(client):
    """Chia sẻ bài viết gốc -> Tạo bài viết mới chứa nội dung trích dẫn (201 Created)."""
    response = client.post(
        "/api/posts/post_test_01/share?user_id=usr_test_02&caption=Rất hay đáng học hỏi!"
    )
    assert response.status_code == 201
    shared_post = response.json()
    assert shared_post["authorId"] == "usr_test_02"
    assert "Rất hay đáng học hỏi!" in shared_post["content"]
    assert "Chia sẻ từ" in shared_post["content"]


# ============================================================================
# 3. COMMENTS SUITE (Phân hệ Bình luận Text-only)
# ============================================================================

def test_get_post_comments_sorted_asc(client):
    """Lấy danh sách bình luận sắp xếp thời gian tăng dần (cũ trước, mới sau)."""
    response = client.get("/api/posts/post_test_01/comments")
    assert response.status_code == 200
    comments = response.json()
    assert isinstance(comments, list)
    assert len(comments) >= 1
    assert comments[0]["id"] == "cmt_test_01"


def test_add_comment_success(client):
    """Thêm bình luận văn bản hợp lệ thành công (201 Created)."""
    payload = {
        "authorId": "usr_test_01",
        "content": "Cảm ơn bạn đã quan tâm!"
    }
    response = client.post("/api/posts/post_test_01/comments", json=payload)
    assert response.status_code in [200, 201]
    new_cmt = response.json()
    assert new_cmt["authorId"] == "usr_test_01"
    assert new_cmt["content"] == payload["content"]
    assert new_cmt["postId"] == "post_test_01"


def test_add_comment_empty_bad_request(client):
    """Gửi bình luận rỗng văn bản bị từ chối với lỗi 400 Bad Request."""
    payload = {
        "authorId": "usr_test_01",
        "content": "   "
    }
    response = client.post("/api/posts/post_test_01/comments", json=payload)
    assert response.status_code == 400
    assert "không được để trống" in response.json()["detail"].lower()


def test_add_comment_nonexistent_post(client):
    """Bình luận vào bài viết không tồn tại trả về lỗi 404 Not Found."""
    payload = {
        "authorId": "usr_test_01",
        "content": "Bình luận dạo"
    }
    response = client.post("/api/posts/post_fake_id/comments", json=payload)
    assert response.status_code == 404
    assert "không tìm thấy bài viết" in response.json()["detail"].lower()


# ============================================================================
# 4. NOTIFICATIONS SUITE (Phân hệ Hệ thống Thông báo)
# ============================================================================

def test_get_user_notifications(client):
    """Lấy danh sách thông báo gửi đến người dùng (200 OK)."""
    response = client.get("/api/notifications?user_id=usr_test_01")
    assert response.status_code == 200
    notifs = response.json()
    assert isinstance(notifs, list)
    assert len(notifs) >= 1
    assert notifs[0]["recipientId"] == "usr_test_01"
    assert notifs[0]["type"] == "LIKE"


def test_mark_single_notification_read(client):
    """Đánh dấu một thông báo cụ thể là đã đọc (/read) thành công (200 OK)."""
    response = client.post("/api/notifications/notif_test_01/read")
    assert response.status_code == 200
    assert response.json()["success"] is True

    # Xác minh lại thông báo đã có isRead = True
    notifs = client.get("/api/notifications?user_id=usr_test_01").json()
    target_notif = next(n for n in notifs if n["id"] == "notif_test_01")
    assert target_notif["isRead"] is True


def test_mark_all_notifications_read(client):
    """Đánh dấu tất cả thông báo của người dùng là đã đọc (/read-all) (200 OK)."""
    response = client.post("/api/notifications/read-all?user_id=usr_test_01")
    assert response.status_code == 200
    assert response.json()["success"] is True

    # Kiểm tra toàn bộ thông báo đều đã đọc
    notifs = client.get("/api/notifications?user_id=usr_test_01").json()
    assert all(n["isRead"] is True for n in notifs)


# ============================================================================
# 5. UPLOAD SUITE (Phân hệ Tải lên Media & Nén Ảnh)
# ============================================================================

def test_upload_valid_image(client):
    """Upload tệp ảnh PNG hợp lệ, nhận về URL ảnh tĩnh và tỷ lệ nén (201 Created)."""
    # Tạo ảnh PNG 100x100 pixel giả lập trong bộ nhớ
    img = Image.new("RGB", (100, 100), color="blue")
    buf = io.BytesIO()
    img.save(buf, format="PNG")
    img_bytes = buf.getvalue()

    response = client.post(
        "/api/upload",
        files={"file": ("test_avatar.png", img_bytes, "image/png")}
    )
    assert response.status_code == 201
    data = response.json()
    assert "url" in data
    assert "filename" in data
    assert data["url"].startswith("http://10.0.2.2:8000/static/uploads/")


def test_upload_invalid_file_type(client):
    """Upload tệp không phải ảnh (.txt) bị từ chối với mã 400 Bad Request."""
    invalid_file_bytes = b"Day la tep tin van ban, khong phai anh."
    response = client.post(
        "/api/upload",
        files={"file": ("note.txt", invalid_file_bytes, "text/plain")}
    )
    assert response.status_code == 400
    assert "định dạng tệp không hỗ trợ" in response.json()["detail"].lower()


# ============================================================================
# 6. FRIENDS SEARCH SUITE (Phân hệ Tìm kiếm Bạn bè)
# ============================================================================

def test_search_friends_by_name(client):
    """Tìm kiếm bạn bè theo từ khóa tên không phân biệt chữ hoa/thường (200 OK)."""
    response = client.get("/api/friends/search?q=Quang")
    assert response.status_code == 200
    results = response.json()
    assert isinstance(results, list)
    assert len(results) >= 1
    assert any(u["id"] == "usr_test_01" for u in results)


def test_search_friends_empty_query(client):
    """Tìm kiếm với từ khóa rỗng trả về danh sách rỗng, không gây lỗi 500 (200 OK)."""
    response = client.get("/api/friends/search?q=")
    assert response.status_code == 200
    results = response.json()
    assert isinstance(results, list)
    assert len(results) == 0
