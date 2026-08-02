#!/usr/bin/env bash
# sudo chmod +x *.sh
CURRENT_PATH="$(dirname "$0")"
source "$CURRENT_PATH/common.sh"
file_name=$(basename "$0" .sh)
default_remote="${file_name##*_}"
read_ssh_params "$default_remote"
#----------------------------------------------------------

NEW_IP="192.168.1.132"
NEW_PORT="8010"
CONFIG="/opt/btremote/config.conf"


ssh_cmd="sed -i 's|\"server\":[[:space:]]*\"http://[^:]*:[0-9]*\"|\"server\": \"http://$NEW_IP:$NEW_PORT\"|' $CONFIG"
sshpass -p "$password" ssh -o StrictHostKeyChecking=no "$username@$remote" "$ssh_cmd"

# Проверяем код возврата
if [ $? -eq 0 ]; then
    echo "✅ IP-адрес сервера обновлён на http://$NEW_IP:$NEW_PORT в файле $CONFIG на удалённой машине $remote"
else
    echo "❌ Ошибка при обновлении конфига на удалённой машине"
    exit 1
fi

# Опционально выводим содержимое обновлённого файла для проверки
echo "Новое содержимое $CONFIG:"
sshpass -p "$password" ssh "$username@$remote" "cat $CONFIG"

echo "OK"
sleep 30
# $SHELL
