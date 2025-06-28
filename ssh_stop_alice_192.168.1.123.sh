#!/usr/bin/env bash
file_name=$(basename "$0" .sh)
remote="${file_name##*_}" 
echo "IP: $remote"
echo "WAIT..."
ssh root@$remote systemctl stop squeeze-alice.service
echo "OK"
sleep 10
