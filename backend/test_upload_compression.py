"""
Unit test kiểm tra tính năng nén ảnh Bitmap và API upload [D9-MD].
Phụ trách: Minh Đức (BE) - Backend Engineer & Storage/Media Specialist.
"""

import os
import sys
import io
import unittest
from PIL import Image, ImageDraw

if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    except Exception:
        pass

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
if BASE_DIR not in sys.path:
    sys.path.insert(0, BASE_DIR)

from routers.upload import compress_image_content, MAX_IMAGE_DIMENSION, JPEG_QUALITY, get_upload_storage_stats


class TestUploadCompression(unittest.TestCase):

    def create_dummy_image(self, width: int, height: int, format: str = "JPEG", quality: int = 95) -> bytes:
        img = Image.new("RGB", (width, height), color=(120, 150, 200))
        draw = ImageDraw.Draw(img)
        # Draw concentric rectangles to give the image realistic photographic complexity
        limit = min(width, height) // 2
        for i in range(0, limit, 20):
            draw.rectangle([i, i, width - 1 - i, height - 1 - i], outline=(i % 255, (i * 2) % 255, (i * 3) % 255))
        buf = io.BytesIO()
        img.save(buf, format=format, quality=quality)
        return buf.getvalue()

    def test_compress_large_image_resizes_and_reduces_size(self):
        """Kiểm tra ảnh lớn (3000x2000) được thu nhỏ về tối đa 1920px và tiết kiệm dung lượng"""
        large_bytes = self.create_dummy_image(3000, 2000, format="JPEG", quality=95)
        orig_size = len(large_bytes)

        comp_bytes, ext, orig_res, comp_res = compress_image_content(large_bytes, ".jpg")

        self.assertEqual(ext, ".jpg")
        self.assertEqual(orig_res, orig_size)
        self.assertLess(comp_res, orig_size, "Dung lượng sau nén phải nhỏ hơn dung lượng ban đầu")

        # Kiểm tra kích thước sau khi nén <= 1920
        res_img = Image.open(io.BytesIO(comp_bytes))
        self.assertLessEqual(max(res_img.width, res_img.height), MAX_IMAGE_DIMENSION)
        self.assertEqual(res_img.width, 1920)

        saved_pct = (1.0 - (comp_res / orig_size)) * 100
        print(f"\n[*] Test Large Image: {orig_size/1024:.1f}KB -> {comp_res/1024:.1f}KB (Tiết kiệm {saved_pct:.1f}%)")
        self.assertGreater(saved_pct, 40.0, "Dung lượng tiết kiệm phải đạt trên 40%")

    def test_compress_png_without_alpha_converts_to_jpg(self):
        """Ảnh PNG không có kênh trong suốt được chuyển đổi sang JPG để nén tốt hơn"""
        import os
        img = Image.frombytes("RGB", (1000, 1000), os.urandom(3000000))
        buf = io.BytesIO()
        img.save(buf, format="PNG")
        png_bytes = buf.getvalue()
        orig_size = len(png_bytes)

        comp_bytes, ext, orig_res, comp_res = compress_image_content(png_bytes, ".png")

        self.assertEqual(ext, ".jpg", "PNG ảnh chụp không alpha chuyển sang JPG")
        self.assertLess(comp_res, orig_size)
        saved_pct = (1.0 - (comp_res / orig_size)) * 100
        print(f"[*] Test PNG photo conversion: {orig_size/1024:.1f}KB -> {comp_res/1024:.1f}KB (Tiết kiệm {saved_pct:.1f}%)")

    def test_compress_png_with_alpha_preserves_png(self):
        """Ảnh PNG có kênh trong suốt (Alpha) phải giữ nguyên định dạng PNG"""
        img = Image.new("RGBA", (400, 400), color=(255, 0, 0, 128))
        buf = io.BytesIO()
        img.save(buf, format="PNG")
        rgba_bytes = buf.getvalue()

        comp_bytes, ext, orig_res, comp_res = compress_image_content(rgba_bytes, ".png")
        self.assertEqual(ext, ".png", "PNG có kênh alpha phải giữ nguyên định dạng PNG")

    def test_already_optimized_small_image_not_bloated(self):
        """Ảnh nhỏ đã nén sẵn không bị phình to dung lượng"""
        small_bytes = self.create_dummy_image(100, 100, format="JPEG", quality=50)
        orig_size = len(small_bytes)

        comp_bytes, ext, orig_res, comp_res = compress_image_content(small_bytes, ".jpg")
        self.assertLessEqual(comp_res, orig_size, "Không bao giờ được làm phình dung lượng ảnh")

    def test_get_upload_storage_stats(self):
        """Kiểm tra hàm thống kê dung lượng thư mục upload"""
        stats = get_upload_storage_stats()
        self.assertIn("uploadDirectory", stats)
        self.assertIn("totalFiles", stats)
        self.assertIn("totalSizeMB", stats)
        print(f"[*] Storage stats: {stats['totalFiles']} files, {stats['totalSizeMB']} MB")


if __name__ == "__main__":
    unittest.main()
