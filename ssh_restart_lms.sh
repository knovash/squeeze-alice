#!/usr/bin/env bash
# sudo chmod +x *.sh
#ssh root@192.168.1.110 systemctl daemon-reload
ssh root@192.168.1.110 systemctl restart logitechmediaserver.service
