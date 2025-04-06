#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

export JAVA_HOME=~/.jdks/corretto-18.0.2
export PATH=$JAVA_HOME/bin:$PATH

echo -e ${BGreen}"READ FROM set_remote_ip.txt"${NC}
printf '%s\n' "DIR: ${PWD##*/}"
remote=`cat set_remote_ip.txt`
echo -e ${BGreen}"REMOTE IP "$remote${NC}

echo -e "\n"${BGreen}"MVN PACKAGE > TAR > UPLOAD > INSTALL"${NC}"\n"

echo -e "\n"${BGreen}"CLEAR TARGET DIR"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
rm -r target

cp *.json target

echo -e "\n"${BGreen}"MVN PACKAGE"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
mvn package

echo -e "\n"${BGreen}"TAR"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
sh tar.sh

echo -e "\n"${BGreen}"COPY TAR TO REMOTE "$remote${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
sshpass -p "12345" scp *.gz root@$remote:/root/

echo -e "\n"${BGreen}"UNTAR to root"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
ssh root@$remote sudo tar xzvf squeeze-alice-pak.tar.gz -C /root

echo -e "\n"${BGreen}"RUN INSTALL ALICE"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
ssh root@$remote sh install_alice.sh

echo -e "\n"${BGreen}"FINISH"${NC}"\n"
sleep 15
#$SHELL
