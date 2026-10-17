# 📚 BỘ TÀI LIỆU KỸ THUẬT & QUẢN TRỊ DỰ ÁN FACEBOOK MOBILE CLIENT

> **Dự án:** Ứng dụng di động Facebook Client (Bài tập lớn / Đồ án môn học)  
> **Nền tảng:** Android (Java 17 LTS + XML, Android Studio) & Backend Python FastAPI + SQLite  
> **Quy mô nhóm:** 7 thành viên  
> **Tiêu chuẩn thiết kế:** Clean Architecture + MVVM, RESTful API (Retrofit 2), Modular Design, Agile/Scrum Industry Standard  

---

## 📌 DANH MỤC HỆ THỐNG TÀI LIỆU

Bộ tài liệu này được thiết kế và chuẩn hóa toàn diện theo quy trình phát triển phần mềm doanh nghiệp, đáp ứng đầy đủ 17 yêu cầu đặc tả của dự án:

| STT | Tài liệu | Mô tả nội dung chính | Đáp ứng yêu cầu |
| :---: | :--- | :--- | :---: |
| **01** | [**01_OVERVIEW.md**](./01_OVERVIEW.md) | Tổng quan bài toán, mục tiêu đồ án, phạm vi nghiệp vụ **In-Scope vs Out-of-Scope** | Mục 1, 10 |
| **02** | [**02_ARCHITECTURE.md**](./02_ARCHITECTURE.md) | Kiến trúc hệ thống, Pattern chuẩn (MVVM + Clean Architecture), Retrofit 2, Modular Design, Package structure | Mục 2, 6 |
| **03** | [**03_DESIGN_SYSTEM.md**](./03_DESIGN_SYSTEM.md) | Hệ thống thiết kế UI/UX (**DESIGN.md**), Color Palette, Typography, Component Specs | Mục 8 |
| **04** | [**04_USER_FLOW.md**](./04_USER_FLOW.md) | Sơ đồ luồng người dùng (User Journeys, Sequence Diagrams với Mermaid) | Mục 9 |
| **05** | [**05_TEAM_ROLES_WORKLOAD.md**](./05_TEAM_ROLES_WORKLOAD.md) | Cơ cấu 7 nhân sự chuẩn công ty IT, phân bổ vai trò và ma trận cân bằng khối lượng công việc (**Equal Workload**) | Mục 11, 16 |
| **06** | [**06_TASKS_SPRINT_PLAN.md**](./06_TASKS_SPRINT_PLAN.md) | Kế hoạch 2 tuần (14 ngày) thần tốc, chi tiết từng ngày cho từng thành viên, có **checkbox tick** | Mục 3, 14 |
| **07** | [**07_GIT_WORKFLOW_NAMING.md**](./07_GIT_WORKFLOW_NAMING.md) | Quy trình Git nhóm (Branch, Push, Pull, PR, Review) & Quy chuẩn đặt tên toàn diện (**Naming Conventions**) | Mục 4, 5 |
| **08** | [**08_CODING_CONVENTION_DOCS.md**](./08_CODING_CONVENTION_DOCS.md) | Quy chuẩn viết code Java/XML, ViewBinding, Quản lý State, Javadoc & Tiêu chuẩn tài liệu hóa | Mục 7 |
| **09** | [**09_BACKEND_FREE_SERVER.md**](./09_BACKEND_FREE_SERVER.md) | Kiến trúc Server Backend (Python FastAPI) & CSDL (SQLite), Schema ERD, RESTful API Contracts | Mục 12 |
| **10** | [**10_TESTING_GUIDE.md**](./10_TESTING_GUIDE.md) | Hướng dẫn viết **Unit Test** Android (JUnit + Mockito) & Backend (Pytest) có code mẫu chi tiết | Mục 13 |
| **11** | [**11_BUG_TRIAGE_PROCESS.md**](./11_BUG_TRIAGE_PROCESS.md) | Quy trình quản lý và xử lý lỗi (**Bug Triage**), Ma trận trách nhiệm ai fix lỗi gì (RACI), Hotfix flow | Mục 15 |
| **12** | [**12_ENVIRONMENT_SETUP.md**](./12_ENVIRONMENT_SETUP.md) | Hướng dẫn cài đặt **JDK 17**, Python FastAPI, Clone GitHub, Setup Package, **Máy ảo chuẩn Pixel 10a (API 37.2)** | Mục 17 |

---

## 🧭 HƯỚNG DẪN ĐỌC TÀI LIỆU THEO VAI TRÒ

- **Dành cho Tech Lead & Quản lý:** Đọc ngay `01_OVERVIEW.md`, `05_TEAM_ROLES_WORKLOAD.md`, `06_TASKS_SPRINT_PLAN.md`, `11_BUG_TRIAGE_PROCESS.md`.
- **Dành cho Frontend Developers (Tùng Dương, Minh Lương, Phúc Đức, Tạ Quang Huy):** Đọc kỹ `02_ARCHITECTURE.md`, `03_DESIGN_SYSTEM.md`, `04_USER_FLOW.md`, `07_GIT_WORKFLOW_NAMING.md`, `08_CODING_CONVENTION_DOCS.md`.
- **Dành cho Backend Developers (Ánh Dương, Minh Đức, Hà Đăng Huy):** Đọc kỹ `02_ARCHITECTURE.md`, `06_TASKS_SPRINT_PLAN.md`, `09_BACKEND_FREE_SERVER.md`, `07_GIT_WORKFLOW_NAMING.md`.
- **Dành cho Testers / QA (Hà Đăng Huy, Tạ Quang Huy):** Đọc kỹ `10_TESTING_GUIDE.md`, `11_BUG_TRIAGE_PROCESS.md`, `04_USER_FLOW.md`.
- **Dành cho tất cả thành viên khi Onboarding:** Bắt buộc thực hiện theo các bước trong `12_ENVIRONMENT_SETUP.md`.
