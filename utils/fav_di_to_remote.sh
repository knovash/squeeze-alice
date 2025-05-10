#!/usr/bin/env bash

ip=$(cat lms_config.json | jq -r '.lms_ip')
#ip=$(echo $(basename "$0") | grep -oP "[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}")

echo "FAVORITES TO REMOTE "$ip
echo "WAIT 5 sec... FOR CANCEL!!!"
sleep 5
echo "FAVORITES SEND START"

sshpass -p "12345" scp favorites_di.opml root@$ip:/var/lib/squeezeboxserver/prefs/favorites.opml

echo "FAVORITES SEND FINISH"

sleep 10
#$SHELL
