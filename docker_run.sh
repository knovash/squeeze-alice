#!/bin/sh

#docker run --rm --name squeeze-alice-local --user $(id -u):$(id -g) -v /home/konstantin/sa_data:/app/target/logs -p 8010:8010 knovash/squeeze-alice:latest

#docker run --rm --name squeeze-alice-local -p 8010:8010 knovash/squeeze-alice:latest

#docker run -it --rm --name squeeze-alice-local -p 8010:8010 knovash/squeeze-alice:latest sh

# Остановить ранее запущеный контейнер
docker stop squeeze-alice-local

# Запуск. удалить контейнер после остановки
mkdir ~/sa_data
docker run --rm --name squeeze-alice-local -v ~/sa_data:/app/data -p 8010:8010 knovash/squeeze-alice-local:latest

#sleep 15
$SHELL
