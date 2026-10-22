#!/usr/bin/env python3
"""
=============================================================================
Dự án: Facebook Mobile Client (Android Java + Python FastAPI & SQLite)
Module: backend/seed_data.py
Tác giả: Hà Đăng Huy (Git user: HuyHaDang) - Backend Engineer & QA Lead
Nhiệm vụ: [D2-HDH] Xây dựng script seed_data.py chèn dữ liệu mẫu SQLite (facebook.db)
          Khớp 100% dữ liệu với MockDataGenerator.java trên Android (Data Parity).
=============================================================================
"""

import argparse
import os
import sqlite3
import sys
import time

# Ensure UTF-8 output on Windows consoles to prevent UnicodeEncodeError
if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    except Exception:
        pass

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DEFAULT_DB_PATH = os.path.join(BASE_DIR, "facebook.db")
SCHEMA_SQL_PATH = os.path.join(BASE_DIR, "schema.sql")


def get_connection(db_path: str) -> sqlite3.Connection:
    """
    Tạo kết nối SQLite, kích hoạt WAL Mode và Foreign Keys.
    """
    conn = sqlite3.connect(db_path)
    conn.execute("PRAGMA journal_mode=WAL;")
    conn.execute("PRAGMA foreign_keys=ON;")
    return conn


def init_schema(conn: sqlite3.Connection):
    """
    Đọc và thực thi tệp schema.sql để khởi tạo cấu trúc bảng và indexes.
    """
    if os.path.exists(SCHEMA_SQL_PATH):
        with open(SCHEMA_SQL_PATH, "r", encoding="utf-8") as f:
            schema_script = f.read()
        conn.executescript(schema_script)
    else:
        # Fallback DDL nếu không tìm thấy tệp schema.sql
        conn.executescript("""
        PRAGMA foreign_keys = ON;

        CREATE TABLE IF NOT EXISTS users (
            id TEXT PRIMARY KEY,
            email TEXT UNIQUE NOT NULL,
            display_name TEXT NOT NULL,
            avatar_url TEXT,
            cover_url TEXT,
            created_at INTEGER NOT NULL
        );

        CREATE TABLE IF NOT EXISTS posts (
            id TEXT PRIMARY KEY,
            author_id TEXT NOT NULL,
            content TEXT,
            media_url TEXT,
            is_public INTEGER DEFAULT 1,
            created_at INTEGER NOT NULL,
            FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
        );

        CREATE TABLE IF NOT EXISTS comments (
            id TEXT PRIMARY KEY,
            post_id TEXT NOT NULL,
            author_id TEXT NOT NULL,
            content TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
            FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
        );

        CREATE TABLE IF NOT EXISTS post_likes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            post_id TEXT NOT NULL,
            user_id TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            UNIQUE(post_id, user_id),
            FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        );

        CREATE TABLE IF NOT EXISTS friendships (
            id TEXT PRIMARY KEY,
            requester_id TEXT NOT NULL,
            receiver_id TEXT NOT NULL,
            status TEXT CHECK(status IN ('PENDING', 'ACCEPTED', 'REJECTED')) NOT NULL,
            created_at INTEGER NOT NULL,
            UNIQUE(requester_id, receiver_id),
            FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
            FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
        );

        CREATE TABLE IF NOT EXISTS notifications (
            id TEXT PRIMARY KEY,
            recipient_id TEXT NOT NULL,
            sender_id TEXT NOT NULL,
            type TEXT CHECK(type IN ('LIKE', 'COMMENT', 'FRIEND_REQUEST', 'FRIEND_ACCEPT')) NOT NULL,
            post_id TEXT,
            message TEXT NOT NULL,
            is_read INTEGER DEFAULT 0,
            created_at INTEGER NOT NULL,
            FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
            FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
            FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE SET NULL
        );
        """)
    conn.commit()


def reset_database(conn: sqlite3.Connection):
    """
    Xóa sạch dữ liệu cũ theo đúng thứ tự ràng buộc khóa ngoại.
    """
    cursor = conn.cursor()
    # Tắt kiểm tra khóa ngoại tạm thời để xóa dữ liệu sạch sẽ
    cursor.execute("PRAGMA foreign_keys = OFF;")
    tables = ["notifications", "friendships", "post_likes", "comments", "posts", "users"]
    for table in tables:
        cursor.execute(f"DROP TABLE IF EXISTS {table};")
    cursor.execute("PRAGMA foreign_keys = ON;")
    conn.commit()
    # Tạo lại bảng từ schema
    init_schema(conn)


def seed_data(conn: sqlite3.Connection):
    """
    Nạp dữ liệu mẫu chuẩn khớp 100% với MockDataGenerator.java trên Android Client.
    """
    cursor = conn.cursor()
    now = int(time.time() * 1000)

    # 1. Seed 6 Users
    users_data = [
        (
            "user_current",
            "current.user@facebook.com",
            "Người dùng hiện tại",
            "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
            "https://images.unsplash.com/photo-1707343843437-caacff5cfa74?w=1200",
            now - (120 * 24 * 60 * 60 * 1000)
        ),
        (
            "user_101",
            "an.nguyen@facebook.com",
            "Nguyễn Văn An",
            "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200",
            now - (90 * 24 * 60 * 60 * 1000)
        ),
        (
            "user_102",
            "mai.tran@facebook.com",
            "Trần Thị Mai",
            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200",
            now - (80 * 24 * 60 * 60 * 1000)
        ),
        (
            "user_103",
            "nam.le@facebook.com",
            "Lê Hoàng Nam",
            "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
            "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=1200",
            now - (75 * 24 * 60 * 60 * 1000)
        ),
        (
            "user_104",
            "tuan.pham@facebook.com",
            "Phạm Minh Tuấn",
            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
            "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=1200",
            now - (60 * 24 * 60 * 60 * 1000)
        ),
        (
            "user_105",
            "thao.dang@facebook.com",
            "Đặng Thu Thảo",
            "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150",
            "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=1200",
            now - (45 * 24 * 60 * 60 * 1000)
        )
    ]
    cursor.executemany(
        """
        INSERT OR REPLACE INTO users (id, email, display_name, avatar_url, cover_url, created_at)
        VALUES (?, ?, ?, ?, ?, ?);
        """,
        users_data
    )

    # 2. Seed 10 Posts
    posts_data = [
        (
            "post_1001",
            "user_101",
            "Cuối tuần đi săn mây Đà Lạt cùng hội bạn thân, thời tiết se lạnh tuyệt vời quá mọi người ơi!",
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
            1,
            now - (5 * 60 * 1000)
        ),
        (
            "post_1002",
            "user_102",
            "Đồ án môn Phát triển Ứng dụng Di động đang chạy đúng tiến độ! Cả nhóm 7 anh em cố gắng hoàn thành xuất sắc nhé!",
            None,
            1,
            now - (20 * 60 * 1000)
        ),
        (
            "post_1003",
            "user_103",
            "Góc làm việc mới setup để chiến code Android Studio và Python FastAPI xuyên màn đêm.",
            "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800",
            1,
            now - (60 * 60 * 1000)
        ),
        (
            "post_1004",
            "user_104",
            "Bữa sáng nhẹ nhàng với phở Hà Nội truyền thống trước giờ lên giảng đường.",
            "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800",
            1,
            now - (2 * 60 * 60 * 1000)
        ),
        (
            "post_1005",
            "user_105",
            "Có ai biết quán cà phê nào yên tĩnh quanh khu vực trường mình để ngồi học nhóm không ạ? Cho mình xin địa chỉ với!",
            None,
            1,
            now - (3 * 60 * 60 * 1000)
        ),
        (
            "post_1006",
            "user_101",
            "Một ly Espresso nóng cho buổi chiều tỉnh táo fix bug.",
            "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800",
            1,
            now - (5 * 60 * 60 * 1000)
        ),
        (
            "post_1007",
            "user_103",
            "Chiều nay 17h30 đá bóng sân cỏ nhân tạo nhé anh em! Đừng ai cao su nhé.",
            "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=800",
            1,
            now - (8 * 60 * 60 * 1000)
        ),
        (
            "post_1008",
            "user_104",
            "Áp dụng kiến trúc MVVM + Clean Architecture kết hợp ViewBinding giúp code Android gọn gàng và dễ viết Unit Test hơn hẳn.",
            None,
            1,
            now - (12 * 60 * 60 * 1000)
        ),
        (
            "post_1009",
            "user_102",
            "Nhớ biển Phú Quốc mùa nắng đẹp.",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
            1,
            now - (24 * 60 * 60 * 1000)
        ),
        (
            "post_1010",
            "user_105",
            "Cuốn sách Clean Code thực sự rất đáng đọc cho mọi lập trình viên.",
            "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800",
            1,
            now - (48 * 60 * 60 * 1000)
        )
    ]
    cursor.executemany(
        """
        INSERT OR REPLACE INTO posts (id, author_id, content, media_url, is_public, created_at)
        VALUES (?, ?, ?, ?, ?, ?);
        """,
        posts_data
    )

    # 3. Seed 7 Comments (Text-only theo đặc tả PDF)
    comments_data = [
        ("cmt_1", "post_1001", "user_102", "Chuyến đi tuyệt vời quá bạn ơi!", now - (4 * 60 * 1000)),
        ("cmt_2", "post_1001", "user_103", "Ảnh chụp nét căng, săn mây ở đồi nào thế An?", now - (3 * 60 * 1000)),
        ("cmt_3", "post_1001", "user_104", "Đợt tới bảo vệ đồ án xong cả nhóm mình làm một chuyến nhé!", now - (1 * 60 * 1000)),
        ("cmt_4", "post_1002", "user_101", "Quyết tâm đạt điểm A+ môn này nhé cả nhà!", now - (15 * 60 * 1000)),
        ("cmt_5", "post_1002", "user_105", "Giao diện lên hình mượt lắm rồi!", now - (10 * 60 * 1000)),
        ("cmt_6", "post_1003", "user_104", "Góc setup gọn gàng và chất lượng quá Nam ơi.", now - (30 * 60 * 1000)),
        ("cmt_7", "post_1005", "user_102", "Qua quán The Coffee House ngay cổng trường tầng 2 yên tĩnh lắm Thảo ơi.", now - (2 * 60 * 60 * 1000))
    ]
    cursor.executemany(
        """
        INSERT OR REPLACE INTO comments (id, post_id, author_id, content, created_at)
        VALUES (?, ?, ?, ?, ?);
        """,
        comments_data
    )

    # 4. Seed Lượt thích bài viết (Post Likes)
    likes_data = [
        ("post_1002", "user_current", now - (18 * 60 * 1000)),
        ("post_1004", "user_current", now - (60 * 60 * 1000)),
        ("post_1007", "user_current", now - (7 * 60 * 60 * 1000)),
        ("post_1009", "user_current", now - (20 * 60 * 60 * 1000)),
        ("post_1001", "user_102", now - (4 * 60 * 1000))
    ]
    cursor.executemany(
        """
        INSERT OR IGNORE INTO post_likes (post_id, user_id, created_at)
        VALUES (?, ?, ?);
        """,
        likes_data
    )

    # 5. Seed Quan hệ bạn bè (Friendships: 2 Pending Requests + 2 Accepted Friends)
    friendships_data = [
        ("fr_01", "user_101", "user_current", "PENDING", now - (10 * 60 * 1000)),
        ("fr_02", "user_102", "user_current", "PENDING", now - (2 * 60 * 60 * 1000)),
        ("fr_03", "user_current", "user_103", "ACCEPTED", now - (75 * 24 * 60 * 60 * 1000)),
        ("fr_04", "user_current", "user_104", "ACCEPTED", now - (60 * 24 * 60 * 60 * 1000))
    ]
    cursor.executemany(
        """
        INSERT OR REPLACE INTO friendships (id, requester_id, receiver_id, status, created_at)
        VALUES (?, ?, ?, ?, ?);
        """,
        friendships_data
    )

    # 6. Seed 5 Thông báo (Notifications)
    notifications_data = [
        ("notif_1", "user_current", "user_101", "LIKE", "post_1001", "đã thích bài viết của bạn.", 0, now - (5 * 60 * 1000)),
        ("notif_2", "user_current", "user_102", "COMMENT", "post_1001", 'đã bình luận về bài viết của bạn: "Chuyến đi tuyệt vời quá bạn ơi!"', 0, now - (25 * 60 * 1000)),
        ("notif_3", "user_current", "user_103", "FRIEND_REQUEST", None, "đã gửi cho bạn một lời mời kết bạn.", 1, now - (2 * 60 * 60 * 1000)),
        ("notif_4", "user_current", "user_104", "FRIEND_ACCEPT", None, "đã chấp nhận lời mời kết bạn của bạn.", 1, now - (24 * 60 * 60 * 1000)),
        ("notif_5", "user_current", "user_105", "LIKE", "post_1002", "đã thích bài viết của bạn.", 1, now - (48 * 60 * 60 * 1000))
    ]
    cursor.executemany(
        """
        INSERT OR REPLACE INTO notifications (id, recipient_id, sender_id, type, post_id, message, is_read, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """,
        notifications_data
    )

    conn.commit()


def check_database(conn: sqlite3.Connection):
    """
    Kiểm tra tính toàn vẹn và in số lượng bản ghi của từng bảng.
    """
    cursor = conn.cursor()
    cursor.execute("PRAGMA integrity_check;")
    integrity = cursor.fetchone()[0]
    print(f"[SQLite PRAGMA] Integrity Check: {integrity}")

    cursor.execute("PRAGMA foreign_key_check;")
    fk_errors = cursor.fetchall()
    if fk_errors:
        print(f"[SQLite PRAGMA] Lỗi Foreign Key: {fk_errors}")
    else:
        print("[SQLite PRAGMA] Foreign Key Check: HỢP LỆ (0 lỗi)")

    print("-" * 50)
    print(f"{'TÊN BẢNG':<20} | {'SỐ LƯỢNG BẢN GHI':<15}")
    print("-" * 50)

    tables = ["users", "posts", "comments", "post_likes", "friendships", "notifications"]
    for table in tables:
        cursor.execute(f"SELECT COUNT(*) FROM {table};")
        count = cursor.fetchone()[0]
        print(f"{table:<20} | {count:<15}")
    print("-" * 50)


def main():
    parser = argparse.ArgumentParser(
        description="Nạp dữ liệu mẫu SQLite (facebook.db) phục vụ phát triển Facebook Client."
    )
    parser.add_argument(
        "--db",
        type=str,
        default=DEFAULT_DB_PATH,
        help=f"Đường dẫn file database SQLite (mặc định: {DEFAULT_DB_PATH})"
    )
    parser.add_argument(
        "--reset",
        action="store_true",
        help="Xóa dữ liệu cũ và khởi tạo lại toàn bộ bảng trước khi seed"
    )
    parser.add_argument(
        "--check",
        action="store_true",
        help="Kiểm tra tính toàn vẹn và in số lượng bản ghi của các bảng"
    )

    args = parser.parse_args()

    print("=" * 60)
    print("🚀 FACEBOOK CLIENT - SEED DATA GENERATOR (D2-HDH)")
    print(f"📁 Target Database: {args.db}")
    print("=" * 60)

    conn = get_connection(args.db)

    try:
        if args.reset:
            print("🔄 Đang reset database...")
            reset_database(conn)
            print("✅ Đã reset cấu trúc bảng thành công.")

        init_schema(conn)

        print("🌱 Đang chèn dữ liệu mẫu...")
        seed_data(conn)
        print("✅ Đã chèn toàn bộ dữ liệu mẫu thành công!")

        if args.check or True:  # Luôn hiển thị bảng kiểm tra sau khi seed
            print("\n📊 Báo cáo kiểm tra cơ sở dữ liệu:")
            check_database(conn)

    except Exception as e:
        print(f"❌ Có lỗi xảy ra trong quá trình seed dữ liệu: {e}", file=sys.stderr)
        conn.rollback()
        sys.exit(1)
    finally:
        conn.close()


if __name__ == "__main__":
    main()
