#!/usr/bin/env bash
# sudo chmod +x *.sh

#sudo systemctl stop squeeze-tunnel.service
#sudo systemctl stop squeeze-alice.service

cd /root
sudo tar xzvf squeeze-alice*.tar.gz -C /opt/
#sudo cp squeeze-tunnel.service /lib/systemd/system/
#sudo cp squeeze-alice.service /lib/systemd/system/
#
#sudo systemctl enable squeeze-tunnel.service
#sudo systemctl enable squeeze-alice.service
#sudo systemctl daemon-reload
#sudo systemctl start squeeze-tunnel.service
#sudo systemctl start squeeze-alice.service

#$SHELL

