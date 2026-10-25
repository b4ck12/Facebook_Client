package com.facebook.client.features.friends;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.facebook.client.R;
import com.facebook.client.data.model.FriendRequest;
import com.facebook.client.databinding.ItemFriendRequestBinding;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendRequestAdapter extends
        RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder> {
    public interface OnActionListener {
        void onAccept(FriendRequest request);
        void onReject(FriendRequest request);
    }

    private final List<FriendRequest> requests = new ArrayList<>();
    private final Set<String> busyIds = new HashSet<>();
    private final OnActionListener listener;

    public FriendRequestAdapter(List<FriendRequest> initial, @NonNull OnActionListener listener) {
        this.listener = listener;
        if (initial != null) requests.addAll(initial);
    }

    @NonNull @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendRequestViewHolder(ItemFriendRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        FriendRequest request = requests.get(position);
        holder.binding.tvFriendRequestName.setText(request.getRequesterName());
        long now = System.currentTimeMillis();
        long time = request.getCreatedAt() <= 0 ? now : Math.min(now, request.getCreatedAt());
        holder.binding.tvFriendRequestTime.setText(DateUtils.getRelativeTimeSpanString(
                time, now, DateUtils.MINUTE_IN_MILLIS));
        Glide.with(holder.itemView).load(request.getRequesterAvatar())
                .placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile)
                .circleCrop().into(holder.binding.ivFriendRequestAvatar);
        boolean enabled = !busyIds.contains(request.getId());
        holder.binding.btnAcceptRequest.setEnabled(enabled);
        holder.binding.btnRejectRequest.setEnabled(enabled);
        holder.binding.btnAcceptRequest.setOnClickListener(view -> dispatch(holder, true));
        holder.binding.btnRejectRequest.setOnClickListener(view -> dispatch(holder, false));
    }

    private void dispatch(FriendRequestViewHolder holder, boolean accept) {
        int position = holder.getAdapterPosition();
        if (position == RecyclerView.NO_POSITION) return;
        FriendRequest request = requests.get(position);
        if (busyIds.contains(request.getId())) return;
        if (accept) listener.onAccept(request); else listener.onReject(request);
    }

    @Override public int getItemCount() { return requests.size(); }

    public void updateRequests(List<FriendRequest> items) {
        List<FriendRequest> copy = items == null ? new ArrayList<>() : new ArrayList<>(items);
        requests.clear();
        requests.addAll(copy);
        busyIds.clear();
        notifyDataSetChanged();
    }

    public void setRequestBusy(String requestId, boolean busy) {
        if (busy) busyIds.add(requestId); else busyIds.remove(requestId);
        for (int i = 0; i < requests.size(); i++) {
            if (requestId.equals(requests.get(i).getId())) {
                notifyItemChanged(i);
                return;
            }
        }
    }

    public static class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        private final ItemFriendRequestBinding binding;
        public FriendRequestViewHolder(ItemFriendRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}