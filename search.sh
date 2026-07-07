#!/bin/bash

LMS_HOST="${LMS_HOST:-192.168.1.131}"
LMS_PORT="${LMS_PORT:-9000}"
LMS_URL="http://$LMS_HOST:$LMS_PORT/cometd"

if [ $# -eq 0 ]; then
    echo "Использование: $0 \"поисковый запрос\""
    echo "Пример: $0 \"Depeche mode\""
    exit 1
fi

SEARCH_TERM="$*"

# Проверка jq
if ! command -v jq &> /dev/null; then
    echo "ОШИБКА: Установите jq (sudo apt install jq)" >&2
    exit 1
fi

# 1. Handshake
echo "Handshake..."
HANDSHAKE_RESP=$(curl -s -X POST "$LMS_URL" \
    -H "Content-Type: text/json;charset=UTF-8" \
    -d '[{"channel":"/meta/handshake","version":"1.0","minimumVersion":"1.0","supportedConnectionTypes":["long-polling"],"advice":{"timeout":60000}}]')
CLIENT_ID=$(echo "$HANDSHAKE_RESP" | jq -r '.[0].clientId')
if [ "$CLIENT_ID" == "null" ] || [ -z "$CLIENT_ID" ]; then
    echo "ОШИБКА: Не удалось получить clientId. Ответ:" >&2
    echo "$HANDSHAKE_RESP" | jq . >&2
    exit 1
fi
echo "Client ID: $CLIENT_ID"

# 2. Subscribe
echo "Subscribe..."
curl -s -X POST "$LMS_URL" \
    -H "Content-Type: text/json;charset=UTF-8" \
    -d "[{\"channel\":\"/meta/subscribe\",\"clientId\":\"$CLIENT_ID\",\"subscription\":\"/slim/request\",\"id\":\"2\"}]" | jq '.'

# 3. Connect (обязательно!)
echo "Connect..."
curl -s -X POST "$LMS_URL" \
    -H "Content-Type: text/json;charset=UTF-8" \
    -d "[{\"channel\":\"/meta/connect\",\"clientId\":\"$CLIENT_ID\",\"connectionType\":\"long-polling\",\"id\":\"3\"}]" | jq '.'

# 4. Поиск
echo "Searching for: $SEARCH_TERM"
ESCAPED_TERM=$(printf '%s' "$SEARCH_TERM" | sed 's/"/\\"/g')
SEARCH_JSON="[{\"channel\":\"/slim/request\",\"clientId\":\"$CLIENT_ID\",\"id\":\"4\",\"data\":{\"request\":[\"\",[\"SpottyExtrasspotty\",\"items\",0,25000,\"search:$ESCAPED_TERM\",\"cachesearch:1\",\"menu:SpottyExtrasspotty\"]],\"response\":\"/slim/request/4\"}}]"

RESPONSE=$(curl -s -X POST "$LMS_URL" \
    -H "Content-Type: text/json;charset=UTF-8" \
    -d "$SEARCH_JSON")

# Проверка на ошибки
ERROR=$(echo "$RESPONSE" | jq -r '.[0].error')
if [ "$ERROR" != "null" ] && [ -n "$ERROR" ]; then
    echo "ОШИБКА поиска: $ERROR" >&2
    echo "$RESPONSE" | jq . >&2
    exit 1
fi

echo "=== РЕЗУЛЬТАТЫ ==="
echo "$RESPONSE" | jq '.'
