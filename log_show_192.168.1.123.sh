#!/usr/bin/env bash

file_name=$(basename "$0" .sh)
remote="${file_name##*_}" 
echo -e ${BGreen}"IP: $remote"${NC}
#remote=192.168.1.123
#remote_ip=$(cat bash_config.json | jq -r '.remote_ip')

ssh root@$remote tail -f /opt/squeeze-alice-1.0/data/log.txt


#sshpass -p "$password" ssh root@$remote tail -f /opt/squeeze-alice-1.0/data/log.txt

