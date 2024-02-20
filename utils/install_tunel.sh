#!/usr/bin/env bash
# sudo chmod +x *.sh
curl -s https://ngrok-agent.s3.amazonaws.com/ngrok.asc | sudo tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null && echo "deb https://ngrok-agent.s3.amazonaws.com buster main" | sudo tee /etc/apt/sources.list.d/ngrok.list && sudo apt update && sudo apt install ngrok
ngrok config add-authtoken 1wRUYOxT1LsSQyozlfXziUUv0qk_6vSqXU8RMvscXiQjJe6CP
ngrok http --domain=unicorn-neutral-badly.ngrok-free.app 8010
