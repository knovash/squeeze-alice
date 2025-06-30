#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

#file_name=$(basename "$0" .sh)
#remote="${file_name##*_}" 
#echo -e ${BGreen}"INSTALL TO IP: $remote"${NC}

# Запрашиваем ip
read -p "Enter ip. Press Enter for 192.168.1.123: " remote
remote=${remote:-192.168.1.123}
# Запрашиваем пользователя
read -p "Enter SSH username. Press Enter for root: " username
username=${username:-root}
# Запрашиваем пароль
read -p "Enter SSH password. Press Enter for 12345: " password
password=${password:-12345}

echo -e ${BGreen}"\nINSTALL TO: $username@$remote $password"${NC}
#------------------------------------------------------------

# создать папку на ремоут
echo -e ${BGreen}"\nCREATE DIR $remote/opt/squeeze-alice-1.0"${NC}
sshpass -p "$password" ssh "$username@$remote" "mkdir -p /opt/squeeze-alice-1.0"

# копирование приложения на ремоут
echo -e ${BGreen}"\nCOPY JAR $remote/opt/squeeze-alice-1.0/squeeze-alice-1.0.jar"${NC}
sshpass -p "$password" rsync -avh --progress bin/squeeze-alice-1.0.jar $username@$remote:/opt/squeeze-alice-1.0/
echo -e ${BGreen}"COPY SCRIPT $remote~/log.sh"${NC}
sshpass -p "$password" rsync -avh --progress log.sh $username@$remote:~/
echo -e ${BGreen}"COPY SERVICE $remote/lib/systemd/system/squeeze-alice.service"${NC}
sshpass -p "$password" rsync -avh --progress squeeze-alice.service $username@$remote:/lib/systemd/system/

# проверка файлов
echo -e ${BGreen}"\n$remote/opt/squeeze-alice-1.0/"${NC}
sshpass -p "$password" ssh "$username@$remote" "ls /opt/squeeze-alice-1.0/"
echo -e ${BGreen}"$remote/lib/systemd/system/"${NC}
sshpass -p "$password" ssh "$username@$remote" "ls /lib/systemd/system/sq*.service"

# запуск сервиса
echo -e ${BGreen}"\nSYSTEMCTL enable, daemon-reload, restart squeeze-alice.service"${NC}
sshpass -p "$password" ssh $username@$remote sudo systemctl enable squeeze-alice.service
sshpass -p "$password" ssh $username@$remote sudo systemctl daemon-reload
sshpass -p "$password" ssh $username@$remote sudo systemctl restart squeeze-alice.service

echo -e ${BGreen}"\nSERVER STARTED http://"$remote":8010"${NC}

# статус сервиса
#sleep 1
echo -e ${BGreen}"\nSTATUS"${NC}
sshpass -p "$password" ssh $username@$remote sudo systemctl status squeeze-alice.service

# логи сервиса
sleep 5
echo -e ${BGreen}"\nLOG"${NC}
sshpass -p "$password" ssh $username@$remote tail -f /opt/squeeze-alice-1.0/data/log.txt

sleep 120
#$SHELL
