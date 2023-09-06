package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.util.ResourceBundle;

@Log4j2
public class Main {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");
    public static final String SILENCE = bundle.getString("silence");
    private static final String WAKESECONDS = bundle.getString("wake_seconds");
    public static ServerLMS serverLMS = new ServerLMS();

    public static void main(String[] args) {
        log.info("\nSTART MAIN\n");
        log.info(SILENCE);
        serverLMS = new ServerLMS();
//        serverLMS.readFile();
        serverLMS.updatePlayers();
        serverLMS.writeFile();
        serverLMS.readFile();
//        ServerController.start();
    }
}