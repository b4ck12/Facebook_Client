# 🎨 DESIGN.md - HỆ THỐNG THIẾT KẾ FACEBOOK CLIENT

> Tài liệu này là bản đối chiếu nhanh của hệ thống thiết kế giao diện (Design System).  
> Chi tiết toàn bộ mã màu, thông số typography, component specs và trạng thái UI được định nghĩa đầy đủ tại:  
> 👉 [**docs/03_DESIGN_SYSTEM.md**](./03_DESIGN_SYSTEM.md)

---

## TỔNG HỢP NHANH QUY CHUẨN THIẾT KẾ:
1. **Primary Color:** `#1877F2` (Facebook Blue)
2. **Background Color:** `#F0F2F5` (Xám nền) & Card Surface: `#FFFFFF` (Trắng)
3. **Typography:** Roboto (12sp - 22sp)
4. **Spacing Grid:** 4dp, 8dp, 12dp, 16dp, 24dp
5. **Bo góc:** 8dp (Card/Button), 16dp (Comment bubble), Circle (Avatar)
6. **Thành phần:**
   - Home Feed + Create Post Quickbox
   - CreatePostActivity (Chỉ có ô nhập text + nút đính kèm 1 ảnh/video duy nhất)
   - PostCard (Header, Content text/img, Like/Comment/Share buttons)
   - CommentBottomSheet (Text-only, no stickers, no nested reply)
   - Friends Tab (Lời mời kết bạn + Bạn bè hiện tại + Tìm kiếm)
   - Profile Screen (Ảnh bìa 200dp, Avatar tròn 120dp, Tab All Posts, Tab Photos Grid 3 cột)
   - Notifications Tab (Danh sách thông báo Like, Comment, Friend)
