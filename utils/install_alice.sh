#!/usr/bin/env bash
# sudo chmod +x *.sh
cd /root
sudo tar xzvf squeeze-alice*.tar.gz -C /opt/

sudo systemctl stop squeeze-alice.service
sudo systemctl daemon-reload
sudo systemctl enable squeeze-alice.service
sudo systemctl start squeeze-alice.service
#$SHELL

