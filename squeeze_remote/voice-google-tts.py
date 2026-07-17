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
import logging
from logging.handlers import RotatingFileHandler
import json

# ---------- ЗАГРУЗКА КОНФИГУРАЦИИ ----------
CONFIG_FILE = os.path.join(os.path.dirname(__file__), 'config.conf')
try:
    with open(CONFIG_FILE, 'r') as f:
        settings = json.load(f)
except Exception as e:
    print(f"ОШИБКА: не удалось загрузить {CONFIG_FILE}: {e}")
    sys.exit(1)

if 'server' not in settings:
    print("ОШИБКА: в config.conf отсутствует ключ 'server'")
    sys.exit(1)
if 'voice' not in settings:
    print("ОШИБКА: в config.conf отсутствует секция 'voice'")
    sys.exit(1)
if 'google' not in settings:
    print("ОШИБКА: в config.conf отсутствует секция 'google'")
    sys.exit(1)

SERVER = settings['server'].rstrip('/')
SPEAK_URL = f"{SERVER}/cmd"
voice = settings['voice']
google_conf = settings['google']

# Параметры голоса
EVENT_CODE = voice.get('event_code', 582)
DEVICE_NAME = voice.get('device_name', 'HAOBO Technology USB Composite Device Keyboard')
RECORD_SECONDS = voice.get('record_seconds', 4)
ENABLE_START_BEEP = voice.get('enable_start_beep', True)
START_DELAY = voice.get('start_delay', 1)
SIGNAL_PLAYER = voice.get('signal_player', 'btremote')
SIGNAL_VALUE = voice.get('signal_value', 'beep2')

# Параметры Google
CREDENTIALS_FILE = google_conf.get('credentials_file')
PROJECT_ID = google_conf.get('project_id', '')
ALTERNATIVE_LANGUAGES = google_conf.get('alternative_language_codes', [])
PHRASES = google_conf.get('phrases', [])
BOOST = google_conf.get('boost', 10.0)

# Запасной путь из основной секции (если есть)
BTDEV_FALLBACK = settings.get('btdevice')

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

# ---------- ИНИЦИАЛИЗАЦИЯ GOOGLE SPEECH-TO-TEXT ----------
from google.cloud import speech_v1p1beta1 as speech
import os
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = CREDENTIALS_FILE
try:
    speech_client = speech.SpeechClient()
except Exception as e:
    logger.error(f"❌ Не удалось инициализировать Google Speech клиент: {e}")
    sys.exit(1)

# Подготовка адаптации модели (если есть фразы)
adaptation = None
if PHRASES:
    try:
        # Создаём набор фраз (PhraseSet) прямо в запросе, не сохраняя в облаке
        phrase_set = speech.PhraseSet(
            phrases=[{"value": p} for p in PHRASES],
            boost=BOOST
        )
        adaptation = speech.SpeechAdaptation(
            phrase_sets=[phrase_set]
        )
        logger.info(f"✅ Адаптация модели включена с фразами: {PHRASES}")
    except Exception as e:
        logger.warning(f"⚠️ Не удалось создать адаптацию: {e}")

# ---------- ФУНКЦИИ ----------

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
        logger.error(f"❌ Ошибка отправки сигнала: {e}")

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
        logger.info(f"   Полный URL: {url}")
        logger.info(f"   Текст: {text}")
        with urllib.request.urlopen(url, timeout=3) as response:
            resp = response.read().decode('utf-8')
            logger.info(f"✅ Ответ сервера: код {response.getcode()}, тело: {resp.strip()}")
    except Exception as e:
        logger.error(f"❌ Ошибка отправки запроса на озвучивание: {e}")

def send_custom_request(action, player, value):
    """Отправляет GET-запрос с произвольными параметрами."""
    try:
        params = urllib.parse.urlencode({
            'action': action,
            'player': player,
            'value': value
        })
        url = f"{SPEAK_URL}?{params}"
        logger.info(f"📤 Отправка запроса: {url}")
        with urllib.request.urlopen(url, timeout=2) as response:
            resp = response.read().decode('utf-8')
            logger.info(f"✅ Ответ сервера: код {response.getcode()}, тело: {resp.strip()}")
    except Exception as e:
        logger.error(f"❌ Ошибка отправки запроса: {e}")

def record_and_recognize():
    """Запись с микрофона и распознавание через Google Speech-to-Text."""
    if ENABLE_START_BEEP:
        logger.info("🔔 Сигнал к началу речи...")
        send_signal_request()
        time.sleep(START_DELAY)

    logger.info("⚡ Запись началась, говорите...")
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
        tmp_path = tmp.name

    mic_card = get_card_index_by_name("USB Composite Device", is_capture=True)
    if mic_card is None:
        logger.error("❌ Не найден микрофон HAOBO (USB Composite Device) в arecord -l")
        return

    cmd = f"arecord -D hw:{mic_card},0 -f S16_LE -r 16000 -c 1 -d {RECORD_SECONDS} {tmp_path} 2>/dev/null"
    subprocess.run(cmd, shell=True, check=False)

    logger.info("🔔 Сигнал окончания записи")
    send_signal_request()
    time.sleep(0.1)

    logger.info("⏹️ Запись завершена, распознаю через Google Speech-to-Text...")
    recognized_text = ""

    try:
        # Чтение файла
        with open(tmp_path, 'rb') as f:
            audio_data = f.read()

        # Настройка конфигурации распознавания
        config = speech.RecognitionConfig(
            encoding=speech.RecognitionConfig.AudioEncoding.LINEAR16,
            sample_rate_hertz=16000,
            language_code="ru-RU",
            alternative_language_codes=ALTERNATIVE_LANGUAGES if ALTERNATIVE_LANGUAGES else None,
            adaptation=adaptation,
            enable_automatic_punctuation=True,
        )

        audio = speech.RecognitionAudio(content=audio_data)

        # Отправка запроса
        response = speech_client.recognize(config=config, audio=audio)

        # Извлечение текста
        if response.results:
            best_alternative = response.results[0].alternatives[0]
            recognized_text = best_alternative.transcript.strip()
            confidence = best_alternative.confidence
            logger.info(f"📊 Уверенность: {confidence:.2f}")
        else:
            logger.warning("⚠️ Результатов распознавания нет")

    except Exception as e:
        logger.error(f"❌ Ошибка при вызове Google Speech API: {e}")
    finally:
        try:
            os.unlink(tmp_path)
        except:
            pass

    if recognized_text:
        logger.info(f"🔊 Распознанный текст: {recognized_text}")
        send_speak_request(recognized_text)
    else:
        logger.info("❓ Ничего не распознано")
        send_custom_request(action="speak", player="btremote", value="текст не распознан")

# ---------- ФУНКЦИЯ ПОИСКА УСТРОЙСТВА (общая) ----------
def find_device(name_substring, vendor=None, product=None):
    """Возвращает путь (str) к первому подходящему устройству или None."""
    devices = [evdev.InputDevice(path) for path in evdev.list_devices()]
    for dev in devices:
        if name_substring in dev.name:
            return dev.path
    if vendor is not None and product is not None:
        for dev in devices:
            if dev.info.vendor == vendor and dev.info.product == product:
                return dev.path
    return None

# ---------- ОСНОВНОЙ ЦИКЛ С ПЕРЕПОДКЛЮЧЕНИЕМ ----------
def main():
    logger.info("🎤 Запуск voice.py (Google Speech) с автоматическим переподключением")
    try:
        send_custom_request("its_alive", "btremote", "voice_started")
    except:
        pass

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
            dev_path = find_device(DEVICE_NAME, 0x4842, 0x0001)
            if dev_path is None:
                logger.error("❌ Устройство не найдено, повтор через 5 секунд")
                time.sleep(5)
                continue
            logger.info(f"✅ Найдено устройство: {dev_path}")

        try:
            remote = evdev.InputDevice(dev_path)
            logger.info(f"🔌 Устройство открыто: {remote.path} - {remote.name}")
            for event in remote.read_loop():
                if event.type == evdev.ecodes.EV_KEY and event.code == EVENT_CODE and event.value == 1:
                    record_and_recognize()
                    time.sleep(0.5)
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