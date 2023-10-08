#!/usr/bin/env bash
# sudo chmod +x *.sh
sudo systemctl stop squeeze-ngrok.service
#cd /root
sudo cp squeeze-ngrok.service /lib/systemd/system/
sudo systemctl enable squeeze-ngrok.service
sudo systemctl daemon-reload
sudo systemctl start squeeze-ngrok.service
$SHELL

