#!/usr/bin/env bash
# sudo chmod +x *.sh

echo ""
echo "START APT UPDATE & UPGRADE"

sudo apt update	
sudo apt upgrade

echo ""
echo "FINISH APT UPDATE & UPGRADE"
echo ""
echo "START INSTALL ALL"
#echo "LMS"
echo "JAVA"
echo "NGROK"
echo "ALICE"

#sh install_lms
sh install_java.sh
sh install_ngrok.sh
sh install_alice.sh




