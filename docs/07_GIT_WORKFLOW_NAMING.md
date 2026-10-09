# 🌿 07. QUY TRÌNH LÀM VIỆC GIT & QUY CHUẨN ĐẶT TÊN (GIT WORKFLOW & NAMING CONVENTIONS)

---

## 1. MÔ HÌNH NHÁNH GIT (GIT BRANCHING STRATEGY)

Dự án áp dụng mô hình chuẩn **Feature Branch Workflow** (biến thể tinh gọn của Git Flow phù hợp cho nhóm 7 người):

```
master (chỉ chứa code đã phát hành nghiệm thu, bảo vệ đồ án)
  ^
  | (Merge khi kết thúc Sprint và chạy ổn định)
develop (nhánh tích hợp chính của cả nhóm)
  ^
  |-- feature/feed-ui-TSK-10          (Làm xong -> PR -> Merge vào develop)
  |-- feature/create-post-TSK-13      (Làm xong -> PR -> Merge vào develop)
  |-- feature/backend-feed-TSK-19     (Làm xong -> PR -> Merge vào develop)
  |-- bugfix/fix-comment-crash-BUG-05 (Sửa lỗi -> PR -> Merge vào develop)
```

### Các nhánh chính:
1. `master`: Nhánh production. Tuyệt đối không ai được phép push code trực tiếp lên `master`.
2. `develop`: Nhánh làm việc tích hợp chung của cả 7 người. Mọi tính năng sau khi review xong đều merge vào đây.
3. Nhánh tính năng (`feature/*`): Tạo ra từ `develop` để làm từng task riêng biệt.
4. Nhánh sửa lỗi (`bugfix/*`): Tạo ra để sửa lỗi phát sinh trong quá trình kiểm thử.
5. Nhánh sửa lỗi khẩn cấp (`hotfix/*`): Tạo trực tiếp từ `master` khi xảy ra lỗi nghiêm trọng lúc demo.

---

## 2. QUY CHUẨN ĐẶT TÊN NHÁNH (BRANCH NAMING)

Cú pháp: `<loại-nhánh>/<tên-ngắn-gọn>-<mã-task>`

- **Tính năng mới:** `feature/<tên-tính-năng>-<task-id>`
  - Ví dụ: `feature/feed-ui-TSK-10`, `feature/create-post-TSK-13`, `feature/friends-backend-TSK-21`
- **Sửa lỗi:** `bugfix/<mô-tả-lỗi>-<bug-id>`
  - Ví dụ: `bugfix/empty-post-validation-BUG-01`, `bugfix/like-count-sync-BUG-04`
- **Tối ưu / Tái cấu trúc:** `refactor/<tên-module>`
  - Ví dụ: `refactor/post-viewholder-optimize`
- **Viết kiểm thử:** `test/<tên-module>-TSK-26`
  - Ví dụ: `test/post-repository-unit-test`

---

## 3. QUY TRÌNH COMMIT, PUSH, PULL & TẠO PULL REQUEST (PR)

### 3.1. Cú pháp Commit Message (Conventional Commits)
Mỗi commit phải giải thích rõ ràng thay đổi làm gì, bắt đầu bằng từ khóa phân loại:
- `feat:` Thêm tính năng mới (Feature)
- `fix:` Sửa lỗi (Bugfix)
- `docs:` Thay đổi hoặc bổ sung tài liệu
- `style:` Chỉnh sửa format, khoảng trắng, không ảnh hưởng logic
- `refactor:` Tái cấu trúc code nhưng không thay đổi hành vi
- `test:` Viết thêm hoặc chỉnh sửa Unit Test
- `chore:` Thay đổi cấu hình build, dependencies Gradle

**Ví dụ commit chuẩn:**
```bash
feat(feed): implement PostAdapter and bind item_post layout (TSK-10)
fix(create-post): disable post button when both text and media are empty (BUG-01)
test(repository): add unit tests for PostRepositoryImpl with Mockito (TSK-26)
chore(deps): add Glide image loading library to build.gradle
```

---

### 3.2. Hướng dẫn từng bước làm việc hàng ngày (Step-by-step Flow)

#### Bước 1: Trước khi bắt đầu làm task mới
Luôn luôn lấy code mới nhất từ nhánh `develop` về máy:
```bash
git checkout develop
git pull origin develop
git checkout -b feature/feed-ui-TSK-10
```

#### Bước 2: Trong quá trình code (Thường xuyên commit nhỏ, có ý nghĩa)
```bash
git status
git add .
git commit -m "feat(feed): add layout for item_post and view holder"
```

#### Bước 3: Chuẩn bị đẩy code lên Remote
Trước khi push, bắt buộc phải kéo code mới nhất từ `develop` về nhánh của mình để giải quyết xung đột (conflict) ngay tại máy cá nhân:
```bash
git fetch origin
git merge origin/develop
# Nếu có conflict -> giải quyết trong Android Studio -> commit kết quả merge
git push origin feature/feed-ui-TSK-10
```

#### Bước 4: Tạo Pull Request (PR) & Code Review
1. Truy cập GitHub/GitLab, tạo Pull Request từ nhánh `feature/feed-ui-TSK-10` vào nhánh `develop`.
2. Đặt tiêu đề PR: `[TSK-10] Xây dựng giao diện danh sách bài viết Feed`.
3. Chỉ định Reviewer:
   - Nếu là code FE: Chỉ định **Tùng Dương** (FE Lead).
   - Nếu là code BE: Chỉ định **Ánh Dương** (BE Lead).
4. **Quy tắc Merge:** Phải có ít nhất **01 lượt chấp thuận (Approved)** từ Reviewer thì mới được merge vào `develop`.
5. Sau khi merge thành công, xóa nhánh feature trên remote và local để giữ repository sạch sẽ.

---

## 4. QUY CHUẨN ĐẶT TÊN TOÀN DIỆN (NAMING CONVENTIONS)

Để code của 7 thành viên viết ra trông như được viết bởi một người duy nhất, toàn đội ngũ bắt buộc tuân thủ 100% các quy ước sau:

### 4.1. Quy chuẩn đặt tên Java Code
- **Package:** Toàn bộ chữ thường, không có dấu gạch dưới.  
  *Ví dụ:* `com.facebook.client.features.feed`, `com.facebook.client.data.repository`
- **Class & Interface:** `PascalCase` (chữ cái đầu viết hoa).
  - Activity: Kết thúc bằng `Activity` (ví dụ: `LoginActivity`, `CreatePostActivity`).
  - Fragment: Kết thúc bằng `Fragment` (ví dụ: `FeedFragment`, `ProfileFragment`).
  - ViewModel: Kết thúc bằng `ViewModel` (ví dụ: `FeedViewModel`, `PostViewModel`).
  - Adapter: Kết thúc bằng `Adapter` (ví dụ: `PostAdapter`, `FriendListAdapter`).
  - ViewHolder: Kết thúc bằng `ViewHolder` (ví dụ: `PostViewHolder`).
  - Interface Repository: Bắt đầu bằng chữ `I` (ví dụ: `IPostRepository`, `IFriendRepository`).
  - Implementation Class: Kết thúc bằng `Impl` (ví dụ: `PostRepositoryImpl`).
- **Method (Phương thức):** `camelCase`, bắt đầu bằng động từ thể hiện rõ hành động.  
  *Ví dụ:* `loadFeedPosts()`, `toggleLike()`, `sendFriendRequest()`, `validateInput()`
- **Biến cục bộ & Tham số (Variable & Parameter):** `camelCase`.  
  *Ví dụ:* `postId`, `authorName`, `isLiked`
- **Hằng số (Constant):** `UPPER_SNAKE_CASE` (viết hoa toàn bộ, cách nhau bằng dấu gạch dưới).  
  *Ví dụ:* `DEFAULT_PAGE_SIZE = 20`, `EXTRA_POST_ID = "extra_post_id"`

---

### 4.2. Quy chuẩn đặt tên Tài nguyên XML (Res Folder)

#### A. File Giao diện (Layout Files): `<tiền-tố>_<tên-màn-hình>.xml`
| Tiền tố | Mục đích | Ví dụ minh họa |
| :--- | :--- | :--- |
| `activity_` | Màn hình Activity | `activity_main.xml`, `activity_create_post.xml` |
| `fragment_` | Màn hình con Fragment | `fragment_feed.xml`, `fragment_profile.xml` |
| `item_` | 1 dòng trong RecyclerView | `item_post.xml`, `item_comment.xml`, `item_friend.xml` |
| `bottom_sheet_` | Hộp thoại trượt từ đáy | `bottom_sheet_comments.xml` |
| `dialog_` | Hộp thoại thông báo | `dialog_confirm_share.xml`, `dialog_loading.xml` |
| `view_` | Custom View dùng chung | `view_post_header.xml` |

#### B. Định danh Thành phần Giao diện (View IDs trong XML)
Cú pháp: `@+id/<viết-tắt-loại-view>_<tên-nghiệp-vụ>`

| Loại View | Viết tắt chuẩn | Ví dụ định danh chuẩn | Tuyệt đối KHÔNG đặt |
| :--- | :---: | :--- | :--- |
| **TextView** | `tv_` | `tv_author_name`, `tv_post_time`, `tv_like_count` | `text1`, `tv1`, `name` |
| **ImageView** | `iv_` | `iv_avatar`, `iv_post_image`, `iv_like_icon` | `image`, `imgAvatar` |
| **Button / MaterialButton** | `btn_` | `btn_submit_post`, `btn_confirm_friend`, `btn_cancel`| `button`, `btn1`, `submit` |
| **ImageButton** | `ib_` | `ib_back`, `ib_more_options`, `ib_send_comment` | `btn_back` |
| **EditText** | `et_` | `et_post_content`, `et_comment_input`, `et_search` | `edit1`, `input` |
| **RecyclerView** | `rv_` | `rv_feed_posts`, `rv_comments`, `rv_friend_requests` | `recycler`, `list` |
| **ProgressBar** | `pb_` | `pb_loading`, `pb_upload_image` | `loading`, `progress` |
| **CardView / MaterialCard** | `cv_` | `cv_post_container` | `card1` |
| **Layout container** | `layout_` | `layout_like_action`, `layout_empty_state` | `linear1`, `box` |

#### C. Hình ảnh & Vector Drawables
- Icons: `ic_<tên-icon>.xml` (ví dụ: `ic_like_outline.xml`, `ic_like_filled.xml`, `ic_comment.xml`, `ic_share.xml`).
- Hình nền / Bo góc: `bg_<mô-tả>.xml` (ví dụ: `bg_comment_input.xml`, `bg_button_blue.xml`).
- Trạng thái chọn: `selector_<mô-tả>.xml` (ví dụ: `selector_tab_color.xml`).

#### D. Chuỗi văn bản (Strings Resources)
Cú pháp: `<tên-màn-hình/module>_<mô-tả>`
```xml
<string name="feed_action_like">Thích</string>
<string name="feed_action_comment">Bình luận</string>
<string name="feed_action_share">Chia sẻ</string>
<string name="create_post_hint">Bạn đang nghĩ gì?</string>
<string name="create_post_btn_submit">ĐĂNG</string>
<string name="error_empty_post">Vui lòng nhập nội dung hoặc chọn ảnh để đăng!</string>
```
