#!/usr/bin/env bash
# sudo chmod +x *.sh
CURRENT_PATH="$(dirname "$0")"
source "$CURRENT_PATH/common.sh"
file_name=$(basename "$0" .sh)
default_remote="${file_name##*_}"
read_ssh_params "$default_remote"
#----------------------------------------------------------

echo "COPY TO REMOTE"
echo "WAIT..."

# BT REMOTE
sshpass -p "$password" scp btremote.py config.conf btremote.service install_service_btremote.sh "$username@$remote:/root/"

# VOICE SERVICE
sshpass -p "$password" scp voice.py voice.service install_service_voice.sh "$username@$remote:/root/"


sshpass -p "$password" scp first_run.sh install.sh log.sh "$username@$remote:/root/"

echo "COPY TO REMOTE FINISHED"
echo "CONNECT TO REMOTE"
echo "RUN sh install.sh for all services"
echo "RUN sh install_service_btremote.sh for only bt remote"
echo "RUN sh install_service_voice.sh for only voice search"
echo "RUN sh log.sh for log"
# CONNECT
sshpass -p "$password" ssh "$username@$remote"

echo "OK"
sleep 10
# $SHELL
