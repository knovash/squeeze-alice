#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color
echo -e ${BGreen}"READ FROM set_remote_ip.txt"${NC}
remote=`cat set_remote_ip.txt`
echo -e ${BGreen}"REMOTE IP "$remote${NC}
echo -e "json to "${BGreen}$remote${NC}"\n"
sshpass -p "12345" scp -r /home/konstantin/IdeaProjects/squeeze-alice root@$remote:~
echo -e ${BGreen}"OK"${NC}
sleep 5
#$SHELL
