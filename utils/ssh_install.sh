#!/usr/bin/env bash
sshpass -p "12345" scp * root@192.168.1.52:/home/
ssh root@192.168.1.52 /home/./install.sh
