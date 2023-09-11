#!/usr/bin/env bash

BGreen='\033[1;32m'
NC='\033[0m' # No Color
#echo -e "branch "${BGreen}$branch${NC}"\n"
echo -e ${BGreen}"Run squeezelite players"${NC}"\n"

~/Downloads/./squeezelite-x86-64 -n "Mi Box" -m cc:cc:be:b5:90:66 &
~/Downloads/./squeezelite-x86-64 -n "Bathroom" -m cc:cc:31:b3:64:ab &
~/Downloads/./squeezelite-x86-64 -n "HomePod" -m aa:aa:c2:7c:e0:12 &
~/Downloads/./squeezelite-x86-64 -n "ggmm" -m bb:bb:6c:b8:26:52 &

BGreen='\033[1;32m'
NC='\033[0m' # No Color
#echo -e "branch "${BGreen}$branch${NC}"\n"
echo -e ${BGreen}"Run logitech media server service"${NC}"\n"

#sudo systemctl enable logitechmediaserver.service
#sudo systemctl daemon-reload
sudo systemctl start logitechmediaserver.service

$SHELL

