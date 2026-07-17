package org.knovash.squeezealice.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;

@Log4j2
public class HandlerFavicon implements HttpHandler {

    private static final byte[] FAVICON_DATA;
    private static final long FAVICON_LAST_MODIFIED;

    static {
        byte[] data = null;
        long lastModified = 0L;
        try (InputStream is = Server.class.getResourceAsStream("/public/favicon.ico")) {
            if (is != null) {
                data = is.readAllBytes();
                // Запоминаем время загрузки как "последнее изменение" (для кеширования)
                lastModified = System.currentTimeMillis();
                log.info("Favicon loaded, size: {} bytes", data.length);
            } else {
                log.warn("Favicon not found at /public/favicon.ico");
            }
        } catch (IOException e) {
            log.error("Failed to load favicon: {}", e.getMessage());
        }
        FAVICON_DATA = data;
        FAVICON_LAST_MODIFIED = lastModified;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        // Обрабатываем только GET и HEAD
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        if (FAVICON_DATA == null) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        // Заголовки ответа
        exchange.getResponseHeaders().set("Content-Type", "image/x-icon");
        exchange.getResponseHeaders().set("Cache-Control", "public, max-age=31536000, immutable");
        exchange.getResponseHeaders().set("Last-Modified", String.valueOf(FAVICON_LAST_MODIFIED));

        try {
            if ("GET".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(200, FAVICON_DATA.length);
                exchange.getResponseBody().write(FAVICON_DATA);
            } else { // HEAD
                exchange.sendResponseHeaders(200, FAVICON_DATA.length);
                // HEAD не должен отправлять тело
            }
        } catch (IOException e) {
            log.error("Error sending favicon response: {}", e.getMessage());
            // Если заголовки уже отправлены, ничего не делаем, просто закрываем в finally
        } finally {
            exchange.close();
        }
    }
}