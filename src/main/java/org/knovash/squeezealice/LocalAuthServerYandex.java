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