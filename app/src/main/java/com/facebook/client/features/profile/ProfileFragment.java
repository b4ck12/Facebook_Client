package com.facebook.client.features.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.facebook.client.R;
import com.facebook.client.core.base.BaseFragment;
import com.facebook.client.core.utils.FriendsProfileUi;
import com.facebook.client.data.model.Post;
import com.facebook.client.data.model.User;
import com.facebook.client.data.repository.IProfileRepository;
import com.facebook.client.data.repository.IPostRepository;
import com.facebook.client.data.repository.ProfileRepositoryImpl;
import com.facebook.client.data.repository.PostRepositoryImpl;
import com.facebook.client.data.repository.RepositoryCallback;
import com.facebook.client.databinding.FragmentProfileBinding;
import com.facebook.client.features.comment.CommentBottomSheetDialog;
import com.facebook.client.features.feed.PostAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.appcompat.app.AlertDialog;
import com.facebook.client.core.network.ApiClient;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.ContentResolver;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {
    private static final String ARG_USER_ID = "viewed_user_id";
    // Tích hợp ProfileRepositoryImpl gọi API thật GET /api/users/{id}/photos từ FastAPI SQLite ([D9-TQH])
    private final IProfileRepository profileRepository = new ProfileRepositoryImpl();
    private final IPostRepository postRepository = new PostRepositoryImpl();
    private PhotoGridAdapter photoAdapter;
    private PostAdapter postAdapter;
    private String viewedUserId;
    private AlertDialog changeNameDialog;
    private Call<User> changeNameCall;
    private Call<?> avatarCall;
    private String avatarPickerUserId;

    private final ActivityResultLauncher<String> avatarPicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        String userId = avatarPickerUserId;
                        avatarPickerUserId = null;

                        // Người dùng đóng trình chọn ảnh.
                        if (uri == null) return;

                        if (!isAdded() || getView() == null) return;
                        if (!canChangeAvatar(userId)) return;

                        uploadAvatar(uri, userId);
                    }
            );
    private int selectedTab;
    private int generation;
    private final Set<String> pendingLikeIds = new HashSet<>();
    private int pendingLoads;
    private String loadError;

    private final FragmentManager.FragmentLifecycleCallbacks commentLifecycle =
            new FragmentManager.FragmentLifecycleCallbacks() {
                @Override public void onFragmentDetached(@NonNull FragmentManager manager,
                                                         @NonNull Fragment fragment) {
                    if (fragment instanceof CommentBottomSheetDialog && isResumed() && getView() != null) {
                        loadProfile();
                    }
                }
            };

    public static ProfileFragment newInstance(String userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_USER_ID, userId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        if (state != null) {
            avatarPickerUserId = state.getString("avatar_picker_user_id");
        }
        selectedTab = state == null ? 0 : state.getInt("selected_tab", 0);
        getChildFragmentManager().registerFragmentLifecycleCallbacks(commentLifecycle, false);
    }

    @Override protected FragmentProfileBinding inflateBinding(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override protected void initViews() {
        photoAdapter = new PhotoGridAdapter((url, position) -> showPhoto(url));
        getBinding().rvProfilePhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        getBinding().rvProfilePhotos.setAdapter(photoAdapter);
        postAdapter = new PostAdapter(new PostAdapter.OnPostActionListener() {
            @Override public void onLikeClick(@NonNull Post post, int position) { like(post); }
            @Override public void onCommentClick(@NonNull Post post) {
                if (getChildFragmentManager().findFragmentByTag("profile_comments") == null) {
                    CommentBottomSheetDialog.newInstance(post.getId())
                            .show(getChildFragmentManager(), "profile_comments");
                }
            }
            @Override public void onShareClick(@NonNull Post post) { share(post); }
        });
        getBinding().rvProfilePosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvProfilePosts.setAdapter(postAdapter);
        getBinding().rvProfilePosts.setNestedScrollingEnabled(false);
        getBinding().rvProfilePhotos.setNestedScrollingEnabled(false);
        getBinding().btnProfileRetry.setOnClickListener(view -> loadProfile());
        TabLayout tabs = getBinding().tabLayoutProfile;
        tabs.removeAllTabs();
        tabs.addTab(tabs.newTab().setText(R.string.fp_posts), selectedTab == 0);
        tabs.addTab(tabs.newTab().setText(R.string.fp_photos), selectedTab == 1);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                render();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        getBinding().btnChangeProfile.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.fp_change_profile)
                    .setPositiveButton(R.string.fp_change_name,
                            (dialog, which) -> showChangeNameDialog())
                    .setNeutralButton(R.string.fp_change_picture,
                            (dialog, which) -> chooseAvatar())
                    .show();
        });
    }

    @Override public void onResume() {
        super.onResume();
        String activeUser = FriendsProfileUi.activeUserId(requireContext());
        String requested = getArguments() == null ? null : getArguments().getString(ARG_USER_ID);
        viewedUserId = requested == null || requested.trim().isEmpty() ? activeUser : requested;
        boolean isOwnProfile =
                activeUser != null && activeUser.equals(viewedUserId);

        getBinding().btnChangeProfile.setVisibility(
                isOwnProfile ? View.VISIBLE : View.GONE
        );
        getBinding().btnChangeProfile.setEnabled(avatarCall == null);
        getBinding().btnChangeProfile.setText(
                avatarCall == null
                        ? R.string.fp_change_profile
                        : R.string.fp_avatar_uploading
        );
        loadProfile();

    }

    private boolean active(int token) {
        return token == generation && isAdded() && getView() != null;
    }

    private void loadProfile() {
        int token = ++generation;
        pendingLoads = 3;
        loadError = null;
        postAdapter.setPosts(null);
        photoAdapter.setPhotos(null);
        getBinding().tvProfileName.setText("");
        Glide.with(this).clear(getBinding().ivProfileAvatar);
        Glide.with(this).clear(getBinding().ivProfileCover);
        getBinding().ivProfileAvatar.setImageResource(R.drawable.ic_profile);
        getBinding().ivProfileCover.setImageResource(R.color.fb_surface_variant);
        render();
        profileRepository.getUserProfile(viewedUserId, new RepositoryCallback<User>() {
            @Override public void onSuccess(User user) {
                if (!active(token)) return;
                getBinding().tvProfileName.setText(user.getDisplayName());
                Glide.with(ProfileFragment.this).load(user.getAvatarUrl())
                        .placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile)
                        .circleCrop().into(getBinding().ivProfileAvatar);
                Glide.with(ProfileFragment.this).load(user.getCoverUrl())
                        .placeholder(R.color.fb_surface_variant).error(R.color.fb_surface_variant)
                        .centerCrop().into(getBinding().ivProfileCover);
                completePart(null);
            }
            @Override public void onError(String message) {
                if (active(token)) completePart(message);
            }
        });
        profileRepository.getUserPosts(viewedUserId, new RepositoryCallback<List<Post>>() {
            @Override public void onSuccess(List<Post> posts) {
                if (!active(token)) return;
                postAdapter.setPosts(posts);
                completePart(null);
            }
            @Override public void onError(String message) {
                if (active(token)) completePart(message);
            }
        });
        profileRepository.getUserPhotos(viewedUserId, new RepositoryCallback<List<Post>>() {
            @Override public void onSuccess(List<Post> posts) {
                if (!active(token)) return;
                List<String> urls = new ArrayList<>();
                if (posts != null) {
                    for (Post post : posts) {
                        if (post.getMediaUrl() != null && !post.getMediaUrl().trim().isEmpty()) {
                            // Chuẩn hóa URL trước khi nạp vào PhotoAdapter
                            String fullUrl = ApiClient.normalizeStaticUrl(post.getMediaUrl());
                            if (fullUrl != null) {
                                urls.add(fullUrl);
                            }
                        }
                    }
                }
                photoAdapter.setPhotos(urls);
                completePart(null);
            }
            @Override public void onError(String message) {
                if (active(token)) completePart(message);
            }
        });
    }

    private void completePart(String error) {
        if (error != null) loadError = error;
        pendingLoads = Math.max(0, pendingLoads - 1);
        render();
    }

    private void render() {
        boolean loading = pendingLoads > 0;
        boolean failed = !loading && loadError != null;
        boolean photos = selectedTab == 1;
        int count = photos ? photoAdapter.getItemCount() : postAdapter.getItemCount();
        getBinding().pbProfileLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        getBinding().rvProfilePosts.setVisibility(!loading && !failed && !photos && count > 0
                ? View.VISIBLE : View.GONE);
        getBinding().rvProfilePhotos.setVisibility(!loading && !failed && photos && count > 0
                ? View.VISIBLE : View.GONE);
        getBinding().tvProfileEmpty.setText(photos ? R.string.fp_empty_photos : R.string.fp_empty_posts);
        getBinding().tvProfileEmpty.setVisibility(!loading && !failed && count == 0
                ? View.VISIBLE : View.GONE);
        getBinding().layoutProfileError.setVisibility(failed ? View.VISIBLE : View.GONE);
        getBinding().tvProfileError.setText(loadError);
    }

    private void like(Post post) {
        String postId = post.getId();

        if (postId == null || postId.trim().isEmpty()) return;
        if (!pendingLikeIds.add(postId)) return;

        int token = generation;
        boolean previousLiked = post.isLiked();
        int previousCount = Math.max(0, post.getLikesCount());

        String currentUserId =
                FriendsProfileUi.activeUserId(requireContext());

        postRepository.toggleLike(
                postId,
                currentUserId,
                new RepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean liked) {
                        pendingLikeIds.remove(postId);

                        if (!active(token)) return;

                        if (liked == null) {
                            showToast("Unable to update like.");
                            return;
                        }

                        int updatedCount = previousCount;

                        if (liked != previousLiked) {
                            updatedCount = liked
                                    ? previousCount + 1
                                    : Math.max(0, previousCount - 1);
                        }

                        post.setLiked(liked);
                        post.setLikesCount(updatedCount);

                        List<Post> displayed = postAdapter.getPosts();

                        for (int i = 0; i < displayed.size(); i++) {
                            if (displayed.get(i) == post) {
                                postAdapter.notifyItemChanged(i);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onError(String message) {
                        pendingLikeIds.remove(postId);

                        if (!active(token)) return;

                        showToast("Unable to update like. Please try again.");
                    }
                }
        );
    }

    private void share(Post post) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.fp_share_title).setMessage(R.string.fp_share_message)
                .setNegativeButton(R.string.fp_cancel, null)
                .setPositiveButton(R.string.fp_share, (dialog, which) -> {
                    if (!isAdded() || getView() == null) return;
                    int token = generation;
                    postRepository.sharePost(post.getId(),
                            FriendsProfileUi.activeUserId(requireContext()), "",
                            new RepositoryCallback<Post>() {
                                @Override public void onSuccess(Post shared) {
                                    if (!active(token)) return;
                                    showToast(getString(R.string.fp_share_success));
                                    loadProfile();
                                }
                                @Override public void onError(String message) {
                                    if (active(token)) showToast(message);
                                }
                            });
                }).show();
    }

    private void showPhoto(String url) {
        // 1. Chuẩn hóa đường dẫn tĩnh (/static/uploads/...) thành URL đầy đủ http://10.0.2.2:8000/...
        String normalizedUrl = ApiClient.normalizeStaticUrl(url);
        if (normalizedUrl == null) return;

        // 2. Tạo ImageView hiển thị ảnh xem phóng to
        ImageView image = new ImageView(requireContext());
        int previewHeight = (int) (320 * getResources().getDisplayMetrics().density);
        image.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, previewHeight));
        image.setAdjustViewBounds(true); // Co giãn theo tỷ lệ ảnh
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // 3. Tải ảnh bằng Glide với URL đã chuẩn hóa
        Glide.with(this)
                .load(normalizedUrl)
                .placeholder(R.drawable.bg_avatar_placeholder)
                .error(R.drawable.bg_avatar_placeholder)
                .into(image);

        // 4. Hiển thị Dialog
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.fp_photo)
                .setView(image)
                .setPositiveButton(R.string.fp_close, null)
                .show();
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        state.putInt("selected_tab", selectedTab);
        state.putString("avatar_picker_user_id", avatarPickerUserId);
        super.onSaveInstanceState(state);
    }

    @Override public void onDestroyView() {
        if (avatarCall != null) {
            avatarCall.cancel();
            avatarCall = null;
        }
        if (changeNameCall != null) {
            changeNameCall.cancel();
            changeNameCall = null;
        }

        if (changeNameDialog != null) {
            changeNameDialog.dismiss();
            changeNameDialog = null;
        }
        generation++;
        getBinding().rvProfilePosts.setAdapter(null);
        getBinding().rvProfilePhotos.setAdapter(null);
        super.onDestroyView();
    }

    @Override public void onDestroy() {
        getChildFragmentManager().unregisterFragmentLifecycleCallbacks(commentLifecycle);
        super.onDestroy();
    }
    private void showChangeNameDialog() {
        String currentUserId =
                FriendsProfileUi.activeUserId(requireContext());

        // Chỉ cho sửa trang cá nhân của tài khoản đang đăng nhập.
        if (currentUserId == null
                || !currentUserId.equals(viewedUserId)) {
            return;
        }

        if (changeNameDialog != null && changeNameDialog.isShowing()) {
            return;
        }

        EditText input = new EditText(requireContext());
        input.setHint(R.string.fp_name_hint);
        input.setSingleLine(true);
        input.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                        | InputType.TYPE_TEXT_FLAG_CAP_WORDS
        );

        input.setText(getBinding().tvProfileName.getText());
        input.setSelection(input.length());

        FrameLayout container = new FrameLayout(requireContext());
        int padding = (int) (
                24 * getResources().getDisplayMetrics().density
        );
        container.setPadding(padding, padding / 2, padding, 0);
        container.addView(input, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.fp_change_name)
                .setView(container)
                .setNegativeButton(R.string.fp_name_cancel, null)
                .setPositiveButton(R.string.fp_name_save, null)
                .create();

        changeNameDialog = dialog;

        dialog.setOnDismissListener(ignored -> {
            if (changeNameDialog == dialog) {
                changeNameDialog = null;
            }
        });

        dialog.show();

        // Gắn sau show() để nhập sai thì hộp thoại không tự đóng.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    String newName = input.getText().toString().trim();

                    if (newName.isEmpty()) {
                        input.setError(getString(R.string.fp_name_required));
                        input.requestFocus();
                        return;
                    }

                    saveDisplayName(currentUserId, newName, input, dialog);
                });
    }
    private void saveDisplayName(
            String userId,
            String newName,
            EditText input,
            AlertDialog dialog
    ) {
        if (changeNameCall != null) return;

        if (!userId.equals(
                FriendsProfileUi.activeUserId(requireContext()))
                || !userId.equals(viewedUserId)) {
            dialog.dismiss();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("displayName", newName);

        input.setEnabled(false);
        dialog.setCancelable(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setText(R.string.fp_name_saving);

        View originalView = getView();

        changeNameCall = ApiClient.getApiService()
                .updateUserProfile(userId, body);

        changeNameCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(
                    @NonNull Call<User> call,
                    @NonNull Response<User> response
            ) {
                if (changeNameCall != call) return;
                changeNameCall = null;

                if (!isAdded() || getView() != originalView) return;

                if (!userId.equals(
                        FriendsProfileUi.activeUserId(requireContext()))
                        || !userId.equals(viewedUserId)) {
                    dialog.dismiss();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    dialog.dismiss();
                    showToast(getString(R.string.fp_name_updated));
                    loadProfile();
                } else {
                    restoreNameForm(input, dialog);
                    input.setError(getString(R.string.fp_name_update_failed));
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<User> call,
                    @NonNull Throwable error
            ) {
                if (changeNameCall != call) return;
                changeNameCall = null;

                if (!isAdded() || getView() != originalView) return;

                restoreNameForm(input, dialog);
                input.setError(getString(R.string.fp_name_update_failed));
            }
        });

    }

    private void restoreNameForm(EditText input, AlertDialog dialog) {
        input.setEnabled(true);
        dialog.setCancelable(true);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setText(R.string.fp_name_save);
    }
    private boolean canChangeAvatar(String userId) {
        if (!isAdded() || userId == null) return false;

        String currentUserId =
                FriendsProfileUi.activeUserId(requireContext());

        String requestedUserId = getArguments() == null
                ? null
                : getArguments().getString(ARG_USER_ID);

        boolean ownPage = requestedUserId == null
                || requestedUserId.trim().isEmpty()
                || userId.equals(requestedUserId);

        return userId.equals(currentUserId) && ownPage;
    }

    private void chooseAvatar() {
        if (avatarCall != null) return;

        String userId =
                FriendsProfileUi.activeUserId(requireContext());

        if (!canChangeAvatar(userId)) return;

        avatarPickerUserId = userId;
        avatarPicker.launch("image/*");
    }
    private void uploadAvatar(Uri uri, String userId) {
        if (avatarCall != null || !canChangeAvatar(userId)) return;

        ContentResolver resolver = requireContext()
                .getApplicationContext()
                .getContentResolver();

        String mimeType;
        try {
            mimeType = resolver.getType(uri);
        } catch (SecurityException error) {
            showToast(getString(R.string.fp_avatar_failed));
            return;
        }

        String fileName;

        if ("image/jpeg".equals(mimeType)) {
            fileName = "avatar.jpg";
        } else if ("image/png".equals(mimeType)) {
            fileName = "avatar.png";
        } else if ("image/webp".equals(mimeType)) {
            fileName = "avatar.webp";
        } else {
            showToast(getString(R.string.fp_avatar_failed));
            return;
        }

        final MediaType mediaType = MediaType.parse(mimeType);

        RequestBody imageBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public void writeTo(@NonNull BufferedSink sink)
                    throws IOException {
                try (InputStream input = resolver.openInputStream(uri)) {
                    if (input == null) {
                        throw new IOException("Cannot open image.");
                    }

                    byte[] buffer = new byte[8192];
                    long totalBytes = 0;
                    int bytesRead;

                    while ((bytesRead = input.read(buffer)) != -1) {
                        totalBytes += bytesRead;

                        if (totalBytes > 10L * 1024 * 1024) {
                            throw new IOException("Image exceeds 10 MB.");
                        }

                        sink.write(buffer, 0, bytesRead);
                    }

                    if (totalBytes == 0) {
                        throw new IOException("Image is empty.");
                    }
                } catch (SecurityException error) {
                    throw new IOException("Cannot read image.", error);
                }
            }
        };

        MultipartBody.Part part = MultipartBody.Part.createFormData(
                "file",
                fileName,
                imageBody
        );

        getBinding().btnChangeProfile.setEnabled(false);
        getBinding().btnChangeProfile.setText(R.string.fp_avatar_uploading);

        Call<Map<String, Object>> uploadCall =
                ApiClient.getApiService().uploadImage(part);

        avatarCall = uploadCall;

        uploadCall.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(
                    @NonNull Call<Map<String, Object>> call,
                    @NonNull Response<Map<String, Object>> response
            ) {
                if (!isCurrentAvatarRequest(call, userId)) return;

                Object rawUrl = response.body() == null
                        ? null
                        : response.body().get("url");

                if (!response.isSuccessful() || !(rawUrl instanceof String)) {
                    finishAvatarUpdate(false, userId);
                    return;
                }

                String avatarUrl =
                        ApiClient.normalizeStaticUrl((String) rawUrl);

                if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                    finishAvatarUpdate(false, userId);
                    return;
                }

                saveAvatarUrl(userId, avatarUrl);
            }

            @Override
            public void onFailure(
                    @NonNull Call<Map<String, Object>> call,
                    @NonNull Throwable error
            ) {
                if (!isCurrentAvatarRequest(call, userId)) return;
                finishAvatarUpdate(false, userId);
            }
        });
    }
    private void saveAvatarUrl(String userId, String avatarUrl) {
        Map<String, Object> body = new HashMap<>();
        body.put("avatarUrl", avatarUrl);

        Call<User> updateCall = ApiClient.getApiService()
                .updateUserProfile(userId, body);

        avatarCall = updateCall;

        updateCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(
                    @NonNull Call<User> call,
                    @NonNull Response<User> response
            ) {
                if (!isCurrentAvatarRequest(call, userId)) return;

                boolean success =
                        response.isSuccessful() && response.body() != null;

                finishAvatarUpdate(success, userId);
            }

            @Override
            public void onFailure(
                    @NonNull Call<User> call,
                    @NonNull Throwable error
            ) {
                if (!isCurrentAvatarRequest(call, userId)) return;
                finishAvatarUpdate(false, userId);
            }
        });
    }

    private boolean isCurrentAvatarRequest(Call<?> call, String userId) {
        if (avatarCall != call) return false;
        if (!isAdded() || getView() == null) return false;

        if (!canChangeAvatar(userId)) {
            finishAvatarUpdate(false, userId);
            return false;
        }

        return true;
    }

    private void finishAvatarUpdate(boolean success, String userId) {
        avatarCall = null;

        if (!isAdded() || getView() == null) return;

        getBinding().btnChangeProfile.setEnabled(true);
        getBinding().btnChangeProfile.setText(R.string.fp_change_profile);

        if (!canChangeAvatar(userId)) return;

        showToast(getString(
                success
                        ? R.string.fp_avatar_updated
                        : R.string.fp_avatar_failed
        ));

        if (success) {
            loadProfile();
        }
    }
}