#!/usr/bin/env bash
ssh root@192.168.1.123 systemctl stop squeeze-tunnel.service
ngrok http --domain=unicorn-neutral-badly.ngrok-free.app 8010
ssh root@192.168.1.123 systemctl restart squeeze-tunnel.service
$SHELL
