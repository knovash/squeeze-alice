#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e "\nrun "${BGreen}"INSTALL alise and tunnel"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"

cd /root
printf '%s\n' "DIR: ${PWD##*/}"

sh install_alice.sh
printf '%s\n' "DIR: ${PWD##*/}"
sh install_alice_service.sh
printf '%s\n' "DIR: ${PWD##*/}"

sh install_tunnel.sh
printf '%s\n' "DIR: ${PWD##*/}"
sh install_tunnle_service.sh
printf '%s\n' "DIR: ${PWD##*/}"

#$SHELL

