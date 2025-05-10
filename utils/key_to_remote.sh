#!/usr/bin/env bash

ip=$(cat lms_config.json | jq -r '.lms_ip')
#ip=$(echo $(basename "$0") | grep -oP "[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}")

echo "FAVORITES TO REMOTE "$ip
echo "WAIT 5 sec..."
sleep 5

ssh root@$ip mkdir -p .ssh
sshpass -p "12345" scp /home/konstantin/.ssh/id_rsa.pub root@$ip:.ssh/authorized_keys

sleep 60
#$SHELL
