package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.web.PageIndex;
import org.knovash.squeezealice.web.PagePlayers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.web.PagePlayers.*;

@Log4j2
public class HandlerForm implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("HANDLER START >>>>>>>>>>>>>>>");
        ContextForm context = ContextForm.contextCreate(httpExchange);



        log.info("PROCESS CONTEXT");
        processContext(context);

//        если редирект
        if (context.headers.containsKey("location")) {
            log.info("REDIRRECT location");
            String response = context.bodyResponse;
//        отправка ответа сервера
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            httpExchange.getResponseHeaders().putAll(context.headers);
            httpExchange.sendResponseHeaders(context.code, responseBytes.length);
            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                outputStream.write(responseBytes);
            }
            log.info("HTTP HANDLER FINISH <<<<<<<<<<<");
        }

        log.info("SEND RESPONSE context");
        sendResponse(httpExchange, context);
        log.info("HANDLER FINISH <<<<<<<<<<<<<<<");
    }

    private ContextForm processContext(ContextForm context) {
        log.info("PROCESS CONTEXT START");
        Map<String, String> bodyMap = Parser.run(context.body);
        context.code = 200;
        if (bodyMap.containsKey("action")) {
            String action = bodyMap.get("action");
            log.info("SWITCH CASE action: " + action);
            switch (action) {
//  страница главная
                case (statusbar_refresh): // информация кнопка обновить
                    PageIndex.refresh((HashMap<String, String>) bodyMap);
                    context.code = 302;
                    context.setRedirect("/");
                    break;
//  страница Настройка плееров
                case (delay_expire_save): // время до сброса громкости
                    lmsPlayers.delayExpireSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case (autoremote_save): // таскер урл для обновления виджета НЕГОТОВО
                    lmsPlayers.autoremoteSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case (alt_sync_save): // синхронизация альтернативная
                    lmsPlayers.altSyncSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case (last_this_save): // включать последнее игравшее тут
                    lmsPlayers.lastThisSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case (player_save): // плеер сохранить
                    lmsPlayers.playerSave((HashMap<String, String>) bodyMap);
                    context.code = 302;
                    context.setRedirect("/players");
                    break;
//  страница Настройка ЛМС
                case (lms_save): // кнопка Сохранить
                    lmsPlayers.lmsSave((HashMap<String, String>) bodyMap);
                    context.code = 302;
                    context.setRedirect("/lms");
                    break;
                default:
                    log.info("ACTION ERROR " + action);
                    break;
            }
        }
        log.info("PROCESS CONTEXT FINISH");
        return context;
    }


    private void sendResponse(HttpExchange exchange, ContextForm context) throws IOException {
        log.info("SEND RESPONSE");
        context.responseHeaders.set("Content-Type", "text/html; charset=UTF-8");
        context.responseHeaders.set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().putAll(context.responseHeaders);
        // Обработка редиректов
        if (context.code >= 300 && context.code < 400) {
            log.info("REDIRECT " + context.code);
            exchange.getResponseHeaders().putAll(context.responseHeaders);
            exchange.sendResponseHeaders(context.code, -1); // Без тела ответа
            return;
        }
        byte[] responseBytes = context.bodyResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().putAll(context.responseHeaders);
        exchange.sendResponseHeaders(context.code, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}