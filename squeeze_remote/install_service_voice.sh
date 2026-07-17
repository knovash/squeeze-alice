#!/usr/bin/env bash

echo "INSTALL SERVICE VOICE ASSISTANT"

systemctl stop voice.service 2>/dev/null

sudo mkdir -p /opt/btremote

sudo cp voice.py /opt/btremote/
#sudo cp voice.conf /opt/btremote/
sudo cp voice.service /lib/systemd/system/
sudo chmod u+x /opt/btremote/voice.py

sudo systemctl stop voice.service
sudo systemctl daemon-reload
sudo systemctl enable voice.service
sudo systemctl restart voice.service
sudo systemctl status voice.service --no-pager

echo "OK"
sleep 10
# $SHELL
