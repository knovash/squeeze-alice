#!/usr/bin/env bash
# sudo chmod +x *.sh
BGreen='\033[1;32m'
NC='\033[0m' # No Color
echo -e ${BGreen}"UNINSTAL"${NC}

echo -e ${BGreen}"DEL /opt/squeeze-alice-1.0/"${NC}
sudo rm -r /opt/squeeze-alice-1.0

echo -e ${BGreen}"DEL /lib/systemd/system/squeeze-alice.service"${NC}
sudo rm /lib/systemd/system/squeeze-alice.service

echo -e ${BGreen}"SYSTEMCTL STOP DISABLE, RELOAD"${NC}
sudo systemctl stop squeeze-alice.service
sudo systemctl disable squeeze-alice.service
sudo systemctl daemon-reload

echo -e ${BGreen}"FINISH"${NC}
