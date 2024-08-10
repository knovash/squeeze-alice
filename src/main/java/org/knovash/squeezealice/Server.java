package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.knovash.squeezealice.Main.port;

@Log4j2
public class Server {

    public static void start() {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/", new Handler());
        server.setExecutor(null);
        server.start();
        log.info("SERVER START http://localhost:" + port);
    }
}