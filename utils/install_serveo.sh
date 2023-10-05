#!/usr/bin/env bash
# sudo chmod +x *.sh
sudo systemctl stop squeeze-serveo.service
cd /root
sudo cp squeeze-serveo.service /lib/systemd/system/
sudo systemctl enable squeeze-serveo.service
sudo systemctl daemon-reload
sudo systemctl start squeeze-serveo.service
$SHELL

