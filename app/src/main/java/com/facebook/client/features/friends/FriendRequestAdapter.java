package com.facebook.client.features.friends;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.client.R;
import com.facebook.client.data.model.FriendRequest;
import com.facebook.client.databinding.ItemFriendRequestBinding;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestAdapter extends
        RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder> {

    private final List<FriendRequest> requests = new ArrayList<>();
    private final OnActionListener listener;

    // Fragment sẽ triển khai hai hàm này để xử lý thao tác.
    public interface OnActionListener {
        void onAccept(FriendRequest request);

        void onReject(FriendRequest request);
    }

    // Nhận danh sách ban đầu và đối tượng xử lý sự kiện.
    public FriendRequestAdapter(
            List<FriendRequest> initialRequests,
            @NonNull OnActionListener listener) {

        this.listener = listener;

        if (initialRequests != null) {
            requests.addAll(initialRequests);
        }
    }

    // Tạo giao diện một dòng lời mời từ XML.
    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        ItemFriendRequestBinding binding =
                ItemFriendRequestBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new FriendRequestViewHolder(binding);
    }

    // Gán dữ liệu và sự kiện cho dòng đang hiển thị.
    @Override
    public void onBindViewHolder(
            @NonNull FriendRequestViewHolder holder,
            int position) {

        FriendRequest request = requests.get(position);

        holder.binding.friendRequestName.setText(
                request.getRequesterName()
        );

        Glide.with(holder.itemView)
                .load(request.getRequesterAvatar())
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(holder.binding.friendRequestAvatar);

        holder.binding.btnAcceptRequest.setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition();

            if (currentPosition != RecyclerView.NO_POSITION) {
                listener.onAccept(requests.get(currentPosition));
            }
        });

        holder.binding.btnRejectRequest.setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition();

            if (currentPosition != RecyclerView.NO_POSITION) {
                listener.onReject(requests.get(currentPosition));
            }
        });
    }

    // RecyclerView dùng số này để biết danh sách có bao nhiêu dòng.
    @Override
    public int getItemCount() {
        return requests.size();
    }

    // Thay danh sách khi Fragment nhận dữ liệu từ repository.
    public void updateRequests(List<FriendRequest> newRequests) {
        requests.clear();

        if (newRequests != null) {
            requests.addAll(newRequests);
        }

        notifyDataSetChanged();
    }

    // Fragment gọi sau khi repository xác nhận thao tác thành công.
    public void removeRequest(String requestId) {
        if (requestId == null) {
            return;
        }

        for (int i = 0; i < requests.size(); i++) {
            if (requestId.equals(requests.get(i).getId())) {
                requests.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, requests.size() - i); // Cập nhật lại chỉ số cho các phần tử đằng sau
                return;
            }
        }
    }

    // Giữ các View thuộc một dòng lời mời.
    public static class FriendRequestViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemFriendRequestBinding binding;

        public FriendRequestViewHolder(
                ItemFriendRequestBinding binding) {

            super(binding.getRoot());
            this.binding = binding;
        }
    }
}