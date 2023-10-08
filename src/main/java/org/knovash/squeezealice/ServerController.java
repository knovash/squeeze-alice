package org.knovash.squeezealice;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.*;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class ServerController {

    public static void start() {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/", new HandlerIndex());
        server.createContext("/cmd", new HandlerKuzja());
        server.createContext("/alice", new HandlerAlice());
        server.createContext("/spotify", new HandlerSpotify());
        server.createContext("/v1.0", new HandlerCheck());
        server.createContext("/v1.0/user/unlink", new HandlerUnlink());
        server.createContext("/v1.0/user/devices", new HandlerDevices());
        server.createContext("/v1.0/user/devices/query", new HandlerQuery());
        server.createContext("/v1.0/user/devices/action", new HandlerAction());
        server.createContext("/auth", new HandlerAuth());
        server.createContext("/token", new HandlerToken());
        server.createContext("/tokenref", new HandlerTokenRef());
        server.setExecutor(null);
        server.start();
        log.info("http://localhost:"+ port);
    }
}