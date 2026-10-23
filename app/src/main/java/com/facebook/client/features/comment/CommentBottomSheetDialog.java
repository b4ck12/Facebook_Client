package com.facebook.client.features.comment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.client.R;
import com.facebook.client.core.utils.SessionManager;
import com.facebook.client.data.mock.MockDataGenerator;
import com.facebook.client.data.model.User;
import com.facebook.client.databinding.BottomSheetCommentsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * BottomSheetDialogFragment hiển thị danh sách bình luận trượt lên từ đáy màn hình ([D6-ML]).
 * Quản lý ViewBinding (BottomSheetCommentsBinding), CommentAdapter và kết nối CommentViewModel.
 */
public class CommentBottomSheetDialog extends BottomSheetDialogFragment {

    private static final String ARG_POST_ID = "arg_post_id";

    private BottomSheetCommentsBinding binding;
    private CommentViewModel viewModel;
    private CommentAdapter commentAdapter;
    private String postId;

    /**
     * Khởi tạo một thể hiện của dialog với postId chỉ định.
     *
     * @param postId Mã bài viết cần xem hoặc gửi bình luận.
     */
    @NonNull
    public static CommentBottomSheetDialog newInstance(@NonNull String postId) {
        CommentBottomSheetDialog dialog = new CommentBottomSheetDialog();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetCommentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
        initViewModel();
        observeViewModel();

        // Tải danh sách bình luận của bài viết
        if (postId != null) {
            viewModel.loadComments(postId);
        }
    }

    private void initViews() {
        Context context = requireContext();

        // 1. Cấu hình RecyclerView danh sách bình luận
        commentAdapter = new CommentAdapter();
        binding.rvComments.setLayoutManager(new LinearLayoutManager(context));
        binding.rvComments.setAdapter(commentAdapter);

        // 2. Nạp Avatar của người dùng hiện đang đăng nhập vào thanh input
        loadCurrentUserAvatar(context);

        // 3. Bấm nút đóng (X) trên góc phải
        binding.ibCloseComments.setOnClickListener(v -> dismiss());

        // 4. Bấm nút Gửi bình luận
        binding.ibSendComment.setOnClickListener(v -> handleSendComment());
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(CommentViewModel.class);
    }

    private void observeViewModel() {
        // Quan sát danh sách bình luận
        viewModel.getCommentsLiveData().observe(getViewLifecycleOwner(), comments -> {
            if (comments == null || comments.isEmpty()) {
                binding.tvEmptyComments.setVisibility(View.VISIBLE);
                binding.rvComments.setVisibility(View.GONE);
                commentAdapter.setComments(null);
            } else {
                binding.tvEmptyComments.setVisibility(View.GONE);
                binding.rvComments.setVisibility(View.VISIBLE);
                commentAdapter.setComments(comments);
                // Tự động cuộn xuống bình luận mới nhất
                binding.rvComments.scrollToPosition(comments.size() - 1);
            }
        });

        // Quan sát thông báo lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Xử lý gửi bình luận khi người dùng nhấn nút gửi.
     */
    private void handleSendComment() {
        String content = binding.etCommentInput.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        // Lấy userId đang đăng nhập từ SessionManager
        String currentUserId = SessionManager.getInstance(requireContext()).getCurrentUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            currentUserId = "user_101"; // Fallback nếu chưa đăng nhập
        }

        if (postId != null) {
            viewModel.sendComment(postId, currentUserId, content);
            // Xóa trắng ô nhập và ẩn bàn phím ảo
            binding.etCommentInput.setText("");
            hideKeyboard();
        }
    }

    /**
     * Nạp ảnh đại diện của người dùng đang đăng nhập vào góc thanh nhập bình luận.
     */
    private void loadCurrentUserAvatar(@NonNull Context context) {
        String currentUserId = SessionManager.getInstance(context).getCurrentUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            currentUserId = "user_101";
        }

        User currentUser = MockDataGenerator.getInstance().getUserById(currentUserId);
        String avatarUrl = (currentUser != null) ? currentUser.getAvatarUrl() : null;

        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl.trim())
                    .placeholder(R.drawable.bg_avatar_placeholder)
                    .error(R.drawable.bg_avatar_placeholder)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivCurrentUserAvatar);
        } else {
            binding.ivCurrentUserAvatar.setImageResource(R.drawable.bg_avatar_placeholder);
        }
    }

    /**
     * Ẩn bàn phím mềm sau khi nhấn gửi.
     */
    private void hideKeyboard() {
        if (getActivity() != null && binding != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(binding.etCommentInput.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh rò rỉ bộ nhớ ViewBinding
    }
}