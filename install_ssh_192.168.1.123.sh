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

echo -e ${BGreen}"MKDIR /opt/squeeze-alice-1.0"${NC}
sshpass -p "$password" ssh "$username@$remote" "mkdir -p /opt/squeeze-alice-1.0"

echo -e ${BGreen}"RM /opt/squeeze-alice-1.0/data"${NC}
#sshpass -p "$password" ssh "$username@$remote" "rm -r /opt/squeeze-alice-1.0/data"

# копирование файлов
echo -e ${BGreen}"COPY JAR squeeze-alice-1.0.jar TO /opt/squeeze-alice-1.0/"${NC}
sshpass -p "$password" rsync -avh --progress        squeeze-alice-1.0.jar root@$remote:/opt/squeeze-alice-1.0/ 
#sshpass -p "12345" scp target/*.jar root@$remote::/opt/squeeze-alice-1.0/
#sshpass -p "$password" rsync -avh --progress target/*.jar root@$remote:/opt/squeeze-alice-1.0/
#[ -f "squeeze-alice-1.0.jar" ] && \
#sshpass -p "$password" rsync -avh --progress        squeeze-alice-1.0.jar root@$remote:/opt/squeeze-alice-1.0/ || \
#sshpass -p "$password" rsync -avh --progress target/squeeze-alice-1.0.jar root@$remote:/opt/squeeze-alice-1.0/
echo -e ${BGreen}"COPY SERVICE squeeze-alice.service TO /lib/systemd/system/"${NC}
sshpass -p "$password" rsync -avh --progress       squeeze-alice.service root@$remote:/lib/systemd/system/
#sshpass -p "$password" rsync -avh --progress utils/squeeze-alice.service root@$remote:/lib/systemd/system/
#[ -f "squeeze-alice.service" ] && \
#sshpass -p "$password" rsync -avh --progress       squeeze-alice.service root@$remote:/lib/systemd/system/ || \
#sshpass -p "$password" rsync -avh --progress utils/squeeze-alice.service root@$remote:/lib/systemd/system/

# проверка файлов
echo -e ${BGreen}"CHECK LS /opt/squeeze-alice-1.0/"${NC}
sshpass -p "$password" ssh "$username@$remote" "ls /opt/squeeze-alice-1.0/"
echo -e ${BGreen}"CHECK LS /lib/systemd/system/"${NC}
sshpass -p "$password" ssh "$username@$remote" "ls /lib/systemd/system/sq*.service"

# запуск сервиса
echo -e ${BGreen}"\nSYSTEMCTL enable, daemon-reload, restart squeeze-alice.service"${NC}
sshpass -p "$password" ssh root@$remote sudo systemctl enable squeeze-alice.service
sshpass -p "$password" ssh root@$remote sudo systemctl daemon-reload
sshpass -p "$password" ssh root@$remote sudo systemctl restart squeeze-alice.service

echo -e ${BGreen}"\nSERVER STARTED http://"$remote":8010"${NC}

# статус сервиса
sleep 1
echo -e ${BGreen}"\nSTATUS"${NC}
sshpass -p "$password" ssh root@$remote sudo systemctl status squeeze-alice.service

# логи сервиса
sleep 5
echo -e ${BGreen}"\nLOG"${NC}
sshpass -p "$password" ssh root@$remote tail -f /opt/squeeze-alice-1.0/data/log.txt

#sleep 60
$SHELL
