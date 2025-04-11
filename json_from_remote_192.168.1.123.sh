#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

file_name=$(basename "$0" .sh)
remote="${file_name##*_}" 
echo -e ${BGreen}"IP: $remote"${NC}
#remote=192.168.1.123

echo -e "copy json from "${BGreen}$remote${NC}"\n"
sshpass -p "12345" scp -r root@$remote:/opt/squeeze-alice-1.0/data/ .

echo -e ${BGreen}"OK"${NC}
sleep 5
#$SHELL
