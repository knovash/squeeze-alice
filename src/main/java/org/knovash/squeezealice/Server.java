package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
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
        // Добавляем контексты и их обработчики
        server.createContext("/", new HandlerAll());          // Существующий обработчик
//        server.createContext("/v1.0/", new HandlerV1());         // Новый обработчик для /v1.0/
//        server.createContext("/alice/", new HandlerAlicePath()); // Новый обработчик для /alice/
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        log.info("SERVER STARTED OK http://localhost:" + config.port);
    }
}