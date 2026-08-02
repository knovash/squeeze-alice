#!/bin/bash

# --- Настройки ---
API_KEY="AQVN3dKzcyDkaTa9JPJ5Y4q9PXiRCIDaDuGPa3w2"
FOLDER_ID="ajetqapn6874d4rbq97i"
TEXT="Привет! Это тестовое сообщение."
OUTPUT_FILE="speech.wav"

# Параметры синтеза
VOICE="oksana"
EMOTION="neutral"
FORMAT="WAV"  # Для gRPC проще использовать WAV
SAMPLE_RATE=48000

# Громкость
VOLUME_LUFS="-15"
LOUDNESS_TYPE="LUFS"

# --- Формирование JSON-запроса для gRPC ---
JSON_DATA=$(cat <<EOF
{
  "text": "$TEXT",
  "outputAudioSpec": {
    "containerAudio": {
      "containerAudioType": "$FORMAT"
    },
    "sampleRateHertz": $SAMPLE_RATE
  },
  "hints": [
    { "voice": "$VOICE" },
    { "role": "$EMOTION" },
    { "volume": $VOLUME_LUFS }
  ],
  "loudnessNormalizationType": "$LOUDNESS_TYPE"
}
EOF
)

# --- Отправка запроса через gRPC ---
echo "Отправка запроса через gRPC..."
echo "$JSON_DATA" | jq -c . | \
grpcurl -H "authorization: Api-Key $API_KEY" \
       -H "x-folder-id: $FOLDER_ID" \
       -d @ \
       tts.api.ml.yandexcloud.kz:443 \
       speechkit.tts.v3.Synthesizer/UtteranceSynthesis | \
jq -r '.audioChunk.data' | base64 -d > "$OUTPUT_FILE"

# --- Проверка ---
if [ $? -eq 0 ] && [ -f "$OUTPUT_FILE" ] && [ -s "$OUTPUT_FILE" ]; then
    echo "✅ Аудио сохранено в $OUTPUT_FILE"
else
    echo "❌ Ошибка при синтезе"
    exit 1
fi
