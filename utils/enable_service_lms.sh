#!/usr/bin/env bash

BGreen='\033[1;32m'
NC='\033[0m' # No Color
#echo -e "branch "${BGreen}$branch${NC}"\n"
echo -e ${BGreen}"Enable logitech media server service"${NC}"\n"

sudo systemctl enable logitechmediaserver.service
sudo systemctl daemon-reload
sudo systemctl start logitechmediaserver.service

$SHELL

