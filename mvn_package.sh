#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

#export JAVA_HOME=~/.jdks/corretto-18.0.2
#export PATH=$JAVA_HOME/bin:$PATH

echo -e ${BGreen}"MVN PACKAGE"${NC}"\n"

echo -e ${BGreen}"CLEAR TARGET DIR"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
rm -r target

echo -e ${BGreen}"MVN PACKAGE"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"
mvn clean package
#cp *.json target
#mv target/*.jar target/app.jar


echo -e "\n"${BGreen}"FINISH"${NC}"\n"
#sleep 15
$SHELL
