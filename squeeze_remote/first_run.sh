# sudo chmod ugo+x install.sh
# ./install.sh

echo "FIRST RUN INSTALL"

apt update

# python3 и python3-pip – интерпретатор и менеджер пакетов Python.
# alsa-utils – утилита arecord для записи звука с микрофона (используется в voice.py).
apt install -y python3 python3-pip alsa-utils

# build-essential - компилятор gcc, утилита make.
# python3-dev - нужны для сборки расширений Python, написанных на C или Cython.
#apt install -y python3-dev build-essential

# evdev – для чтения событий с устройств ввода (кнопки пульта / клавиатуры).
# requests – для выполнения HTTP-запросов к серверу (используется в обоих скриптах).
#pip3 install evdev requests
apt install -y python3-evdev python3-requests

# Права доступа к устройствам ввода (/dev/input/event*).
#usermod -aG input <username>



# Проверка доступа к микрофону
lsusb | grep -i haobo
arecord -l


echo "OK"
sleep 10
# $SHELL
