#!/usr/bin/env bash
BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e ${BGreen}"STOP REMOTE TUNNEL"${NC}"\n"
echo -e ${BGreen}"START LOCAL TUNNEL"${NC}"\n"
echo -e ${BGreen}"FOR START REMOTE TUNNEL STOP THIS CTRL+C"${NC}"\n"
sleep 3
ssh root@192.168.1.123 systemctl stop squeeze-tunnel.service

ngrok http --domain=unicorn-neutral-badly.ngrok-free.app 8010

echo -e ${BGreen}"START REMOTE TUNNEL"${NC}"\n"
ssh root@192.168.1.123 systemctl restart squeeze-tunnel.service
$SHELL
