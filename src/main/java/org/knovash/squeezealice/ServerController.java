package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.handler.HandlerAlice;
import org.knovash.squeezealice.handler.HandlerKuzja;
import org.knovash.squeezealice.web.*;
import org.knovash.squeezealice.provider.*;
import org.knovash.squeezealice.provider.HandlerAction;
import org.knovash.squeezealice.provider.HandlerQuery;

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
        server.createContext("/", new HandlerIndex());
        server.createContext("/cmd", new HandlerKuzja());
        server.createContext("/alice", new HandlerAlice());
        server.createContext("/redirect", new HandlerRedirect());
        server.createContext("/spotify", new HandlerSpotify());
        server.createContext("/speakers", new HandlerSpeakers());
        server.createContext("/players", new HandlerPlayers());
        server.createContext("/testcmd", new HandlerTestCommands());

        server.createContext("/v1.0", new HandlerCheck());
        server.createContext("/v1.0/user/unlink", new HandlerUserUnlink());
        server.createContext("/v1.0/user/devices", new HandlerUserDevices());
        server.createContext("/v1.0/user/devices/query", new HandlerQuery());
        server.createContext("/v1.0/user/devices/action", new HandlerAction());

        server.createContext("/auth", new HandlerAuth());
        server.createContext("/token", new HandlerToken());
        server.createContext("/refresh", new HandlerTokenRefresh());
        server.createContext("/redirecturi", new HandlerRedirectUri());

        server.setExecutor(null);
        server.start();
        log.info("http://localhost:" + port);
    }
}