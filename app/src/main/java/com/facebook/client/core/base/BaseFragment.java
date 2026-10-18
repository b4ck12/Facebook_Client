package com.facebook.client.core.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

/**
 * BaseFragment chuẩn quản lý ViewBinding tự động giải phóng bộ nhớ (chống Memory Leak).
 *
 * @param <VB> Kiểu ViewBinding tương ứng của Fragment
 */
public abstract class BaseFragment<VB extends ViewBinding> extends Fragment {

    private VB _binding;

    /**
     * Truy cập binding an toàn trong vòng đời của View.
     */
    protected VB getBinding() {
        if (_binding == null) {
            throw new IllegalStateException("Không thể truy cập binding khi View đã bị hủy.");
        }
        return _binding;
    }

    /**
     * Phương thức trừu tượng để Fragment con cung cấp ViewBinding.
     */
    protected abstract VB inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    /**
     * Khởi tạo giao diện, adapter, sự kiện click...
     */
    protected abstract void initViews();

    /**
     * Lắng nghe LiveData từ ViewModel (mặc định để trống, override khi cần).
     */
    protected void initObservers() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        _binding = inflateBinding(inflater, container);
        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        initObservers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Bắt buộc: Giải phóng binding để tránh Memory Leak
        _binding = null;
    }

    /**
     * Tiện ích hiển thị nhanh Toast thông báo.
     */
    protected void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}