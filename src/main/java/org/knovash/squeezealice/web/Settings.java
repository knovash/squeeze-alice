package org.knovash.squeezealice.web;

import java.util.Arrays;
import java.util.List;

public class Settings extends SettingsBase {

    public static Settings settings = new Settings();
    // ---------- Булевы переключатели ----------
    public enum KeyToggle implements ToggleKey {
        TOGGLE_WAKE(true, "Ожидание пробуждения колонки перед воспроизведением (вкл)"),
        TOGGLE_EXPIRED_ALWAYS(false, "Всегда будить плеер, считать время просроченным (откл)"),
        TOGGLE_YANDEX_REFRESH(true, "Отправлять в Яндекс состояния устройств после действия (вкл)"),
        TOGGLE_SCHEDULER_SPOTIFY_REFRESH_TOKEN(true, "Периодически обновлять токен Spotify (вкл)"),
        TOGGLE_SCHEDULER_YANDEX_UPDATE_DEVICES(true, "Периодически отправлять состояние устройств в Яндекс (вкл)");

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
        VALUE_PERIOD_UPDATE_SPOTIFY_TOKEN(10, "Период обновления токена Spotify, 10 мин"),
        VALUE_PERIOD_UPDATE_DEVICES_TO_YANDEX(5, "Период отправки состояний устройств в Яндекс, 5 мин");

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
        TEST("---", "Google TTS API key");

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

    //@Override
    //protected List<? extends StringKey> getStringKeys() {
    //    return Arrays.asList(KeyString.values());
    //}

    // ---------- Заголовки страницы ----------
    @Override
    protected String getPageTitle() {
        return "Настройки";
    }

    @Override
    protected String getPageHeader() {
        return "Настройки";
    }
}