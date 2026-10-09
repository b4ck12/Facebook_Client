# 📱 FACEBOOK MOBILE CLIENT (ANDROID APP)

> **Học phần:** Phát triển Ứng dụng Di động / Đồ án Bài tập lớn (BTL)  
> **Nền tảng:** Android OS (Java 17 LTS + XML Layouts)  
> **Kiến trúc:** MVVM + Clean Architecture + Modular by Feature + Repository Pattern  
> **Backend & Database:** Google Firebase (Firestore + Cloud Storage - Free Tier)  
> **Thiết bị máy ảo chuẩn:** Google Pixel 10a (Android 14 - API Level 34)  
> **Quy mô đội ngũ:** 7 thành viên (Vận hành theo mô hình Agile/Scrum Squad chuyên nghiệp)

---

## 🧭 HƯỚNG DẪN ĐỌC BỘ TÀI LIỆU DỰ ÁN (DOCUMENTATION GUIDE)

Dự án sở hữu hệ thống tài liệu kỹ thuật hoàn chỉnh gồm **13 tài liệu chi tiết** trong thư mục [`docs/`](./docs/INDEX.md). Để tiếp cận thông tin nhanh nhất, vui lòng chọn hướng dẫn đọc phù hợp với vai trò của bạn:

```
                                  BẮT ĐẦU TỪ ĐÂY
                                        │
        ┌───────────────────────────────┼───────────────────────────────┐
        ▼                               ▼                               ▼
 🎓 GIẢNG VIÊN / CHẤM ĐIỂM       🚀 THÀNH VIÊN MỚI (ONBOARDING)   🛠️ DEV THEO VAI TRÒ
   1. docs/01_OVERVIEW.md          1. docs/12_ENVIRONMENT_SETUP.md   Frontend: 02, 03, 04, 08
   2. docs/02_ARCHITECTURE.md      2. docs/07_GIT_WORKFLOW_NAMING.md  Backend:  02, 06, 09
   3. docs/05_TEAM_ROLES.md        3. TODO.md (Bảng tiến độ)          QA/Test:  04, 10, 11
   4. docs/10_TESTING_GUIDE.md
```

### 1. Dành cho Giảng viên & Hội đồng Đánh giá Đồ án:
1. Đọc [**01_OVERVIEW.md**](./docs/01_OVERVIEW.md) để đánh giá tính tuân thủ 100% đặc tả gốc của đề tài, các ranh giới In-Scope vs Out-of-Scope.
2. Đọc [**02_ARCHITECTURE.md**](./docs/02_ARCHITECTURE.md) để thẩm định kiến trúc Clean Architecture, MVVM, ViewBinding, phân tách module và Repository Pattern.
3. Đọc [**05_TEAM_ROLES_WORKLOAD.md**](./docs/05_TEAM_ROLES_WORKLOAD.md) để xem ma trận đóng góp công bằng (**Equal Workload 20 Story Points / 14.3% mỗi người**) của 7 sinh viên.
4. Đọc [**10_TESTING_GUIDE.md**](./docs/10_TESTING_GUIDE.md) để xem báo cáo kiểm thử Unit Test (JUnit + Mockito) đạt chuẩn chất lượng.

---

### 2. Dành cho Thành viên khi mới bắt đầu (Onboarding Checklist):
*Mọi thành viên bắt buộc phải đọc và thực hành theo đúng thứ tự 3 bước:*
* **Bước 1:** Mở ngay [**12_ENVIRONMENT_SETUP.md**](./docs/12_ENVIRONMENT_SETUP.md) để cài đặt đúng **JDK 17**, tạo máy ảo chuẩn **Pixel 10a (API 34)** và clone repo về Android Studio.
* **Bước 2:** Đọc kỹ [**07_GIT_WORKFLOW_NAMING.md**](./docs/07_GIT_WORKFLOW_NAMING.md) để nắm quy tắc đặt tên nhánh (`feature/*`), quy chuẩn commit (`feat:`, `fix:`) và cách tạo Pull Request (PR) không bị từ chối.
* **Bước 3:** Mở [**TODO.md**](./TODO.md) để xem danh sách công việc được phân công cho mình trong 14 ngày và tick `[x]` khi làm xong.

---

### 3. Dành cho Lập trình viên theo từng chuyên môn:

#### A. Nhóm Frontend (Tùng Dương, Minh Lương, Phúc Đức, Tạ Quang Huy):
1. [**03_DESIGN_SYSTEM.md**](./docs/03_DESIGN_SYSTEM.md) (hoặc [DESIGN.md](./docs/DESIGN.md)): Bảng màu Facebook `#1877F2`, kích thước lề, typography và quy cách từng component.
2. [**04_USER_FLOW.md**](./docs/04_USER_FLOW.md): Sơ đồ luồng người dùng tạo bài viết, like, comment text-only, tìm bạn bè.
3. [**08_CODING_CONVENTION_DOCS.md**](./docs/08_CODING_CONVENTION_DOCS.md): Chuẩn viết Java, ViewBinding trong Activity/Fragment (tránh memory leak), State pattern `Resource<T>`.

#### B. Nhóm Backend (Ánh Dương, Minh Đức, Hà Đăng Huy):
1. [**09_BACKEND_FREE_SERVER.md**](./docs/09_BACKEND_FREE_SERVER.md): Cấu hình Google Firebase (Firestore, Storage), ERD CSDL, Firestore Security Rules.
2. [**02_ARCHITECTURE.md**](./docs/02_ARCHITECTURE.md): Định nghĩa Repository Interface và Data DTOs.
3. [**06_TASKS_SPRINT_PLAN.md**](./docs/06_TASKS_SPRINT_PLAN.md): Quy trình cung cấp Mock Data cho FE trong tuần 1.

#### C. Nhóm Kiểm thử & Đảm bảo Chất lượng QA (Hà Đăng Huy, Tạ Quang Huy):
1. [**10_TESTING_GUIDE.md**](./docs/10_TESTING_GUIDE.md): Cách viết Unit Test với JUnit 4, Mockito và InstantTaskExecutorRule cho LiveData.
2. [**11_BUG_TRIAGE_PROCESS.md**](./docs/11_BUG_TRIAGE_PROCESS.md): Quy trình báo lỗi, phân loại P0-P3, ma trận RACI *"Ai fix lỗi gì"* và quy trình đóng Bug.

---

## 📚 BẢNG MỤC LỤC CHI TIẾT TOÀN BỘ TÀI LIỆU (`docs/`)

| STT | File Tài liệu | Tóm tắt nội dung chính | Đối tượng cần đọc |
| :---: | :--- | :--- | :--- |
| **00** | [**docs/INDEX.md**](./docs/INDEX.md) | Mục lục tổng quan liên kết toàn bộ tài liệu | Toàn đội ngũ |
| **01** | [**docs/01_OVERVIEW.md**](./docs/01_OVERVIEW.md) | Tổng quan dự án, đối chiếu 100% đặc tả PDF, giới hạn In-Scope / Out-of-Scope | Cả nhóm & Giảng viên |
| **02** | [**docs/02_ARCHITECTURE.md**](./docs/02_ARCHITECTURE.md) | Kiến trúc hệ thống Clean Arch + MVVM, Modular Design, Package structure | Developers |
| **03** | [**docs/03_DESIGN_SYSTEM.md**](./docs/03_DESIGN_SYSTEM.md) | Hệ thống thiết kế UI/UX (**DESIGN.md**), Palette màu, Roboto Font, Specs | Frontend Devs |
| **--** | [**docs/DESIGN.md**](./docs/DESIGN.md) | Bản đối chiếu nhanh DESIGN.md | Frontend Devs |
| **04** | [**docs/04_USER_FLOW.md**](./docs/04_USER_FLOW.md) | Sơ đồ luồng người dùng (User Journeys & Sequence Diagrams bằng Mermaid) | Frontend & QA |
| **05** | [**docs/05_TEAM_ROLES_WORKLOAD.md**](./docs/05_TEAM_ROLES_WORKLOAD.md) | Cơ cấu 7 nhân sự, cân bằng 100% khối lượng công việc (Equal Workload 20 SP) | Tech Lead & Cả nhóm |
| **06** | [**docs/06_TASKS_SPRINT_PLAN.md**](./docs/06_TASKS_SPRINT_PLAN.md) | Kế hoạch chi tiết 2 tuần (14 ngày), task từng ngày từng người có tickbox | Toàn đội ngũ |
| **07** | [**docs/07_GIT_WORKFLOW_NAMING.md**](./docs/07_GIT_WORKFLOW_NAMING.md) | Quy trình Git nhóm, Branch, PR Review & Quy chuẩn đặt tên Java / XML | Developers |
| **08** | [**docs/08_CODING_CONVENTION_DOCS.md**](./docs/08_CODING_CONVENTION_DOCS.md) | Tiêu chuẩn mã nguồn Google Java, ViewBinding, Resource Pattern, Javadoc | Developers |
| **09** | [**docs/09_BACKEND_FREE_SERVER.md**](./docs/09_BACKEND_FREE_SERVER.md) | Giải pháp Server & DB Free (Firebase), Schema ERD, Security Rules | Backend Devs |
| **10** | [**docs/10_TESTING_GUIDE.md**](./docs/10_TESTING_GUIDE.md) | Hướng dẫn viết Unit Test (JUnit + Mockito) có code mẫu Java chi tiết | QA & Developers |
| **11** | [**docs/11_BUG_TRIAGE_PROCESS.md**](./docs/11_BUG_TRIAGE_PROCESS.md) | Quy trình quản lý lỗi Bug Triage, ma trận phân định trách nhiệm RACI | QA & Developers |
| **12** | [**docs/12_ENVIRONMENT_SETUP.md**](./docs/12_ENVIRONMENT_SETUP.md) | Hướng dẫn cài đặt Java 17, Clone Git, Package & Máy ảo Pixel 10a chuẩn | Bắt buộc khi Onboard |

---

## ⚡ KHỞI ĐỘNG NHANH TRONG 3 BƯỚC (QUICKSTART)

### Bước 1: Clone Repository
```bash
git clone https://github.com/<your-team>/Facebook_Client.git
cd Facebook_Client
```

### Bước 2: Mở bằng Android Studio & Chọn JDK 17
1. Mở **Android Studio** -> Chọn **Open** -> Trỏ tới thư mục `Facebook_Client`.
2. Vào `Settings -> Build Tools -> Gradle -> Gradle JDK` -> Chọn **JDK 17**.
3. Bấm **Sync Project with Gradle Files**.

### Bước 3: Tạo Máy ảo Pixel 10a & Chạy App
1. Vào `Tools -> Device Manager -> Create Device`.
2. Chọn/Tạo cấu hình phần cứng **Pixel 10a** (6.1 inch, 1080x2400).
3. Chọn System Image **API 34 (Android 14 - UpsideDownCake)** -> Đặt tên `Pixel_10a_API_34`.
4. Bấm tổ hợp phím `Shift + F10` để khởi chạy ứng dụng!

---

## 👥 THÀNH VIÊN DỰ ÁN & PHÂN CÔNG VAI TRÒ

| STT | Thành viên | Vai trò chính | Vai trò kiêm nhiệm | Chức danh chuẩn | Đóng góp (SP) |
| :---: | :--- | :--- | :--- | :--- | :---: |
| 1 | **Ánh Dương** | Back-end | Kiến trúc CSDL | **Lead Backend Engineer & Database Architect** | 20 SP (14.3%) |
| 2 | **Minh Đức** | Back-end | Quản trị Cloud | **Backend Engineer & Cloud/Storage Specialist** | 20 SP (14.3%) |
| 3 | **Hà Đăng Huy** | Back-end | Kiểm thử (QA) | **Backend Engineer & QA Lead / API Tester** | 20 SP (14.3%) |
| 4 | **Tạ Quang Huy** | Front-end | Kiểm thử (QA) | **Frontend Engineer & Mobile QA / UI Tester** | 20 SP (14.3%) |
| 5 | **Tùng Dương** | Front-end | Điều phối UI | **Lead Frontend Engineer & UI/UX Coordinator** | 20 SP (14.3%) |
| 6 | **Minh Lương** | Front-end | Feed & Social | **Frontend Engineer (Feed & Social Interactions)** | 20 SP (14.3%) |
| 7 | **Phúc Đức** | Front-end | Profile & Friends | **Frontend Engineer (Profile & Friends System)** | 20 SP (14.3%) |

👉 **Bảng theo dõi tiến độ công việc hàng ngày:** [**TODO.md**](./TODO.md)
