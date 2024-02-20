#!/usr/bin/env bash
# sudo chmod +x *.sh
#ssh root@192.168.1.123 systemctl daemon-reload
#ssh root@192.168.1.110 systemctl restart logitechmediaserver.service
ssh root@192.168.1.123 systemctl restart squeeze-tunnel.service
#ssh root@192.168.1.123 systemctl restart squeeze-alice.service

