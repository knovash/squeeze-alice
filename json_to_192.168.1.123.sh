#!/usr/bin/env bash
# sudo chmod +x *.sh

BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e ${BGreen}"READ FROM set_remote_ip.txt"${NC}
remote=`cat set_remote_ip.txt`
echo -e ${BGreen}"REMOTE IP "$remote${NC}
# $remote

#name=$(basename "$0")
#name=${name/.sh/}
#name=${name/json_to_/}

echo -e "json to "${BGreen}$remote${NC}"\n"
sshpass -p "12345" scp *.json root@$remote:/opt/squeeze-alice-1.0/
$SHELL
