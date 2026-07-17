#!/usr/bin/env bash
# sudo chmod +x *.sh
CURRENT_PATH="$(dirname "$0")"
source "$CURRENT_PATH/common.sh"
file_name=$(basename "$0" .sh)
default_remote="${file_name##*_}"
read_ssh_params "$default_remote"
#----------------------------------------------------------


echo -e ${BGreen}"MVN PACKAGE"${NC}
rm -r target
mvn package


# остановить сервис
sshpass -p "$password" ssh $username@$remote sudo systemctl stop squeeze-alice.service

# ЧИСТАЯ УСТАНОВКА !!! закоментировать чтоб сохранялись настройки
#echo -e ${BGreen}"CLEAR INSTALL !!! REMOVE DIR /opt/squeeze-alice-1.0"${NC}
#sshpass -p "$password" ssh "$username@$remote" "rm -r /opt/squeeze-alice-1.0"

# создать папку на ремоут
echo -e ${BGreen}"CREATE DIR /opt/squeeze-alice-1.0"${NC}
sshpass -p "$password" ssh "$username@$remote" "mkdir -p /opt/squeeze-alice-1.0"

# копирование приложения на ремоут
echo -e ${BGreen}"COPY JAR /opt/squeeze-alice-1.0/squeeze-alice-1.0.jar"${NC}
sshpass -p "$password" rsync -avh --progress target/squeeze-alice-1.0.jar $username@$remote:/opt/squeeze-alice-1.0/
echo -e ${BGreen}"COPY SCRIPT log.sh"${NC}
sshpass -p "$password" rsync -avh --progress log.sh $username@$remote:~/
echo -e ${BGreen}"COPY SERVICE /lib/systemd/system/squeeze-alice.service"${NC}
sshpass -p "$password" rsync -avh --progress squeeze-alice.service $username@$remote:/lib/systemd/system/

# удалить лог на ремоут
echo -e ${BGreen}"DELETE LOG /opt/squeeze-alice-1.0/data/log.txt"${NC}
sshpass -p "$password" ssh "$username@$remote" "rm /opt/squeeze-alice-1.0/data/log.txt"

# проверка файлов
#echo -e ${BGreen}"\n/opt/squeeze-alice-1.0/"${NC}
#sshpass -p "$password" ssh "$username@$remote" "ls /opt/squeeze-alice-1.0/"
#echo -e ${BGreen}"/lib/systemd/system/"${NC}
#sshpass -p "$password" ssh "$username@$remote" "ls /lib/systemd/system/sq*.service"

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

sleep 60
#$SHELL
