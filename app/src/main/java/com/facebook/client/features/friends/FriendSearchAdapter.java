package com.facebook.client.features.friends;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.client.R;
import com.facebook.client.data.model.User;
import com.facebook.client.databinding.ItemFriendSearchBinding;

import java.util.ArrayList;
import java.util.List;

public class FriendSearchAdapter extends RecyclerView.Adapter<FriendSearchAdapter.SearchViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(@NonNull User user);
    }

    private final List<User> users = new ArrayList<>();
    @Nullable
    private final OnUserClickListener listener;

    public FriendSearchAdapter(@Nullable OnUserClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFriendSearchBinding binding = ItemFriendSearchBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new SearchViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        User user = users.get(position);

        holder.binding.tvUserName.setText(user.getDisplayName());

        Glide.with(holder.itemView)
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(holder.binding.ivUserAvatar);

        holder.binding.btnViewProfile.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(@Nullable List<User> newUsers) {
        users.clear();
        if (newUsers != null) {
            users.addAll(newUsers);
        }
        notifyDataSetChanged();
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        final ItemFriendSearchBinding binding;

        public SearchViewHolder(@NonNull ItemFriendSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}