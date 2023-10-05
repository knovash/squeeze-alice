package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.util.ResourceBundle;

@Log4j2
public class Main {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");
    public static String lmsIP = bundle.getString("lmsIP"); // lmsIP=192.168.1.52
    public static String lmsPort = bundle.getString("lmsPort"); // lmsPort=9000
    public static String lmsServer = "http://" + lmsIP + ":" + lmsPort + "/jsonrpc.js/";
    public static String silence = bundle.getString("silence");
    public static int port = Integer.parseInt(bundle.getString("port"));
    public static String context = bundle.getString("context");
    public static Server server = new Server();

    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");
        log.info("lmsServer " + lmsServer);
        log.info("port " + port);
        log.info("context " + context);
        ArgsParser.parse(args);
        log.info("lmsServer " + lmsServer);
        log.info("port " + port);
        log.info("context " + context);

        server = new Server();
        server.readServerFile();
        server.updatePlayers();
        ServerController.start();
    }
}