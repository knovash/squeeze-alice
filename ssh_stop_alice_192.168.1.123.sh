#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

file_name=$(basename "$0" .sh)
remote="${file_name##*_}" 
echo -e ${BGreen}"IP: $remote"${NC}
#remote=192.168.1.123

#ssh root@$remote systemctl daemon-reload

echo -e ${BGreen}"STOP"${NC}
ssh root@$remote systemctl stop squeeze-alice.service
echo -e ${BGreen}"STATUS"${NC}
ssh root@$remote systemctl status squeeze-alice.service

sleep 20
