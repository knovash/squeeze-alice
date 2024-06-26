#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e ${BGreen}"READ FROM set_remote_ip.txt"${NC}
remote=`cat set_remote_ip.txt`
echo -e ${BGreen}"REMOTE IP "$remote${NC}
# $remote

echo -e "\n"${BGreen}"READ FROM set_ngrok_domain.txt"${NC}"\n"
domain=`cat set_ngrok_domain.txt`
echo -e "\n"${BGreen}"NGROK DOMAIN "$domain${NC}"\n"
# $domain

echo -e ${BGreen}"STOP REMOTE TUNNEL"${NC}"\n"
echo -e ${BGreen}"START LOCAL TUNNEL"${NC}"\n"
echo -e ${BGreen}"FOR START REMOTE TUNNEL STOP THIS CTRL+C"${NC}"\n"

ssh root@$remote systemctl stop squeeze-tunnel.service
sleep 5

#ngrok http --domain=unicorn-neutral-badly.ngrok-free.app 8010
ngrok http --domain=$domain

echo -e ${BGreen}"START REMOTE TUNNEL"${NC}"\n"
ssh root@$remote systemctl restart squeeze-tunnel.service

sleep 10
#$SHELL
