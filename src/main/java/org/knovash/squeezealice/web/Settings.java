package org.knovash.squeezealice.web;

import java.util.Arrays;
import java.util.List;

public class Settings extends SettingsBase {

    // ---------- Булевы переключатели ----------
    public enum KeyToggle implements ToggleKey {
        TOGGLE_WAKE(true, "Ожидание пробуждения колонки перед воспроизведением"),
        TOGGLE_LMS_SAY_CHANNEL_NAME(true, "Говорить включаю канал <название канала>"),
        TOGGLE_EXPIRED_ALWAYS(false, "Всегда будить плеер, считать время просроченным"),
        TOGGLE_TASKER_REFRESH(false, "Отправлять запрос в Таскер для обновления виджетов"),
        TOGGLE_YANDEX_REFRESH(false, "Отправлять запрос в Яндекс для обновления устройств"),
        TOGGLE_VOICE(true, "Голосовые уведомления через плеер в LMS"),
        TOGGLE_VOLUME_AMP_LMS(false, "Увеличивать громкость уведомлений в LMS"),
        TOGGLE_VOLUME_AMP_FFMPEG(false, "Увеличивать громкость уведомлений в FFMPEG"),
        TOGGLE_TTS_YANDEX(true, "TTS Yandex"),
        TOGGLE_TTS_GOOGLE(false, "TTS Google"),
        TOGGLE_SCHEDULER_SPOTIFY_REFRESH_TOKEN(true, "Периодически обновлять токен Spotify"),
        TOGGLE_SCHEDULER_UPDATE_PLAYERS(true, "Периодически обновлять состояние плееров LMS");

        public final String description;
        public volatile boolean value;

        KeyToggle(boolean value, String description) {
            this.description = description;
            this.value = value;
        }

        @Override
        public boolean getValue() {
            return value;
        }

        @Override
        public void setValue(boolean value) {
            this.value = value;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    // ---------- Числовые настройки ----------
    public enum KeyValue implements ValueKey {
        VALUE_VOLUME_AMP_LMS(10, "Увеличение громкости уведомлений через LMS, 10"),
        VALUE_VOLUME_APM_FFMPEG(3, "Увеличение громкости уведомлений через FFmpeg, 3"),
        VALUE_VOLUME_AMP_GOOGLE_TTS(5, "Увеличение громкости уведомлений через TTS Google, 5"),
        VALUE_DELAY_AFTER_NOTIFICATION(2000, "Добавлять задержку после голосового уведомления, 2000 мс"),
        VALUE_WAIT_FOR_NOTIFICATION_COMPLITED(15, "Ожидание завершения голосовых уведомлений, 15 c");

        public final String description;
        public volatile Integer value;

        KeyValue(Integer value, String description) {
            this.description = description;
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }

        @Override
        public void setValue(int value) {
            this.value = value;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    // ---------- Строковые настройки ----------
    public enum KeyString implements StringKey {
        VALUE_TASKER_IP( "192.168.1.126", "Tasker IP"),
        VALUE_TASKER_PORT("1821", "Tasker Port"),
        VALUE_GOOGLE_API_KEY("---", "Google TTS API key");

        public final String description;
        public volatile String value;

        KeyString(String value, String description) {
            this.description = description;
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    // ---------- Конструктор ----------
    public Settings() {
        // Имя файла, имя обработчика сохранения, URL редиректа (null — рендерить страницу после сохранения)
        super("data/settings.properties", "settings_save_all",  "/settings");
    }

    // ---------- Переопределение методов для получения списков ключей ----------
    @Override
    protected List<? extends ToggleKey> getToggleKeys() {
        return Arrays.asList(KeyToggle.values());
    }

    @Override
    protected List<? extends ValueKey> getValueKeys() {
        return Arrays.asList(KeyValue.values());
    }

    @Override
    protected List<? extends StringKey> getStringKeys() {
        return Arrays.asList(KeyString.values());
    }

    // ---------- Заголовки страницы ----------
    @Override
    protected String getPageTitle() {
        return "Настройки";
    }

    @Override
    protected String getPageHeader() {
        return "Настройки дополнительные";
    }
}