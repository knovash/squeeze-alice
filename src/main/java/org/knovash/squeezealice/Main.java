package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.ServerLMS;

import java.util.ResourceBundle;

@Log4j2
public class Main {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");
    private static final String HOSTNAME = bundle.getString("hostname");
    private static final Integer PORT = Integer.valueOf(bundle.getString("port"));
    private static final String SILENCE = bundle.getString("silence");
    private static final String WAKESECONDS = bundle.getString("wake_seconds");

    public static ServerLMS serverLMS = new ServerLMS();

    public static void main(String[] args) {
        log.info("\nSTART MAIN\n");
        log.info(SILENCE);
        serverLMS = new ServerLMS();



//        serverLMS.readFile();
        serverLMS.updatePlayers();
        serverLMS.writeFile();
        ServerController.start();
    }
}