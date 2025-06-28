#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color
echo -e ${BGreen}"START INSTALL"${NC}

file_name=$(basename "$0" .sh)
remote="${file_name##*_}" 
echo -e ${BGreen}"INSTALL TO IP: $remote"${NC}

# Запрашиваем пользователя
read -p "Enter SSH username. Press Enter for root: " username
#read -p "Введите SSH пользователя. Нажмите Enter если root: " username
username=${username:-root}
# Запрашиваем пароль
read -p "Enter SSH password. Press Enter for 12345: " password
#read -p "Введите SSH пароль. Нажмите Enter если 12345: " password
password=${password:-12345}
echo $username" "$password
#------------------------------------------------------------

# удаление файлов
echo -e ${BGreen}"REMOVE /opt/squeeze-alice-1.0"${NC}
sshpass -p "$password" ssh "$username@$remote" rm -r /opt/squeeze-alice-1.0
echo -e ${BGreen}"REMOVE /lib/systemd/system/squeeze-alice.service"${NC}
sshpass -p "$password" ssh "$username@$remote" rm /lib/systemd/system/squeeze-alice.service
# остановка сервиса
echo -e ${BGreen}"SYSTEMCTL stop squeeze-alice.service"${NC}
sshpass -p "$password" ssh "$username@$remote" systemctl stop squeeze-alice.service
echo -e ${BGreen}"SYSTEMCTL disable squeeze-alice.service"${NC}
sshpass -p "$password" ssh "$username@$remote" systemctl disable squeeze-alice.service
echo -e ${BGreen}"SYSTEMCTL stop, disable, daemon-reload"${NC}
sshpass -p "$password" ssh "$username@$remote" systemctl daemon-reload

echo -e ${BGreen}"\nUNINSTALL FINISHED"${NC}

sleep 30
#$SHELL
