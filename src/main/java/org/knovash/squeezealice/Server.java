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

        server.createContext("/favicon.ico", new HandlerFavicon());
        server.createContext("/", new HandlerAll());
        server.createContext("/form", new HandlerForm());
        server.createContext("/html", new HandlerHtml());
//     запрос на выполнение авторизации в Яндекс сервером в облаке,
//     токен вернется на колбэк в облаке и вернется сюда через mqtt
        server.createContext("/auth", new LocalAuthServerYandex.AuthHandler());
//     запрос на выполнение авторизации в Spotify сервером в облаке,
//     токен вернется на колбэк в облаке и вернется сюда через mqtt
        server.createContext("/auth_spotify", new LocalAuthServerSpotify.AuthHandler());

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        log.info("SERVER STARTED OK http://localhost:" + config.port);
    }
}