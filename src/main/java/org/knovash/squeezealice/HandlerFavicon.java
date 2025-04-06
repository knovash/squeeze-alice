package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;

@Log4j2
public class HandlerFavicon implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
//        log.info("HANDLER FAVICON START >>>");
        try (InputStream is = Server.class.getResourceAsStream("/public/favicon.ico")) {
            if (is != null) {
                byte[] iconData = is.readAllBytes();
                exchange.getResponseHeaders().set("Content-Type", "image/x-icon");
                exchange.sendResponseHeaders(200, iconData.length);
                exchange.getResponseBody().write(iconData);
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        } catch (IOException e) {
            log.error("Error serving favicon: {}", e.getMessage());
            exchange.sendResponseHeaders(500, -1);
        } finally {
            exchange.close();
        }
//        log.info("HANDLER FAVICON FINISH <<<");
    }
}