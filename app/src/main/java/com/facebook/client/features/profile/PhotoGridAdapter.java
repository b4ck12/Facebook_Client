package com.facebook.client.features.profile;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.client.databinding.ItemPhotoGridBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RecyclerView Adapter quản lý danh sách hình ảnh hiển thị dạng Lưới 3 cột (Profile Photo Grid - [D5-TQH]).
 * Phục vụ Tab "Ảnh & Video" trên Trang cá nhân (ProfileFragment).
 * 
 * <p>Các tính năng chính:
 * - Hiển thị ảnh vuông tỷ lệ 1:1 trong GridLayoutManager(context, 3).
 * - Tự động tải và lưu cache ảnh mượt mà với Glide.
 * - Điều phối sự kiện click mở xem ảnh phóng to qua OnPhotoClickListener.
 * - Hỗ trợ nạp mới (setPhotos) hoặc tải thêm ảnh phân trang (addPhotos).
 */
public class PhotoGridAdapter extends RecyclerView.Adapter<PhotoGridViewHolder> {

    /**
     * Interface giao tiếp sự kiện khi người dùng nhấn vào một bức ảnh trong lưới.
     */
    public interface OnPhotoClickListener {
        /**
         * Được gọi khi người dùng chạm vào một bức ảnh cụ thể.
         *
         * @param photoUrl URL của bức ảnh được chọn.
         * @param position Vị trí của bức ảnh trong danh sách lưới.
         */
        void onPhotoClick(@NonNull String photoUrl, int position);
    }

    private final List<String> photoUrls = new ArrayList<>();
    @Nullable
    private final OnPhotoClickListener listener;

    /**
     * Khởi tạo Adapter với bộ lắng nghe sự kiện click.
     *
     * @param listener Bộ xử lý sự kiện khi nhấn vào ảnh.
     */
    public PhotoGridAdapter(@Nullable OnPhotoClickListener listener) {
        this.listener = listener;
    }

    /**
     * Khởi tạo Adapter với danh sách ảnh ban đầu.
     *
     * @param initialPhotos Danh sách các đường dẫn ảnh ban đầu.
     * @param listener      Bộ xử lý sự kiện khi nhấn vào ảnh.
     */
    public PhotoGridAdapter(@Nullable List<String> initialPhotos, @Nullable OnPhotoClickListener listener) {
        if (initialPhotos != null) {
            this.photoUrls.addAll(initialPhotos);
        }
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Thổi phồng giao diện res/layout/item_photo_grid.xml thành ViewBinding
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPhotoGridBinding binding = ItemPhotoGridBinding.inflate(inflater, parent, false);
        return new PhotoGridViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoGridViewHolder holder, int position) {
        // Lấy URL ảnh tại vị trí position và chuyển giao cho ViewHolder vẽ lên màn hình
        String photoUrl = photoUrls.get(position);
        holder.bind(photoUrl, position, listener);
    }

    @Override
    public int getItemCount() {
        return photoUrls.size();
    }

    /**
     * Thay thế toàn bộ danh sách ảnh hiện tại bằng danh sách ảnh mới và làm mới giao diện.
     *
     * @param newPhotos Danh sách URL ảnh mới tải từ Repository / API.
     */
    public void setPhotos(@Nullable List<String> newPhotos) {
        photoUrls.clear();
        if (newPhotos != null) {
            photoUrls.addAll(newPhotos);
        }
        notifyDataSetChanged();
    }

    /**
     * Bổ sung thêm ảnh vào cuối danh sách (hỗ trợ phân trang / tải thêm) mà không làm chớp cả danh sách.
     *
     * @param morePhotos Danh sách các ảnh mới cần thêm vào.
     */
    public void addPhotos(@Nullable List<String> morePhotos) {
        if (morePhotos != null && !morePhotos.isEmpty()) {
            int startPosition = photoUrls.size();
            photoUrls.addAll(morePhotos);
            notifyItemRangeInserted(startPosition, morePhotos.size());
        }
    }

    /**
     * Lấy danh sách ảnh hiện tại (bản sao chỉ đọc).
     */
    @NonNull
    public List<String> getPhotos() {
        return Collections.unmodifiableList(photoUrls);
    }

    /**
     * Lấy URL ảnh tại một vị trí xác định.
     *
     * @param position Vị trí cần lấy.
     * @return URL ảnh hoặc null nếu vượt ngoài phạm vi danh sách.
     */
    @Nullable
    public String getPhotoAt(int position) {
        if (position >= 0 && position < photoUrls.size()) {
            return photoUrls.get(position);
        }
        return null;
    }
}
