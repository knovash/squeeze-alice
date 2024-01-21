#!/usr/bin/env bash
# sudo chmod +x *.sh

sudo systemctl stop squeeze-alice.service
sudo systemctl stop squeeze-tunnel.service

sudo systemctl daemon-reload
