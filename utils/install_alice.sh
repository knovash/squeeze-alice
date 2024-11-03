#!/usr/bin/env bash
# sudo chmod +x *.sh

echo "START INSTALL ALICE"

sudo systemctl stop squeeze-alice.service
sleep 1
#rm -r /opt/squeeze-alice*
cd /root
sudo tar xzvf squeeze-alice-1.0.tar.gz -C /opt/

sudo systemctl stop squeeze-alice.service
sudo cp squeeze-alice.service /lib/systemd/system/
sudo systemctl enable squeeze-alice.service
sudo systemctl daemon-reload
sudo systemctl start squeeze-alice.service

echo "FINISH INSTALL ALICE"

