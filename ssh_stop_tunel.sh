#!/usr/bin/env bash
# sudo chmod +x *.sh

echo -e ${BGreen}"READ FROM set_remote_ip.txt"${NC}
remote=`cat set_remote_ip.txt`
echo -e ${BGreen}"REMOTE IP "$remote${NC}
# $remote

ssh root@$remote systemctl stop squeeze-tunnel.service
