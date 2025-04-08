#!/usr/bin/env bash

file_name=$(basename "$0" .sh)
remote="${file_name##*_}" 
echo -e ${BGreen}"IP: $remote"${NC}
#remote=192.168.1.123

ssh root@$remote mkdir -p .ssh
sshpass -p "12345" scp ~/.ssh/id_rsa.pub root@$remote:.ssh/authorized_keys

echo "OK"
sleep 10
#$SHELL
