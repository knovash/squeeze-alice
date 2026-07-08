#!/usr/bin/env bash

echo "INSTALL SERVICE BT REMOTE"

systemctl stop btremote.service 2>/dev/null

sudo mkdir -p /opt/btremote

sudo cp btremote.py /opt/btremote/
sudo cp config.conf /opt/btremote/
sudo cp btremote.service /lib/systemd/system/
sudo chmod u+x /opt/btremote/btremote.py

sudo systemctl stop btremote.service
sudo systemctl daemon-reload
sudo systemctl enable btremote.service
sudo systemctl restart btremote.service
sudo systemctl status btremote.service --no-pager

echo "OK"
sleep 10
# $SHELL
