#!/usr/bin/env bash
# sudo chmod +x *.sh

cd /root

sudo systemctl stop squeeze-alice.service
sudo cp squeeze-alice.service /lib/systemd/system/
sudo systemctl enable squeeze-alice.service
sudo systemctl daemon-reload
sudo systemctl start squeeze-alice.service

