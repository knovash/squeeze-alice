#!/usr/bin/env bash
BGreen='\033[1;32m'
NC='\033[0m' # No Color
name=$(basename "$0")
name=${name/.sh/}
name=${name/key_to_/}
echo -e "favorites to "${BGreen}$name${NC}"\n"
ssh root@$name mkdir -p .ssh
sshpass -p "12345" scp id_rsa.pub root@$name:.ssh/authorized_keys
$SHELL
