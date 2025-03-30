package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static org.knovash.squeezealice.Main.config;

@Log4j2
public class Server {

    public static void start() {
        log.info("SERVER STARTING...");
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(config.port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Добавляем обработчик для favicon.ico ПЕРВЫМ
//        server.createContext("/favicon.ico", exchange -> {
//            try (InputStream is = Server.class.getResourceAsStream("/public/favicon.ico")) {
//                if (is != null) {
//                    byte[] iconData = is.readAllBytes();
//                    exchange.getResponseHeaders().set("Content-Type", "image/x-icon");
//                    exchange.sendResponseHeaders(200, iconData.length);
//                    exchange.getResponseBody().write(iconData);
//                } else {
//                    exchange.sendResponseHeaders(404, -1);
//                }
//            } catch (IOException e) {
//                log.error("Error serving favicon: {}", e.getMessage());
//                exchange.sendResponseHeaders(500, -1);
//            } finally {
//                exchange.close();
//            }
//        });

        server.createContext("/favicon.ico", new HandlerFavicon());

        server.createContext("/", new HandlerAll());
        server.createContext("/form", new HandlerForm());
        server.createContext("/html", new HandlerHtml());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        log.info("SERVER STARTED OK http://localhost:" + config.port);
    }
}