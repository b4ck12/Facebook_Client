package com.facebook.client.core.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import com.facebook.client.data.mock.MockDataGenerator;

/** Các hàm dùng chung cho hai màn hình độc lập Friends/Profile. */
public final class FriendsProfileUi {
    private FriendsProfileUi() {}

    public static String activeUserId(Context context) {
        MockDataGenerator generator = MockDataGenerator.getInstance();
        String id = SessionManager.getInstance(context).getCurrentUserId();
        if (id == null || generator.getUserById(id) == null) id = generator.getCurrentUserId();
        generator.setCurrentUserId(id);
        return id;
    }

    public static void applySystemInsets(Activity activity, View root) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        int left = root.getPaddingLeft(), top = root.getPaddingTop();
        int right = root.getPaddingRight(), bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(left + bars.left, top + bars.top,
                    right + bars.right, bottom + bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}