"""
=============================================================================
Dự án: Facebook Mobile Client (Android Java + Python FastAPI & SQLite)
Module: backend/tests/test_multi_account_friends.py
Tác giả: Hà Đăng Huy (Git user: HuyHaDang) - Backend Engineer & QA Lead
Nhiệm vụ: [D8-HDH] Kiểm thử độc lập API kết bạn trên 2 tài khoản qua Postman/Swagger
Mục tiêu: Kiểm thử thực tế tương tác mạng xã hội giữa 2 tài khoản độc lập (A và B);
          xác thực tính chính xác của lời mời kết bạn, ngăn chặn gửi trùng,
          chặn tự gửi kết bạn cho chính mình, và đồng bộ thông báo tự động.
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

# Cấu hình in-memory SQLite database riêng biệt với StaticPool
TEST_DB_URL = "sqlite:///:memory:"
engine = create_engine(
    TEST_DB_URL,
    connect_args={"check_same_thread": False},
    poolclass=StaticPool
)
TestingSession = sessionmaker(autocommit=False, autoflush=False, bind=engine)


@pytest.fixture(scope="function")
def multi_user_db():
    """Tạo schema CSDL test và nạp 3 tài khoản demo độc lập (A, B, C)."""
    Base.metadata.create_all(bind=engine)
    db = TestingSession()
    now = int(time.time() * 1000)

    # Khởi tạo 3 tài khoản thử nghiệm độc lập
    u1 = models.User(
        id="user_acc_a",
        email="nguyenvanan@test.com",
        display_name="Nguyễn Văn An",
        avatar_url="http://localhost:8000/static/uploads/avatar_an.jpg",
        created_at=now - 200000
    )
    u2 = models.User(
        id="user_acc_b",
        email="tranthimai@test.com",
        display_name="Trần Thị Mai",
        avatar_url="http://localhost:8000/static/uploads/avatar_mai.jpg",
        created_at=now - 150000
    )
    u3 = models.User(
        id="user_acc_c",
        email="lequangcuong@test.com",
        display_name="Lê Quang Cường",
        avatar_url="http://localhost:8000/static/uploads/avatar_cuong.jpg",
        created_at=now - 100000
    )
    db.add_all([u1, u2, u3])
    db.commit()

    try:
        yield db
    finally:
        db.close()
        Base.metadata.drop_all(bind=engine)


@pytest.fixture(scope="function")
def client(multi_user_db):
    """FastAPI TestClient với dependency override trỏ vào multi_user_db."""
    def override_get_db():
        try:
            yield multi_user_db
        finally:
            pass

    app.dependency_overrides[get_db] = override_get_db
    with TestClient(app) as c:
        yield c
    app.dependency_overrides.clear()


def test_send_friend_request_delivered_to_exact_recipient(client):
    """
    Kịch bản 1: Tài khoản A gửi lời mời kết bạn đến Tài khoản B.
    - Trả về HTTP 201 Created.
    - Lời mời chỉ xuất hiện trong danh sách của B, KHÔNG xuất hiện ở A.
    - Tự động sinh Notification loại FRIEND_REQUEST trong hộp thư của B.
    """
    payload = {"requesterId": "user_acc_a", "receiverId": "user_acc_b"}
    res = client.post("/api/friends/requests", json=payload)
    assert res.status_code == 201
    data = res.json()
    assert data["requesterId"] == "user_acc_a"
    assert data["receiverId"] == "user_acc_b"
    assert data["status"] == "PENDING"

    # Kiểm tra danh sách lời mời của B: phải có lời mời từ A
    res_b = client.get("/api/friends/requests?user_id=user_acc_b")
    assert res_b.status_code == 200
    requests_b = res_b.json()
    assert len(requests_b) == 1
    assert requests_b[0]["requesterId"] == "user_acc_a"
    assert requests_b[0]["requesterName"] == "Nguyễn Văn An"

    # Kiểm tra danh sách lời mời của A: không có lời mời nào gửi đến A
    res_a = client.get("/api/friends/requests?user_id=user_acc_a")
    assert res_a.status_code == 200
    assert len(res_a.json()) == 0

    # Kiểm tra thông báo của B: có thông báo FRIEND_REQUEST từ A
    notifs_b = client.get("/api/notifications?user_id=user_acc_b").json()
    assert len(notifs_b) == 1
    assert notifs_b[0]["type"] == "FRIEND_REQUEST"
    assert notifs_b[0]["senderId"] == "user_acc_a"
    assert notifs_b[0]["recipientId"] == "user_acc_b"
    assert "lời mời kết bạn" in notifs_b[0]["message"].lower()

    # Kiểm tra thông báo của A: A không nhận thông báo về việc chính mình gửi
    notifs_a = client.get("/api/notifications?user_id=user_acc_a").json()
    assert len(notifs_a) == 0


def test_prevent_duplicate_friend_request(client):
    """
    Kịch bản 2: Ngăn chặn gửi lời mời kết bạn trùng lặp giữa 2 tài khoản.
    - Lần 1: Thành công (201 Created).
    - Lần 2: Bị chặn với HTTP 400 Bad Request kèm thông báo 'tồn tại' hoặc 'đã là bạn bè'.
    """
    payload = {"requesterId": "user_acc_a", "receiverId": "user_acc_b"}

    # Gửi lần 1: thành công
    res1 = client.post("/api/friends/requests", json=payload)
    assert res1.status_code == 201

    # Gửi lại lần 2: phải bị từ chối với lỗi 400
    res2 = client.post("/api/friends/requests", json=payload)
    assert res2.status_code == 400
    assert "tồn tại" in res2.json()["detail"].lower() or "đã" in res2.json()["detail"].lower()


def test_prevent_self_friend_request(client):
    """
    Kịch bản 3: Chặn tự gửi lời mời kết bạn cho chính mình.
    - requesterId == receiverId -> HTTP 400 Bad Request.
    """
    payload = {"requesterId": "user_acc_a", "receiverId": "user_acc_a"}
    res = client.post("/api/friends/requests", json=payload)
    assert res.status_code == 400
    detail = res.json()["detail"].lower()
    assert "chính mình" in detail or "bản thân" in detail


def test_accept_friend_request_bidirectional_and_notifies_sender(client):
    """
    Kịch bản 4: Tài khoản B chấp nhận lời mời từ A.
    - Trở thành bạn bè 2 chiều (cả A và B đều thấy nhau trong danh sách bạn bè).
    - B nhận được status ACCEPTED.
    - A nhận được thông báo loại FRIEND_ACCEPT từ B.
    """
    # 1. A gửi lời mời cho B
    req = client.post(
        "/api/friends/requests",
        json={"requesterId": "user_acc_a", "receiverId": "user_acc_b"}
    ).json()
    req_id = req["id"]

    # 2. B chấp nhận lời mời
    res_acc = client.post("/api/friends/accept", json={"requestId": req_id})
    assert res_acc.status_code == 200
    acc_data = res_acc.json()
    assert acc_data["status"] == "ACCEPTED"
    assert acc_data["success"] is True

    # 3. Kiểm tra danh sách bạn bè 2 chiều
    friends_of_a = client.get("/api/friends?user_id=user_acc_a").json()
    friends_of_b = client.get("/api/friends?user_id=user_acc_b").json()

    assert any(f["id"] == "user_acc_b" for f in friends_of_a)
    assert any(f["id"] == "user_acc_a" for f in friends_of_b)

    # 4. Kiểm tra A nhận được thông báo FRIEND_ACCEPT từ B
    notifs_a = client.get("/api/notifications?user_id=user_acc_a").json()
    assert any(
        n["type"] == "FRIEND_ACCEPT" and n["senderId"] == "user_acc_b"
        for n in notifs_a
    )


def test_reject_friend_request_cleans_pending_state(client):
    """
    Kịch bản 5: Tài khoản B từ chối lời mời từ C.
    - B từ chối lời mời -> Trả về HTTP 200 OK.
    - Danh sách lời mời chờ của B trở về rỗng.
    - Cả B và C đều không có nhau trong danh sách bạn bè.
    """
    # 1. C gửi lời mời cho B
    req = client.post(
        "/api/friends/requests",
        json={"requesterId": "user_acc_c", "receiverId": "user_acc_b"}
    ).json()
    req_id = req["id"]

    # 2. B từ chối lời mời
    res_rej = client.post("/api/friends/reject", json={"requestId": req_id})
    assert res_rej.status_code == 200
    assert res_rej.json()["status"] == "REJECTED"

    # 3. Danh sách lời mời gửi tới B không còn lời mời này
    requests_b = client.get("/api/friends/requests?user_id=user_acc_b").json()
    assert not any(r["id"] == req_id for r in requests_b)

    # 4. B và C không phải bạn bè
    friends_b = client.get("/api/friends?user_id=user_acc_b").json()
    friends_c = client.get("/api/friends?user_id=user_acc_c").json()
    assert not any(f["id"] == "user_acc_c" for f in friends_b)
    assert not any(f["id"] == "user_acc_b" for f in friends_c)
