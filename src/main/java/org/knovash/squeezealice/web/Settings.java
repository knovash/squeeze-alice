package org.knovash.squeezealice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.utils.Parser;

import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static org.knovash.squeezealice.web.PageIndex.pageOuter;

@Log4j2
public class Settings {


    private static final ObjectMapper mapper = new ObjectMapper();

    // --- Булевы переключатели ---
    public enum KeyToggle {
        TOGGLE_WAKE(true, "Ожидание пробуждения колонки перед воспроизведением"),
        TOGGLE_LMS_SAY_CHANNEL_NAME(true, "Говорить включаю канал <название канала>"),
        TOGGLE_EXPIRED_ALWAYS(false, "Всегда будить плеер, считать время просроченным"),
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
    }

    // --- Числовые настройки ---
    public enum KeyValue {
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
    }

    // --- Строковые настройки ---
    public enum KeyString {
        VALUE_GOOGLE_API_KEY("---", "Google TTS API key");

        public final String description;
        public volatile String value;

        KeyString(String value, String description) {
            this.description = description;
            this.value = value;
        }
    }

    private final String propertiesFileName;

    public Settings() {
        this("data/settings.properties");
    }

    public Settings(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
        loadFromFile();
    }

    // --- Сохранение всех настроек (булевы + числовые + строковые) в один файл ---
    public synchronized void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(propertiesFileName))) {
            // Заголовок (как в Properties.store)
            writer.println("# Settings saved");
            writer.println("# " + new Date());
            // Сохраняем в порядке: KeyToggle, KeyValue, KeyString
            for (KeyToggle key : KeyToggle.values()) {
                writer.println(key.name() + "=" + key.value);
            }
            for (KeyValue key : KeyValue.values()) {
                writer.println(key.name() + "=" + key.value);
            }
            for (KeyString key : KeyString.values()) {
                // Для строковых значений возможно экранирование, но если значения не содержат спецсимволов,
                // можно писать как есть. Если нужно экранирование, используйте Properties.saveConvert.
                writer.println(key.name() + "=" + key.value);
            }
            writer.flush();
            log.info("Settings saved to " + propertiesFileName + " (in enum order)");
        } catch (IOException e) {
            log.error("Failed to save settings to file", e);
        }
    }

    // --- Загрузка всех настроек из файла ---
    public synchronized void loadFromFile() {
        File file = new File(propertiesFileName);
        if (!file.exists()) {
            log.info("Settings file not found, keeping initial values (from enum constructors)");
            return;
        }
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            props.load(in);

            for (KeyToggle key : KeyToggle.values()) {
                String val = props.getProperty(key.name());
                if (val != null) {
                    key.value = Boolean.parseBoolean(val);
                }
            }
            for (KeyValue key : KeyValue.values()) {
                String val = props.getProperty(key.name());
                if (val != null) {
                    try {
                        key.value = Integer.parseInt(val);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid integer value for {}: {}, keeping current", key.name(), val);
                    }
                }
            }
            for (KeyString key : KeyString.values()) {
                String val = props.getProperty(key.name());
                if (val != null) {
                    key.value = val;
                }
            }

            log.info("Settings loaded from " + propertiesFileName);
        } catch (IOException e) {
            log.error("Failed to load settings from file, keeping current values", e);
        }
    }

    // --- Веб-интерфейс ---

    public String renderPage() {
        String innerHtml = renderForm();
        return pageOuter(innerHtml, "Настройки", "Настройки дополнительные");
    }

    private String renderForm() {
        StringBuilder fields = new StringBuilder();

        // --- Булевы настройки (select) ---
        for (KeyToggle key : KeyToggle.values()) {
            fields.append("<fieldset>")
                    .append("<legend><b>").append(key.description).append("</b></legend>")
                    .append("<select name=\"").append(key.name()).append("\" required>")
                    .append("<option value=\"true\" ").append(key.value ? "selected" : "").append(">вкл</option>")
                    .append("<option value=\"false\" ").append(!key.value ? "selected" : "").append(">выкл</option>")
                    .append("</select> ")
                    .append("<span style=\"font-size:0.8em;color:gray;\">").append(key.name()).append("</span>")
                    .append("</fieldset><br>");
        }

        // --- Числовые настройки (input type="number") ---
        for (KeyValue key : KeyValue.values()) {
            fields.append("<fieldset>")
                    .append("<legend><b>").append(key.description).append("</b></legend>")
                    .append("<input type=\"number\" name=\"").append(key.name())
                    .append("\" value=\"").append(key.value).append("\" required>")
                    .append(" <span style=\"font-size:0.8em;color:gray;\">").append(key.name()).append("</span>")
                    .append("</fieldset><br>");
        }

        // --- Строковые настройки (input type="text") ---
        for (KeyString key : KeyString.values()) {
            fields.append("<fieldset>")
                    .append("<legend><b>").append(key.description).append("</b></legend>")
                    .append("<input type=\"text\" name=\"").append(key.name())
                    .append("\" value=\"").append(escapeHtml(key.value)).append("\" required>")
                    .append(" <span style=\"font-size:0.8em;color:gray;\">").append(key.name()).append("</span>")
                    .append("</fieldset><br>");
        }

        return "<form method='POST' action='/form'>" +
                fields.toString() +
                "<input type='hidden' name='action' value='settings_save_all'>" +
                "<button type='submit'>Сохранить все</button>" +
                "</form>";
    }

    // Простая защита от XSS для вывода строк в HTML
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    // --- Обновление из POST-запроса (все типы) ---
    public synchronized void updateFromRequest(Map<String, String> params) {
        // Булевы
        for (KeyToggle key : KeyToggle.values()) {
            String val = params.get(key.name());
            if (val != null) {
                val = val.trim().toLowerCase();
                key.value = "true".equals(val) || "on".equals(val) || "1".equals(val);
            }
        }
        // Числовые
        for (KeyValue key : KeyValue.values()) {
            String val = params.get(key.name());
            if (val != null) {
                try {
                    key.value = Integer.parseInt(val.trim());
                } catch (NumberFormatException e) {
                    log.warn("Invalid number for {}: {}, keeping old value", key.name(), val);
                }
            }
        }
        // Строковые
        for (KeyString key : KeyString.values()) {
            String val = params.get(key.name());
            if (val != null) {
                key.value = val.trim(); // сохраняем как есть
            }
        }
        log.info("Settings updated from request (in memory)");
    }

    // --- Веб-обработчики ---

    public Context action(Context context) {
        log.info("Settings page requested");
        context.bodyResponse = renderPage();
        context.code = 200;
        return context;
    }

    public Context handleSaveAll(Context context) {
        log.info("Saving all settings from POST request");
        Map<String, String> params = Parser.bodyToMap(context.body);
        updateFromRequest(params);
        saveToFile();
        context.bodyResponse = renderPage();
        context.code = 200;
        return context;
    }

    public String settingsGet() {
        File file = new File(propertiesFileName);
        if (!file.exists()) {
            log.warn("Settings file not found: {}", propertiesFileName);
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            log.error("Failed to read settings file", e);
            return "";
        }
    }

    public synchronized void settingsPost(String rawContent) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            log.warn("Empty raw content, skipping update");
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFileName))) {
            writer.write(rawContent);
            writer.flush();
            log.info("Settings file overwritten with raw content");
            // Перезагружаем enum-поля из нового файла
            loadFromFile();
        } catch (IOException e) {
            log.error("Failed to write settings file", e);
        }
    }
}