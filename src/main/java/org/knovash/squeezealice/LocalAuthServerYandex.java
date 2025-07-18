package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.yandex.YandexJwtUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.Main.hive;

@Log4j2
public class LocalAuthServerYandex {

    private static final Map<String, String> tokenStorage = new HashMap<>();

//    static class TokenHandler implements HttpHandler {
//
//        @Override
//        public void handle(HttpExchange exchange) throws IOException {
//            log.info("TOKEN HANDLER");
//            try {
//                String query = exchange.getRequestURI().getQuery();
//                String sessionId = getParam(query, "sessionId");
//                String token = getParam(query, "token");
//                log.info("TOKEN: " + token);
//
//                tokenStorage.put(sessionId, token);
//                sendResponse(exchange, 200, "Token stored");
//
//            } catch (Exception e) {
//                sendError(exchange, 400, "Invalid request");
//            }
//        }
//    }

//    // Метод для извлечения параметров из URL-запроса
//    private static String getParam(String query, String paramName) {
//        if (query == null || query.isEmpty()) {
//            throw new IllegalArgumentException("Query string is empty");
//        }
//
//        String[] pairs = query.split("&");
//        for (String pair : pairs) {
//            int idx = pair.indexOf("=");
//            String key = (idx > 0) ? pair.substring(0, idx) : pair;
//            log.info("KEY: " + key);
//            if (key.equals(paramName)) {
//                return (idx > 0 && pair.length() > idx + 1)
//                        ? java.net.URLDecoder.decode(pair.substring(idx + 1), java.nio.charset.StandardCharsets.UTF_8)
//                        : "";
//            }
//        }
//        throw new IllegalArgumentException("Parameter not found: " + paramName);
//    }

//    // Метод для отправки успешного HTTP-ответа
//    private static void sendResponse(HttpExchange exchange, int code, String message) throws IOException {
//        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
//        byte[] responseBytes = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
//        exchange.sendResponseHeaders(code, responseBytes.length);
//        try (OutputStream os = exchange.getResponseBody()) {
//            os.write(responseBytes);
//        }
//    }

//    // Метод для отправки ошибок
//    private static void sendError(HttpExchange exchange, int code, String message) throws IOException {
//        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
//        byte[] responseBytes = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
//        exchange.sendResponseHeaders(code, responseBytes.length);
//        try (OutputStream os = exchange.getResponseBody()) {
//            os.write(responseBytes);
//        }
//    }

//    public static String getToken(String sessionId) {
//        return tokenStorage.getOrDefault(sessionId, null);
//    }

    static class AuthHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            log.info("START AuthHandler");

            String displayName = "";
            String token;
            token = config.yandexToken;
            if (token != null && !token.equals(""))
                displayName = YandexJwtUtils.getValueByTokenAndKey(token, "display_name");

            // Генерация уникального sessionId
            String sessionId = UUID.randomUUID().toString();
            log.info("sessionId: " + sessionId);
            // Формирование URL для авторизации на сервере2
            String authUrl = "https://alice-lms.zeabur.app/authorize?state=" + sessionId;
            log.info("authUrl: " + authUrl);
            // HTML страница с JavaScript для опроса токена
            String htmlResponse = "<html><body>" +
                    "Вы вошли как: " + displayName + "<br>" +
                    "<a href='" + authUrl + "'>Авторизоваться через Яндекс</a>" +
                    "</body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, htmlResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                log.info("TRY");
                os.write(htmlResponse.getBytes());
            }

            log.info("PUBLISH REQUEST FOR TOKEN");
            Context context = new Context();

            hive.publishAndWaitForResponse("from_local_request", context, 30, "token", sessionId);
        }
    }
}