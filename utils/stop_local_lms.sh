#!/usr/bin/env bash

BGreen='\033[1;32m'
NC='\033[0m' # No Color
#echo -e "branch "${BGreen}$branch${NC}"\n"
echo -e ${BGreen}"Stop logitech media server service"${NC}"\n"

sudo service logitechmediaserver stop
sudo systemctl disable logitechmediaserver.service
sudo systemctl daemon-reload

$SHELL
