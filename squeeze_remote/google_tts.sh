#!/bin/bash

# ВСТАВЬТЕ СЮДА ВАШ НОВЫЙ API-КЛЮЧ
API_KEY="AIzaSyCkZDOhiXYpo3VTX-L0pR7BRZBWtvOoJJI"

# Настройки синтеза
TEXT="сейчас на homepod играет kraftwerk"
LANGUAGE_CODE="ru-RU"
VOICE_NAME="ru-RU-Standard-A"      # или ru-RU-Wavenet-A
SSML_GENDER="FEMALE"
AUDIO_ENCODING="MP3"
VOLUME_GAIN_DB="+10.0"               # громкость: от -96.0 до +16.0
OUTPUT_FILE="speech.mp3"

# Формируем JSON-запрос
JSON_DATA=$(cat <<EOF
{
  "input": { "text": "$TEXT" },
  "voice": {
    "languageCode": "$LANGUAGE_CODE",
    "name": "$VOICE_NAME",
    "ssmlGender": "$SSML_GENDER"
  },
  "audioConfig": {
    "audioEncoding": "$AUDIO_ENCODING",
    "volumeGainDb": $VOLUME_GAIN_DB
  }
}
EOF
)

# Отправляем запрос с API-ключом
TEMP_FILE="response.json"
curl -s -X POST \
  -H "Content-Type: application/json; charset=utf-8" \
  -d "$JSON_DATA" \
  "https://texttospeech.googleapis.com/v1/text:synthesize?key=$API_KEY" > "$TEMP_FILE"

# Проверяем ответ
if grep -q "error" "$TEMP_FILE"; then
  echo "Ошибка:"
  cat "$TEMP_FILE"
  rm "$TEMP_FILE"
  exit 1
fi

# Извлекаем и декодируем аудио
grep -o '"audioContent": "[^"]*' "$TEMP_FILE" | sed 's/"audioContent": "//' | base64 -d > "$OUTPUT_FILE"
rm "$TEMP_FILE"
echo "Готово! Файл: $OUTPUT_FILE"
