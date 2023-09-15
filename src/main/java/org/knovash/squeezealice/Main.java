package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.util.ResourceBundle;

@Log4j2
public class Main {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");
    public static final String SILENCE = bundle.getString("silence");
    public static final int PORT = Integer.parseInt(bundle.getString("port"));
    public static final String CONTEXT = bundle.getString("context");

    public static Server server = new Server();
    public static String lmsServer;
    public static String lmsIP;

    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");
        Utils.getLmsIp(args);
        server = new Server();
        server.readServerFile();
        server.updatePlayers();

        ServerController.start();
    }
}