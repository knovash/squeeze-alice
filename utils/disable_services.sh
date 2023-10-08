#!/usr/bin/env bash
# sudo chmod +x *.sh
sudo systemctl stop squeeze-alice.service

sudo systemctl stop squeeze-tunnel.service
sudo systemctl stop squeeze-lt.service
sudo systemctl stop squeeze-serveo.service
sudo systemctl stop squeeze-ngrok.service

sudo systemctl disable squeeze-alice.service
sudo systemctl disable squeeze-lt.service
sudo systemctl disable squeeze-tunnel.service
sudo systemctl disable squeeze-serveo.service
sudo systemctl disable squeeze-ngrok.service

sudo systemctl daemon-reload
$SHELL

