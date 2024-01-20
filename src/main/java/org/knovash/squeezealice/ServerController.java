package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;
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


        server.createContext("/cmd", new Handler());
        server.createContext("/alice", new Handler());


        server.createContext("/v1.0/user/unlink", new Handler());
        server.createContext("/v1.0/user/devices", new Handler());


        server.createContext("/", new Handler());
        server.createContext("/spotify", new Handler());
        server.createContext("/yandex", new Handler());
        server.createContext("/speakers", new Handler());
        server.createContext("/players", new Handler());

        server.createContext("/v1.0", new Handler());
        server.createContext("/v1.0/user/devices/query", new Handler());
        server.createContext("/v1.0/user/devices/action", new Handler());

        server.createContext("/auth", new Handler());
        server.createContext("/token", new Handler());
        server.createContext("/refresh", new Handler());
        server.createContext("/bearer", new Handler());

        server.setExecutor(null);
        server.start();
        log.info("http://localhost:" + port);
    }
}