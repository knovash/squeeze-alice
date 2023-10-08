#!/usr/bin/env bash
# sudo chmod +x *.sh
cd /root

sh disable_services.sh
sudo systemctl daemon-reload

sh install_alice.sh
sh install_alice_service.sh
sh install_tunnel.sh
sudo systemctl daemon-reload

#$SHELL

