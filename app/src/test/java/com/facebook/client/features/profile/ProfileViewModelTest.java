package com.facebook.client.features.profile;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.facebook.client.data.model.Post;
import com.facebook.client.data.model.User;
import com.facebook.client.data.repository.IProfileRepository;
import com.facebook.client.data.repository.RepositoryCallback;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Bộ Unit Test kiểm tra logic tầng ViewModel của ProfileViewModel ([D10-TQH]).
 * <p>
 * Kiểm thử các kịch bản:
 * 1. Tải kho ảnh thành công: Trích xuất đúng các URL ảnh (mediaUrl) và nạp vào photoUrlsLiveData cho lưới 3 cột.
 * 2. Lọc bỏ các bài viết không có ảnh: Chỉ lấy các bài viết có mediaUrl hợp lệ.
 * 3. Tải kho ảnh thất bại: Bắt đúng thông điệp lỗi và tắt cờ Loading.
 * 4. Tải thông tin cá nhân: Cập nhật đúng đối tượng User lên userProfileLiveData.
 */
public class ProfileViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private IProfileRepository profileRepository;

    @Mock
    private Observer<List<String>> photosObserver;

    @Mock
    private Observer<Boolean> loadingObserver;

    @Mock
    private Observer<String> errorObserver;

    @Captor
    private ArgumentCaptor<RepositoryCallback<List<Post>>> photosCallbackCaptor;

    @Captor
    private ArgumentCaptor<RepositoryCallback<User>> userCallbackCaptor;

    private ProfileViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new ProfileViewModel(profileRepository);
        viewModel.getPhotoUrls().observeForever(photosObserver);
        viewModel.getIsLoading().observeForever(loadingObserver);
        viewModel.getErrorMessage().observeForever(errorObserver);
    }

    /**
     * Kịch bản 1: Tải kho ảnh thành công và lọc chính xác các bài viết có mediaUrl.
     */
    @Test
    public void loadUserPhotos_success_extractsMediaUrlsAndStopsLoading() {
        String userId = "user_101";

        viewModel.loadUserPhotos(userId);

        verify(profileRepository).getUserPhotos(eq(userId), photosCallbackCaptor.capture());

        List<Post> mockPosts = new ArrayList<>();
        // Bài viết 1: Có ảnh
        mockPosts.add(new Post(
                "p1", userId, "Huy", null, "Ảnh đẹp",
                "https://images.unsplash.com/photo-1", true, 5, 2, false, 1000
        ));
        // Bài viết 2: Chỉ có chữ (mediaUrl = null) -> Phải bị lọc bỏ
        mockPosts.add(new Post(
                "p2", userId, "Huy", null, "Chỉ có chữ",
                null, true, 2, 0, false, 2000
        ));
        // Bài viết 3: Có ảnh
        mockPosts.add(new Post(
                "p3", userId, "Huy", null, "Ảnh thứ 2",
                "https://images.unsplash.com/photo-2", true, 10, 1, false, 3000
        ));

        photosCallbackCaptor.getValue().onSuccess(mockPosts);

        // Kiểm tra LiveData chỉ chứa đúng 2 link ảnh
        List<String> photoUrls = viewModel.getPhotoUrls().getValue();
        assertNotNull(photoUrls);
        assertEquals(2, photoUrls.size());
        assertEquals("https://images.unsplash.com/photo-1", photoUrls.get(0));
        assertEquals("https://images.unsplash.com/photo-2", photoUrls.get(1));

        // Loading phải tắt
        assertFalse(viewModel.getIsLoading().getValue());
    }

    /**
     * Kịch bản 2: Bắt lỗi khi tải kho ảnh thất bại.
     */
    @Test
    public void loadUserPhotos_error_emitsErrorMessageAndStopsLoading() {
        String userId = "user_101";

        viewModel.loadUserPhotos(userId);

        verify(profileRepository).getUserPhotos(eq(userId), photosCallbackCaptor.capture());

        String errorMsg = "Không tìm thấy người dùng có ID 'user_101'";
        photosCallbackCaptor.getValue().onError(errorMsg);

        assertEquals(errorMsg, viewModel.getErrorMessage().getValue());
        assertFalse(viewModel.getIsLoading().getValue());
    }

    /**
     * Kịch bản 3: Tải thông tin cá nhân của người dùng thành công.
     */
    @Test
    public void loadUserProfile_success_updatesUserProfileLiveData() {
        String userId = "user_101";

        viewModel.loadUserProfile(userId);

        verify(profileRepository).getUserProfile(eq(userId), userCallbackCaptor.capture());

        User mockUser = new User(
                userId, "huy@gmail.com", "Tạ Quang Huy",
                "https://avatar.com/1.jpg", "https://cover.com/1.jpg", 1000
        );
        userCallbackCaptor.getValue().onSuccess(mockUser);

        User actualUser = viewModel.getUserProfile().getValue();
        assertNotNull(actualUser);
        assertEquals("Tạ Quang Huy", actualUser.getDisplayName());
        assertEquals("https://avatar.com/1.jpg", actualUser.getAvatarUrl());
    }
}
