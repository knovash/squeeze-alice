#!/usr/bin/env python3
from evdev import InputDevice, ecodes
import time
import sys
import json
import requests
import logging
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

# Загрузка конфигурации
try:
    logger.info('Opening config.conf')
    with open('config.conf', 'r') as json_file:
        settings = json.load(json_file)
    logger.info('Config loaded successfully')
except Exception as e:
    logger.error('Failed to load config.conf: %s', e)
    sys.exit(1)

# Проверка наличия обязательных полей
required_top = ['btdevice', 'server', 'actions']
for key in required_top:
    if key not in settings:
        logger.error('Missing required key in config: %s', key)
        sys.exit(1)

BTDEV = settings['btdevice']
TUNNEL_URL = settings['server'].rstrip('/')

# Построение карты действий из списка actions
KEY_ACTION_MAP = {}
for idx, action in enumerate(settings['actions']):
    # Проверка наличия обязательных полей в каждом действии
    if not all(k in action for k in ('code', 'description', 'command')):
        logger.error('Action #%d is missing one of required fields (code, description, command)', idx)
        sys.exit(1)

    # Получение и преобразование кода
    code_val = action['code']
    if isinstance(code_val, str) and code_val.startswith('KEY_'):
        try:
            code = ecodes.KEY[code_val]
        except KeyError:
            logger.error('Unknown key name: %s', code_val)
            sys.exit(1)
    else:
        try:
            code = int(code_val)
        except (ValueError, TypeError):
            logger.error('Invalid code value for action #%d: %s', idx, code_val)
            sys.exit(1)

    description = action['description']
    command = action['command']

    # Если команда пустая — пропускаем эту кнопку
    if not command:
        logger.debug('Skipping action for code %d (empty command)', code)
        continue

    if code in KEY_ACTION_MAP:
        logger.warning('Duplicate code %d in actions, overwriting', code)
    KEY_ACTION_MAP[code] = (description, command)

logger.info('Loaded %d actions', len(KEY_ACTION_MAP))

def send_request(action_name, query):
    """Выполнить GET-запрос к серверу с заданным параметром."""
    logger.info(action_name)
    try:
        url = f"{TUNNEL_URL}/{query.lstrip('/')}"
        logger.info("REQUEST %s", url)
        r = requests.get(url, timeout=5)
        r.raise_for_status()
        logger.info("RESPONSE %s %s", r.status_code, r.text)
    except Exception as e:
        logger.error('Request error: %s', e)

def getkey():
    logger.info('getkey started')
    remote = InputDevice(BTDEV)
    logger.info(remote)
    for event in remote.read_loop():
        if event.type == ecodes.EV_KEY and event.value == 1:
            key_name = ecodes.KEY.get(event.code, f'UNKNOWN_{event.code}')
            logger.info('Key pressed: %s (%d)', key_name, event.code)
            if event.code in KEY_ACTION_MAP:
                action_name, query = KEY_ACTION_MAP[event.code]
                send_request(action_name, query)

def main():
    # Отправка alive при старте
    try:
        send_request("its_alive", "cmd?action=its_alive")
    except Exception as e:
        logger.error("its_alive request failed: %s", e)
    while True:
        logger.info('Trying to open device: %s', BTDEV)
        try:
            remote = InputDevice(BTDEV)
            logger.info('Device opened: %s', remote)
            getkey()
        except OSError as err:
            logger.error("OS error: %s", err)
            logger.error('Device not available, retrying in 5 seconds...')
        except Exception as e:
            logger.exception("Unexpected error in main loop")
        time.sleep(5)

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        logger.info("Interrupted by user, exiting.")
        sys.exit(0)
