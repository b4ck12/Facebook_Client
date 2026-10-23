package com.facebook.client.features.profile;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.client.R;
import com.facebook.client.databinding.ItemPhotoGridBinding;

/**
 * ViewHolder quản lý giao diện hiển thị cho một ô ảnh trong Lưới ảnh Trang cá nhân (Photo Grid Item - [D5-TQH]).
 * Tuân thủ chuẩn Facebook Client Design System:
 * 1. Nhận ViewBinding từ layout res/layout/item_photo_grid.xml.
 * 2. Tải và cắt cúp trung tâm ảnh tỷ lệ 1:1 bằng Glide với DiskCacheStrategy.ALL.
 * 3. Hỗ trợ hiển thị màu placeholder xám nhẹ trong lúc tải ảnh từ mạng.
 * 4. Bắt sự kiện click vào bức ảnh để kích hoạt callback phóng to / xem chi tiết ảnh.
 */
public class PhotoGridViewHolder extends RecyclerView.ViewHolder {

    private final ItemPhotoGridBinding binding;

    public PhotoGridViewHolder(@NonNull ItemPhotoGridBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /**
     * Nạp URL hình ảnh vào ImageView và cài đặt sự kiện click.
     *
     * @param photoUrl URL ảnh cần tải (từ Internet hoặc thư mục static/uploads/).
     * @param position Vị trí của ô ảnh trong lưới 3 cột.
     * @param listener Bộ lắng nghe sự kiện click khi người dùng chạm vào ảnh.
     */
    public void bind(@NonNull String photoUrl, int position, @Nullable PhotoGridAdapter.OnPhotoClickListener listener) {
        Context context = binding.getRoot().getContext();

        // 1. TẢI ẢNH VỚI GLIDE (CẮT CÚP CENTER CROP VÀ LƯU CACHE)
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            Glide.with(context)
                    .load(photoUrl.trim())
                    .placeholder(R.color.fb_surface_variant)
                    .error(R.drawable.bg_avatar_placeholder)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivGridPhoto);
        } else {
            Glide.with(context).clear(binding.ivGridPhoto);
            binding.ivGridPhoto.setImageResource(R.drawable.bg_avatar_placeholder);
        }

        // 2. GẮN SỰ KIỆN CLICK KHI CHẠM VÀO BỨC ẢNH
        binding.layoutPhotoGridRoot.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoClick(photoUrl, position);
            }
        });
    }
}
