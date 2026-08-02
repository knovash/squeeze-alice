from speechkit import model_repository, configure_credentials, creds

configure_credentials(
    yandex_credentials=creds.YandexCredentials(
        api_key='AQVN3dKzcyDkaTa9JPJ5Y4q9PXiRCIDaDuGPa3w2'
    )
)

def synthesize(text, export_path):
    # Казахстанский эндпоинт
    custom_endpoint = ('tts.api.ml.yandexcloud.kz:443', True)
    model = model_repository.synthesis_model(custom_endpoint=custom_endpoint)

    model.voice = 'oksana'
    model.role = 'neutral'          # амплуа (emotion)
    model.loudness_normalization_type = 'LUFS'
    model.volume = -15              # громкость, диапазон [-145; 0)

    result = model.synthesize(text, raw_format=False)
    result.export(export_path, 'wav')   # или 'mp3'

if __name__ == '__main__':
    synthesize('Привет! Это тестовое сообщение.', 'speech.wav')
