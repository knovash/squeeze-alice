#!/usr/bin/env bash

BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e "\n"${BGreen}"MVN PACKAGE > TAR > UPLOAD > INSTALL"${NC}"\n"

echo -e "\nrun "${BGreen}"CLEAR TARGET DIR"${NC}"\n"
rm -r target

echo -e "\nrun "${BGreen}"MVN PACKAGE"${NC}"\n"
mvn package

echo -e "\nrun "${BGreen}"TAR"${NC}"\n"
sh tar.sh

cd squeeze-alice-pak
echo -e "\nrun "${BGreen}"COPY TO 192.168.1.123"${NC}"\n"
sshpass -p "12345" scp * root@192.168.1.123:/root/

echo -e "\nrun "${BGreen}"INSTALL ALICE"${NC}"\n"
ssh root@192.168.1.123 sh install_alice.sh

echo -e "\nrun "${BGreen}"OPEN SSH TERMINAL"{NC}"\n"
ssh root@192.168.1.123

$SHELL
