package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.handler.HandlerKuzja;
import org.knovash.squeezealice.provider.*;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.knovash.squeezealice.Main.port;

@Log4j2
public class ServerController {

    public static void start() {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        server.createContext("/cmd", new HandlerUniversal());
        server.createContext("/alice", new HandlerUniversal());


        server.createContext("/v1.0/user/unlink", new HandlerUserUnlink());
        server.createContext("/v1.0/user/devices", new HandlerUserDevices());


        server.createContext("/", new HandlerUniversal());
        server.createContext("/spotify", new HandlerUniversal());
        server.createContext("/yandex", new HandlerUniversal());
        server.createContext("/speakers", new HandlerUniversal());
        server.createContext("/players", new HandlerUniversal());

        server.createContext("/v1.0", new HandlerUniversal());
        server.createContext("/v1.0/user/devices/query", new HandlerUniversal());
        server.createContext("/v1.0/user/devices/action", new HandlerAction());

        server.createContext("/auth", new HandlerAuth());
        server.createContext("/token", new HandlerToken());
        server.createContext("/refresh", new HandlerUniversal());
        server.createContext("/bearer", new HandlerUniversal());

        server.setExecutor(null);
        server.start();
        log.info("http://localhost:" + port);
    }
}