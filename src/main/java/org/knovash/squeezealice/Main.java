package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.util.ResourceBundle;

@Log4j2
public class Main {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");
    public static final String SILENCE = bundle.getString("silence");
    public static Server server = new Server();

    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");
        server = new Server();
        server.readFile();
        server.updatePlayers();
        server.writeFile();
        Controller.start();
    }
}