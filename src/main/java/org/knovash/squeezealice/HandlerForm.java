package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
//import org.knovash.squeezealice.provider.OAuthExample;
import org.knovash.squeezealice.yandex.Yandex;
//import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.web.PageIndex;
import org.knovash.squeezealice.web.PagePlayers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class HandlerForm implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("HANDLER START >>>>>>>>>>>>>>>");
        ContextForm context = ContextForm.contextCreate(httpExchange);
        processContext(context );

        if (context.headers.containsKey("location")){

            log.info("ALT");
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


        sendResponse(httpExchange, context);
        log.info("HANDLER FINISH <<<<<<<<<<<<<<<");
    }

    private ContextForm processContext(ContextForm context) {
        log.info("HANDLER FORM PROCESS CONTEXT START >>>");
        log.info("BODY: " + context.body);
//        Map<String, String> bodyMap = parse(context.body);
        Map<String, String> bodyMap = Parser.run(context.body);

        log.info("PARESER1: " +bodyMap);
//        log.info("PARESER2: " +bodyMap2);

        context.code = 200;
        if (bodyMap.containsKey("action")) {
            String action = bodyMap.get("action");
            switch (action) {
                case ("delay_expire_save"):
                    lmsPlayers.delayExpireSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case ("alt_sync_save"):
                    lmsPlayers.altSyncSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case ("last_this_save"):
                    lmsPlayers.lastThisSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case ("lms_save"):
                    lmsPlayers.lmsSave((HashMap<String, String>) bodyMap);
                    context.code = 302;
                    context.setRedirect("/lms");
                    break;
                case ("statusbar_refresh"):
                    PageIndex.refresh((HashMap<String, String>) bodyMap);
                    context.code = 302;
                    context.setRedirect("/");
                    break;
                case ("player_save"):
                    lmsPlayers.playerSave((HashMap<String, String>) bodyMap);
                    context.code = 302;
                    context.setRedirect("/players");
                    break;
                default:
                    log.info("ACTION ERROR " + action);
                    break;
            }
        }
        log.info("HANDLER FORM FINISH <<<<<");
        return context;
    }

//    public static Map<String, String> parse(String query) {
//        Map<String, String> result = new HashMap<>();
//        if (query == null || query.isEmpty()) {
//            return result;
//        }
//        String[] pairs = query.split("&");
//        for (String pair : pairs) {
//            if (pair.isEmpty()) {
//                continue;
//            }
//            String[] keyValue = pair.split("=", 2);
////            String key = keyValue[0];
//            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
////            String value = keyValue.length > 1 ? keyValue[1] : null;
//            String value = keyValue.length > 1 ?
//                    URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8) :
//                    null;
//            result.put(key, value);
//        }
//        return result;
//    }

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