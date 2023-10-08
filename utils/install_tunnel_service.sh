#!/usr/bin/env bash
# sudo chmod +x *.sh
sudo systemctl stop squeeze-tunnel.service
cd /root
sudo cp squeeze-tunnel.service /lib/systemd/system/
sudo systemctl enable squeeze-tunnel.service
sudo systemctl daemon-reload
sudo systemctl start squeeze-tunnel.service
$SHELL

