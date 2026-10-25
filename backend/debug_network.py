"""
=============================================================================
Công cụ chẩn đoán kết nối mạng giữa Android Emulator (Pixel 10a) và FastAPI Server.
=============================================================================
"""
import socket
import urllib.request
import json
import time

def check_local_ips():
    print("=" * 60)
    print("1. THÔNG TIN ĐỊA CHỈ IP MẠNG MÁY CHỦ BACKEND:")
    print("=" * 60)
    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)
    print(f" - Hostname máy chủ: {hostname}")
    print(f" - Local IP (Máy tính): {local_ip}")
    print(f" - Loopback IP (Máy tính): 127.0.0.1")
    print(f" - Android Emulator Gateway: 10.0.2.2 (Trỏ tới máy chủ)")
    print(f" -> Base URL cho Retrofit trên Android: http://10.0.2.2:8000/\n")

def test_api_endpoints():
    print("=" * 60)
    print("2. KIỂM THỬ KẾT NỐI TỚI FASTAPI SERVER (CỔNG 8000):")
    print("=" * 60)
    base_url = "http://127.0.0.1:8000"
    endpoints = [
        ("/", "Health Check"),
        ("/api/network-test", "Android Network Test"),
        ("/api/posts?limit=3", "Feed Posts API"),
    ]

    for path, name in endpoints:
        url = f"{base_url}{path}"
        try:
            start = time.time()
            req = urllib.request.Request(url, headers={"User-Agent": "Retrofit-Debug-Client/1.0"})
            with urllib.request.urlopen(req, timeout=3) as resp:
                elapsed = round((time.time() - start) * 1000, 2)
                print(f" [PASS] {name} ({path}): HTTP {resp.status} trong {elapsed}ms")
        except Exception as e:
            print(f" [FAIL] {name} ({path}): HTTP {resp.status} trong {elapsed}ms")
    print()

if __name__ == "__main__":
    check_local_ips()
    test_api_endpoints() 