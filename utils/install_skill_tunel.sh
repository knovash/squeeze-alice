#!/usr/bin/env bash
# sudo chmod +x *.sh

sudo systemctl stop squeeze-skill-tunnel.service
sudo cp squeeze-skill-tunnel.service /lib/systemd/system/
sudo systemctl enable squeeze-skill-tunnel.service
sudo systemctl daemon-reload
sudo systemctl start squeeze-skill-tunnel.service

$SHELL

