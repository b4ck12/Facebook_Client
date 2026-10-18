package com.facebook.client.core.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

/**
 * BaseActivity chuẩn quản lý ViewBinding và các lifecycle hook dùng chung.
 *
 * @param <VB> Kiểu ViewBinding tương ứng của Activity
 */
public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {

    protected VB binding;

    /**
     * Phương thức trừu tượng để Activity con cung cấp ViewBinding.
     * Ví dụ: inflater -> ActivityMainBinding.inflate(inflater)
     */
    protected abstract VB inflateBinding(LayoutInflater inflater);

    /**
     * Khởi tạo các view, adapter, sự kiện click...
     */
    protected abstract void initViews();

    /**
     * Lắng nghe LiveData từ ViewModel (mặc định để trống, override khi cần).
     */
    protected void initObservers() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = inflateBinding(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        initObservers();
    }

    /**
     * Tiện ích hiển thị nhanh Toast thông báo.
     */
    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}