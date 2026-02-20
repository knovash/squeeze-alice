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
        Context context = Context.contextCreate(httpExchange);

        log.info("PROCESS CONTEXT");
        processContext(context);

        // проверяем responseHeaders на наличие редиректа
        if (context.responseHeaders.containsKey("Location")) {
            log.info("REDIRECT location");
            String response = context.bodyResponse;
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            httpExchange.getResponseHeaders().putAll(context.responseHeaders);
            httpExchange.sendResponseHeaders(context.code, responseBytes.length);
            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                outputStream.write(responseBytes);
            }
            log.info("HTTP HANDLER FINISH <<<<<<<<<<<");
            return; // важно выйти, чтобы не отправлять ответ повторно
        }

        log.info("SEND RESPONSE context");
        sendResponse(httpExchange, context);
        log.info("HANDLER FINISH <<<<<<<<<<<<<<<");
    }

    private void processContext(Context context) {
        log.info("PROCESS CONTEXT START");
        Map<String, String> bodyMap = Parser.run(context.body);
        context.code = 200;
        if (bodyMap.containsKey("action")) {
            String action = bodyMap.get("action");
            log.info("SWITCH CASE action: " + action);
            switch (action) {
                case statusbar_refresh:
                    PageIndex.refresh((HashMap<String, String>) bodyMap);
                    context.setRedirect("/");
                    break;
                case delay_expire_save:
                    lmsPlayers.delayExpireSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case toggle_wake_save:
                    lmsPlayers.toggleWakeSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case autoremote_save:
                    lmsPlayers.autoremoteSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case autoremote_remove:
                    lmsPlayers.autoremoteRemove((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case alt_sync_save:
                    lmsPlayers.altSyncSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case last_this_save:
                    lmsPlayers.lastThisSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case player_save:
                    lmsPlayers.playerSave((HashMap<String, String>) bodyMap);
                    context.setRedirect("/players");
                    break;
                case player_remove:
                    lmsPlayers.playerRemove((HashMap<String, String>) bodyMap);
                    context.setRedirect("/players");
                    break;
                case lms_save:
                    lmsPlayers.lmsSave((HashMap<String, String>) bodyMap);
                    context.setRedirect("/lms");
                    break;
                default:
                    log.info("ACTION ERROR " + action);
                    break;
            }
        }
        log.info("PROCESS CONTEXT FINISH");
    }

    private void sendResponse(HttpExchange exchange, Context context) throws IOException {
        log.info("SEND RESPONSE");
        context.responseHeaders.set("Content-Type", "text/html; charset=UTF-8");
        context.responseHeaders.set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().putAll(context.responseHeaders);

        if (context.code >= 300 && context.code < 400) {
            log.info("REDIRECT " + context.code);
            exchange.sendResponseHeaders(context.code, -1);
            return;
        }

        byte[] responseBytes = context.bodyResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(context.code, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}