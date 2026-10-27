package com.facebook.client.features.feed;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.facebook.client.core.common.Resource;
import com.facebook.client.data.model.Post;
import com.facebook.client.data.repository.IPostRepository;
import com.facebook.client.data.repository.RepositoryCallback;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Bộ Unit Test kiểm tra logic tầng ViewModel của CreatePostViewModel ([D10-TQH]).
 * <p>
 * Áp dụng InstantTaskExecutorRule để LiveData thực thi ngay trên luồng kiểm thử đồng bộ.
 * <p>
 * Kiểm thử các kịch bản:
 * 1. Chặn bài viết rỗng (cả text và media đều rỗng): Trả về Resource.ERROR mà KHÔNG gọi Repository.
 * 2. Đăng bài chỉ có văn bản (Text-only): Gọi Repository.createPost thành công và trả về Resource.SUCCESS.
 * 3. Đăng bài chỉ có ảnh (Media-only): Chấp nhận ảnh hợp lệ và tạo bài viết thành công.
 * 4. Xử lý lỗi máy chủ: Khi Repository trả về onError, ViewModel phát tín hiệu Resource.ERROR.
 * 5. Luồng xử lý trạng thái: Kiểm tra trạng thái chuyển từ LOADING sang SUCCESS/ERROR.
 */
public class CreatePostViewModelTest {

    // Rule bắt buộc khi test ViewModel có chứa LiveData: ép thực thi tác vụ nền về đồng bộ ngay lập tức
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private IPostRepository postRepository;

    @Mock
    private Observer<Resource<Post>> statusObserver;

    @Captor
    private ArgumentCaptor<RepositoryCallback<Post>> postCallbackCaptor;

    private CreatePostViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new CreatePostViewModel(postRepository);
        viewModel.getCreatePostStatus().observeForever(statusObserver);
    }

    /**
     * Kịch bản 1: Chặn bài viết rỗng (Validation Test).
     * Cả nội dung văn bản và ảnh đều rỗng -> ViewModel phải chặn ngay, báo lỗi và không gọi xuống Repository.
     */
    @Test
    public void submitPost_emptyContentAndNoImage_returnsErrorWithoutCallingRepository() {
        // Thực thi gửi bài viết không có chữ lẫn ảnh
        viewModel.submitPost("user_101", "   ", "");

        // Kiểm tra kết quả
        Resource<Post> currentStatus = viewModel.getCreatePostStatus().getValue();
        assertNotNull(currentStatus);
        assertEquals(Resource.Status.ERROR, currentStatus.status);
        assertEquals("Vui lòng nhập nội dung hoặc chọn ảnh!", currentStatus.message);

        // Khẳng định chắc chắn rằng Repository.createPost() không bao giờ được gọi
        verify(postRepository, never()).createPost(anyString(), anyString(), any(), anyBoolean(), any());
    }

    /**
     * Kịch bản 2: Bài viết rỗng khi cả 2 tham số đều null.
     */
    @Test
    public void submitPost_nullContentAndNullImage_returnsErrorWithoutCallingRepository() {
        viewModel.submitPost("user_101", null, null);

        Resource<Post> currentStatus = viewModel.getCreatePostStatus().getValue();
        assertNotNull(currentStatus);
        assertEquals(Resource.Status.ERROR, currentStatus.status);
        assertEquals("Vui lòng nhập nội dung hoặc chọn ảnh!", currentStatus.message);

        verify(postRepository, never()).createPost(anyString(), anyString(), any(), anyBoolean(), any());
    }

    /**
     * Kịch bản 3: Đăng bài viết chỉ có văn bản (Text-only) thành công.
     */
    @Test
    public void submitPost_validTextOnly_callsCreatePostAndReturnsSuccess() {
        String authorId = "user_101";
        String content = "Chào cả nhóm Facebook Client!";

        // Thực thi
        viewModel.submitPost(authorId, content, null);

        // Bắt callback truyền vào Repository
        verify(postRepository).createPost(
                eq(authorId),
                eq(content),
                eq(null),
                eq(true),
                postCallbackCaptor.capture()
        );

        // Giả lập Repository phản hồi thành công
        Post mockCreatedPost = new Post(
                "post_new_1",
                authorId,
                "Tạ Quang Huy",
                null,
                content,
                null,
                true,
                0,
                0,
                false,
                System.currentTimeMillis()
        );
        postCallbackCaptor.getValue().onSuccess(mockCreatedPost);

        // Kiểm tra trạng thái cuối cùng của LiveData
        Resource<Post> finalStatus = viewModel.getCreatePostStatus().getValue();
        assertNotNull(finalStatus);
        assertEquals(Resource.Status.SUCCESS, finalStatus.status);
        assertEquals(mockCreatedPost, finalStatus.data);
    }

    /**
     * Kịch bản 4: Đăng bài viết chỉ có ảnh (Media-only) thành công.
     */
    @Test
    public void submitPost_validImageOnly_callsCreatePostAndReturnsSuccess() {
        String authorId = "user_101";
        String imageUri = "https://images.unsplash.com/photo-1579202673506-ca3ce28943ef";

        viewModel.submitPost(authorId, "", imageUri);

        verify(postRepository).createPost(
                eq(authorId),
                eq(""),
                eq(imageUri),
                eq(true),
                postCallbackCaptor.capture()
        );

        Post mockCreatedPost = new Post(
                "post_new_2",
                authorId,
                "Tạ Quang Huy",
                null,
                "",
                imageUri,
                true,
                0,
                0,
                false,
                System.currentTimeMillis()
        );
        postCallbackCaptor.getValue().onSuccess(mockCreatedPost);

        Resource<Post> finalStatus = viewModel.getCreatePostStatus().getValue();
        assertNotNull(finalStatus);
        assertEquals(Resource.Status.SUCCESS, finalStatus.status);
        assertEquals(mockCreatedPost, finalStatus.data);
    }

    /**
     * Kịch bản 5: Xử lý lỗi khi máy chủ FastAPI gặp sự cố mạng (HTTP 500 hoặc Timeout).
     */
    @Test
    public void submitPost_serverError_returnsErrorResource() {
        String authorId = "user_101";
        String content = "Kiểm tra lỗi server";

        viewModel.submitPost(authorId, content, null);

        verify(postRepository).createPost(
                eq(authorId),
                eq(content),
                eq(null),
                eq(true),
                postCallbackCaptor.capture()
        );

        // Giả lập lỗi trả về từ máy chủ
        String errorMessage = "Lỗi kết nối cơ sở dữ liệu SQLite";
        postCallbackCaptor.getValue().onError(errorMessage);

        Resource<Post> finalStatus = viewModel.getCreatePostStatus().getValue();
        assertNotNull(finalStatus);
        assertEquals(Resource.Status.ERROR, finalStatus.status);
        assertEquals(errorMessage, finalStatus.message);
        assertNull(finalStatus.data);
    }
}
