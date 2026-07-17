#!/usr/bin/env python3
from evdev import InputDevice, ecodes
import time
import sys
import json
import requests
import logging
from logging.handlers import RotatingFileHandler
import os

# ---------- ЛОГИРОВАНИЕ ----------
log_formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
log_file = 'log.log'
file_handler = RotatingFileHandler(log_file, maxBytes=10*1024*1024, backupCount=3)
file_handler.setFormatter(log_formatter)
file_handler.setLevel(logging.INFO)
console_handler = logging.StreamHandler()
console_handler.setFormatter(log_formatter)
console_handler.setLevel(logging.INFO)
logger = logging.getLogger()
logger.setLevel(logging.INFO)
logger.addHandler(file_handler)
logger.addHandler(console_handler)

# ---------- ЗАГРУЗКА КОНФИГУРАЦИИ ----------
try:
    logger.info('📂 Открываем config.conf')
    with open('config.conf', 'r') as json_file:
        settings = json.load(json_file)
    logger.info('✅ Конфиг загружен')
except Exception as e:
    logger.error('❌ Не удалось загрузить config.conf: %s', e)
    sys.exit(1)

# Проверка обязательных полей
required_top = ['btdevice', 'server', 'actions']
for key in required_top:
    if key not in settings:
        logger.error('❌ В конфиге отсутствует ключ: %s', key)
        sys.exit(1)

TUNNEL_URL = settings['server'].rstrip('/')

# Построение карты действий
KEY_ACTION_MAP = {}
for idx, action in enumerate(settings['actions']):
    if not all(k in action for k in ('code', 'description', 'command')):
        logger.error('❌ Действие #%d не содержит обязательных полей', idx)
        sys.exit(1)

    code_val = action['code']
    if isinstance(code_val, str) and code_val.startswith('KEY_'):
        try:
            code = ecodes.KEY[code_val]
        except KeyError:
            logger.error('❌ Неизвестное имя ключа: %s', code_val)
            sys.exit(1)
    else:
        try:
            code = int(code_val)
        except (ValueError, TypeError):
            logger.error('❌ Некорректный код для действия #%d: %s', idx, code_val)
            sys.exit(1)

    description = action['description']
    command = action['command']

    if not command:
        logger.debug('⏩ Пропускаем кнопку %s (команда пустая)', description)
        continue

    if code in KEY_ACTION_MAP:
        logger.warning('⚠️ Дублирующийся код %d, перезаписываем', code)
    KEY_ACTION_MAP[code] = (description, command)

logger.info('✅ Загружено %d действий', len(KEY_ACTION_MAP))

# ---------- ПАРАМЕТРЫ ПОИСКА УСТРОЙСТВА ----------
# Имя устройства из секции voice (как в voice.py)
DEVICE_NAME = settings.get('voice', {}).get('device_name', 'HAOBO Technology USB Composite Device Keyboard')
# Запасной путь из конфига
BTDEV_FALLBACK = settings.get('btdevice')  # может быть None или строка
VENDOR = 0x4842   # HAOBO
PRODUCT = 0x0001

# ---------- ФУНКЦИЯ ПОИСКА (общая) ----------
def find_device(name_substring, vendor=None, product=None):
    """Возвращает путь (str) к первому подходящему устройству или None."""
    devices = [InputDevice(path) for path in evdev.list_devices()]
    for dev in devices:
        if name_substring in dev.name:
            return dev.path
    if vendor is not None and product is not None:
        for dev in devices:
            if dev.info.vendor == vendor and dev.info.product == product:
                return dev.path
    return None

# ---------- ОТПРАВКА ЗАПРОСОВ ----------
def send_request(action_name, query):
    """Отправка GET-запроса к серверу."""
    logger.info(action_name)
    try:
        url = f"{TUNNEL_URL}/{query.lstrip('/')}"
        logger.info("📤 REQUEST %s", url)
        r = requests.get(url, timeout=5)
        r.raise_for_status()
        logger.info("📥 RESPONSE %s %s", r.status_code, r.text)
    except Exception as e:
        logger.error('⚠️ Ошибка запроса: %s', e)

# ---------- ОБРАБОТКА СОБЫТИЙ ----------
def getkey(remote):
    """Бесконечный цикл чтения событий с устройства."""
    logger.info('🎯 Начинаем слушать кнопки')
    for event in remote.read_loop():
        if event.type == ecodes.EV_KEY and event.value == 1:
            key_name = ecodes.KEY.get(event.code, f'UNKNOWN_{event.code}')
            logger.info('🔘 Нажата кнопка: %s (%d)', key_name, event.code)
            if event.code in KEY_ACTION_MAP:
                action_name, query = KEY_ACTION_MAP[event.code]
                send_request(action_name, query)

# ---------- ОСНОВНОЙ ЦИКЛ С ПЕРЕПОДКЛЮЧЕНИЕМ ----------
def main():
    logger.info("🚀 Запуск btremote.py с автоматическим переподключением")
    # Отправка alive при старте
    send_request("its_alive", "cmd?action=its_alive")

    fixed_path = BTDEV_FALLBACK if BTDEV_FALLBACK and os.path.exists(BTDEV_FALLBACK) else None
    if fixed_path:
        logger.info(f"📌 Используем запасной путь: {fixed_path}")
    else:
        logger.info("🔍 Запасной путь не задан или недоступен, будем искать по имени")

    while True:
        dev_path = None
        if fixed_path:
            dev_path = fixed_path
        else:
            dev_path = find_device(DEVICE_NAME, VENDOR, PRODUCT)
            if dev_path is None:
                logger.error("❌ Устройство не найдено, повтор через 5 секунд")
                time.sleep(5)
                continue
            logger.info(f"✅ Найдено устройство: {dev_path}")

        try:
            remote = InputDevice(dev_path)
            logger.info(f"🔌 Устройство открыто: {remote.path} - {remote.name}")
            getkey(remote)   # этот цикл будет работать, пока устройство доступно
        except OSError as e:
            logger.error(f"⚠️ Ошибка при работе с устройством: {e}")
            if fixed_path:
                logger.info("🔄 Фиксированный путь перестал работать, переключаемся на автоматический поиск")
                fixed_path = None
            time.sleep(5)
        except Exception as e:
            logger.exception("💥 Непредвиденная ошибка")
            time.sleep(5)

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        logger.info("🛑 Остановка пользователем")
        sys.exit(0)