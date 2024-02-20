#!/usr/bin/env bash

BGreen='\033[1;32m'
NC='\033[0m' # No Color

echo -e "\n"${BGreen}"UPDATE"${NC}"\n"
sudo apt update
sudo apt upgrade

echo -e "\nrun "${BGreen}"INSTALL JAVA"${NC}"\n"
#https://sciencesappliquees.com/noncato/350-orange-pi-install-java-jdk-8
sudo apt-get install default-jre

echo "\nrun "${BGreen}"INSTALL NGROK"${NC}"\n"
#https://dashboard.ngrok.com/get-started/setup/linux
curl -s https://ngrok-agent.s3.amazonaws.com/ngrok.asc | sudo tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null && echo "deb https://ngrok-agent.s3.amazonaws.com buster main" | sudo tee /etc/apt/sources.list.d/ngrok.list && sudo apt update && sudo apt install ngrok
echo -e "\nrun "${BGreen}"ADD NGROK TOKEN"${NC}"\n"
#https://dashboard.ngrok.com/get-started/your-authtoken
ngrok config add-authtoken 1wRUYOxT1LsSQyozlfXziUUv0qk_6vSqXU8RMvscXiQjJe6CP &

echo -e "\nrun "${BGreen}"LIST FILES"${NC}"\n"
ls /root/

echo -e "\nrun "${BGreen}"UNTAR SERVER TO /OPT/"${NC}"\n"
rm -r /opt/squeeze-alice*
sudo tar xzvf /root/squeeze-alice*.tar.gz -C /opt/

echo -e "\nrun "${BGreen}"START TUNNEL SERVICE"${NC}"\n"
sudo cp /root/squeeze-tunnel.service /lib/systemd/system/
sudo systemctl enable squeeze-tunnel.service
sudo systemctl daemon-reload
sudo systemctl start squeeze-tunnel.service

echo -e "\nrun "${BGreen}"START SERVER SERVICE"${NC}"\n"
sudo cp /root/squeeze-alice.service /lib/systemd/system/
sudo systemctl enable squeeze-alice.service
sudo systemctl daemon-reload
sudo systemctl start squeeze-alice.service

echo -e "\nrun "${BGreen}"FINISH"${NC}"\n"
echo -e "\nrun "${BGreen}"https://{YOUR ADDRESS}.ngrok-free.app/"${NC}"\n"

$SHELL
