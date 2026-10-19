import os
from sqlalchemy import create_engine, event
from sqlalchemy.orm import declarative_base, sessionmaker

# Đường dẫn tệp tin CSDL SQLite facebook.db
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATABASE_FILE = os.path.join(BASE_DIR, "facebook.db")
SQLALCHEMY_DATABASE_URL = f"sqlite:///{DATABASE_FILE}"

# Khởi tạo SQLAlchemy Engine hỗ trợ multithreading cho SQLite
engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False}
)

# Kích hoạt WAL Mode (Write-Ahead Logging) và Foreign Keys cho SQLite
# Giúp thực hiện đồng thời nhiều thao tác đọc khi có thao tác ghi
@event.listens_for(engine, "connect")
def set_sqlite_pragma(dbapi_connection, connection_record):
    cursor = dbapi_connection.cursor()
    cursor.execute("PRAGMA journal_mode=WAL;")
    cursor.execute("PRAGMA foreign_keys=ON;")
    cursor.close()

# Session quản lý giao dịch
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Base class cho các SQLAlchemy ORM models
Base = declarative_base()

def get_db():
    """
    Dependency generator cung cấp session database cho từng HTTP request.
    Đảm bảo session được đóng sau khi request hoàn tất.
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
