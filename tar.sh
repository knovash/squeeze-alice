#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo "\nrun "${BGreen}"TAR"${NC}"\n"
printf '%s\n' "DIR: ${PWD##*/}"

rm -r squeeze-alice-pak
mkdir squeeze-alice-pak
mkdir squeeze-alice-pak/squeeze-alice-1.0
#echo -e "\n"${BGreen}"COPY json "${NC}"\n"
#cp *.json squeeze-alice-pak/squeeze-alice-1.0/

cp -r target/* squeeze-alice-pak/squeeze-alice-1.0/
cp utils/* squeeze-alice-pak/



cd squeeze-alice-pak
printf '%s\n' "DIR: ${PWD##*/}"

echo -e "\n"${BGreen}"TAR TO squeeze-alice-1.0.tar.gz"${NC}
printf '%s\n' "DIR: ${PWD##*/}"
tar -czvf squeeze-alice-1.0.tar.gz squeeze-alice-1.0
rm -r squeeze-alice-1.0



echo "\n"${BGreen}"TAR TO squeeze-alice-pak.tar.gz"${NC}
printf '%s\n' "DIR: ${PWD##*/}"
tar -czvf squeeze-alice-pak.tar.gz *

echo "\n"${BGreen}"MOVE squeeze-alice-pak.tar.gz"${NC}
printf '%s\n' "DIR: ${PWD##*/}"
cp squeeze-alice-pak.tar.gz ..
cd ..

echo "\n"${BGreen}"REMOVE DIR squeeze-alice-pak"${NC}
printf '%s\n' "DIR: ${PWD##*/}"
rm -r squeeze-alice-pak
ls



echo -e "\n"${BGreen}"TAR FINISH"${NC}"\n"
#$SHELL
