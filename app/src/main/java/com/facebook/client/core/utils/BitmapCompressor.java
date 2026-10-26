package com.facebook.client.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Tiện ích nén ảnh Bitmap trước khi upload lên máy chủ Backend [D9-MD].
 * Giúp giảm thiểu tối đa dung lượng tệp tin (thường giảm 80-90% băng thông)
 * và ngăn chặn triệt để lỗi tràn bộ nhớ (OutOfMemoryError) trên thiết bị di động.
 *
 * <p>Phụ trách: Minh Đức (BE & Storage Specialist) hỗ trợ nhóm Frontend.
 */
public final class BitmapCompressor {

    private static final String TAG = "BitmapCompressor";
    public static final int DEFAULT_MAX_DIMENSION = 1920;
    public static final int DEFAULT_QUALITY = 82;

    private BitmapCompressor() {
        // Private constructor ngăn khởi tạo class tiện ích
    }

    /**
     * Nén tệp tin ảnh từ Uri thành một File tạm thời trong thư mục cache để chuẩn bị upload.
     *
     * @param context      Context ứng dụng.
     * @param imageUri     Uri của ảnh được chọn từ thư viện (ContentProvider / FileProvider).
     * @param maxDimension Kích thước tối đa cho chiều rộng hoặc chiều cao (ví dụ: 1920px).
     * @param quality      Mức chất lượng nén JPEG (từ 1 đến 100, khuyến nghị 80-85).
     * @return File tạm thời chứa ảnh đã được nén và xoay đúng hướng, hoặc null nếu có lỗi.
     */
    @Nullable
    public static File compressImageFile(@NonNull Context context,
                                         @NonNull Uri imageUri,
                                         int maxDimension,
                                         int quality) {
        Bitmap sampledBitmap = null;
        Bitmap rotatedBitmap = null;
        Bitmap scaledBitmap = null;

        try {
            // 1. Đọc kích thước ban đầu (không giải mã toàn bộ ảnh vào RAM)
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try (InputStream is = context.getContentResolver().openInputStream(imageUri)) {
                if (is == null) {
                    Log.e(TAG, "Không thể mở InputStream từ Uri: " + imageUri);
                    return null;
                }
                BitmapFactory.decodeStream(is, null, options);
            }

            int origWidth = options.outWidth;
            int origHeight = options.outHeight;
            if (origWidth <= 0 || origHeight <= 0) {
                Log.e(TAG, "Kích thước ảnh không hợp lệ: " + origWidth + "x" + origHeight);
                return null;
            }

            // 2. Tính toán inSampleSize để giảm tải bộ nhớ heap
            options.inSampleSize = calculateInSampleSize(origWidth, origHeight, maxDimension, maxDimension);
            options.inJustDecodeBounds = false;

            // 3. Giải mã ảnh với sample size đã tính
            try (InputStream is = context.getContentResolver().openInputStream(imageUri)) {
                if (is == null) {
                    return null;
                }
                sampledBitmap = BitmapFactory.decodeStream(is, null, options);
            }

            if (sampledBitmap == null) {
                Log.e(TAG, "Giải mã Bitmap thất bại từ Uri: " + imageUri);
                return null;
            }

            // 4. Xử lý xoay ảnh dựa theo thông tin EXIF
            int orientationDegree = getExifOrientationDegrees(context, imageUri);
            if (orientationDegree != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(orientationDegree);
                rotatedBitmap = Bitmap.createBitmap(
                        sampledBitmap, 0, 0,
                        sampledBitmap.getWidth(), sampledBitmap.getHeight(),
                        matrix, true
                );
            } else {
                rotatedBitmap = sampledBitmap;
            }

            // 5. Thu nhỏ chính xác tỉ lệ nếu kích thước vẫn lớn hơn maxDimension
            int currentMax = Math.max(rotatedBitmap.getWidth(), rotatedBitmap.getHeight());
            if (currentMax > maxDimension) {
                float scale = (float) maxDimension / currentMax;
                int finalWidth = Math.max(1, Math.round(rotatedBitmap.getWidth() * scale));
                int finalHeight = Math.max(1, Math.round(rotatedBitmap.getHeight() * scale));
                scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap, finalWidth, finalHeight, true);
            } else {
                scaledBitmap = rotatedBitmap;
            }

            // 6. Ghi ảnh đã nén vào file tạm trong cacheDir
            File cacheDir = new File(context.getCacheDir(), "compressed_uploads");
            if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                Log.w(TAG, "Không thể tạo thư mục cache compressed_uploads");
            }

            File tempFile = new File(cacheDir, "upload_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                int targetQuality = Math.max(10, Math.min(100, quality));
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, targetQuality, fos);
                fos.flush();
            }

            Log.d(TAG, "Nén ảnh thành công: " + tempFile.getName() + " (" + (tempFile.length() / 1024) + " KB)");
            return tempFile;

        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong quá trình nén ảnh: " + e.getMessage(), e);
            return null;
        } finally {
            // Giải phóng bộ nhớ Bitmap
            if (sampledBitmap != null && sampledBitmap != rotatedBitmap && sampledBitmap != scaledBitmap) {
                sampledBitmap.recycle();
            }
            if (rotatedBitmap != null && rotatedBitmap != scaledBitmap) {
                rotatedBitmap.recycle();
            }
            if (scaledBitmap != null) {
                scaledBitmap.recycle();
            }
        }
    }

    /**
     * Nén ảnh với cấu hình mặc định (1920px, chất lượng 82%).
     */
    @Nullable
    public static File compressImageFile(@NonNull Context context, @NonNull Uri imageUri) {
        return compressImageFile(context, imageUri, DEFAULT_MAX_DIMENSION, DEFAULT_QUALITY);
    }

    /**
     * Tính toán hệ số mẫu (inSampleSize) dạng lũy thừa của 2 để tối ưu hóa bộ nhớ.
     */
    public static int calculateInSampleSize(int rawWidth, int rawHeight, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (rawHeight > reqHeight || rawWidth > reqWidth) {
            final int halfHeight = rawHeight / 2;
            final int halfWidth = rawWidth / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return Math.max(1, inSampleSize);
    }

    /**
     * Đọc góc xoay ảnh từ thẻ EXIF (chống ảnh chụp dọc bị xoay 90 độ ngang).
     */
    private static int getExifOrientationDegrees(@NonNull Context context, @NonNull Uri uri) {
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            if (is == null) {
                return 0;
            }
            ExifInterface exif = new ExifInterface(is);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            Log.w(TAG, "Không thể đọc dữ liệu EXIF: " + e.getMessage());
            return 0;
        }
    }
}
