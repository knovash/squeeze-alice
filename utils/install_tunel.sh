#!/usr/bin/env bash
# sudo chmod +x *.sh

echo -e "\n"${BGreen}"READ FROM set_ngrok_token.txt"${NC}"\n"
ngrok_token=`cat set_ngrok_token.txt`
echo -e "\n"${BGreen}"NGROK TOKEN "$remote${NC}"\n"

curl -s https://ngrok-agent.s3.amazonaws.com/ngrok.asc | sudo tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null && echo "deb https://ngrok-agent.s3.amazonaws.com buster main" | sudo tee /etc/apt/sources.list.d/ngrok.list && sudo apt update && sudo apt install ngrok
ngrok config add-authtoken $ngrok_token
ngrok http --domain=unicorn-neutral-badly.ngrok-free.app 8010
