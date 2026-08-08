package org.knovash.squeezealice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.utils.Parser;

import java.util.*;
import java.util.function.Consumer;

import static org.knovash.squeezealice.web.PageIndex.pageOuter;

@Log4j2
public abstract class ActionsBase {

    protected static final ObjectMapper mapper = new ObjectMapper();

    // ---- Внутренний класс для описания действия ----
    public static class Action {
        private final String id;
        private final String description;
        private final Consumer<Context> action;

        public Action(String id, String description, Consumer<Context> action) {
            this.id = id;
            this.description = description;
            this.action = action;
        }

        public String getId() { return id; }
        public String getDescription() { return description; }
        public Consumer<Context> getAction() { return action; }
    }

    // ---- Абстрактные методы, которые должны быть реализованы в наследниках ----
    protected abstract List<Action> getActions();

    protected String getPageTitle() {
        return "Действия";
    }

    protected String getPageHeader() {
        return "Управление действиями";
    }

    // ---- Веб-рендеринг ----
    public String renderPage() {
        StringBuilder buttonsHtml = new StringBuilder();
        for (Action action : getActions()) {
            buttonsHtml.append("<form method='POST' action='/form' style='display:inline; margin:5px;'>")
                    .append("<input type='hidden' name='action' value='").append(escapeHtml(action.getId())).append("'>")
                    .append("<button type='submit'>").append(escapeHtml(action.getDescription())).append("</button>")
                    .append("</form>");
        }

        String innerHtml = "<div style='padding:20px;'>" +
                "<h3>Нажмите кнопку для выполнения действия:</h3>" +
                buttonsHtml.toString() +
                "</div>";

        return pageOuter(innerHtml, getPageTitle(), getPageHeader());
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    // ---- Обработчики запросов ----
    public Context action(Context context) {
        log.info("Actions page requested");
        context.bodyResponse = renderPage();
        context.code = 200;
        return context;
    }

    public Context handleAction(Context context) {
        Map<String, String> params = Parser.bodyToMap(context.body);
        String actionId = params.get("action");

        if (actionId == null || actionId.isEmpty()) {
            log.warn("No action parameter found in request");
            context.bodyResponse = "Ошибка: не указано действие";
            context.code = 400;
            return context;
        }

        // Ищем действие по ID
        Optional<Action> found = getActions().stream()
                .filter(a -> a.getId().equals(actionId))
                .findFirst();

        if (!found.isPresent()) {
            log.warn("Unknown action ID: {}", actionId);
            context.bodyResponse = "Ошибка: неизвестное действие";
            context.code = 404;
            return context;
        }

        Action action = found.get();
        log.info("Executing action: {} ({})", action.getId(), action.getDescription());

        try {
            // Выполняем действие
            action.getAction().accept(context);
            // После выполнения перенаправляем обратно на страницу действий
            context.setRedirect("/actions");   // предполагаем, что GET-обработчик зарегистрирован на "/actions"
        } catch (Exception e) {
            log.error("Error executing action " + actionId, e);
            context.bodyResponse = "Ошибка при выполнении действия: " + e.getMessage();
            context.code = 500;
        }

        return context;
    }
}