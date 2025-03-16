#!/bin/sh

# Соберите образ локально
docker build -t knovash/sqa-server:latest .
# your-username — ваш логин на Docker Hub
# your-image-name — название образа (например, my-web-app)
# tag — версия образа (например, latest, v1.0)

echo "FINISH"
sleep 15
#$SHELL
