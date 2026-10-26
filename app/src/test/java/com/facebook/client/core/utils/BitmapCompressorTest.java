package com.facebook.client.core.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit Test kiểm tra tính toán hệ số nén inSampleSize của BitmapCompressor [D9-MD].
 * Đảm bảo giảm tải dung lượng bộ nhớ Heap và tránh OutOfMemoryError trên Android.
 */
public class BitmapCompressorTest {

    @Test
    public void calculateInSampleSize_smallerImage_returnsOne() {
        int sampleSize = BitmapCompressor.calculateInSampleSize(800, 600, 1920, 1920);
        assertEquals(1, sampleSize);
    }

    @Test
    public void calculateInSampleSize_exactSize_returnsOne() {
        int sampleSize = BitmapCompressor.calculateInSampleSize(1920, 1080, 1920, 1080);
        assertEquals(1, sampleSize);
    }

    @Test
    public void calculateInSampleSize_4kImage_returnsTwo() {
        // Ảnh 4K (3840 x 2160) yêu cầu giảm về 1920 x 1080 -> sampleSize = 2 (giảm 4 lần số pixel)
        int sampleSize = BitmapCompressor.calculateInSampleSize(3840, 2160, 1920, 1080);
        assertEquals(2, sampleSize);
    }

    @Test
    public void calculateInSampleSize_veryHighResCamera_returnsFour() {
        // Ảnh camera 48MP (8000 x 6000) giảm về 1920 x 1920 -> sampleSize = 4 (giảm 16 lần số pixel)
        int sampleSize = BitmapCompressor.calculateInSampleSize(8000, 6000, 1920, 1920);
        assertEquals(4, sampleSize);
    }

    @Test
    public void calculateInSampleSize_extremeDimensions_returnsPowerOfTwo() {
        int sampleSize = BitmapCompressor.calculateInSampleSize(16000, 12000, 1920, 1920);
        assertEquals(8, sampleSize);
    }
}
