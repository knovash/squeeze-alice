#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

file_name=$(basename "$0" .sh)
remote="${file_name##*_}" 
echo -e ${BGreen}"SSH"${NC}

# Запрашиваем ip
echo -e "Enter SSH ip. ${BGreen}Press Enter for [$remote]"${NC}
read -p "" remote
remote=${remote:-192.168.1.131}

# Запрашиваем пользователя
echo -e "Enter SSH username. ${BGreen}Press Enter for [root]:${NC} "
read -p "" username
username=${username:-root}

# Запрашиваем пароль
echo -e "Enter SSH password. ${BGreen}Press Enter for [12345]:${NC} "
read -p "" password
password=${password:-12345}

echo $remote" "$username" "$password

#------------------------------------------------------------

sshpass -p "$password" ssh $username@$remote reboot

echo "OK"
sleep 10




