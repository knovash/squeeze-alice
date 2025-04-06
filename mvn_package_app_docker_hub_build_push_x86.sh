#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

export JAVA_HOME=~/.jdks/corretto-18.0.2
export PATH=$JAVA_HOME/bin:$PATH

echo -e "\n"${BGreen}"MVN PACKAGE"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
rm -r target
mvn package
#cp *.json target
mv target/*.jar target/app.jar
echo -e "\n"${BGreen}"FINISH MVN"${NC}"\n"

# Соберите образ локально
#docker build --no-cache -t knovash/squeeze-alice:latest .
docker build -f dockerfile_x86 --no-cache -t knovash/squeeze-alice-local-x86:latest . 2>&1 | tee build.log
echo -e "\n"${BGreen}"FINISH DOCKER BUILD"${NC}"\n"

# Загрузите образ:
#docker push your-username/your-image-name:tag
docker push knovash/squeeze-alice-local-x86:latest
echo -e "\n"${BGreen}"FINISH DOCKER PUSH"${NC}"\n"

#sleep 5
$SHELL
