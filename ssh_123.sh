#!/usr/bin/env bash
BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e ${BGreen}"READ FROM set_remote_ip.txt"${NC}
remote=`cat set_remote_ip.txt`
echo -e ${BGreen}"REMOTE IP "$remote${NC}

ssh root@$remote

$SHELL
