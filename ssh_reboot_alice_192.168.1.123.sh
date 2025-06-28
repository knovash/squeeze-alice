#!/usr/bin/env bash
file_name=$(basename "$0" .sh)
remote="${file_name##*_}" 
echo "IP: $remote"
echo "REBOOT"
ssh root@$remote reboot
sleep 10
