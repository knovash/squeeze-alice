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

#sshpass -p "$password" ssh $username@$remote systemctl start squeeze-alice.service
#sshpass -p "$password" ssh $username@$remote systemctl stop squeeze-alice.service
#sshpass -p "$password" ssh $username@$remote systemctl restart squeeze-alice.service

#sshpass -p "$password" ssh $username@$remote systemctl start lyrionmusicserver.service
sshpass -p "$password" ssh $username@$remote systemctl stop lyrionmusicserver.service
#sshpass -p "$password" ssh $username@$remote systemctl restart lyrionmusicserver.service

#sshpass -p "$password" ssh $username@$remote tail -f /opt/squeeze-alice-1.0/data/log.txt
#sshpass -p "$password" ssh $username@$remote systemctl daemon-reload
#sshpass -p "$password" ssh $username@$remote reboot

# подключиться
#ssh $username@$remote

echo "OK"
sleep 10




