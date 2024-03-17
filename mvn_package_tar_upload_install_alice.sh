#!/usr/bin/env bash
# sudo chmod +x *.sh

BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e ${BGreen}"READ FROM set_remote_ip.txt"${NC}
remote=`cat set_remote_ip.txt`
echo -e ${BGreen}"REMOTE IP "$remote${NC}
# $remote

echo -e "\n"${BGreen}"MVN PACKAGE > TAR > UPLOAD > INSTALL"${NC}"\n"

echo -e "\n"${BGreen}"CLEAR TARGET DIR"${NC}"\n"
rm -r target

echo -e "\n"${BGreen}"MVN PACKAGE"${NC}"\n"
mvn package

echo -e "\n"${BGreen}"TAR"${NC}"\n"
sh tar.sh

cd squeeze-alice-pak
echo -e "\n"${BGreen}"COPY TAR TO "$remote${NC}"\n"
sshpass -p "12345" scp * root@$remote:/root/

echo -e "\n"${BGreen}"INSTALL ALICE"${NC}"\n"
ssh root@$remote sh install_alice.sh

echo -e "\n"${BGreen}"OPEN SSH TERMINAL"${NC}"\n"
ssh root@$remote

$SHELL
