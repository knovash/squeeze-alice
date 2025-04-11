#!/usr/bin/env bash
sudo mkdir -p /opt/squeeze-alice-1.0
sudo cp squeeze-alice-1.0.jar /opt/squeeze-alice-1.0/
sudo cp squeeze-alice.service /lib/systemd/system/
sudo systemctl enable squeeze-alice.service
sudo systemctl daemon-reload
sudo systemctl restart squeeze-alice.service
sudo systemctl status squeeze-alice.service
tail -f /opt/squeeze-alice-1.0/data/log.txt
