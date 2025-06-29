#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

file_name=$(basename "$0" .sh)
remote="${file_name##*_}" 
echo -e ${BGreen}"IP: $remote"${NC}


mkdir -p data/bak

echo -e "copy /opt/squeeze-alice-1.0/data from remote"
sshpass -p "12345" scp -r root@$remote:/opt/squeeze-alice-1.0/data data/bak/

echo -e ${BGreen}"FINISHED"${NC}
sleep 20
#$SHELL
