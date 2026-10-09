# 🎨 03. HỆ THỐNG THIẾT KẾ GIAO DIỆN & TRẢI NGHIỆM (DESIGN SYSTEM - DESIGN.md)

---

## 1. TỔNG QUAN PHONG CÁCH (DESIGN PHILOSOPHY)

Hệ thống thiết kế của **Facebook Mobile Client** hướng tới phong cách tối giản, hiện đại, mang đậm bản sắc giao diện quen thuộc của ứng dụng Facebook Android chính thức:
- **Tone màu chủ đạo:** Xanh Facebook (#1877F2), Nền xám nhạt (#F0F2F5) phân tách các thẻ bài viết trắng (#FFFFFF).
- **Trải nghiệm mượt mà:** Bo góc mềm mại (8dp - 12dp), độ tương phản cao, typography rõ ràng, khoảng cách chuẩn lưới 4dp/8dp.
- **Tiêu chí tinh gọn:** Loại bỏ hoàn toàn các thành phần trang trí gây rối mắt theo đặc tả `Mobile App.pdf` (không sticker, không hiệu ứng phức tạp).

---

## 2. BẢNG MÀU CHUẨN HÓA (COLOR PALETTE)

Mọi lập trình viên giao diện bắt buộc khai báo và sử dụng màu sắc trong `res/values/colors.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Màu thương hiệu chính (Brand Colors) -->
    <color name="fb_blue">#1877F2</color>
    <color name="fb_blue_dark">#166FE5</color>
    <color name="fb_blue_light">#E7F3FF</color>

    <!-- Nền và Thẻ (Surfaces & Backgrounds) -->
    <color name="fb_background">#F0F2F5</color>
    <color name="fb_surface">#FFFFFF</color>
    <color name="fb_surface_variant">#E4E6EB</color>

    <!-- Văn bản (Typography Colors) -->
    <color name="fb_text_primary">#050505</color>
    <color name="fb_text_secondary">#65676B</color>
    <color name="fb_text_hint">#8A8D91</color>
    <color name="fb_text_on_blue">#FFFFFF</color>

    <!-- Đường kẻ & Phân cách (Borders & Dividers) -->
    <color name="fb_divider">#CED0D4</color>
    <color name="fb_divider_light">#E4E6EB</color>

    <!-- Màu trạng thái & Tương tác (Semantic & Action Colors) -->
    <color name="fb_like_blue">#1877F2</color>
    <color name="fb_heart_red">#FA383E</color>
    <color name="fb_action_gray">#65676B</color>
    <color name="fb_online_green">#31A24C</color>
    <color name="fb_error_red">#E41E3F</color>
    <color name="fb_shimmer_gray">#D8DADF</color>
</resources>
```

---

## 3. TYPOGRAPHY & PHÔNG CHỮ

- Phông chữ hệ thống: **Roboto** (mặc định Android).
- Quy chuẩn kích thước và độ đậm (Style Scale):

| Kiểu văn bản (Style) | Kích thước (sp) | Độ đậm (Font Weight) | Màu sắc gợi ý | Mục đích sử dụng |
| :--- | :---: | :---: | :---: | :--- |
| **Headline / App Title** | 22sp | Bold (700) | `fb_blue` | Logo "facebook" trên thanh điều hướng đỉnh |
| **User Name (Tác giả)** | 15sp | SemiBold (600) | `fb_text_primary` | Tên người đăng bài, tên trong danh sách bạn bè |
| **Post Content Text** | 15sp | Regular (400) | `fb_text_primary` | Nội dung bài viết (lineSpacingExtra: 3dp) |
| **Comment Text** | 14sp | Regular (400) | `fb_text_primary` | Nội dung bình luận |
| **Metadata / Timestamp** | 12sp | Regular (400) | `fb_text_secondary`| Thời gian đăng (5 phút trước), ngày kết bạn |
| **Action Button Text** | 13sp | Medium (500) | `fb_action_gray` | Chữ nút Thích, Bình luận, Chia sẻ |
| **Badge / Count** | 12sp | Regular (400) | `fb_text_secondary`| Số lượt thích, số bình luận |

---

## 4. QUY CHUẨN KHOẢNG CÁCH & BO GÓC (SPACING & ELEVATION)

Khai báo trong `res/values/dimens.xml`:
- **Lưới khoảng cách (Spacing Grid):**
  - `spacing_xs`: 4dp
  - `spacing_sm`: 8dp
  - `spacing_md`: 12dp
  - `spacing_lg`: 16dp
  - `spacing_xl`: 24dp
- **Bo góc (Corner Radius):**
  - Avatar tròn: 50% (CircleCrop)
  - Card bài viết: 0dp (tràn viền ngang) hoặc 8dp
  - Khung nhập bình luận (Comment bubble): 16dp
  - Nút bấm (Button): 6dp - 8dp
  - Nút tròn icon (Circle action button): 20dp
- **Độ nổi khối (Elevation):**
  - Top AppBar & Bottom Navigation: 4dp
  - Card bài viết: 1dp

---

## 5. ĐẶC TẢ CHI TIẾT CÁC THÀNH PHẦN GIAO DIỆN (COMPONENT SPECS)

### 5.1. Top Navigation Bar (Header đỉnh)
- Chiều cao: `56dp`.
- Trái: Logo "facebook" viết thường chữ đậm, màu `fb_blue`, cỡ chữ 22sp.
- Phải: Hai nút icon tròn màu nền `fb_surface_variant`:
  - Nút Tìm kiếm (`ic_search`, 20dp)
  - Nút Menu/Cài đặt đơn giản (`ic_menu`, 20dp)

### 5.2. Bottom Navigation Bar (4 Tab chính)
- Chiều cao: `56dp`, nền `fb_surface`, đường viền đỉnh 1dp `fb_divider_light`.
- 4 Tab cố định theo đặc tả:
  1. 🏠 **Feed** (Icon bảng tin)
  2. 👥 **Friends** (Icon bạn bè)
  3. 🔔 **Notifications** (Icon quả chuông)
  4. 👤 **Profile** (Icon người dùng / Mini Avatar)
- Trạng thái Active: Icon & chữ chuyển màu `fb_blue`. Trạng thái Inactive: màu `fb_action_gray`.

### 5.3. Khu vực tạo bài viết nhanh (Create Post Box trên đỉnh Feed)
- Nằm ngay dưới Top Bar của Feed.
- Thành phần:
  - Mini Avatar tròn (36dp).
  - Khung giả lập nhập liệu (Mock Input) với chữ hint: "Bạn đang nghĩ gì?" (What's on your mind?). Khi bấm vào -> Chuyển sang màn hình `CreatePostActivity`.
  - Phân cách bằng đường viền mỏng 8dp màu `fb_background`.

### 5.4. Màn hình tạo bài viết (CreatePostActivity)
- **Top Bar:** Nút quay lại (Back arrow), tiêu đề "Tạo bài viết", nút "ĐĂNG" (POST) ở góc phải trên cùng.
- **Header tác giả:** Avatar (44dp), Tên người dùng, Huy hiệu quyền riêng tư "Công khai" (Public) kèm icon quả địa cầu cố định.
- **Khu vực nhập văn bản:** `EditText` không viền, cỡ chữ 17sp, hint "Bạn đang nghĩ gì?".
- **Preview Media:** `ImageView` hiển thị ảnh vừa chọn từ máy (kèm nút [X] ở góc ảnh để xóa ảnh nếu không muốn đăng nữa).
- **Thanh công cụ đáy (Bottom Toolbar):**
  - Chỉ có duy nhất 1 nút bấm: **"Thêm ảnh/video" (Add photo/video)** có icon ảnh màu xanh lá cây.
  - **TUYỆT ĐỐI KHÔNG HIỂN THỊ:** icon Gắn thẻ (Tag), Vị trí (Location), Âm nhạc (Music), Cảm xúc (Feeling).
- **Ràng buộc UI:** Nút "ĐĂNG" ở trạng thái mờ (Disabled - alpha 0.4) khi ô văn bản trống VÀ chưa chọn ảnh. Khi có văn bản HOẶC có ảnh -> Nút sáng màu `fb_blue` và cho phép bấm.

### 5.5. Thẻ bài viết (Post Card - `item_post.xml`)
- Nền trắng `fb_surface`, bên dưới là dải phân cách 8dp màu `fb_background`.
- **Phần đầu (Header):**
  - Avatar (40dp) tròn bên trái.
  - Tên tác giả (15sp Bold), bên dưới là thời gian (12sp Regular) + icon quả địa cầu (12dp).
  - Nút 3 chấm ngang [...] bên phải (tùy chọn Xóa bài nếu là bài của chính mình).
- **Nội dung (Body):**
  - Đoạn văn bản mô tả (padding 12dp).
  - `ImageView` hiển thị ảnh (nếu bài viết có ảnh): chiều rộng `match_parent`, `adjustViewBounds=true`, `scaleType=centerCrop`, chiều cao tối đa `400dp`.
- **Thống kê (Footer Stats):**
  - Trái: Icon like xanh nhỏ + Số lượt thích (Ví dụ: `👍 24`).
  - Phải: Số lượng bình luận (Ví dụ: `5 bình luận`).
- **Thanh thao tác (Action Bar):** Chia đều 3 cột với đường kẻ viền mỏng trên dưới:
  1. Nút **Thích (Like):** Icon ngón tay cái. Khi đã like: Icon đổi sang màu xanh `fb_like_blue` và chữ "Thích" in đậm màu xanh.
  2. Nút **Bình luận (Comment):** Icon bong bóng thoại + chữ "Bình luận". Khi bấm -> Mở `CommentBottomSheetDialog`.
  3. Nút **Chia sẻ (Share):** Icon mũi tên cong + chữ "Chia sẻ". Khi bấm -> Hiển thị hộp thoại xác nhận "Chia sẻ ngay lên bảng tin?".

### 5.6. Hộp thoại Bình luận (Comment Bottom Sheet)
- Trượt từ dưới lên (Bottom Sheet Dialog).
- **Danh sách bình luận (`RecyclerView`):**
  - Mỗi bình luận gồm: Avatar nhỏ (32dp), bong bóng xám bo tròn 16dp màu `fb_background` chứa Tên người bình luận (Bold) và Nội dung chữ thuần túy (Text only).
  - Bên dưới bong bóng: Thời gian đăng (11sp).
  - **Không có nút Trả lời, không có reaction vào comment, không có đính kèm ảnh/video.**
- **Thanh nhập bình luận đáy:**
  - Ô nhập `EditText` viền bo tròn 20dp, màu nền `fb_surface_variant`, hint "Viết bình luận...".
  - Nút icon "Gửi" (`ic_send`, màu `fb_blue`).

### 5.7. Màn hình Bạn bè (`fragment_friends.xml`)
- **Thanh tìm kiếm:** `SearchView` hoặc `EditText` bo tròn, hint "Tìm kiếm bạn bè".
- **Mục Lời mời kết bạn (Friend Requests):**
  - Avatar tròn lớn (60dp).
  - Tên người gửi kết bạn (16sp Bold), thời gian gửi lời mời.
  - Cặp nút bấm:
    - Nút xanh "Chấp nhận" (Confirm) - màu `fb_blue`.
    - Nút xám "Xóa" (Delete) - màu `fb_surface_variant`, chữ `fb_text_primary`.
- **Mục Bạn bè của bạn (Your Friends):**
  - Danh sách người bạn hiện tại, nút tùy chọn Xem trang cá nhân.

### 5.8. Màn hình Trang cá nhân (`fragment_profile.xml`)
- **Ảnh bìa (Cover Photo):** Tỷ lệ 16:9, chiều cao 200dp, `scaleType=centerCrop`.
- **Ảnh đại diện (Profile Picture):** Hình tròn 120dp, viền trắng 4dp đè 50% lên ảnh bìa.
- **Tên hiển thị:** 22sp Bold, căn giữa hoặc căn trái dưới avatar.
- **Hệ thống Tab điều hướng (TabLayout):**
  - Tab 1: **Bài viết (Posts)** -> Hiển thị dòng thời gian các bài viết do user đăng.
  - Tab 2: **Ảnh & Video (Photos/Videos)** -> Hiển thị `GridLayout` 3 cột các bức ảnh đã tải lên.

---

## 6. TRẠNG THÁI HIỂN THỊ (UI STATES)

Mỗi màn hình bắt buộc phải xử lý đủ 4 trạng thái:
1. **Loading State:** Hiển thị `ProgressBar` xoay vòng ở giữa màn hình hoặc khung xám Shimmer Placeholder.
2. **Empty State:** Khi không có dữ liệu (chưa có bài viết nào, không có lời mời kết bạn), hiển thị một hình minh họa vector nhẹ nhàng kèm dòng thông báo: *"Chưa có bài viết nào. Hãy là người đầu tiên chia sẻ!"*.
3. **Error State:** Khi lỗi kết nối mạng hoặc lỗi server, hiển thị thông báo lỗi và nút "Thử lại" (Retry Button).
4. **Content State:** Hiển thị dữ liệu hoàn chỉnh.
