# 🔄 04. LUỒNG NGƯỜI DÙNG & TƯƠNG TÁC (USER FLOW)

---

## 1. SƠ ĐỒ ĐIỀU HƯỚNG TỔNG QUAN (APP NAVIGATION MAP)

```mermaid
flowchart TD
    Start([Khởi động App]) --> Splash{Đã đăng nhập?}
    Splash -- Chưa --> Login[Màn hình Đăng nhập / Chọn tài khoản Demo]
    Splash -- Rồi --> Main[MainActivity - Màn hình chính]
    Login --> Main

    Main --> Tab1[Tab 1: Bảng tin Home Feed]
    Main --> Tab2[Tab 2: Bạn bè Friends Tab]
    Main --> Tab3[Tab 3: Thông báo Notifications]
    Main --> Tab4[Tab 4: Trang cá nhân Profile]

    Tab1 --> CreatePost[Màn hình Tạo bài viết CreatePostActivity]
    Tab1 --> CommentSheet[Hộp thoại Bình luận CommentBottomSheet]
    Tab1 --> ShareDialog[Hộp thoại Xác nhận Chia sẻ ShareDialog]

    Tab2 --> SearchFriend[Màn hình Tìm kiếm bạn bè SearchFriendsActivity]
    Tab2 --> OtherProfile[Xem Trang cá nhân người khác]

    Tab3 --> TargetPost[Mở chi tiết bài viết tương ứng]
    Tab3 --> TargetProfile[Mở trang cá nhân gửi lời mời]

    Tab4 --> TabPosts[Tab con: Tất cả bài viết]
    Tab4 --> TabPhotos[Tab con: Bộ sưu tập ảnh lưới 3 cột]
```

---

## 2. LUỒNG NGHIỆP VỤ CHI TIẾT (DETAILED USER JOURNEYS)

### 2.1. Luồng Tạo bài viết mới (Create Post Flow)
Tuân thủ ràng buộc nghiêm ngặt từ `Mobile App.pdf`: Nút đăng chỉ sáng khi có chữ HOẶC có ảnh.

```mermaid
sequenceDiagram
    autonumber
    actor User as Người dùng
    participant UI as CreatePostActivity
    participant VM as CreatePostViewModel
    participant Repo as PostRepository (Retrofit)
    participant Server as FastAPI Server & SQLite

    User->>UI: Bấm vào khung "Bạn đang nghĩ gì?" trên Feed
    UI->>User: Mở màn hình tạo bài viết (Nút Đăng ở trạng thái Disabled)
    
    alt Người dùng gõ chữ
        User->>UI: Gõ nội dung văn bản
        UI->>UI: Kiểm tra (Text không rỗng) -> Enable nút "ĐĂNG"
    end

    alt Người dùng thêm ảnh/video
        User->>UI: Bấm nút "Thêm ảnh/video"
        UI->>User: Mở Photo Picker của hệ thống
        User->>UI: Chọn 1 bức ảnh từ thư viện
        UI->>UI: Hiển thị Preview ảnh kèm nút [X] xóa -> Enable nút "ĐĂNG"
    end

    User->>UI: Bấm nút "ĐĂNG"
    UI->>UI: Hiển thị Dialog Loading (Đang đăng tải...)
    UI->>VM: submitPost(content, imageUri)
    
    opt Nếu có ảnh đính kèm
        VM->>Server: POST /api/upload (Tải file ảnh lên máy chủ)
        Server-->>VM: Trả về URL ảnh tĩnh (/static/uploads/...)
    end

    VM->>Repo: createPost(authorId, content, mediaUrl, isPublic=true)
    Repo->>Server: POST /api/posts (Ghi bản ghi vào bảng posts trong SQLite)
    Server-->>Repo: 201 Created (Post DTO)
    Repo-->>VM: Result.Success
    VM-->>UI: Cập nhật LiveData trạng thái thành công
    UI->>User: Đóng màn hình, quay về Feed, bài viết mới xuất hiện trên đỉnh Feed
```

---

### 2.2. Luồng Tương tác Bài viết (Like, Comment, Share Flow)

```mermaid
sequenceDiagram
    autonumber
    actor User as Người dùng
    participant Feed as FeedFragment / PostAdapter
    participant CmtUI as CommentBottomSheet
    participant VM as FeedViewModel / CommentViewModel
    participant Server as FastAPI Server & SQLite

    Note over User, Feed: 1. THAO TÁC LIKE / THẢ TIM
    User->>Feed: Bấm nút "Thích" trên thẻ bài viết
    Feed->>Feed: Cập nhật giao diện tức thì (Optimistic UI Update: Đổi màu xanh, Tăng số like +1)
    Feed->>VM: toggleLike(postId, currentUserId)
    VM->>Server: POST /api/posts/{id}/like (Cập nhật bảng post_likes trong SQLite)
    Server-->>VM: Xác nhận thành công (isLiked=true/false, likesCount)

    Note over User, CmtUI: 2. THAO TÁC BÌNH LUẬN
    User->>Feed: Bấm nút "Bình luận"
    Feed->>CmtUI: Mở CommentBottomSheetDialog(postId)
    CmtUI->>VM: loadComments(postId)
    VM->>Server: GET /api/posts/{id}/comments (Truy vấn bảng comments trong SQLite)
    Server-->>VM: Trả về danh sách Comment JSON
    VM-->>CmtUI: Hiển thị danh sách RecyclerView
    User->>CmtUI: Nhập text bình luận và bấm nút "Gửi"
    CmtUI->>VM: addComment(postId, userId, textContent)
    VM->>Server: POST /api/posts/{id}/comments (Lưu bản ghi text-only vào SQLite)
    Server-->>VM: Trả về Comment vừa tạo
    VM-->>CmtUI: Thêm dòng bình luận vào cuối danh sách, cuộn xuống dưới cùng

    Note over User, Feed: 3. THAO TÁC CHIA SẺ
    User->>Feed: Bấm nút "Chia sẻ"
    Feed->>User: Hiển thị AlertDialog "Chia sẻ bài viết này lên dòng thời gian của bạn?"
    User->>Feed: Bấm "Chia sẻ ngay"
    Feed->>VM: sharePost(originalPostId, currentUserId)
    VM->>Server: POST /api/posts/{id}/share (Tạo bài viết mới trong SQLite)
    Server-->>VM: Thành công 201 Created
    Feed->>User: Hiển thị Toast "Đã chia sẻ thành công!"
```

---

### 2.3. Luồng Quản lý Bạn bè (Friend Tab Flow)

```mermaid
flowchart TD
    StartFriend[Vào Tab Bạn bè] --> ChooseAction{Chọn hành động}

    ChooseAction -- Lời mời kết bạn --> ViewReq[Xem danh sách Friend Requests]
    ViewReq --> ActConfirm{Thao tác}
    ActConfirm -- Bấm Chấp nhận --> ConfirmFriend[Gọi API acceptFriend -> Tạo quan hệ bạn bè -> Xóa khỏi lời mời -> Tăng số bạn bè]
    ActConfirm -- Bấm Xóa --> DeleteFriend[Gọi API rejectFriend -> Xóa khỏi danh sách lời mời]

    ChooseAction -- Bạn bè hiện tại --> ViewMyFriends[Xem danh sách tất cả bạn bè]
    ViewMyFriends --> ClickFriend[Bấm vào bạn bè -> Điều hướng sang Profile của người đó]

    ChooseAction -- Tìm kiếm bạn bè --> InputSearch[Gõ tên vào thanh Search]
    InputSearch --> Debounce[Debounce 300ms chống spam request]
    Debounce --> QueryDB[Truy vấn danh sách User có tên chứa từ khóa]
    QueryDB --> ShowResult[Hiển thị danh sách kết quả tìm kiếm]
    ShowResult --> SendReq[Bấm 'Thêm bạn bè' -> Gửi Friend Request]
```

---

### 2.4. Luồng Trang cá nhân (Profile Flow)

```mermaid
flowchart TD
    ProfileStart[Vào Tab Trang cá nhân] --> LoadInfo[Tải Thông tin: Cover Photo, Avatar, Tên người dùng]
    LoadInfo --> Tabs{Chọn Tab con}
    Tabs -- Tab 'Tất cả bài viết' --> LoadUserPosts[Truy vấn danh sách bài viết do UserId này đăng]
    LoadUserPosts --> RenderPosts[Hiển thị danh sách bài viết theo dòng thời gian giống Feed]
    Tabs -- Tab 'Ảnh & Video' --> LoadUserMedia[Truy vấn danh sách các Post có mediaUrl != null của UserId]
    LoadUserMedia --> RenderGrid[Hiển thị danh sách ảnh dạng lưới 3 cột - Grid 3 columns]
    RenderGrid --> ClickMedia[Bấm vào 1 ảnh trong lưới -> Mở xem ảnh toàn màn hình hoặc bài viết gốc]
```
