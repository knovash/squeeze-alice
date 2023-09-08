#!/usr/bin/env bash
sudo service squeeze-tunnel stop
sudo systemctl disable squeeze-tunnel.service
sudo systemctl daemon-reload
sudo service squeeze-tunnel status
$SHELL
