#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

#export JAVA_HOME=~/.jdks/corretto-18.0.2
#export PATH=$JAVA_HOME/bin:$PATH


# Соберите образ локально
docker build -f dockerfile_arm --no-cache -t knovash/squeeze-alice-local-arm:latest . 2>&1 | tee build.log
#docker build -f dockerfile_arm --platform linux/arm/v7 --no-cache -t knovash/squeeze-alice-local-arm:latest . 2>&1 | tee build.log
echo -e "\n"${BGreen}"FINISH DOCKER BUILD"${NC}"\n"

# Загрузите образ:
docker push knovash/squeeze-alice-local-arm:latest
echo -e "\n"${BGreen}"FINISH DOCKER PUSH"${NC}"\n"

#sleep 5
$SHELL
