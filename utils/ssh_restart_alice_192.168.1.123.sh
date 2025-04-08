#!/usr/bin/env bash
# sudo chmod +x *.sh

file_name=$(basename "$0" .sh)
remote="${file_name##*_}" 
echo -e ${BGreen}"IP: $remote"${NC}
#remote=192.168.1.123

#ssh root@$remote systemctl daemon-reload
ssh root@$remote systemctl restart squeeze-alice.service

sleep 10
