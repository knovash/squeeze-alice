#!/usr/bin/env bash
sudo service squeeze-alice stop
sudo service squeeze-tunnel stop
sudo systemctl disable squeeze-alice.service
sudo systemctl disable squeeze-tunnel.service
sudo systemctl daemon-reload
