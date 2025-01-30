#!/usr/bin/env bash
# sudo chmod +x *.sh

echo ""
echo "START INSTALL VPN"

cd /root
sudo systemctl stop squeeze-vpn.service
sudo cp squeeze-vpn.service /lib/systemd/system/
sudo systemctl enable squeeze-vpn.service
sudo systemctl daemon-reload
sudo systemctl start squeeze-vpn.service

echo "FINISH INSTALL VPN"
