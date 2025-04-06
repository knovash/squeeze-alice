#!/usr/bin/env bash

remote_ip=$(cat bash_config.json | jq -r '.remote_ip')
echo $remote_ip

#ssh root@192.168.1.123 mkdir -p .ssh
#sshpass -p "12345" scp /home/konstantin/.ssh/id_rsa.pub root@192.168.1.123:.ssh/authorized_keys

ssh root@$remote_ip mkdir -p .ssh
sshpass -p "12345" scp /home/konstantin/.ssh/id_rsa.pub root@$remote_ip:.ssh/authorized_keys

echo "OK"
#sleep 5
$SHELL
