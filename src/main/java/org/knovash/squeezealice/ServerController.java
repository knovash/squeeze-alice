package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Log4j2
public class ServerController {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");
    private static final String HOSTNAME = bundle.getString("hostname");
    private static final Integer PORT = Integer.valueOf(bundle.getString("port"));
    private static final String CONTEXT = bundle.getString("context");

    public static void start() {
        log.info("\nSTART\n");
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(HOSTNAME, PORT), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext(CONTEXT, new Handler());
        server.setExecutor(threadPoolExecutor);
        server.start();
        log.info("\nServer started on port 8001\n");
    }
}