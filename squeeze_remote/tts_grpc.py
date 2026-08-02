import grpc
from yandex.cloud.ai.tts.v3 import tts_pb2, tts_service_pb2_grpc
import base64

API_KEY = "AQVN3dKzcyDkaTa9JPJ5Y4q9PXiRCIDaDuGPa3w2"
FOLDER_ID = "ajetqapn6874d4rbq97i"
TEXT = "Привет! Это тестовое сообщение."
OUTPUT_FILE = "speech.wav"

def synthesize():
    # Казахстанский эндпоинт (gRPC)
    channel = grpc.insecure_channel("tts.api.ml.yandexcloud.kz:443")
    stub = tts_service_pb2_grpc.SynthesizerStub(channel)

    # Формируем запрос
    request = tts_pb2.UtteranceSynthesisRequest(
        text=TEXT,
        output_audio_spec=tts_pb2.OutputAudioSpec(
            container_audio=tts_pb2.ContainerAudio(
                container_audio_type="WAV"
            ),
            sample_rate_hertz=48000
        ),
        hints=[
            tts_pb2.Hint(voice="oksana"),
            tts_pb2.Hint(role="neutral"),
            tts_pb2.Hint(volume=-15)  # громкость в LUFS
        ],
        loudness_normalization_type="LUFS",
        folder_id=FOLDER_ID
    )

    metadata = [
        ("authorization", f"Api-Key {API_KEY}"),
        ("x-folder-id", FOLDER_ID)
    ]

    # Отправляем запрос (стриминг)
    responses = stub.UtteranceSynthesis(request, metadata=metadata)

    # Собираем аудио
    audio_data = b""
    for response in responses:
        if response.audio_chunk:
            audio_data += response.audio_chunk.data

    if audio_data:
        with open(OUTPUT_FILE, "wb") as f:
            f.write(audio_data)
        print(f"✅ Аудио сохранено в {OUTPUT_FILE}")
    else:
        print("❌ Нет аудиоданных в ответе")

if __name__ == "__main__":
    synthesize()
