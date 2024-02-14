#!/usr/bin/env bash
# sudo chmod +x *.sh
#ssh root@192.168.1.110 systemctl daemon-reload
#echo 12345 | ssh -tt root@192.168.1.110 systemctl restart logitechmediaserver.service

echo "12345" | ssh -tt root@192.168.1.110 log.sh

$SHELL

