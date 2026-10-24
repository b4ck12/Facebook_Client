package com.facebook.client.features.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.facebook.client.R;
import com.facebook.client.core.base.BaseFragment;
import com.facebook.client.core.utils.SessionManager;
import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.Post;
import com.facebook.client.data.repository.IProfileRepository;
import com.facebook.client.data.repository.MockProfileRepository;
import com.facebook.client.data.repository.RepositoryCallback;
import com.facebook.client.databinding.FragmentProfileBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Trang cá nhân (Profile Screen) - Ghép PhotoGrid vào Tab Photos ([D6-TQH]).
 */
public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    // Adapter quản lý hiển thị danh sách ảnh dạng lưới 3 cột
    private PhotoGridAdapter photoGridAdapter;
    private IProfileRepository profileRepository;
    private String currentUserId;

    @Override
    protected FragmentProfileBinding inflateBinding(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initViews() {
        initRepositoryAndUser();

        // Cài đặt lưới ảnh 3 cột
        setupPhotoGridRecyclerView();

        // Cài đặt TabLayout chuyển tab Photos và Posts
        setupTabLayout();

        // Tải dữ liệu ảnh mẫu đổ vào PhotoGridAdapter
        loadUserPhotos();

        // Khung chờ giao diện Header và bài viết của Phúc Đức (D5-PD / D9-PD)
        setupOtherMemberPlaceholders();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tự động tải lại ảnh nếu người dùng đổi tài khoản đăng nhập ở Top Bar
        if (getContext() != null) {
            String activeUserId = SessionManager.getInstance(getContext()).getCurrentUserId();
            if (activeUserId != null && !activeUserId.equals(currentUserId)) {
                currentUserId = activeUserId;
                loadUserPhotos();
            }
        }
    }

    private void initRepositoryAndUser() {
        profileRepository = new MockProfileRepository();
        if (getContext() != null) {
            currentUserId = SessionManager.getInstance(getContext()).getCurrentUserId();
        }
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            currentUserId = MockDataGenerator.getInstance().getCurrentUserId();
        }
    }

    /**
     * Cài đặt RecyclerView hiển thị ảnh dạng lưới 3 cột:
     * - GridLayoutManager chia 3 cột đều nhau.
     * - Tắt cuộn nội bộ để nhường quyền cuộn mượt cho NestedScrollView.
     * - Gắn bộ lắng nghe sự kiện click vào ảnh để mở xem chi tiết.
     */
    private void setupPhotoGridRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);
        getBinding().rvProfilePhotos.setLayoutManager(gridLayoutManager);
        getBinding().rvProfilePhotos.setHasFixedSize(true);
        getBinding().rvProfilePhotos.setNestedScrollingEnabled(false); // Tránh xung đột cuộn với NestedScrollView

        // Bắt sự kiện click vào ô ảnh để mở hộp thoại phóng to
        photoGridAdapter = new PhotoGridAdapter((photoUrl, position) -> showPhotoPreviewDialog(photoUrl, position));
        getBinding().rvProfilePhotos.setAdapter(photoGridAdapter);
    }

    /**
     * Cài đặt thanh TabLayout với 2 tab: Ảnh (Photos) và Bài viết (Posts).
     */
    private void setupTabLayout() {
        TabLayout tabLayout = getBinding().tabLayoutProfile;
        tabLayout.removeAllTabs();

        TabLayout.Tab tabPhotos = tabLayout.newTab().setText(R.string.profile_tab_photos);
        TabLayout.Tab tabPosts = tabLayout.newTab().setText(R.string.profile_tab_posts);

        tabLayout.addTab(tabPhotos, true);  // Mặc định chọn Tab Photos để kiểm thử lưới ảnh
        tabLayout.addTab(tabPosts, false);

        // Lắng nghe sự kiện chuyển đổi tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switchTabContent(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Chuyển đổi hiển thị giữa Tab Lưới Ảnh và Tab Bài viết.
     */
    private void switchTabContent(int tabPosition) {
        if (tabPosition == 0) {
            // Tab Photos: Hiển thị lưới ảnh nếu có dữ liệu, hiển thị layout rỗng nếu không có ảnh
            boolean hasPhotos = photoGridAdapter != null && photoGridAdapter.getItemCount() > 0;
            getBinding().rvProfilePhotos.setVisibility(hasPhotos ? View.VISIBLE : View.GONE);
            getBinding().layoutEmptyPhotos.setVisibility(hasPhotos ? View.GONE : View.VISIBLE);
            getBinding().rvProfilePosts.setVisibility(View.GONE);
        } else {
            // Tab Posts: Ẩn lưới ảnh, hiển thị danh sách bài viết
            getBinding().rvProfilePhotos.setVisibility(View.GONE);
            getBinding().layoutEmptyPhotos.setVisibility(View.GONE);
            getBinding().rvProfilePosts.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Tải danh sách ảnh mẫu từ Repository và nạp vào PhotoGridAdapter.
     */
    private void loadUserPhotos() {
        profileRepository.getUserPhotos(currentUserId, new RepositoryCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> photoPosts) {
                if (!isAdded() || getView() == null) return;

                // Lọc các bài viết có ảnh đính kèm (mediaUrl)
                List<String> photoUrls = new ArrayList<>();
                if (photoPosts != null) {
                    for (Post post : photoPosts) {
                        if (post.getMediaUrl() != null && !post.getMediaUrl().trim().isEmpty()) {
                            photoUrls.add(post.getMediaUrl().trim());
                        }
                    }
                }

                // Cập nhật danh sách ảnh vào Adapter
                photoGridAdapter.setPhotos(photoUrls);

                // Cập nhật lại giao diện nếu đang đứng ở Tab Photos
                if (getBinding().tabLayoutProfile.getSelectedTabPosition() == 0) {
                    switchTabContent(0);
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded() || getView() == null) return;
                showToast("Không thể tải kho ảnh: " + message);
            }
        });
    }

    /**
     * Hiển thị hộp thoại xem ảnh phóng to khi người dùng nhấn vào một ô ảnh trong lưới.
     */
    private void showPhotoPreviewDialog(@NonNull String photoUrl, int position) {
        Context context = getContext();
        if (context == null) return;

        // Tạo ImageView xem ảnh phóng to
        ImageView previewImageView = new ImageView(context);
        int previewHeight = (int) (320 * getResources().getDisplayMetrics().density);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, previewHeight);
        previewImageView.setLayoutParams(layoutParams);
        previewImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        previewImageView.setAdjustViewBounds(true);

        // Tải ảnh bằng Glide
        Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.bg_avatar_placeholder)
                .error(R.drawable.bg_avatar_placeholder)
                .into(previewImageView);

        // Hiển thị Dialog xem ảnh Material 3
        new MaterialAlertDialogBuilder(context)
                .setTitle("Ảnh số " + (position + 1))
                .setView(previewImageView)
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Khung giao diện tạm thời chờ tích hợp của Phúc Đức (Header và PostAdapter - D5/D9).
     */
    private void setupOtherMemberPlaceholders() {
        if (getBinding().tvProfileName != null) {
            getBinding().tvProfileName.setText("Alex Johnson");
        }
        if (getBinding().btnEditProfile != null) {
            getBinding().btnEditProfile.setOnClickListener(v -> showToast("Chỉnh sửa trang cá nhân"));
        }
    }
}
