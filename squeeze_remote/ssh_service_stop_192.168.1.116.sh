#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

file_name=$(basename "$0" .sh)
default_remote="${file_name##*_}" 
echo -e ${BGreen}"SSH"${NC}

# Запрашиваем ip
echo -e "Enter SSH ip. ${BGreen}Press Enter for [$default_remote]"${NC}
read -p "" remote
remote=${remote:-$default_remote}

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

sshpass -p "$password" ssh $username@$remote systemctl stop btremote.service
sshpass -p "$password" ssh $username@$remote systemctl stop voice.service


echo "OK"
sleep 10




