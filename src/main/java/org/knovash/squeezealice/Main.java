package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.util.ResourceBundle;

@Log4j2
public class Main {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");
    public static final String SILENCE = bundle.getString("silence");
    public static Server server = new Server();
    public static String argsLMS;

    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");
        if (args.length != 0) {
            argsLMS = "http://" + args[0] +":9000/jsonrpc.js" ;
            log.info("LMS IP: " + argsLMS);
        }

        server = new Server();
        Utils.readAltNames();
        server.readFile();
        server.updatePlayers();
        Controller.start();
    }
}