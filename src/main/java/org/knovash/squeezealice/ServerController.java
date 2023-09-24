package org.knovash.squeezealice;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;

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
        server.createContext("/cmd", new Handler());
        server.setExecutor(null);
        server.start();
        String myIP = MyIP.getIP();
        log.info("try access http://"+ myIP+":"+ port +"/"+ context + "?action=state");
    }
}