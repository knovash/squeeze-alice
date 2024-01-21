#!/usr/bin/env bash
# sudo chmod +x *.sh
cd /root
sudo tar xzvf squeeze-alice*.tar.gz -C /opt/

systemctl enable squeeze-alice.service
systemctl enable squeeze-tunnel.service
systemctl daemon-reload
systemctl restart squeeze-alice.service
systemctl restart squeeze-tunnel.service

