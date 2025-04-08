#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e "\n"${BGreen}"MVN PACKAGE"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
rm -r target
mvn package
echo -e "\n"${BGreen}"FINISH MVN"${NC}"\n"

# Соберите образ локально
docker build -f dockerfile_arm --no-cache -t knovash/squeeze-alice-local-arm:latest . 2>&1 | tee build.log
echo -e "\n"${BGreen}"FINISH DOCKER BUILD"${NC}"\n"

# Загрузите образ:
docker push knovash/squeeze-alice-local-arm:latest
echo -e "\n"${BGreen}"FINISH DOCKER PUSH"${NC}"\n"

sleep 15
#$SHELL
