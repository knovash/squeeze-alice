#!/usr/bin/env python3
import evdev
import subprocess
import sys
import os
import time
import urllib.request
import urllib.parse
import tempfile
import re
import requests
from datetime import datetime
import logging  # <-- ДОБАВЛЕНО
from logging.handlers import RotatingFileHandler

# Настройка логирования
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

# ========== НАСТРОЙКИ ==========
EVENT_CODE = 582
DEVICE_NAME = "HAOBO Technology USB Composite Device Keyboard"
RECORD_SECONDS = 4
SPEAK_URL = "http://192.168.1.130:8010/cmd"           # сервер для озвучивания и сигналов

ENABLE_START_BEEP = True   # включать звуковой сигнал до и после записи
START_DELAY = 1          # пауза после сигнала перед записью

# ========== YANDEX SPEECHKIT (только STT) ==========
YANDEX_API_KEY = "AQVN3dKzcyDkaTa9JPJ5Y4q9PXiRCIDaDuGPa3w2"
YANDEX_FOLDER_ID = "ajetqapn6874d4rbq97i"

# ========== НАСТРОЙКИ СИГНАЛА ==========
SIGNAL_PLAYER = "btremote"
SIGNAL_VALUE = "beep2"

# ================================================================

# Функция log больше не нужна, используем logger напрямую

def get_card_index_by_name(name_substring, is_capture=True):
    """Поиск индекса звуковой карты (только для захвата)."""
    cmd = "arecord -l" if is_capture else "aplay -l"
    try:
        output = subprocess.check_output(cmd, shell=True, text=True)
    except subprocess.CalledProcessError:
        return None
    for line in output.split('\n'):
        if name_substring in line:
            match = re.search(r'card (\d+):', line)
            if match:
                return int(match.group(1))
    return None

def send_signal_request():
    """Отправляет GET-запрос для воспроизведения звукового сигнала."""
    try:
        params = urllib.parse.urlencode({
            'action': 'signal',
            'player': SIGNAL_PLAYER,
            'value': SIGNAL_VALUE
        })
        url = f"{SPEAK_URL}?{params}"
        logger.info(f"🔔 Отправка сигнала на сервер: {url}")
        with urllib.request.urlopen(url, timeout=2) as response:
            resp = response.read().decode('utf-8')
            logger.info(f"✅ Ответ сервера: код {response.getcode()}, тело: {resp.strip()}")
    except Exception as e:
        logger.error(f"❌ Ошибка отправки сигнала: {e}")  # <-- исправлено

def send_speak_request(text):
    """Отправляет текст на озвучивание через HTTP‑запрос."""
    try:
        params = urllib.parse.urlencode({
            'action': 'voice',
            'player': 'btremote',
            'value': text
        })
        url = f"{SPEAK_URL}?{params}"
        logger.info(f"📤 Отправка запроса на сервер озвучивания")
        logger.info(f"   Метод: GET")
        logger.info(f"   Полный URL: {url}")
        logger.info(f"   Передаваемый текст: {text}")

        with urllib.request.urlopen(url, timeout=3) as response:
            resp = response.read().decode('utf-8')
            logger.info(f"✅ Ответ сервера: код {response.getcode()}, тело: {resp.strip()}")
    except Exception as e:
        logger.error(f"❌ Ошибка отправки запроса на озвучивание: {e}")  # <-- исправлено

def send_custom_request(action, player, value):
    """Отправляет GET-запрос на сервер с произвольными параметрами."""
    try:
        params = urllib.parse.urlencode({
            'action': action,
            'player': player,
            'value': value
        })
        url = f"{SPEAK_URL}?{params}"
        logger.info(f"📤 Отправка запроса: {url}")  # <-- исправлено
        with urllib.request.urlopen(url, timeout=2) as response:
            resp = response.read().decode('utf-8')
            logger.info(f"✅ Ответ сервера: код {response.getcode()}, тело: {resp.strip()}")
    except Exception as e:
        logger.error(f"❌ Ошибка отправки запроса: {e}")  # <-- исправлено (было logger.info)

def record_and_recognize():
    """Запись с микрофона и распознавание через Yandex SpeechKit."""
    if ENABLE_START_BEEP:
        logger.info("🔔 Сигнал к началу речи...")  # <-- исправлено
        send_signal_request()
        time.sleep(START_DELAY)

    logger.info("⚡ Запись началась, говорите...")  # <-- исправлено
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
        tmp_path = tmp.name

    # Поиск микрофона HAOBO
    mic_card = get_card_index_by_name("USB Composite Device", is_capture=True)
    if mic_card is None:
        logger.error("❌ Не найден микрофон HAOBO (USB Composite Device) в arecord -l")
        return

    cmd = f"arecord -D hw:{mic_card},0 -f S16_LE -r 16000 -c 1 -d {RECORD_SECONDS} {tmp_path} 2>/dev/null"
    subprocess.run(cmd, shell=True, check=False)

    logger.info("🔔 Сигнал окончания записи")  # <-- исправлено
    send_signal_request()
    time.sleep(0.1)

    logger.info("⏹️ Запись завершена, распознаю через Yandex SpeechKit...")  # <-- исправлено
    recognized_text = ""

    try:
        with open(tmp_path, 'rb') as f:
            audio_data = f.read()

        url = "https://stt.api.cloud.yandex.net/speech/v1/stt:recognize"
        headers = {
            "Authorization": f"Api-Key {YANDEX_API_KEY}",
        }
        params = {
            "lang": "ru-RU",
            "format": "lpcm",
            "sampleRateHertz": 16000,
        }

        raw_audio = audio_data[44:]  # удаляем WAV-заголовок
        response = requests.post(url, headers=headers, params=params, data=raw_audio, timeout=10)

        if response.status_code == 200:
            result = response.json()
            recognized_text = result.get('result', '').strip()
        else:
            logger.error(f"❌ Ошибка SpeechKit: {response.status_code} - {response.text}")

    except Exception as e:
        logger.error(f"❌ Ошибка при вызове Yandex API: {e}")
    finally:
        try:
            os.unlink(tmp_path)
        except:
            pass

    if recognized_text:
        logger.info(f"🔊 Распознанный текст: {recognized_text}")  # <-- исправлено
        send_speak_request(recognized_text)
    else:
        logger.info("❓ Ничего не распознано")  # <-- исправлено
        send_custom_request(
            action="speak",
            player="Гостиная",
            value="текст не распознан"
        )

# ========== ПОИСК УСТРОЙСТВА ВВОДА ==========
devices = [evdev.InputDevice(path) for path in evdev.list_devices()]
remote = None
for dev in devices:
    if DEVICE_NAME in dev.name:
        remote = dev
        break
if not remote:
    for dev in devices:
        if dev.info.vendor == 0x4842 and dev.info.product == 0x0001:
            remote = dev
            logger.info(f"Найдено устройство по ID: {dev.name}")
            break
if not remote:
    logger.info("Устройство не найдено.")  # <-- можно заменить на logger.error, но оставим info
    sys.exit(1)

logger.info(f"Слушаю кнопки на {remote.path}")
logger.info("🎤 Готов. Нажмите и ОТПУСТИТЕ кнопку микрофона.")
if ENABLE_START_BEEP:
    logger.info(f"После нажатия кнопки → сигнал → пауза {START_DELAY} сек → говорите {RECORD_SECONDS} секунд.")
else:
    logger.info(f"Сразу говорите в течение {RECORD_SECONDS} секунд после нажатия.")

for event in remote.read_loop():
    if event.type == evdev.ecodes.EV_KEY and event.code == EVENT_CODE and event.value == 1:
        record_and_recognize()
        time.sleep(0.5)
