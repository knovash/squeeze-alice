#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

file_name=$(basename "$0" .sh)
remote="${file_name##*_}" 
echo -e ${BGreen}"DATA TO IP: $remote"${NC}

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

sshpass -p "$password" rsync -avh --progress data/bak/data/*.json $username@$remote:/opt/squeeze-alice-1.0/data/

echo -e ${BGreen}"FINISHED"${NC}
sleep 20
#$SHELL
