#!/usr/bin/env bash
BGreen='\033[1;32m'
NC='\033[0m' # No Color
name=$(basename "$0")
name=${name/.sh/}
name=${name/json_from_/}
echo -e "copy json from "${BGreen}$name${NC}"\n"
sshpass -p "12345" scp root@$name:/opt/squeeze-alice-1.0/*.json ./
#$SHELL
