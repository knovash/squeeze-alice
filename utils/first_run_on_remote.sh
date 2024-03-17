#!/usr/bin/env bash

BGreen='\033[1;32m'
NC='\033[0m' # No Color


echo -e "\n"${BGreen}"LIST FILES"${NC}"\n"
ls

echo -e "\n"${BGreen}"READ FROM set_ngrok_token.txt"${NC}"\n"
ngrok_token=`cat set_ngrok_token.txt`
echo -e "\n"${BGreen}"NGROK TOKEN "$remote${NC}"\n"

#echo -e "\n"${BGreen}"UPDATE"${NC}"\n"
#sudo apt update
#sudo apt upgrade

echo -e "\n"${BGreen}"INSTALL JAVA"${NC}"\n"
#https://sciencesappliquees.com/noncato/350-orange-pi-install-java-jdk-8
sudo apt-get install default-jre

echo "\n"${BGreen}"INSTALL NGROK"${NC}"\n"
#https://dashboard.ngrok.com/get-started/setup/linux
curl -s https://ngrok-agent.s3.amazonaws.com/ngrok.asc | sudo tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null && echo "deb https://ngrok-agent.s3.amazonaws.com buster main" | sudo tee /etc/apt/sources.list.d/ngrok.list && sudo apt install ngrok

echo -e "\n"${BGreen}"ADD NGROK TOKEN"${NC}"\n"
#https://dashboard.ngrok.com/get-started/your-authtoken
ngrok config add-authtoken $ngrok_token &

echo -e "\n"${BGreen}"LIST FILES"${NC}"\n"
ls /root/

echo -e "\n"${BGreen}"UNTAR SERVER TO /OPT/"${NC}"\n"
rm -r /opt/squeeze-alice*
sudo tar xzvf /root/squeeze-alice*.tar.gz -C /opt/

echo -e "\n"${BGreen}"START TUNNEL SERVICE"${NC}"\n"
sudo cp /root/squeeze-tunnel.service /lib/systemd/system/
sudo systemctl enable squeeze-tunnel.service
sudo systemctl daemon-reload
sudo systemctl start squeeze-tunnel.service

echo -e "\n"${BGreen}"START SERVER SERVICE"${NC}"\n"
sudo cp /root/squeeze-alice.service /lib/systemd/system/
sudo systemctl enable squeeze-alice.service
sudo systemctl daemon-reload
sudo systemctl start squeeze-alice.service

echo -e "\n"${BGreen}"FINISH"${NC}"\n"
echo -e "\n"${BGreen}"https://{YOUR ADDRESS}.ngrok-free.app/"${NC}"\n"

$SHELL
