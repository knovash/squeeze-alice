#!/usr/bin/env bash
sudo service squeeze-alice stop
sudo systemctl disable squeeze-alice.service
sudo systemctl daemon-reload
