#!/usr/bin/env bash
#ssh root@192.168.1.52 rm -r /home/sq*
sshpass -p "12345" scp *.gz *.service *.sh root@192.168.1.52:/home/
