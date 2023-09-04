package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.ServerLMS;

@Log4j2
public class Main {

    public static void main(String[] args) {
        log.info("\nSTART MAIN\n");
        ServerLMS.countPlayers();
        ServerLMS.updatePlayers();
        ServerLMS.updateFavorites();
        ServerController.start();
    }
}