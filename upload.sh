#!/usr/bin/env bash

#rm -r target
#mvn package

#sh tar.sh

cd squeeze-alice-pak
sshpass -p "12345" scp * root@192.168.1.123:/root/

ssh root@192.168.1.123 sh install_alice.sh

ssh root@192.168.1.123

$SHELL
