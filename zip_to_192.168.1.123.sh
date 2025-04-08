#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color
echo -e ${BGreen}"START"${NC}

file_name=$(basename "$0" .sh)
remote="${file_name##*_}" 
echo -e ${BGreen}"IP: $remote"${NC}

echo -e ${BGreen}"COPY zip TO remote home"${NC}
#sshpass -p "12345" scp -r target root@$remote:/opt/squeeze-alice-1.0/
sshpass -p "12345" rsync -avh --progress *.gz root@$remote:~/



echo -e ${BGreen}"FINISH"${NC}


echo "1. Базовая команда:"
echo "tar -xzvf squeeze-alice-local.tar.gz"
echo "2. С указанием директории для распаковки:"
echo "tar -xzvf squeeze-alice-local.tar.gz -C /путь/к/папке"


sleep 15
#$SHELL
