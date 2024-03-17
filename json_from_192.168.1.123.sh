#!/usr/bin/env bash
# sudo chmod +x *.sh

echo -e ${BGreen}"READ FROM set_remote_ip.txt"${NC}
remote=`cat set_remote_ip.txt`
echo -e ${BGreen}"REMOTE IP "$remote${NC}
# $remote

#name=$(basename "$0")
#name=${name/.sh/}
#name=${name/json_from_/}

echo -e "copy json from "${BGreen}$remote${NC}"\n"
sshpass -p "12345" scp root@$remote:/opt/squeeze-alice-1.0/*.json ./
#$SHELL
