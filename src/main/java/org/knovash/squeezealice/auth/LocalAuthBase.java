package org.knovash.squeezealice.auth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import static org.knovash.squeezealice.Main.hive;

@Log4j2
public abstract class LocalAuthBase implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info(Main.start);

        String displayName = getDisplayName();
        String sessionId = UUID.randomUUID().toString();
        log.info("sessionId: " + sessionId);

        String authUrl = getAuthUrl(sessionId);
        log.info("authUrl: " + authUrl);

        String htmlResponse = "<html><body>" +
                "Вы вошли как: " + displayName + "<br>" +
                "<a href='" + authUrl + "'>" + getLinkText() + "</a>" +
                "</body></html>";

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, htmlResponse.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(htmlResponse.getBytes());
        }

        log.info("PUBLISH REQUEST FOR TOKEN");
        Context context = new Context();
        hive.publishAndWaitForResponse("from_local_request", context, 30, getMqttAction(), sessionId);

        log.info(Main.finish);
    }

    protected abstract String getDisplayName();
    protected abstract String getAuthUrl(String sessionId);
    protected abstract String getMqttAction();
    protected abstract String getLinkText();
}