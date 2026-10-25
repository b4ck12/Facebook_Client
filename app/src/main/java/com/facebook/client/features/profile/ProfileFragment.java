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
import com.facebook.client.data.repository.MockProfileRepository;
import com.facebook.client.data.repository.MockPostRepository;
import com.facebook.client.data.repository.RepositoryCallback;
import com.facebook.client.databinding.FragmentProfileBinding;
import com.facebook.client.features.comment.CommentBottomSheetDialog;
import com.facebook.client.features.feed.PostAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {
    private static final String ARG_USER_ID = "viewed_user_id";
    private final IProfileRepository profileRepository = new MockProfileRepository();
    private final IPostRepository postRepository = new MockPostRepository();
    private PhotoGridAdapter photoAdapter;
    private PostAdapter postAdapter;
    private String viewedUserId;
    private int selectedTab;
    private int generation;
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
    }

    @Override public void onResume() {
        super.onResume();
        String activeUser = FriendsProfileUi.activeUserId(requireContext());
        String requested = getArguments() == null ? null : getArguments().getString(ARG_USER_ID);
        viewedUserId = requested == null || requested.trim().isEmpty() ? activeUser : requested;
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
                            urls.add(post.getMediaUrl());
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
        int token = generation;
        postRepository.toggleLike(post.getId(), FriendsProfileUi.activeUserId(requireContext()),
                new RepositoryCallback<Boolean>() {
                    @Override public void onSuccess(Boolean liked) {
                        if (!active(token)) return;
                        List<Post> displayed = postAdapter.getPosts();
                        for (int i = 0; i < displayed.size(); i++) {
                            if (post.getId().equals(displayed.get(i).getId())) {
                                postAdapter.notifyItemChanged(i);
                                break;
                            }
                        }
                    }
                    @Override public void onError(String message) {
                        if (active(token)) showToast(message);
                    }
                });
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
        ImageView image = new ImageView(requireContext());
        image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (320 * getResources().getDisplayMetrics().density)));
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Glide.with(this).load(url).placeholder(R.color.fb_surface_variant).into(image);
        new MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.fp_photo)
                .setView(image).setPositiveButton(R.string.fp_close, null).show();
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        state.putInt("selected_tab", selectedTab);
        super.onSaveInstanceState(state);
    }

    @Override public void onDestroyView() {
        generation++;
        getBinding().rvProfilePosts.setAdapter(null);
        getBinding().rvProfilePhotos.setAdapter(null);
        super.onDestroyView();
    }

    @Override public void onDestroy() {
        getChildFragmentManager().unregisterFragmentLifecycleCallbacks(commentLifecycle);
        super.onDestroy();
    }
}