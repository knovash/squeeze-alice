#!/usr/bin/env bash
# sudo chmod +x *.sh

echo ""
echo "START INSTALL NGROK"

set=`cat set.json`
ngrok_token=$(echo $set | jq .ngrok_token)
ngrok_tunnel=$(echo $set | jq .ngrok_tunnel)
echo $ngrok_token
echo $ngrok_tunnel

curl -s https://ngrok-agent.s3.amazonaws.com/ngrok.asc | sudo tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null && echo "deb https://ngrok-agent.s3.amazonaws.com buster main" | sudo tee /etc/apt/sources.list.d/ngrok.list && sudo apt update && sudo apt install ngrok

ngrok config add-authtoken 1wRUYOxT1LsSQyozlfXziUUv0qk_6vSqXU8RMvscXiQjJe6CP

echo "ngrok http --domain=unicorn-neutral-badly.ngrok-free.app 8010"

cd /root
sudo systemctl stop squeeze-tunnel.service
sudo cp squeeze-tunnel.service /lib/systemd/system/
sudo systemctl enable squeeze-tunnel.service
sudo systemctl daemon-reload
sudo systemctl start squeeze-tunnel.service

echo "FINISH INSTALL NGROK"
