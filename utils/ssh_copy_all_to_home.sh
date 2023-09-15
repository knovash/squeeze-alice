#!/usr/bin/env bash

BGreen='\033[1;32m'
NC='\033[0m' # No Color
#echo -e "branch "${BGreen}$branch${NC}"\n"
echo -e ${BGreen}"Copy * to root@192.168.1.52:/home/\nwait..."${NC}"\n"

sshpass -p "12345" scp * root@192.168.1.52:/home/
ssh root@192.168.1.52 /home/./install.sh

BGreen='\033[1;32m'
NC='\033[0m' # No Color
#echo -e "branch "${BGreen}$branch${NC}"\n"
echo -e ${BGreen}"end"${NC}"\n"

$SHELL
