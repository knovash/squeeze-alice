#!/usr/bin/env bash

ip=$(cat lms_config.json | jq -r '.lms_ip')
#ip=$(echo $(basename "$0") | grep -oP "[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}")

echo "FAVORITES FROM REMOTE "$ip
echo "WAIT 5 sec..."
sleep 5

sshpass -p "12345" scp root@$ip:/var/lib/squeezeboxserver/prefs/favorites.opml favorites_new.opml

sleep 60
#$SHELL
