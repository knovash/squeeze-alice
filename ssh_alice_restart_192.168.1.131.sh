#!/usr/bin/env bash
# sudo chmod +x *.sh
CURRENT_PATH="$(dirname "$0")"
source "$CURRENT_PATH/common.sh"
file_name=$(basename "$0" .sh)
default_remote="${file_name##*_}"
read_ssh_params "$default_remote"
#----------------------------------------------------------


sshpass -p "$password" ssh $username@$remote systemctl enable squeeze-alice.service
sshpass -p "$password" ssh $username@$remote sudo systemctl daemon-reload
sshpass -p "$password" ssh $username@$remote systemctl restart squeeze-alice.service

sshpass -p "$password" ssh $username@$remote tail -f /opt/squeeze-alice-1.0/data/log.txt

echo "OK"
sleep 10




