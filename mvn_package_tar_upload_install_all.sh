#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e ${BGreen}"READ FROM set_remote_ip.txt"${NC}
printf '%s\n' "DIR: ${PWD##*/}"

remote_ip=$(cat bash_config.json | jq -r '.remote_ip')
#remote=`cat set_remote_ip.txt`
echo -e ${BGreen}"REMOTE IP "$remote_ip${NC}

ssh root@$remote_ip mkdir -p .ssh
sshpass -p "12345" scp /home/konstantin/.ssh/id_rsa.pub root@$remote_ip:.ssh/authorized_keys

echo -e "\n"${BGreen}"MVN PACKAGE > TAR > UPLOAD > INSTALL"${NC}"\n"

echo -e "\n"${BGreen}"CLEAR TARGET DIR"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
rm -r target

echo -e "\n"${BGreen}"MVN PACKAGE"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
mvn package

echo -e "\n"${BGreen}"TAR"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
sh tar.sh

echo -e "\n"${BGreen}"COPY TAR TO REMOTE "$remote_ip${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
sshpass -p "12345" scp *.gz root@$remote_ip:/root/

echo -e "\n"${BGreen}"REMOTE UNTAR to root"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
ssh root@$remote_ip sudo tar xzvf squeeze-alice-pak.tar.gz -C /root

echo -e "\n"${BGreen}"REMOTE RUN INSTALL ALL"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
ssh root@$remote_ip sh install_all.sh

echo -e "\n"${BGreen}"FINISH"${NC}"\n"
sleep 30
#$SHELL
