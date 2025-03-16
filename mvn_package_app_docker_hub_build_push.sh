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

echo -e "\n"${BGreen}"MVN PACKAGE"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
mvn package
cp *.json target
mv target/*.jar target/app.jar


echo -e "\n"${BGreen}"FINISH MVN"${NC}"\n"

# Соберите образ локально
docker build -t knovash/sqa-server:latest .
echo -e "\n"${BGreen}"FINISH DOCKER BUILD"${NC}"\n"

# Загрузите образ:
#docker push your-username/your-image-name:tag
docker push knovash/sqa-server:latest
echo -e "\n"${BGreen}"FINISH DOCKER PUSH"${NC}"\n"

sleep 15
#$SHELL
