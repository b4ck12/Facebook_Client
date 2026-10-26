package com.facebook.client.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import com.facebook.client.core.common.Resource;
import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.Post;
import com.facebook.client.data.repository.IPostRepository;
import com.facebook.client.data.repository.PostRepositoryImpl;
import com.facebook.client.data.repository.RepositoryCallback;
import com.facebook.client.features.feed.CreatePostViewModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Bộ Unit Test kiểm thử tầng Core Network ({@link Resource}, {@link ApiClient},
 * {@link ApiResponseHandler}), {@link PostRepositoryImpl} và {@link CreatePostViewModel} ([D8-TD]).
 */
public class CoreNetworkTest {

  @Rule
  public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

  @Before
  public void setUp() {
    MockDataGenerator.getInstance().resetData();
  }

  @Test
  public void testResourceStates() {
    Resource<String> loading = Resource.loading("cached");
    assertTrue(loading.isLoading());
    assertFalse(loading.isSuccess());
    assertFalse(loading.isError());
    assertEquals(Resource.Status.LOADING, loading.getStatus());
    assertEquals("cached", loading.getData());

    Resource<String> success = Resource.success("ok_data");
    assertTrue(success.isSuccess());
    assertEquals(Resource.Status.SUCCESS, success.status);
    assertEquals("ok_data", success.data);
    assertNull(success.message);

    Resource<String> error = Resource.error("Lỗi mạng");
    assertTrue(error.isError());
    assertEquals(Resource.Status.ERROR, error.status);
    assertEquals("Lỗi mạng", error.message);
    assertNull(error.data);
  }

  @Test
  public void testApiClientConfigurationAndNormalizeStaticUrl() {
    assertEquals("http://10.0.2.2:8000/api/", ApiClient.BASE_URL);
    assertNotNull(ApiClient.getClient());
    assertNotNull(ApiClient.getApiService());

    assertNull(ApiClient.normalizeStaticUrl(null));
    assertNull(ApiClient.normalizeStaticUrl("   "));
    assertNull(ApiClient.normalizeStaticUrl("null"));
    assertEquals(
        "http://10.0.2.2:8000/static/uploads/img_1.jpg",
        ApiClient.normalizeStaticUrl("/static/uploads/img_1.jpg"));
    assertEquals(
        "http://10.0.2.2:8000/static/uploads/img_2.jpg",
        ApiClient.normalizeStaticUrl("http://10.0.2.2:8000/static/uploads/img_2.jpg"));
  }

  @Test
  public void testApiResponseHandler() {
    Response<String> okResp = Response.success("hello");
    Resource<String> okRes = ApiResponseHandler.handleResponse(okResp);
    assertTrue(okRes.isSuccess());
    assertEquals("hello", okRes.data);

    ResponseBody errBody =
        ResponseBody.create(
            "{\"detail\":\"Định dạng tệp không hỗ trợ!\"}",
            MediaType.parse("application/json"));
    Response<String> badReqResp = Response.error(400, errBody);
    Resource<String> errRes = ApiResponseHandler.handleResponse(badReqResp);
    assertTrue(errRes.isError());
    assertEquals("Định dạng tệp không hỗ trợ!", errRes.message);

    assertTrue(
        ApiResponseHandler.parseErrorMessage(new ConnectException("Connection refused"))
            .contains("10.0.2.2:8000"));
    assertTrue(
        ApiResponseHandler.parseErrorMessage(new SocketTimeoutException("timeout"))
            .contains("Timeout"));
    assertTrue(
        ApiResponseHandler.parseErrorMessage(new UnknownHostException("no host"))
            .contains("kết nối mạng"));
    assertTrue(
        ApiResponseHandler.parseErrorMessage(new IOException("broken pipe"))
            .contains("đường truyền"));
  }

  @Test
  public void testCreatePostViewModel_emptyInput_returnsError() {
    IPostRepository mockRepo = mock(IPostRepository.class);
    CreatePostViewModel vm = new CreatePostViewModel(mockRepo);

    vm.submitPost("user_101", "   ", null);

    Resource<Post> status = vm.getCreatePostStatus().getValue();
    assertNotNull(status);
    assertEquals(Resource.Status.ERROR, status.status);
    assertEquals("Vui lòng nhập nội dung hoặc chọn ảnh!", status.message);
  }

  @Test
  public void testCreatePostViewModel_textOnly_success() {
    IPostRepository mockRepo = mock(IPostRepository.class);
    CreatePostViewModel vm = new CreatePostViewModel(mockRepo);

    Post fakePost =
        new Post(
            "post_999",
            "user_101",
            "Nguyễn Văn An",
            null,
            "Xin chào FastAPI",
            null,
            true,
            0,
            0,
            false,
            System.currentTimeMillis());

    doAnswer(
            invocation -> {
              RepositoryCallback<Post> callback = invocation.getArgument(4);
              callback.onSuccess(fakePost);
              return null;
            })
        .when(mockRepo)
        .createPost(eq("user_101"), eq("Xin chào FastAPI"), eq(null), eq(true), any());

    vm.submitPost("user_101", "Xin chào FastAPI", null);

    Resource<Post> status = vm.getCreatePostStatus().getValue();
    assertNotNull(status);
    assertEquals(Resource.Status.SUCCESS, status.status);
    assertNotNull(status.data);
    assertEquals("post_999", status.data.getId());
    assertEquals("Xin chào FastAPI", status.data.getContent());
  }

  @Test
  public void testCreatePostViewModel_withImageFile_uploadsThenCreatesPost() throws Exception {
    IPostRepository mockRepo = mock(IPostRepository.class);
    CreatePostViewModel vm = new CreatePostViewModel(mockRepo);

    File tempImg = File.createTempFile("test_upload_", ".jpg");
    tempImg.deleteOnExit();
    try (FileOutputStream fos = new FileOutputStream(tempImg)) {
      fos.write("fake_jpeg_bytes".getBytes(StandardCharsets.UTF_8));
    }

    String uploadedServerUrl = "http://10.0.2.2:8000/static/uploads/img_abc123.jpg";
    Post fakePost =
        new Post(
            "post_1000",
            "user_101",
            "Nguyễn Văn An",
            null,
            "Bài viết có ảnh",
            uploadedServerUrl,
            true,
            0,
            0,
            false,
            System.currentTimeMillis());

    doAnswer(
            invocation -> {
              RepositoryCallback<String> cb = invocation.getArgument(1);
              cb.onSuccess(uploadedServerUrl);
              return null;
            })
        .when(mockRepo)
        .uploadImage(eq(tempImg), any());

    doAnswer(
            invocation -> {
              RepositoryCallback<Post> cb = invocation.getArgument(4);
              cb.onSuccess(fakePost);
              return null;
            })
        .when(mockRepo)
        .createPost(eq("user_101"), eq("Bài viết có ảnh"), eq(uploadedServerUrl), eq(true), any());

    vm.submitPost("user_101", "Bài viết có ảnh", tempImg, tempImg.toURI().toString());

    verify(mockRepo).uploadImage(eq(tempImg), any());
    verify(mockRepo)
        .createPost(eq("user_101"), eq("Bài viết có ảnh"), eq(uploadedServerUrl), eq(true), any());

    Resource<Post> status = vm.getCreatePostStatus().getValue();
    assertNotNull(status);
    assertEquals(Resource.Status.SUCCESS, status.status);
    assertEquals(uploadedServerUrl, status.data.getMediaUrl());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostRepositoryImpl_uploadImageAndCreatePostWithMockApiService() throws Exception {
    ApiService mockApi = mock(ApiService.class);
    Call<Map<String, Object>> mockUploadCall = mock(Call.class);
    Call<Post> mockCreateCall = mock(Call.class);

    when(mockApi.uploadImage(any(MultipartBody.Part.class))).thenReturn(mockUploadCall);
    when(mockApi.createPost(any())).thenReturn(mockCreateCall);

    doAnswer(
            invocation -> {
              Callback<Map<String, Object>> cb = invocation.getArgument(0);
              Map<String, Object> body = new HashMap<>();
              body.put("url", "/static/uploads/img_test.jpg");
              cb.onResponse(mockUploadCall, Response.success(201, body));
              return null;
            })
        .when(mockUploadCall)
        .enqueue(any());

    doAnswer(
            invocation -> {
              Callback<Post> cb = invocation.getArgument(0);
              Post serverPost =
                  new Post(
                      "post_srv_1",
                      "user_101",
                      "Nguyễn Văn An",
                      "/static/uploads/avatar.jpg",
                      "Nội dung từ Retrofit",
                      "/static/uploads/img_test.jpg",
                      true,
                      0,
                      0,
                      false,
                      1728512345678L);
              cb.onResponse(mockCreateCall, Response.success(201, serverPost));
              return null;
            })
        .when(mockCreateCall)
        .enqueue(any());

    PostRepositoryImpl repo = new PostRepositoryImpl(mockApi);

    File tempImg = File.createTempFile("repo_upload_", ".jpg");
    tempImg.deleteOnExit();
    try (FileOutputStream fos = new FileOutputStream(tempImg)) {
      fos.write("fake_image_content".getBytes(StandardCharsets.UTF_8));
    }

    AtomicReference<String> uploadedUrlRef = new AtomicReference<>();
    repo.uploadImage(
        tempImg,
        new RepositoryCallback<String>() {
          @Override
          public void onSuccess(String data) {
            uploadedUrlRef.set(data);
          }

          @Override
          public void onError(String message) {}
        });

    assertEquals(
        "http://10.0.2.2:8000/static/uploads/img_test.jpg", uploadedUrlRef.get());

    AtomicReference<Post> createdPostRef = new AtomicReference<>();
    repo.createPost(
        "user_101",
        "Nội dung từ Retrofit",
        uploadedUrlRef.get(),
        true,
        new RepositoryCallback<Post>() {
          @Override
          public void onSuccess(Post data) {
            createdPostRef.set(data);
          }

          @Override
          public void onError(String message) {}
        });

    assertNotNull(createdPostRef.get());
    assertEquals("post_srv_1", createdPostRef.get().getId());
    assertEquals(
        "http://10.0.2.2:8000/static/uploads/img_test.jpg",
        createdPostRef.get().getMediaUrl());
    assertEquals(
        "http://10.0.2.2:8000/static/uploads/avatar.jpg",
        createdPostRef.get().getAuthorAvatar());
  }

  @Test
  public void testApiResponseHandler_edgeCases() {
    assertTrue(ApiResponseHandler.parseHttpError(null).contains("Không nhận được phản hồi"));
    assertTrue(ApiResponseHandler.parseErrorMessage(null).contains("không xác định"));

    ResponseBody plainTextBody =
        ResponseBody.create("Internal Server Error", MediaType.parse("text/plain"));
    assertTrue(
        ApiResponseHandler.parseHttpError(Response.error(404, plainTextBody)).contains("404"));
    assertTrue(
        ApiResponseHandler.parseHttpError(Response.error(413, plainTextBody)).contains("413"));
    assertTrue(
        ApiResponseHandler.parseHttpError(Response.error(422, plainTextBody)).contains("422"));
    assertTrue(
        ApiResponseHandler.parseHttpError(Response.error(500, plainTextBody)).contains("500"));

    Resource<Object> failRes =
        ApiResponseHandler.handleFailure(new ConnectException("Connection refused"));
    assertTrue(failRes.isError());
    assertTrue(failRes.message.contains("10.0.2.2:8000"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPostRepositoryImpl_offlineFallbackAndInvalidFile() throws Exception {
    ApiService mockApi = mock(ApiService.class);
    Call<Map<String, Object>> mockUploadCall = mock(Call.class);
    Call<Post> mockCreateCall = mock(Call.class);

    when(mockApi.uploadImage(any(MultipartBody.Part.class))).thenReturn(mockUploadCall);
    when(mockApi.createPost(any())).thenReturn(mockCreateCall);

    doAnswer(
            invocation -> {
              Callback<Map<String, Object>> cb = invocation.getArgument(0);
              cb.onFailure(mockUploadCall, new ConnectException("Server down"));
              return null;
            })
        .when(mockUploadCall)
        .enqueue(any());

    doAnswer(
            invocation -> {
              Callback<Post> cb = invocation.getArgument(0);
              cb.onFailure(mockCreateCall, new ConnectException("Server down"));
              return null;
            })
        .when(mockCreateCall)
        .enqueue(any());

    PostRepositoryImpl repo = new PostRepositoryImpl(mockApi);

    // 1. Null file should return onError
    AtomicReference<String> errorMsgRef = new AtomicReference<>();
    repo.uploadImage(
        null,
        new RepositoryCallback<String>() {
          @Override
          public void onSuccess(String data) {}

          @Override
          public void onError(String message) {
            errorMsgRef.set(message);
          }
        });
    assertNotNull(errorMsgRef.get());

    // 2. Offline fallback on uploadImage returns local URI
    File tempImg = File.createTempFile("offline_upload_", ".jpg");
    tempImg.deleteOnExit();
    try (FileOutputStream fos = new FileOutputStream(tempImg)) {
      fos.write("offline_image".getBytes(StandardCharsets.UTF_8));
    }

    AtomicReference<String> fallbackUriRef = new AtomicReference<>();
    repo.uploadImage(
        tempImg,
        new RepositoryCallback<String>() {
          @Override
          public void onSuccess(String data) {
            fallbackUriRef.set(data);
          }

          @Override
          public void onError(String message) {}
        });
    assertNotNull(fallbackUriRef.get());
    assertTrue(fallbackUriRef.get().startsWith("file:"));

    // 3. Offline fallback on createPost creates post in MockPostRepository
    AtomicReference<Post> fallbackPostRef = new AtomicReference<>();
    repo.createPost(
        "user_101",
        "Bài viết khi mất kết nối server",
        fallbackUriRef.get(),
        true,
        new RepositoryCallback<Post>() {
          @Override
          public void onSuccess(Post data) {
            fallbackPostRef.set(data);
          }

          @Override
          public void onError(String message) {}
        });
    assertNotNull(fallbackPostRef.get());
    assertEquals("Bài viết khi mất kết nối server", fallbackPostRef.get().getContent());
  }

  @Test
  public void testCreatePostViewModel_uploadError_propagatesErrorResource() throws Exception {
    IPostRepository mockRepo = mock(IPostRepository.class);
    CreatePostViewModel vm = new CreatePostViewModel(mockRepo);

    File tempImg = File.createTempFile("err_upload_", ".jpg");
    tempImg.deleteOnExit();
    try (FileOutputStream fos = new FileOutputStream(tempImg)) {
      fos.write("fake_data".getBytes(StandardCharsets.UTF_8));
    }

    doAnswer(
            invocation -> {
              RepositoryCallback<String> cb = invocation.getArgument(1);
              cb.onError("Kích thước tệp ảnh vượt quá giới hạn cho phép (HTTP 413).");
              return null;
            })
        .when(mockRepo)
        .uploadImage(eq(tempImg), any());

    vm.submitPost("user_101", "Ảnh quá lớn", tempImg, tempImg.toURI().toString());

    Resource<Post> status = vm.getCreatePostStatus().getValue();
    assertNotNull(status);
    assertTrue(status.isError());
    assertTrue(status.message.contains("413"));
  }
}
