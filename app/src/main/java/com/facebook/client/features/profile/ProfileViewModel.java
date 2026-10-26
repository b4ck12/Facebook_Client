package com.facebook.client.features.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.facebook.client.data.model.Post;
import com.facebook.client.data.model.User;
import com.facebook.client.data.repository.IProfileRepository;
import com.facebook.client.data.repository.ProfileRepositoryImpl;
import com.facebook.client.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel quản lý dữ liệu và logic nghiệp vụ cho màn hình Trang cá nhân ([D9-TQH]).
 * <p>
 * Vai trò chính:
 * 1. Làm cầu nối giữa ProfileFragment và ProfileRepositoryImpl để lấy kho ảnh lưới 3 cột qua endpoint GET /api/users/{id}/photos.
 * 2. Cung cấp LiveData để Fragment tự động cập nhật danh sách ảnh khi người dùng đổi tài khoản demo hoặc khi dữ liệu mới được tải về.
 * 3. Hỗ trợ đầy đủ phương thức cho Phúc Đức ([D9-PD]) tích hợp Header thông tin cá nhân và Tab bài viết.
 * 4. Hỗ trợ Dependency Injection cho phép truyền Repository giả lập khi viết Unit Test ([D10-TQH]).
 */
public class ProfileViewModel extends ViewModel {

    // Repository cung cấp dữ liệu profile từ server FastAPI
    private final IProfileRepository repository;

    // LiveData nắm giữ danh sách đường dẫn URL ảnh (phục vụ PhotoGridAdapter lưới 3 cột)
    private final MutableLiveData<List<String>> photoUrlsLiveData = new MutableLiveData<>(new ArrayList<>());

    // LiveData nắm giữ thông tin cá nhân (ảnh bìa, avatar, tên)
    private final MutableLiveData<User> userProfileLiveData = new MutableLiveData<>();

    // LiveData nắm giữ danh sách bài viết cá nhân trên Tab Posts
    private final MutableLiveData<List<Post>> userPostsLiveData = new MutableLiveData<>(new ArrayList<>());

    // LiveData thông báo trạng thái đang tải dữ liệu
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);

    // LiveData thông báo lỗi mạng hoặc thông báo lỗi từ server
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    /**
     * Khởi tạo ViewModel mặc định sử dụng ProfileRepositoryImpl kết nối FastAPI.
     */
    public ProfileViewModel() {
        this(new ProfileRepositoryImpl());
    }

    /**
     * Khởi tạo ViewModel cho phép truyền Repository từ bên ngoài vào (phục vụ Unit Test [D10-TQH]).
     */
    public ProfileViewModel(@NonNull IProfileRepository repository) {
        this.repository = repository;
    }

    /** Lấy LiveData danh sách URL ảnh để PhotoGridAdapter quan sát (Observe) */
    @NonNull
    public LiveData<List<String>> getPhotoUrls() {
        return photoUrlsLiveData;
    }

    /** Lấy LiveData thông tin người dùng */
    @NonNull
    public LiveData<User> getUserProfile() {
        return userProfileLiveData;
    }

    /** Lấy LiveData danh sách bài viết cá nhân */
    @NonNull
    public LiveData<List<Post>> getUserPosts() {
        return userPostsLiveData;
    }

    /** Lấy LiveData trạng thái đang tải */
    @NonNull
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    /** Lấy LiveData thông báo lỗi */
    @NonNull
    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    /**
     * Tải kho ảnh của người dùng chỉ định từ endpoint GET /api/users/{id}/photos.
     *
     * @param userId Mã định danh người dùng cần lấy kho ảnh.
     */
    public void loadUserPhotos(String userId) {
        // Bật trạng thái Loading: setValue kích hoạt LiveData báo cho giao diện
        isLoadingLiveData.setValue(true);

        repository.getUserPhotos(userId, new RepositoryCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> photoPosts) {
                // Tắt trạng thái Loading
                isLoadingLiveData.setValue(false);

                // Lọc lấy danh sách các URL ảnh đính kèm (mediaUrl)
                List<String> urls = new ArrayList<>();
                if (photoPosts != null) {
                    for (Post p : photoPosts) {
                        if (p.getMediaUrl() != null && !p.getMediaUrl().trim().isEmpty()) {
                            urls.add(p.getMediaUrl().trim());
                        }
                    }
                }

                // setValue: LiveData tự động bắn danh sách URL ảnh sang Fragment đang observe để cập nhật PhotoGridAdapter
                photoUrlsLiveData.setValue(urls);
            }

            @Override
            public void onError(String message) {
                // Tắt trạng thái Loading
                isLoadingLiveData.setValue(false);

                // setValue: LiveData tự động bắn thông điệp lỗi sang Fragment
                errorMessageLiveData.setValue(message != null ? message : "Không thể tải kho ảnh");
            }
        });
    }

    /**
     * Tải thông tin cá nhân của người dùng từ endpoint GET /api/users/{id}.
     *
     * @param userId Mã định danh người dùng.
     */
    public void loadUserProfile(String userId) {
        repository.getUserProfile(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                userProfileLiveData.setValue(result);
            }

            @Override
            public void onError(String message) {
                errorMessageLiveData.setValue(message != null ? message : "Không thể tải thông tin người dùng");
            }
        });
    }

    /**
     * Tải danh sách bài viết cá nhân của người dùng từ endpoint GET /api/users/{id}/posts.
     *
     * @param userId Mã định danh người dùng.
     */
    public void loadUserPosts(String userId) {
        repository.getUserPosts(userId, new RepositoryCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                userPostsLiveData.setValue(result != null ? result : new ArrayList<>());
            }

            @Override
            public void onError(String message) {
                errorMessageLiveData.setValue(message != null ? message : "Không thể tải bài viết cá nhân");
            }
        });
    }
}
