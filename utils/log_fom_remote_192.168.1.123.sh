#!/usr/bin/env bash

file_name=$(basename "$0" .sh)
remote="${file_name##*_}" 
echo -e ${BGreen}"IP: $remote"${NC}
#remote=192.168.1.123

echo "LOG FROM REMOTE "$ip

sshpass -p "12345" scp root@$remote:/opt/squeeze-alice-1.0/data/log.txt log.txt

sleep 20
#$SHELL
