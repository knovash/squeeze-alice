package org.knovash.squeezealice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.utils.Parser;

import java.io.*;
import java.util.*;

import static org.knovash.squeezealice.web.PageIndex.pageOuter;

@Log4j2
public abstract class SettingsBase {

    protected static final ObjectMapper mapper = new ObjectMapper();
    protected final String propertiesFileName;
    public final String saveHandler;
    public final String redirectUrl;   // новый параметр

    // ---- Интерфейсы для ключей ----
    public interface ToggleKey {
        String name();
        boolean getValue();
        void setValue(boolean value);
        String getDescription();
    }

    public interface ValueKey {
        String name();
        int getValue();
        void setValue(int value);
        String getDescription();
    }

    public interface StringKey {
        String name();
        String getValue();
        void setValue(String value);
        String getDescription();
    }

    // ---- Дефолтные методы для получения ключей (можно переопределить в наследниках) ----
    protected List<? extends ToggleKey> getToggleKeys() {
        return Collections.emptyList();
    }

    protected List<? extends ValueKey> getValueKeys() {
        return Collections.emptyList();
    }

    protected List<? extends StringKey> getStringKeys() {
        return Collections.emptyList();
    }

    // ---- Заголовки страницы (можно переопределить) ----
    protected String getPageTitle() {
        return "Настройки";
    }

    protected String getPageHeader() {
        return "Настройки дополнительные";
    }

    // ---- Конструкторы ----
    public SettingsBase() {
        this("data/settings.properties", "settings_save_all", null);
    }

    public SettingsBase(String propertiesFileName, String saveHandler) {
        this(propertiesFileName, saveHandler, null);
    }

    // Новый конструктор с параметром redirectUrl
    public SettingsBase(String propertiesFileName, String saveHandler, String redirectUrl) {
        this.propertiesFileName = propertiesFileName;
        this.saveHandler = saveHandler;
        this.redirectUrl = redirectUrl;
        loadFromFile();
    }

    // ---- Сохранение всех настроек в файл ----
    public synchronized void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(propertiesFileName))) {
            writer.println("# Settings saved");
            writer.println("# " + new Date());

            for (ToggleKey key : getToggleKeys()) {
                writer.println(key.name() + "=" + key.getValue());
            }
            for (ValueKey key : getValueKeys()) {
                writer.println(key.name() + "=" + key.getValue());
            }
            for (StringKey key : getStringKeys()) {
                writer.println(key.name() + "=" + key.getValue());
            }

            writer.flush();
            log.info("Settings saved to " + propertiesFileName);
        } catch (IOException e) {
            log.error("Failed to save settings to file", e);
        }
    }

    // ---- Загрузка настроек из файла ----
    public synchronized void loadFromFile() {
        File file = new File(propertiesFileName);
        if (!file.exists()) {
            log.info("Settings file not found, keeping initial values");
            return;
        }
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            props.load(in);

            for (ToggleKey key : getToggleKeys()) {
                String val = props.getProperty(key.name());
                if (val != null) {
                    key.setValue(Boolean.parseBoolean(val));
                }
            }
            for (ValueKey key : getValueKeys()) {
                String val = props.getProperty(key.name());
                if (val != null) {
                    try {
                        key.setValue(Integer.parseInt(val));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid integer for {}: {}, keeping current", key.name(), val);
                    }
                }
            }
            for (StringKey key : getStringKeys()) {
                String val = props.getProperty(key.name());
                if (val != null) {
                    key.setValue(val);
                }
            }

            log.info("Settings loaded from " + propertiesFileName);
        } catch (IOException e) {
            log.error("Failed to load settings from file, keeping current values", e);
        }
    }

    // ---- Веб-интерфейс ----
    public String renderPage() {
        String innerHtml = renderForm();
        return pageOuter(innerHtml, getPageTitle(), getPageHeader());
    }

    private String renderForm() {
        StringBuilder fields = new StringBuilder();

        // Булевы переключатели
        for (ToggleKey key : getToggleKeys()) {
            fields.append("<fieldset>")
                    .append("<legend><b>").append(escapeHtml(key.getDescription())).append("</b></legend>")
                    .append("<select name=\"").append(key.name()).append("\" required>")
                    .append("<option value=\"true\" ").append(key.getValue() ? "selected" : "").append(">вкл</option>")
                    .append("<option value=\"false\" ").append(!key.getValue() ? "selected" : "").append(">выкл</option>")
                    .append("</select> ")
                    .append("<span style=\"font-size:0.8em;color:gray;\">").append(key.name()).append("</span>")
                    .append("</fieldset><br>");
        }

        // Числовые настройки
        for (ValueKey key : getValueKeys()) {
            fields.append("<fieldset>")
                    .append("<legend><b>").append(escapeHtml(key.getDescription())).append("</b></legend>")
                    .append("<input type=\"number\" name=\"").append(key.name())
                    .append("\" value=\"").append(key.getValue()).append("\" required>")
                    .append(" <span style=\"font-size:0.8em;color:gray;\">").append(key.name()).append("</span>")
                    .append("</fieldset><br>");
        }

        // Строковые настройки
        for (StringKey key : getStringKeys()) {
            fields.append("<fieldset>")
                    .append("<legend><b>").append(escapeHtml(key.getDescription())).append("</b></legend>")
                    .append("<input type=\"text\" name=\"").append(key.name())
                    .append("\" value=\"").append(escapeHtml(key.getValue())).append("\" required>")
                    .append(" <span style=\"font-size:0.8em;color:gray;\">").append(key.name()).append("</span>")
                    .append("</fieldset><br>");
        }

        return "<form method='POST' action='/form'>" +
                fields.toString() +
                "<input type='hidden' name='action' value='" + saveHandler + "'>" +
                "<button type='submit'>Сохранить все</button>" +
                "</form>";
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    // ---- Обновление значений из POST-запроса ----
    public synchronized void updateFromRequest(Map<String, String> params) {
        for (ToggleKey key : getToggleKeys()) {
            String val = params.get(key.name());
            if (val != null) {
                val = val.trim().toLowerCase();
                key.setValue("true".equals(val) || "on".equals(val) || "1".equals(val));
            }
        }
        for (ValueKey key : getValueKeys()) {
            String val = params.get(key.name());
            if (val != null) {
                try {
                    key.setValue(Integer.parseInt(val.trim()));
                } catch (NumberFormatException e) {
                    log.warn("Invalid number for {}: {}, keeping old value", key.name(), val);
                }
            }
        }
        for (StringKey key : getStringKeys()) {
            String val = params.get(key.name());
            if (val != null) {
                key.setValue(val.trim());
            }
        }
        log.info("Settings updated from request");
    }

    // ---- Веб-обработчики ----
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
        applySettings();

        // Если задан URL редиректа – перенаправляем, иначе рендерим страницу
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            context.setRedirect(redirectUrl);
        } else {
            context.bodyResponse = renderPage();
            context.code = 200;
        }
        return context;
    }

    protected abstract void applySettings();

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
            loadFromFile();
        } catch (IOException e) {
            log.error("Failed to write settings file", e);
        }
    }
}