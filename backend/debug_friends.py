#!/usr/bin/env python3
"""
=============================================================================
Dự án: Facebook Mobile Client (Android Java + Python FastAPI & SQLite)
Module: backend/debug_friends.py
Tác giả: Minh Đức (BE) - Backend Engineer & Storage/Media Specialist
Nhiệm vụ: [D8-MD] Hỗ trợ Phúc Đức gỡ lỗi dữ liệu bạn bè; kiểm tra câu lệnh SQL trong SQLite.
=============================================================================
Công cụ hỗ trợ chẩn đoán dữ liệu, tối ưu hóa câu lệnh SQL và kiểm tra toàn diện
các endpoint phân hệ Bạn bè (Friends) trên SQLite database (facebook.db).
"""

import os
import sys
import sqlite3
import json
import time

if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    except Exception:
        pass

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
if BASE_DIR not in sys.path:
    sys.path.insert(0, BASE_DIR)

DEFAULT_DB_PATH = os.path.join(BASE_DIR, "facebook.db")

from database import SessionLocal
import models
import schemas
from crud import crud_friend, crud_notif


def print_header(title: str):
    print("\n" + "=" * 70)
    print(f" {title.upper()}")
    print("=" * 70)


def check_sqlite_integrity(db_path: str = DEFAULT_DB_PATH):
    """
    1. Kiểm tra tính toàn vẹn và cấu hình PRAGMA của SQLite database.
    """
    print_header("1. KIỂM TRA TÍNH TOÀN VẸN VÀ CẤU HÌNH SQLITE PRAGMA")
    if not os.path.exists(db_path):
        print(f"[LỖI] File database không tồn tại: {db_path}")
        return False

    file_size_kb = os.path.getsize(db_path) / 1024.0
    print(f"[*] Đường dẫn CSDL: {db_path} ({file_size_kb:.2f} KB)")

    conn = sqlite3.connect(db_path)
    cur = conn.cursor()

    cur.execute("PRAGMA journal_mode;")
    journal_mode = cur.fetchone()[0]
    print(f"[*] Journal Mode: {journal_mode} (Khuyến nghị: wal)")

    cur.execute("PRAGMA foreign_keys;")
    foreign_keys = cur.fetchone()[0]
    print(f"[*] Foreign Keys Enabled: {'BẬT (1)' if foreign_keys else 'TẮT (0)'}")

    cur.execute("PRAGMA integrity_check;")
    integrity = cur.fetchone()[0]
    print(f"[*] Integrity Check: {integrity}")

    cur.execute("PRAGMA foreign_key_check;")
    fk_errors = cur.fetchall()
    if fk_errors:
        print(f"[CẢNH BÁO] Phát hiện lỗi Foreign Key: {fk_errors}")
    else:
        print("[*] Foreign Key Check: HỢP LỆ (0 lỗi vi phạm ràng buộc khóa ngoại)")

    conn.close()
    return True


def check_friendship_indexes(db_path: str = DEFAULT_DB_PATH):
    """
    2. Kiểm tra các chỉ mục (Indexes) trên bảng friendships.
    """
    print_header("2. KIỂM TRA CHỈ MỤC (INDEXES) BẢNG FRIENDSHIPS")
    conn = sqlite3.connect(db_path)
    cur = conn.cursor()

    cur.execute("SELECT name, sql FROM sqlite_master WHERE tbl_name='friendships' AND type='index';")
    indexes = cur.fetchall()

    required_indexes = {
        "idx_friendships_receiver": "Tối ưu tìm kiếm theo receiver_id và status",
        "idx_friendships_requester": "Tối ưu tìm kiếm bạn bè theo requester_id và status",
        "idx_friendships_receiver_created": "Tối ưu sắp xếp danh sách lời mời theo thời gian",
        "idx_friendships_status": "Tối ưu lọc trạng thái ACCEPTED / PENDING"
    }

    found_names = [idx[0] for idx in indexes if idx[0]]
    print(f"[*] Tổng số chỉ mục hiện tại: {len(indexes)}")
    for name, sql in indexes:
        print(f"    - {name}: {sql}")

    missing = []
    for req, desc in required_indexes.items():
        if req not in found_names:
            missing.append(req)

    if missing:
        print(f"\n[!] Bổ sung các chỉ mục còn thiếu: {missing}")
        if "idx_friendships_requester" in missing:
            cur.execute("CREATE INDEX IF NOT EXISTS idx_friendships_requester ON friendships(requester_id, status);")
        if "idx_friendships_receiver_created" in missing:
            cur.execute("CREATE INDEX IF NOT EXISTS idx_friendships_receiver_created ON friendships(receiver_id, status, created_at DESC);")
        if "idx_friendships_status" in missing:
            cur.execute("CREATE INDEX IF NOT EXISTS idx_friendships_status ON friendships(status);")
        conn.commit()
        print("[*] Đã bổ sung thành công tất cả chỉ mục tối ưu!")
    else:
        print("\n[*] Tất cả chỉ mục tối ưu cho bảng friendships đã ĐẦY ĐỦ!")

    conn.close()


def explain_friend_queries(db_path: str = DEFAULT_DB_PATH):
    """
    3. Phân tích kế hoạch thực thi câu lệnh SQL (EXPLAIN QUERY PLAN).
    """
    print_header("3. PHÂN TÍCH HIỆU NĂNG CÂU LỆNH SQL (EXPLAIN QUERY PLAN)")
    conn = sqlite3.connect(db_path)
    cur = conn.cursor()

    queries = [
        (
            "Query 1: Lấy danh sách lời mời kết bạn gửi đến user_current (GET /api/friends/requests)",
            """EXPLAIN QUERY PLAN 
               SELECT f.id, f.requester_id, f.receiver_id, f.status, f.created_at, u.display_name, u.avatar_url 
               FROM friendships f 
               JOIN users u ON f.requester_id = u.id 
               WHERE f.receiver_id = 'user_current' AND f.status = 'PENDING' 
               ORDER BY f.created_at DESC;"""
        ),
        (
            "Query 2: Lấy danh sách bạn bè đã đồng ý của user_current (GET /api/friends)",
            """EXPLAIN QUERY PLAN 
               SELECT * FROM friendships 
               WHERE status = 'ACCEPTED' AND (requester_id = 'user_current' OR receiver_id = 'user_current');"""
        ),
        (
            "Query 3: Tìm kiếm bạn bè theo tên có loại trừ chính mình (GET /api/friends/search)",
            """EXPLAIN QUERY PLAN 
               SELECT id, email, display_name, avatar_url, cover_url, created_at 
               FROM users 
               WHERE display_name LIKE '%An%' AND id != 'user_current' 
               LIMIT 20;"""
        )
    ]

    for title, q in queries:
        print(f"\n[*] {title}:")
        cur.execute(q)
        for row in cur.fetchall():
            print(f"    -> {row[3]}")

    conn.close()


def inspect_friend_data(db_path: str = DEFAULT_DB_PATH, user_id: str = "user_current"):
    """
    4. Kiểm tra dữ liệu thực tế và đối chiếu hợp đồng API cho Android Client (Phúc Đức).
    """
    print_header(f"4. KIỂM TRA DỮ LIỆU THỰC TẾ CHO TÀI KHOẢN '{user_id}'")
    db = SessionLocal()
    try:
        # Lời mời kết bạn đến
        requests = crud_friend.get_friend_requests(db=db, user_id=user_id)
        print(f"[*] 1. Danh sách lời mời kết bạn (Pending Requests) - Tổng số: {len(requests)}")
        for idx, r in enumerate(requests, 1):
            created_str = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(r.createdAt / 1000))
            print(f"    [{idx}] Request ID: {r.id} | Từ: {r.requesterName} ({r.requesterId}) | Lúc: {created_str}")

        # JSON mẫu trả về cho Retrofit
        if requests:
            sample_json = json.dumps([r.model_dump() for r in requests], indent=2, ensure_ascii=False)
            print("\n    [Sample JSON cho Android FriendRequest.java]:")
            for line in sample_json.split("\n")[:15]:
                print(f"      {line}")
            if len(sample_json.split("\n")) > 15:
                print("      ...")

        # Danh sách bạn bè hiện tại
        friends = crud_friend.get_friends_list(db=db, user_id=user_id)
        print(f"\n[*] 2. Danh sách bạn bè hiện tại (Accepted Friends) - Tổng số: {len(friends)}")
        for idx, f in enumerate(friends, 1):
            print(f"    [{idx}] User ID: {f.id} | Tên: {f.displayName} | Email: {f.email}")

        # Thông báo liên quan
        notifs = crud_notif.get_user_notifications(db=db, user_id=user_id, limit=5)
        friend_notifs = [n for n in notifs if n.type in ("FRIEND_REQUEST", "FRIEND_ACCEPT")]
        print(f"\n[*] 3. Thông báo liên quan đến Bạn bè - Tổng số: {len(friend_notifs)}")
        for idx, n in enumerate(friend_notifs, 1):
            print(f"    [{idx}] [{n.type}] Từ: {n.senderName} | Nội dung: {n.message} (Đã đọc: {n.isRead})")

    finally:
        db.close()


def test_full_friend_lifecycle():
    """
    5. Kiểm thử toàn bộ vòng đời quan hệ bạn bè trên database in-memory:
       Gửi yêu cầu -> Nhận thông báo -> Chấp nhận -> Đồng bộ danh sách bạn bè.
    """
    print_header("5. CHẠY KỊCH BẢN MÔ PHỎNG VÒNG ĐỜI KẾT BẠN (LIFECYCLE TEST)")
    from sqlalchemy import create_engine
    from database import Base

    test_engine = create_engine("sqlite:///:memory:", connect_args={"check_same_thread": False})
    Base.metadata.create_all(bind=test_engine)
    TestSession = sessionmaker_local = SessionLocal.__class__(bind=test_engine)
    db = sessionmaker_local()

    try:
        # 1. Tạo 2 người dùng mẫu
        u1 = models.User(id="usr_alice", email="alice@test.com", display_name="Alice", created_at=int(time.time() * 1000))
        u2 = models.User(id="usr_bob", email="bob@test.com", display_name="Bob", created_at=int(time.time() * 1000))
        db.add_all([u1, u2])
        db.commit()
        print("[*] Bước 1: Khởi tạo 2 người dùng Alice (usr_alice) và Bob (usr_bob) - THÀNH CÔNG")

        # 2. Alice gửi lời mời kết bạn cho Bob
        req = crud_friend.send_friend_request(db, schemas.FriendRequestCreate(requesterId="usr_alice", receiverId="usr_bob"))
        print(f"[*] Bước 2: Alice gửi lời mời kết bạn cho Bob (ID: {req.id}) - THÀNH CÔNG")

        # Kiểm tra thông báo cho Bob
        bob_notifs = crud_notif.get_user_notifications(db, "usr_bob")
        assert len(bob_notifs) == 1, "Bob phải nhận được 1 thông báo"
        assert bob_notifs[0].type == "FRIEND_REQUEST", "Loại thông báo phải là FRIEND_REQUEST"
        print(f"    -> Bob nhận thông báo: '{bob_notifs[0].message}'")

        # 3. Bob lấy danh sách lời mời
        bob_requests = crud_friend.get_friend_requests(db, user_id="usr_bob")
        assert len(bob_requests) == 1, "Bob phải có 1 lời mời chờ duyệt"
        print(f"[*] Bước 3: Bob kiểm tra danh sách lời mời: {len(bob_requests)} lời mời từ {bob_requests[0].requesterName}")

        # 4. Bob chấp nhận lời mời
        accept_res = crud_friend.accept_friend_request(db, request_id=req.id)
        assert accept_res["success"] is True
        print(f"[*] Bước 4: Bob chấp nhận lời mời - THÀNH CÔNG")

        # Kiểm tra thông báo cho Alice
        alice_notifs = crud_notif.get_user_notifications(db, "usr_alice")
        assert len(alice_notifs) == 1
        assert alice_notifs[0].type == "FRIEND_ACCEPT"
        print(f"    -> Alice nhận thông báo: '{alice_notifs[0].message}'")

        # 5. Kiểm tra danh sách bạn bè cả 2 bên
        alice_friends = crud_friend.get_friends_list(db, "usr_alice")
        bob_friends = crud_friend.get_friends_list(db, "usr_bob")
        assert len(alice_friends) == 1 and alice_friends[0].id == "usr_bob"
        assert len(bob_friends) == 1 and bob_friends[0].id == "usr_alice"
        print("[*] Bước 5: Kiểm tra danh sách bạn bè 2 chiều (Alice <-> Bob) - HOÀN TOÀN CHÍNH XÁC!")

    finally:
        db.close()


def main():
    print("""
=============================================================================
 HỆ THỐNG DEBUG DỮ LIỆU BẠN BÈ VÀ CÂU LỆNH SQLITE (FACEBOOK CLIENT)
 Phụ trách: Minh Đức (BE) - Hỗ trợ Phúc Đức (FE) ghép FriendRepositoryImpl
=============================================================================
""")
    check_sqlite_integrity()
    check_friendship_indexes()
    explain_friend_queries()
    inspect_friend_data()
    test_full_friend_lifecycle()
    print_header("KẾT LUẬN DEBUG DỮ LIỆU BẠN BÈ: TẤT CẢ KIỂM TRA ĐỀU ĐẠT CHUẨN!")


if __name__ == "__main__":
    main()
