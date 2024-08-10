#!/usr/bin/env bash

BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e "\n"${BGreen}"READ FROM set_remote_ip.txt"${NC}"\n"
remote=`cat set_remote_ip.txt`
echo -e "\n"${BGreen}"REMOTE IP "$remote${NC}"\n"

#echo -e "\n"${BGreen}"MVN PACKAGE > TAR > UPLOAD > INSTALL"${NC}"\n"

# копировать ссш ключ
echo -e "\n"${BGreen}"COPY SSH KEY ~/.ssh/id_rsa.pub TO REMOTE "$remote${NC}"\n"
ssh root@$remote mkdir -p .ssh
sshpass -p "12345" scp ~/.ssh/id_rsa.pub root@$remote:.ssh/authorized_keys

echo -e "\n"${BGreen}"CLEAR TARGET DIR"${NC}"\n"
rm -r target

echo -e "\n"${BGreen}"MVN PACKAGE"${NC}"\n"
mvn package

echo -e "\n"${BGreen}"TAR"${NC}"\n"
sh tar.sh

cd squeeze-alice-pak
echo -e "\n"${BGreen}"COPY TAR TO "$remote${NC}"\n"
sshpass -p "12345" scp * root@$remote:/root/

echo -e "\n"${BGreen}"RUN INSTALL SCRIPT ON REMOTE "$remote${NC}"\n"
ssh root@$remote sh first_run_on_remote.sh

echo -e "\n"${BGreen}"OPEN SSH TERMINAL"{NC}"\n"
ssh root@$remote

$SHELL
