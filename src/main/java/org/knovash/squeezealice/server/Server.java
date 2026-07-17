package org.knovash.squeezealice.server;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.auth.LocalAuthServerSpotify;
import org.knovash.squeezealice.auth.LocalAuthServerYandex;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static org.knovash.squeezealice.Main.config;

@Log4j2
public class Server {

    public static void start() {
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

        // Авторизация через Яндекс
        server.createContext("/auth", new LocalAuthServerYandex());
        // Авторизация через Spotify
        server.createContext("/auth_spotify", new LocalAuthServerSpotify());

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        log.info("SERVER START http://localhost:" + config.port);
    }
}