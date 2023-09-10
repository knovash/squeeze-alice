#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color
#echo -e "branch "${BGreen}$branch${NC}"\n"
echo -e ${BGreen}"skip install npm and localtunnel"${NC}"\n"
#sudo apt install npm
#sudo npm install -g localtunnel

echo -e ${BGreen}"install squeeze-alice"${NC}"\n"
sudo tar xzvf squeeze-alice-1.0.tar.gz -C /opt/

echo -e ${BGreen}"install squeeze-tunnel.service"${NC}"\n"
sudo cp squeeze-tunnel.service /lib/systemd/system/
sudo systemctl enable squeeze-tunnel.service
sudo systemctl daemon-reload
sudo systemctl start squeeze-tunnel.service

echo -e ${BGreen}"install squeeze-tunnel.service"${NC}"\n"
#sudo cp squeeze-alice.service /lib/systemd/system/
#sudo systemctl enable squeeze-alice.service
#sudo systemctl daemon-reload
#sudo systemctl start squeeze-alice.service

$SHELL

