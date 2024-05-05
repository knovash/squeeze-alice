#!/usr/bin/env bash
# sudo chmod +x *.sh

cd /root

sudo systemctl stop squeeze-tunnel.service
sudo cp squeeze-tunnel.service /lib/systemd/system/
sudo systemctl enable squeeze-tunnel.service
sudo systemctl daemon-reload
sudo systemctl start squeeze-tunnel.service

