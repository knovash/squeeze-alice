#!/bin/bash
# setup_lms_playlists.sh - быстрое создание папки для плейлистов LMS
# Запускать от root

# Имя пользователя LMS (по умолчанию squeezeboxserver)
LMS_USER="${1:-squeezeboxserver}"
PLAYLIST_DIR="/home/playlists"

mkdir -p "$PLAYLIST_DIR"
chown -R "$LMS_USER":"$LMS_USER" "$PLAYLIST_DIR"
chmod 755 "$PLAYLIST_DIR"

echo "Папка $PLAYLIST_DIR создана и доступна для $LMS_USER"
echo "Не забудьте в настройках LMS указать эту папку как Playlists Folder"
