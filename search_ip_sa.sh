#!/bin/bash
python3 - <<EOF
import socket
import ipaddress
import threading
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

PORT = 8010
TIMEOUT = 0.5   # секунд на соединение
THREADS = 100   # количество параллельных проверок

# --- Определяем локальную подсеть ---
def get_local_subnet():
    try:
        # Создаём временный сокет, чтобы узнать свой IP
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
        s.close()
        # Предполагаем маску /24 (наиболее распространённая)
        subnet = ipaddress.IPv4Network(f"{local_ip}/24", strict=False)
        return subnet
    except:
        # Если не удалось, можно задать вручную или вернуть стандартную
        return ipaddress.IPv4Network("192.168.1.0/24")

subnet = get_local_subnet()
print(f"Сканирование подсети {subnet} на предмет порта {PORT}...")

# --- Функция проверки одного IP ---
def check_port(ip):
    try:
        with socket.create_connection((str(ip), PORT), timeout=TIMEOUT):
            return str(ip)
    except:
        return None

# --- Сканируем все IP в подсети (исключаем широковещательный и сетевой) ---
ips = list(subnet.hosts())  # все адреса, кроме нулевого и широковещательного
found = []

with ThreadPoolExecutor(max_workers=THREADS) as executor:
    futures = {executor.submit(check_port, ip): ip for ip in ips}
    for future in as_completed(futures):
        result = future.result()
        if result:
            found.append(result)
            print(f"  Найден: http://{result}:{PORT}")

# --- Итоговый вывод ---
if found:
    print("\nНайдены устройства с открытым портом 8010:")
    for ip in sorted(found):
        print(f"  http://{ip}:{PORT}")
else:
    print("\nУстройств с открытым портом 8010 не обнаружено.")
EOF

echo "OK"
sleep 20
