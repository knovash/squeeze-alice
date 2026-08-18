package org.knovash.squeezealice.web;

import java.util.Arrays;
import java.util.List;

public class SettingsButtons extends SettingsBase {

    public static SettingsButtons settingsButtons = new SettingsButtons();

    public enum ToggleButton implements ToggleKey {
        TOGGLE_BUTTON_SWITCH_HERE(true, "Переключи сюда"),
        TOGGLE_BUTTON_WHAT_PLAYS(false, "Что играет"),
        TOGGLE_BUTTON_NEXT(true, "Дальше"),
        TOGGLE_BUTTON_BACK(true, "Назад"),
        TOGGLE_BUTTON_SEPARATE(true, "Отдельно"),
        TOGGLE_BUTTON_TOGETHER(true, "Вместе"),
        TOGGLE_BUTTON_REPEAT(true, "Повтор"),
        TOGGLE_BUTTON_SHUFFLE(true, "Рандом"),
        TOGGLE_BUTTON_WHERE_REMOTE(false, "Где пульт"),
        TOGGLE_BUTTON_CONNECT_REMOTE(false, "Подключи пульт");

        public volatile boolean value;
        private final String description;

        ToggleButton(boolean value, String description) {
            this.value = value;
            this.description = description;
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

    // ---- Конструктор ----
    public SettingsButtons() {
        super("data/settings_buttons.properties", "settings_buttons_save", "/settings_buttons");
    }

    @Override
    protected void applySettings() {

    }

    // ---- Переопределяем только нужные методы ----
    @Override
    protected List<? extends ToggleKey> getToggleKeys() {
        return Arrays.asList(ToggleButton.values());
    }

    @Override
    protected String getPageTitle() {
        return "Настройки устройств-действий в УДЯ";
    }

    @Override
    protected String getPageHeader() {
        return "Настройки устройств-действий в УДЯ";
    }

    // getValueKeys() и getStringKeys() возвращают пустые списки (унаследовано от базового класса)
    // handleSaveAll() не переопределяем — всё делает базовый класс с редиректом
}