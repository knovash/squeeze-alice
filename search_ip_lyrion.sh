#!/bin/bash
python3 - <<EOF
import socket
import time

BROADCAST_PORT = 3483
HTTP_PORT = 9000
TIMEOUT = 3

# --- 1. Собираем все IP, ответившие на широковещательный запрос ---
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
sock.settimeout(TIMEOUT)
sock.bind(('', BROADCAST_PORT))

sock.sendto(b'e', ('<broadcast>', BROADCAST_PORT))

candidates = set()
start = time.time()
while time.time() - start < TIMEOUT:
    try:
        _, addr = sock.recvfrom(1024)
        candidates.add(addr[0])   # добавляем любой ответивший IP
    except socket.timeout:
        break
sock.close()

# --- 2. Проверяем, открыт ли TCP-порт 9000 на каждом кандидате ---
lms_ips = []
for ip in candidates:
    try:
        tcp_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        tcp_sock.settimeout(1)
        result = tcp_sock.connect_ex((ip, HTTP_PORT))
        tcp_sock.close()
        if result == 0:
            lms_ips.append(ip)
    except:
        pass

# --- Вывод ---
if lms_ips:
    print("Найдены работающие LMS (порт 9000 открыт):")
    for ip in sorted(lms_ips):
        print(f"  http://{ip}:9000")
else:
    print("Активных LMS с веб-интерфейсом не обнаружено")
EOF

echo "OK"
sleep 20
