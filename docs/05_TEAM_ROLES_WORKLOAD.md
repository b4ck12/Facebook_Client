# 👥 05. CƠ CẤU NHÂN SỰ CHUẨN DOANH NGHIỆP & CÂN BẰNG KHỐI LƯỢNG CÔNG VIỆC (ROLES & WORKLOAD)

---

## 1. MÔ HÌNH TỔ CHỨC ĐỘI NGŨ (SQUAD STRUCTURE - 7 THÀNH VIÊN)

Dự án được vận hành theo mô hình **Agile Cross-Functional Squad** chuẩn các công ty công nghệ lớn (Google, Meta, VNG, Viettel). Mỗi thành viên có chức danh chuyên nghiệp, phân định ranh giới trách nhiệm rõ ràng (RACI: Responsible, Accountable, Consulted, Informed).

### Bảng Cơ cấu Nhân sự & Định danh Chức vụ:

| STT | Họ và Tên | Vai trò chính | Vai trò kiêm nhiệm | Chức danh chuẩn công nghiệp (Industry Title) |
| :---: | :--- | :--- | :--- | :--- |
| **1** | **Ánh Dương** | Back-end | Kiến trúc sư hệ thống | **Lead Backend Engineer & Database Architect** |
| **2** | **Minh Đức** | Back-end | Quản trị Media/Storage & API | **Backend Engineer & Storage/Media Specialist** |
| **3** | **Hà Đăng Huy** | Back-end | Kiểm thử chất lượng (QA) | **Backend Engineer & QA Lead / API Tester** |
| **4** | **Tạ Quang Huy** | Front-end | Kiểm thử tự động (QA) | **Frontend Engineer & Mobile QA / UI Tester** |
| **5** | **Tùng Dương** | Front-end | Thiết kế & Điều phối UI | **Lead Frontend Engineer & UI/UX Coordinator** |
| **6** | **Minh Lương** | Front-end | Phát triển tính năng chính | **Frontend Engineer (Feed & Social Interactions)** |
| **7** | **Phúc Đức** | Front-end | Phát triển tính năng chính | **Frontend Engineer (Profile & Friends System)** |

---

## 2. BẢN MÔ TẢ TRÁCH NHIỆM CHI TIẾT TỪNG THÀNH VIÊN (JOB DESCRIPTIONS)

### 2.1. Nhóm Backend & Dữ liệu (3 Thành viên)
- **Ánh Dương (Lead Backend Engineer & Database Architect - Main Backend):**
  - Chịu trách nhiệm thiết kế tổng thể kiến trúc CSDL SQLite (`facebook.db`), SQLAlchemy Models, Pydantic Schemas.
  - Triển khai Core RESTful API trên Python FastAPI: Feed API, Quản lý đăng bài, tương tác Like/Share, tối ưu truy vấn SQL và indexing.
  - Cấu hình server Uvicorn, CORS middleware, và phê duyệt (Review & Approve) toàn bộ Pull Request của tầng Backend.
- **Minh Đức (Backend Engineer & Storage/Media Specialist):**
  - Xây dựng Media Upload API trên FastAPI (xử lý `UploadFile`, lưu file ảnh vào thư mục `static/uploads/`, sinh URL ảnh).
  - Triển khai các API phân hệ Trang cá nhân (Profile) và Hệ thống Thông báo (Notifications) trên FastAPI + SQLite.
  - Viết tài liệu API Contract (Swagger/OpenAPI) và hỗ trợ Mock/Seed data cho nhóm Frontend trong Sprint 1.
- **Hà Đăng Huy (Backend Engineer & QA Lead / API Tester):**
  - **Phần Backend:** Triển khai các API phân hệ Bạn bè (Friends, Friend Requests, Search) trên FastAPI + SQLite.
  - **Phần Testing:** Lập Master Test Plan, viết Unit Test cho API với `pytest` / `TestClient`, kiểm thử tích hợp Android Retrofit Repositories và SQLite transactions.

### 2.2. Nhóm Frontend & Giao diện (4 Thành viên)
- **Tùng Dương (Lead Frontend Engineer & UI/UX Coordinator):**
  - Khởi tạo kiến trúc khung Android (BaseActivity, BaseFragment, ViewModel setup, Theme, Navigation).
  - Xây dựng Màn hình Đăng nhập (Auth), Top Bar, Bottom Navigation, và Màn hình Tạo bài viết (`CreatePostActivity`).
  - Đảm bảo tính nhất quán của Design System toàn app và review code tầng UI.
- **Minh Lương (Frontend Engineer - Feed & Social Interactions):**
  - Chịu trách nhiệm toàn bộ giao diện và logic của Bảng tin (`FeedFragment`).
  - Xây dựng `PostAdapter`, `PostViewHolder`, xử lý thao tác Like mượt mà (Optimistic UI) và Share bài viết.
  - Xây dựng Hộp thoại Bình luận (`CommentBottomSheetDialog`, `CommentAdapter`) chuẩn text-only.
- **Phúc Đức (Frontend Engineer - Profile & Friends System):**
  - Xây dựng Màn hình Bạn bè (`FriendsFragment`), danh sách lời mời kết bạn và thanh Tìm kiếm bạn bè (`SearchFriendsActivity`).
  - Xây dựng Màn hình Trang cá nhân (`ProfileFragment`), hiển thị Tab bài viết và Tab ảnh/video dạng lưới 3 cột (`PhotoGridAdapter`).
- **Tạ Quang Huy (Frontend Engineer & Mobile QA / UI Tester):**
  - **Phần Frontend:** Xây dựng Màn hình Thông báo (`NotificationsFragment`), các thành phần Empty/Loading/Error State tái sử dụng, tối ưu Glide caching.
  - **Phần Testing:** Thiết kế kịch bản kiểm thử giao diện (UI Test Cases), viết Unit Test cho toàn bộ Presentation Layer (`ViewModels`) và thực hiện kiểm thử thủ công (Manual Testing) trên máy ảo trước mỗi bản release.

---

## 3. MA TRẬN CÂN BẰNG KHỐI LƯỢNG CÔNG VIỆC (WORKLOAD BALANCING MATRIX)

> [!IMPORTANT]
> **Giải quyết bài toán kiêm nhiệm role:**
> - Hà Đăng Huy và Tạ Quang Huy kiêm nhiệm vai trò Testing (QA).
> - Để tổng khối lượng công việc của cả 7 thành viên **hoàn toàn bằng nhau (100% Equal Workload = 14.3% mỗi người)**, khối lượng coding tính năng của hai bạn được giảm tải tương ứng để nhường chỗ cho công tác viết Test Script, lập Test Plan và kiểm thử nghiệm thu.

### Bảng phân bổ Story Points (SP) chuẩn hóa (Tổng dự án: 140 SP):

| Thành viên | Feature Dev (Code tính năng) | Testing & QA (Kiểm thử) | Architecture / DevOps / Docs | TỔNG STORY POINTS | Tỷ trọng dự án |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Ánh Dương** | 12 SP (Core FastAPI & Feed API) | 2 SP (Hỗ trợ Test DB/API) | 6 SP (Lead BE, SQLite Schema, Arch) | **20 SP** | **14.3%** |
| **Minh Đức** | 14 SP (Profile, Notif & Upload API) | 2 SP (Hỗ trợ Test API) | 4 SP (FastAPI Setup, Static Storage) | **20 SP** | **14.3%** |
| **Hà Đăng Huy** | 9 SP (Friend API & Search SQLite) | **9 SP** (Lead QA, API Unit Tests pytest) | 2 SP (Tài liệu Test Plan) | **20 SP** | **14.3%** |
| **Tạ Quang Huy** | 9 SP (Notification UI & States) | **9 SP** (ViewModel Unit Tests & Manual QA) | 2 SP (UI Test Documentation) | **20 SP** | **14.3%** |
| **Tùng Dương** | 12 SP (App Shell, Auth, CreatePost) | 2 SP (Review & Self-test) | 6 SP (Lead FE, Base Code, Design System) | **20 SP** | **14.3%** |
| **Minh Lương** | 16 SP (Feed UI, Like, Comment, Share) | 2 SP (Unit test Feed logic) | 2 SP (Tài liệu Feed module) | **20 SP** | **14.3%** |
| **Phúc Đức** | 16 SP (Profile UI, Friends UI, Grid) | 2 SP (Unit test Profile logic) | 2 SP (Tài liệu Profile module) | **20 SP** | **14.3%** |
| **TỔNG CỘNG** | **88 SP (62.8%)** | **28 SP (20.0%)** | **24 SP (17.2%)** | **140 SP** | **100.0%** |

---

## 4. QUY TRÌNH HỌP VÀ BÁO CÁO TIẾN ĐỘ (AGILE CEREMONIES)

1. **Daily Stand-up (10 phút mỗi ngày - qua Discord/Zalo):**
   - Trả lời 3 câu hỏi:
     1. Hôm qua tôi đã hoàn thành task gì?
     2. Hôm nay tôi sẽ làm task gì?
     3. Tôi có đang gặp khó khăn (blocker) gì cần người khác hỗ trợ không?
2. **Sprint Planning & Daily Tracking (Lộ trình 2 tuần - 14 ngày):**
   - Theo dõi sát sao từng ngày theo bảng điều khiển tại [TODO.md](../TODO.md) và [06_TASKS_SPRINT_PLAN.md](./06_TASKS_SPRINT_PLAN.md).
   - Mỗi thành viên hoàn thành task trong ngày thì tick `[x]` vào bảng tiến độ.
3. **Sprint Review & Retrospective (Cuối mỗi tuần):**
   - Chạy demo trực tiếp trên máy ảo chuẩn **Pixel 10a (API 37.2)**.
   - QA báo cáo số lượng bug đã phát hiện và tình trạng fix lỗi.
