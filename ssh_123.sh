#!/usr/bin/env bash
BGreen='\033[1;32m'
NC='\033[0m' # No Color
remote_ip=$(cat bash_config.json | jq -r '.remote_ip')
echo -e ${BGreen}"REMOTE IP "$remote_ip${NC}
ssh root@$remote_ip
#$SHELL
