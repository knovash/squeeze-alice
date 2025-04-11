#!/usr/bin/env bash
sudo rm -r /opt/squeeze-alice-1.0
sudo rm /lib/systemd/system/squeeze-alice.service
sudo systemctl stop squeeze-alice.service
sudo systemctl disable squeeze-alice.service
sudo systemctl daemon-reload
