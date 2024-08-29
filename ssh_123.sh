#!/usr/bin/env bash
BGreen='\033[1;32m'
NC='\033[0m' # No Color

#echo -e ${BGreen}"READ FROM set_remote_ip.txt"${NC}
#remote=`cat set_remote_ip.txt`
#echo -e ${BGreen}"REMOTE IP "$remote${NC}


remote_ip=$(cat bash_config.json | jq -r '.remote_ip')
#remote=`cat set_remote_ip.txt`
echo -e ${BGreen}"REMOTE IP "$remote_ip${NC}

ssh root@$remote_ip

#$SHELL
