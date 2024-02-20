#!/usr/bin/env bash
BGreen='\033[1;32m'
NC='\033[0m' # No Color


echo -e "favorites to "${BGreen}192.168.1.123${NC}"\n"
ssh root@192.168.1.123 mkdir -p .ssh
sshpass -p "12345" scp /home/konstantin/.ssh/id_rsa.pub root@192.168.1.123:.ssh/authorized_keys
$SHELL
