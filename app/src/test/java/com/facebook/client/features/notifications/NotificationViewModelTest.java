package com.facebook.client.features.notifications;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.facebook.client.data.model.Notification;
import com.facebook.client.data.repository.INotificationRepository;
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
 * Bộ Unit Test kiểm tra logic tầng ViewModel của NotificationViewModel ([D10-TQH]).
 * <p>
 * Áp dụng InstantTaskExecutorRule và Mockito để kiểm thử phân hệ Thông báo.
 * <p>
 * Kiểm thử các kịch bản:
 * 1. Tải danh sách thông báo thành công: LiveData nhận đúng mảng kết quả, trạng thái Loading tắt.
 * 2. Tải thông báo thất bại: Bắt đúng thông điệp lỗi trong errorMessage, Loading tắt.
 * 3. Đánh dấu đã đọc tức thì (Optimistic UI markAsRead): Trạng thái isRead đổi thành true ngay lập tức
 *    và lệnh được gửi xuống Repository.
 */
public class NotificationViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private INotificationRepository notificationRepository;

    @Mock
    private Observer<List<Notification>> notificationsObserver;

    @Mock
    private Observer<Boolean> loadingObserver;

    @Mock
    private Observer<String> errorObserver;

    @Captor
    private ArgumentCaptor<RepositoryCallback<List<Notification>>> callbackCaptor;

    private NotificationViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new NotificationViewModel(notificationRepository);
        viewModel.getNotifications().observeForever(notificationsObserver);
        viewModel.getIsLoading().observeForever(loadingObserver);
        viewModel.getErrorMessage().observeForever(errorObserver);
    }

    /**
     * Kịch bản 1: Tải thông báo thành công từ máy chủ.
     */
    @Test
    public void loadNotifications_success_updatesLiveDataAndStopsLoading() {
        String userId = "user_101";

        // Thực thi
        viewModel.loadNotifications(userId);

        // Bắt callback
        verify(notificationRepository).getNotifications(eq(userId), callbackCaptor.capture());

        // Giả lập danh sách thông báo trả về từ máy chủ
        List<Notification> mockList = new ArrayList<>();
        mockList.add(new Notification(
                "notif_1",
                userId,
                "user_2",
                "Trần Thị Mai",
                null,
                Notification.TYPE_LIKE,
                "post_10",
                "đã thích bài viết của bạn",
                false,
                System.currentTimeMillis()
        ));
        mockList.add(new Notification(
                "notif_2",
                userId,
                "user_3",
                "Nguyễn Văn An",
                null,
                Notification.TYPE_COMMENT,
                "post_10",
                "đã bình luận về bài viết của bạn",
                false,
                System.currentTimeMillis() - 1000
        ));

        // Phản hồi thành công
        callbackCaptor.getValue().onSuccess(mockList);

        // Kiểm tra LiveData nhận đủ 2 phần tử
        List<Notification> actualList = viewModel.getNotifications().getValue();
        assertNotNull(actualList);
        assertEquals(2, actualList.size());
        assertEquals("notif_1", actualList.get(0).getId());

        // Kiểm tra trạng thái Loading đã được tắt
        Boolean isLoading = viewModel.getIsLoading().getValue();
        assertNotNull(isLoading);
        assertFalse(isLoading);
    }

    /**
     * Kịch bản 2: Tải thông báo thất bại do mất kết nối mạng.
     */
    @Test
    public void loadNotifications_error_emitsErrorMessageAndStopsLoading() {
        String userId = "user_101";

        viewModel.loadNotifications(userId);

        verify(notificationRepository).getNotifications(eq(userId), callbackCaptor.capture());

        // Giả lập lỗi trả về
        String networkError = "Không thể kết nối đến máy chủ FastAPI";
        callbackCaptor.getValue().onError(networkError);

        // Kiểm tra LiveData bắt đúng thông điệp lỗi
        String actualError = viewModel.getErrorMessage().getValue();
        assertEquals(networkError, actualError);

        // Loading phải tắt
        Boolean isLoading = viewModel.getIsLoading().getValue();
        assertNotNull(isLoading);
        assertFalse(isLoading);
    }

    /**
     * Kịch bản 3: Đánh dấu đã đọc ngay lập tức (Optimistic UI markAsRead).
     */
    @Test
    public void markAsRead_optimisticUpdate_marksReadImmediately() {
        String userId = "user_101";
        viewModel.loadNotifications(userId);

        verify(notificationRepository).getNotifications(eq(userId), callbackCaptor.capture());

        List<Notification> mockList = new ArrayList<>();
        mockList.add(new Notification(
                "notif_target",
                userId,
                "user_2",
                "Alex Johnson",
                null,
                Notification.TYPE_FRIEND_REQUEST,
                null,
                "đã gửi lời mời kết bạn",
                false, // Ban đầu là CHƯA ĐỌC
                System.currentTimeMillis()
        ));
        callbackCaptor.getValue().onSuccess(mockList);

        // Khẳng định ban đầu isRead = false
        assertFalse(viewModel.getNotifications().getValue().get(0).isRead());

        // Thực thi đánh dấu đã đọc
        viewModel.markAsRead("notif_target");

        // Kiểm tra kết quả: mục thông báo phải chuyển sang isRead = true ngay tức thì
        assertTrue(viewModel.getNotifications().getValue().get(0).isRead());

        // Khẳng định lệnh đã được chuyển giao xuống Repository
        verify(notificationRepository).markAsRead(eq("notif_target"), eq(null));
    }
}
