package org.knovash.squeezealice.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsLms extends SettingsBase {
    // создать
    // public static SettingsTasker settingsTasker = new SettingsTasker();
    // HandlerAll
    // case "/settings_tasker":
    //                return settingsTasker.action(context);
    // PageIndex
    // "<p><a href=\\settings_tasker>Настройки Tasker</a></p>" +
    // Process Form
    // case "settings_tasker_save":
    //                    settingsTasker.handleSaveAll(context);
    //                    break;
    // super("data/settings_tasker.properties", "settings_tasker_save",  "/settings_tasker");
    public static SettingsLms settingsLms = new SettingsLms();

    // ---------- Булевы переключатели ----------
    public enum KeyToggle implements ToggleKey {
        LMS_TOGGLE_SEARCH_AT_START(true, "Проверка ip и поиск LMS при запуске (вкл)");

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
        TASKER_VALUE_LINES(3, "Количество строк виджета плейлиста");

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
        LMS_STRING_IP( "192.168.1.131", "IP где установлен LMS"),
        LMS_STRING_PORT("9000", "Port LMS (9000)");

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
    public SettingsLms() {
        // Имя файла, имя обработчика сохранения, URL редиректа (null — рендерить страницу после сохранения)
        super("data/settings_lms.properties", "settings_lms_save",  "/settings_lms");
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
        //return Arrays.asList(KeyValue.values());
        return new ArrayList<>();
    }

    @Override
    protected List<? extends StringKey> getStringKeys() {
        return Arrays.asList(KeyString.values());
    }

    // ---------- Заголовки страницы ----------
    @Override
    protected String getPageTitle() {
        return "Настройки Tasker";
    }

    @Override
    protected String getPageHeader() {
        return "Настройки Tasker";
    }
}