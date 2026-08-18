package org.knovash.squeezealice.web;

import java.util.Arrays;
import java.util.List;

public class SettingsVoice extends SettingsBase {
    // создать
    // public static SettingsVoice settingsVoice = new SettingsVoice();
    // HandlerAll
    // case "/settings_voice":
    //                return settingsVoice.action(context);
    // PageIndex
    // "<p><a href=\\settings_voice>Настройки Voice</a></p>" +
    // Process Form
    // case "settings_voice_save":
    //                    settingsVoice.handleSaveAll(context);
    //                    break;
    // super("data/settings_voice.properties", "settings_voice_save",  "/settings_voice");
    public static SettingsVoice settingsVoice = new SettingsVoice();
    
    // ---------- Булевы переключатели ----------
    public enum KeyToggle implements ToggleKey {

        VOICE_TOGGLE_VOICE_TO_LMS(true, "Голосовые уведомления через плеер в LMS"),
        VOICE_TOGGLE_LMS_SAY_CHANNEL_NAME(false, "Говорить включаю канал <название канала>"),
        VOICE_TOGGLE_VOLUME_AMP_LMS(false, "Увеличивать громкость уведомлений в LMS"),
        VOICE_TOGGLE_VOLUME_AMP_FFMPEG(false, "Увеличивать громкость уведомлений в FFMPEG"),
        VOICE_TOGGLE_TTS_YANDEX(true, "TTS Yandex"),
        VOICE_TOGGLE_TTS_GOOGLE(false, "TTS Google");

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
        VOICE_VALUE_VOLUME_AMP_LMS(10, "Увеличение громкости уведомлений через LMS, 10"),
        VOICE_VALUE_VOLUME_APM_FFMPEG(3, "Увеличение громкости уведомлений через FFmpeg, 3"),
        VOICE_VALUE_VOLUME_AMP_GOOGLE_TTS(5, "Увеличение громкости уведомлений через TTS Google, 5"),
        VOICE_VALUE_DELAY_AFTER_NOTIFICATION(2000, "Добавлять задержку после голосового уведомления, 2000 мс"),
        VOICE_VALUE_WAIT_FOR_NOTIFICATION_COMPLITED(15, "Ожидание завершения голосовых уведомлений, 15 c");

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
        VOICE_STRING_GOOGLE_API_KEY("---", "Google TTS API key");

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
    public SettingsVoice() {
        // Имя файла, имя обработчика сохранения, URL редиректа (null — рендерить страницу после сохранения)
        super("data/settings_voice.properties", "settings_voice_save",  "/settings_voice");
    }

    @Override
    protected void applySettings() {

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
        return "Настройки голосовых уведомлений через плееры LMS";
    }

    @Override
    protected String getPageHeader() {
        return "Настройки голосовых уведомлений через плееры LMS";
    }
}