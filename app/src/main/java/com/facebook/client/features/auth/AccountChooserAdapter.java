package com.facebook.client.features.auth;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.facebook.client.R;
import com.facebook.client.data.model.User;
import com.facebook.client.databinding.ItemAccountChooserBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách tài khoản demo phục vụ đăng nhập 1 chạm (1-Tap Login).
 */
public class AccountChooserAdapter extends RecyclerView.Adapter<AccountChooserAdapter.AccountViewHolder> {

  public interface OnAccountClickListener {
    void onAccountClick(User user);
  }

  private final List<User> accounts = new ArrayList<>();
  private final OnAccountClickListener listener;

  public AccountChooserAdapter(List<User> accounts, OnAccountClickListener listener) {
    if (accounts != null) {
      this.accounts.addAll(accounts);
    }
    this.listener = listener;
  }

  @NonNull
  @Override
  public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ItemAccountChooserBinding binding =
        ItemAccountChooserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new AccountViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
    holder.bind(accounts.get(position));
  }

  @Override
  public int getItemCount() {
    return accounts.size();
  }

  public class AccountViewHolder extends RecyclerView.ViewHolder {

    private final ItemAccountChooserBinding binding;

    public AccountViewHolder(@NonNull ItemAccountChooserBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }

    public void bind(User user) {
      binding.tvAccountName.setText(user.getDisplayName());
      
      String subtitle = user.getEmail() != null ? user.getEmail() : "demo.user@facebook.com";
      binding.tvAccountEmail.setText(subtitle + " • Active now");

      Glide.with(itemView.getContext())
          .load(user.getAvatarUrl())
          .circleCrop()
          .placeholder(R.drawable.bg_avatar_placeholder)
          .error(R.drawable.bg_avatar_placeholder)
          .into(binding.ivAccountAvatar);

      itemView.setOnClickListener(v -> {
        if (listener != null) {
          listener.onAccountClick(user);
        }
      });
    }
  }
}

