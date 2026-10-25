"""
Test toàn diện các hàm CRUD của phân hệ Bạn bè (crud_friend.py).
Minh Đức (BE) thực hiện kiểm thử logic SQL, SQLAlchemy session và constraints.
"""
import os
import sys
import unittest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    except Exception:
        pass

# Ensure backend directory is in sys.path
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
if BASE_DIR not in sys.path:
    sys.path.insert(0, BASE_DIR)

from database import Base
import models, schemas
from crud import crud_friend, crud_notif
from fastapi import HTTPException

class TestFriendsCrud(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        # Sử dụng SQLite in-memory để test độc lập, an toàn
        cls.engine = create_engine("sqlite:///:memory:", connect_args={"check_same_thread": False})
        Base.metadata.create_all(bind=cls.engine)
        cls.SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=cls.engine)

    def setUp(self):
        self.db = self.SessionLocal()
        # Seed test users
        u_curr = models.User(id="user_current", email="curr@fb.com", display_name="User Current", created_at=1000)
        u1 = models.User(id="user_101", email="u101@fb.com", display_name="Nguyen Van An", created_at=1000)
        u2 = models.User(id="user_102", email="u102@fb.com", display_name="Tran Thi Mai", created_at=1000)
        u3 = models.User(id="user_103", email="u103@fb.com", display_name="Le Hoang Nam", created_at=1000)
        self.db.add_all([u_curr, u1, u2, u3])
        self.db.commit()

    def tearDown(self):
        self.db.query(models.Notification).delete()
        self.db.query(models.Friendship).delete()
        self.db.query(models.User).delete()
        self.db.commit()
        self.db.close()

    def test_send_and_get_friend_requests(self):
        # 1. user_101 gửi lời mời cho user_current
        req_dto = schemas.FriendRequestCreate(requesterId="user_101", receiverId="user_current")
        created = crud_friend.send_friend_request(self.db, req_dto)
        self.assertIsNotNone(created.id)
        self.assertEqual(created.requesterId, "user_101")
        self.assertEqual(created.receiverId, "user_current")
        self.assertEqual(created.status, "PENDING")

        # Kiểm tra thông báo được tạo cho user_current
        notifs = crud_notif.get_user_notifications(self.db, "user_current")
        self.assertEqual(len(notifs), 1)
        self.assertEqual(notifs[0].type, "FRIEND_REQUEST")
        self.assertEqual(notifs[0].senderId, "user_101")

        # 2. Lấy danh sách lời mời gửi tới user_current
        requests = crud_friend.get_friend_requests(self.db, user_id="user_current")
        self.assertEqual(len(requests), 1)
        self.assertEqual(requests[0].requesterName, "Nguyen Van An")

    def test_cannot_send_to_self(self):
        req_dto = schemas.FriendRequestCreate(requesterId="user_current", receiverId="user_current")
        with self.assertRaises(HTTPException) as ctx:
            crud_friend.send_friend_request(self.db, req_dto)
        self.assertEqual(ctx.exception.status_code, 400)

    def test_duplicate_friend_request_rejected(self):
        req_dto = schemas.FriendRequestCreate(requesterId="user_101", receiverId="user_current")
        crud_friend.send_friend_request(self.db, req_dto)
        # Gửi lại
        with self.assertRaises(HTTPException) as ctx:
            crud_friend.send_friend_request(self.db, req_dto)
        self.assertEqual(ctx.exception.status_code, 400)

    def test_accept_friend_request(self):
        req_dto = schemas.FriendRequestCreate(requesterId="user_101", receiverId="user_current")
        res = crud_friend.send_friend_request(self.db, req_dto)

        # Chấp nhận
        accept_res = crud_friend.accept_friend_request(self.db, request_id=res.id)
        self.assertTrue(accept_res["success"])
        self.assertEqual(accept_res["status"], "ACCEPTED")

        # Kiểm tra thông báo FRIEND_ACCEPT cho user_101
        notifs = crud_notif.get_user_notifications(self.db, "user_101")
        self.assertEqual(len(notifs), 1)
        self.assertEqual(notifs[0].type, "FRIEND_ACCEPT")
        self.assertEqual(notifs[0].senderId, "user_current")

        # Kiểm tra danh sách bạn bè
        friends_curr = crud_friend.get_friends_list(self.db, user_id="user_current")
        self.assertEqual(len(friends_curr), 1)
        self.assertEqual(friends_curr[0].id, "user_101")

        friends_101 = crud_friend.get_friends_list(self.db, user_id="user_101")
        self.assertEqual(len(friends_101), 1)
        self.assertEqual(friends_101[0].id, "user_current")

    def test_reject_friend_request(self):
        req_dto = schemas.FriendRequestCreate(requesterId="user_102", receiverId="user_current")
        res = crud_friend.send_friend_request(self.db, req_dto)

        reject_res = crud_friend.reject_friend_request(self.db, request_id=res.id)
        self.assertTrue(reject_res["success"])
        self.assertEqual(reject_res["status"], "REJECTED")

        # Không còn trong danh sách pending
        pending = crud_friend.get_friend_requests(self.db, user_id="user_current")
        self.assertEqual(len(pending), 0)

    def test_search_friends(self):
        results = crud_friend.search_friends(self.db, query="Van", user_id="user_current")
        self.assertEqual(len(results), 1)
        self.assertEqual(results[0].id, "user_101")

        # Search excludes self
        results_self = crud_friend.search_friends(self.db, query="User", user_id="user_current")
        self.assertEqual(len(results_self), 0)

if __name__ == "__main__":
    unittest.main()
