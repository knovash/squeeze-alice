#!/usr/bin/env bash
# sudo chmod +x *.sh

BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e "\nrun "${BGreen}"STOP TUNNEL SERVICE"${NC}"\n"
systemctl stop squeeze-tunnel.service
echo -e "\nrun "${BGreen}"STOP SERVER SERVICE"${NC}"\n"
systemctl stop squeeze-alice.service

#rm -r /opt/squeeze-alice*

cd /root

echo -e "\nrun "${BGreen}"UNTAR SERVER TO /OPT/"${NC}"\n"
sudo tar xzvf squeeze-alice*.tar.gz -C /opt/

echo -e "\nrun "${BGreen}"RESTRT SERVER SERVICE"${NC}"\n"
echo -e "\nrun "${BGreen}"RESTRT TUNNEL SERVICE"${NC}"\n"
systemctl enable squeeze-alice.service
systemctl enable squeeze-tunnel.service
systemctl daemon-reload
systemctl restart squeeze-alice.service
systemctl restart squeeze-tunnel.service
echo -e "\nrun "${BGreen}"\nINSTALL FINISH"${NC}"\n"

