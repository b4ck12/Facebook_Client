"""
Bộ kiểm thử tự động toàn diện cho phân hệ Bạn bè (Friends API) - Day 3, Day 4, Day 5.
Phụ trách QA Lead: Hà Đăng Huy (HuyHaDang)
"""
import sys
import os
import time
import uuid
import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# Thêm thư mục backend vào sys.path
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
if BASE_DIR not in sys.path:
    sys.path.insert(0, BASE_DIR)

from main import app
from database import Base, get_db
import models
import schemas

from sqlalchemy.pool import StaticPool

# Sử dụng in-memory SQLite database riêng cho testing để cô lập dữ liệu 100%
TEST_DATABASE_URL = "sqlite:///:memory:"
test_engine = create_engine(
    TEST_DATABASE_URL,
    connect_args={"check_same_thread": False},
    poolclass=StaticPool
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=test_engine)

@pytest.fixture(scope="function")
def db_session():
    """
    Tạo schema CSDL test mới và nạp dữ liệu mẫu trước mỗi test function.
    """
    Base.metadata.create_all(bind=test_engine)
    db = TestingSessionLocal()

    now = int(time.time() * 1000)

    # 1. Tạo 4 người dùng mẫu
    user1 = models.User(
        id="usr_01",
        email="tungduong@gmail.com",
        display_name="Tùng Dương",
        avatar_url="http://localhost:8000/static/uploads/avatar1.jpg",
        cover_url=None,
        created_at=now
    )
    user2 = models.User(
        id="usr_02",
        email="minhluong@gmail.com",
        display_name="Minh Lương",
        avatar_url="http://localhost:8000/static/uploads/avatar2.jpg",
        cover_url=None,
        created_at=now
    )
    user3 = models.User(
        id="usr_03",
        email="anhduong@gmail.com",
        display_name="Ánh Dương",
        avatar_url="http://localhost:8000/static/uploads/avatar3.jpg",
        cover_url=None,
        created_at=now
    )
    user4 = models.User(
        id="usr_04",
        email="hadanghuy@gmail.com",
        display_name="Hà Đăng Huy",
        avatar_url="http://localhost:8000/static/uploads/avatar4.jpg",
        cover_url=None,
        created_at=now
    )
    db.add_all([user1, user2, user3, user4])
    db.commit()

    # 2. Tạo lời mời kết bạn mẫu: usr_03 gửi lời mời tới usr_01 (PENDING)
    req1 = models.Friendship(
        id="frnd_test_01",
        requester_id="usr_03",
        receiver_id="usr_01",
        status="PENDING",
        created_at=now - 50000
    )
    # Lời mời kết bạn: usr_02 gửi tới usr_01 (PENDING)
    req2 = models.Friendship(
        id="frnd_test_02",
        requester_id="usr_02",
        receiver_id="usr_01",
        status="PENDING",
        created_at=now - 20000
    )
    # Quan hệ đã là bạn bè: usr_04 và usr_01 (ACCEPTED)
    req3 = models.Friendship(
        id="frnd_test_03",
        requester_id="usr_04",
        receiver_id="usr_01",
        status="ACCEPTED",
        created_at=now - 100000
    )
    db.add_all([req1, req2, req3])
    db.commit()

    try:
        yield db
    finally:
        db.close()
        Base.metadata.drop_all(bind=test_engine)


@pytest.fixture(scope="function")
def client(db_session):
    """
    FastAPI TestClient với dependency override trỏ vào test database.
    """
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
# TEST SUITE NGÀY 03 (D3-HDH): GET /api/friends/requests
# ============================================================================

def test_get_friend_requests_success(client):
    """
    TC-D3-01: Kiểm tra lấy danh sách lời mời kết bạn gửi đến usr_01.
    """
    response = client.get("/api/friends/requests?user_id=usr_01")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    assert len(data) == 2  # frnd_test_01 và frnd_test_02

    # Kiểm tra cấu trúc trường dữ liệu DTO
    first_req = data[0]
    assert "id" in first_req
    assert "requesterId" in first_req
    assert "requesterName" in first_req
    assert "requesterAvatar" in first_req
    assert "receiverId" in first_req
    assert "status" in first_req
    assert "createdAt" in first_req
    assert first_req["status"] == "PENDING"
    assert first_req["receiverId"] == "usr_01"


def test_get_friend_requests_empty_when_no_requests(client):
    """
    TC-D3-02: Kiểm tra khi user không có lời mời kết bạn nào gửi đến.
    """
    response = client.get("/api/friends/requests?user_id=usr_04")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    assert len(data) == 0


def test_get_friend_requests_pagination(client):
    """
    TC-D3-03: Kiểm tra phân trang skip và limit.
    """
    response = client.get("/api/friends/requests?user_id=usr_01&skip=0&limit=1")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 1


# ============================================================================
# TEST SUITE NGÀY 04 (D4-HDH): POST /api/friends/accept & POST /api/friends/reject
# ============================================================================

def test_accept_friend_request_success(client, db_session):
    """
    TC-D4-01: Chấp nhận lời mời kết bạn thành công và kiểm tra tự động sinh Notification FRIEND_ACCEPT.
    """
    response = client.post(
        "/api/friends/accept",
        json={"requestId": "frnd_test_01"}
    )
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    assert data["status"] == "ACCEPTED"

    # Kiểm tra database cập nhật
    f = db_session.query(models.Friendship).filter(models.Friendship.id == "frnd_test_01").first()
    assert f.status == "ACCEPTED"

    # Kiểm tra notification đã được tạo cho người yêu cầu (usr_03)
    notif = (
        db_session.query(models.Notification)
        .filter(models.Notification.recipient_id == "usr_03", models.Notification.type == "FRIEND_ACCEPT")
        .first()
    )
    assert notif is not None
    assert "chấp nhận lời mời kết bạn" in notif.message


def test_accept_friend_request_idempotent(client):
    """
    TC-D4-02: Gọi chấp nhận lần 2 trên lời mời đã ACCEPTED không gây lỗi.
    """
    client.post("/api/friends/accept", json={"requestId": "frnd_test_01"})
    response = client.post("/api/friends/accept", json={"requestId": "frnd_test_01"})
    assert response.status_code == 200
    assert response.json()["status"] == "ACCEPTED"


def test_accept_friend_request_not_found(client):
    """
    TC-D4-03: Gọi chấp nhận với requestId không tồn tại trả về 404 Not Found.
    """
    response = client.post(
        "/api/friends/accept",
        json={"requestId": "non_existent_id"}
    )
    assert response.status_code == 404
    assert "Lời mời kết bạn không tồn tại" in response.json()["detail"]


def test_reject_friend_request_success(client, db_session):
    """
    TC-D4-04: Từ chối lời mời kết bạn thành công.
    """
    response = client.post(
        "/api/friends/reject",
        json={"requestId": "frnd_test_02"}
    )
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    assert data["status"] == "REJECTED"

    # Kiểm tra trong CSDL
    f = db_session.query(models.Friendship).filter(models.Friendship.id == "frnd_test_02").first()
    assert f.status == "REJECTED"


def test_reject_friend_request_not_found(client):
    """
    TC-D4-05: Từ chối với requestId không tồn tại trả về 404 Not Found.
    """
    response = client.post(
        "/api/friends/reject",
        json={"requestId": "invalid_req_id"}
    )
    assert response.status_code == 404


# ============================================================================
# TEST SUITE NGÀY 05 (D5-HDH): GET /api/friends/search, GET /api/friends & POST /api/friends/requests
# ============================================================================

def test_search_friends_by_name(client):
    """
    TC-D5-01: Tìm kiếm bạn bè theo tên (SQL LIKE không phân biệt hoa thường).
    """
    response = client.get("/api/friends/search?q=Dương")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 2  # Tùng Dương (usr_01) và Ánh Dương (usr_03)
    names = [u["displayName"] for u in data]
    assert "Tùng Dương" in names
    assert "Ánh Dương" in names


def test_search_friends_excludes_self(client):
    """
    TC-D5-02: Tìm kiếm loại trừ chính người dùng hiện tại nếu truyền userId.
    """
    response = client.get("/api/friends/search?q=Dương&userId=usr_01")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 1
    assert data[0]["id"] == "usr_03"
    assert data[0]["displayName"] == "Ánh Dương"


def test_search_friends_empty_query(client):
    """
    TC-D5-03: Tìm kiếm với chuỗi rỗng hoặc khoảng trắng trả về mảng rỗng [].
    """
    response = client.get("/api/friends/search?q=%20%20")
    assert response.status_code == 200
    assert response.json() == []


def test_get_friends_list_accepted(client):
    """
    TC-D5-04: Lấy danh sách bạn bè hiện tại (ACCEPTED).
    """
    response = client.get("/api/friends?user_id=usr_01")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 1
    assert data[0]["id"] == "usr_04"
    assert data[0]["displayName"] == "Hà Đăng Huy"


def test_send_friend_request_success(client, db_session):
    """
    TC-D5-05: Gửi lời mời kết bạn mới thành công và sinh notification FRIEND_REQUEST.
    """
    response = client.post(
        "/api/friends/requests",
        json={"requesterId": "usr_02", "receiverId": "usr_04"}
    )
    assert response.status_code == 201
    data = response.json()
    assert data["requesterId"] == "usr_02"
    assert data["receiverId"] == "usr_04"
    assert data["status"] == "PENDING"

    # Kiểm tra notification được sinh cho usr_04
    notif = (
        db_session.query(models.Notification)
        .filter(models.Notification.recipient_id == "usr_04", models.Notification.type == "FRIEND_REQUEST")
        .first()
    )
    assert notif is not None
    assert "lời mời kết bạn" in notif.message


def test_send_friend_request_to_self_error(client):
    """
    TC-D5-06: Chặn tự kết bạn với chính mình -> HTTP 400 Bad Request.
    """
    response = client.post(
        "/api/friends/requests",
        json={"requesterId": "usr_01", "receiverId": "usr_01"}
    )
    assert response.status_code == 400
    assert "Không thể gửi lời mời kết bạn cho chính mình" in response.json()["detail"]


def test_send_friend_request_duplicate_error(client):
    """
    TC-D5-07: Chặn gửi lời mời kết bạn khi đã có lời mời PENDING trước đó.
    """
    response = client.post(
        "/api/friends/requests",
        json={"requesterId": "usr_03", "receiverId": "usr_01"}
    )
    assert response.status_code == 400
    assert "đã tồn tại" in response.json()["detail"]
