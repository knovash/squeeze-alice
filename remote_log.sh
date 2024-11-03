#!/usr/bin/env bash
remote_ip=$(cat bash_config.json | jq -r '.remote_ip')
ssh root@$remote_ip tail -f /opt/squeeze-alice-1.0/log/log.txt

