package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.util.ResourceBundle;

@Log4j2
public class Main {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");
    public static String lmsIP = bundle.getString("lmsIP");
    public static String lmsPort = bundle.getString("lmsPort");
    public static String lmsServer = bundle.getString("lmsServer");
    public static String silence = bundle.getString("silence");
    public static int port = Integer.parseInt(bundle.getString("port"));
    public static String context = bundle.getString("context");
    public static Server server = new Server();

    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");
        log.info("lmsIP " + lmsIP);
        log.info("lmsPort " + lmsPort);
        log.info("lmsServer " + lmsServer);
        log.info("port " + port);
        log.info("context " + context);
        ArgsParser.parse(args);
        log.info("lmsIP " + lmsIP);
        log.info("lmsPort " + lmsPort);
        log.info("lmsServer " + lmsServer);
        log.info("port " + port);
        log.info("context " + context);

        server = new Server();
        server.readServerFile();
        server.updatePlayers();
        ServerController.start();
    }
}