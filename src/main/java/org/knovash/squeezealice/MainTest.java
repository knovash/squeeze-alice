package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.ResourceBundle;

@Log4j2
public class MainTest {

    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");
        log.info("lmsIP " + Main.lmsIP);
        log.info("lmsPort " + Main.lmsPort);
        log.info("lmsServer " + Main.lmsServer);
        log.info("port " + Main.port);
        log.info("context " + Main.context);
        ArgsParser.parse(args);
        log.info("lmsIP " + Main.lmsIP);
        log.info("lmsPort " + Main.lmsPort);
        log.info("lmsServer " + Main.lmsServer);
        log.info("port " + Main.port);
        log.info("context " + Main.context);

        Main.server = new Server();
        Main.server.readServerFile();
        Main.server.updatePlayers();

        log.info(Main.server.players);
        log.info(Main.server.players.get(1));

        Player player = Main.server.players.get(1);

        log.info(player.timeVolume);

        Map<Integer,Integer> mmm =player.timeVolume;

        JsonUtils.mapToJsonFile(mmm, "mmm.json");

        log.info("TEST  " + Utils.timeVolumeGet(Main.server.players.get(1)));

        log.info( player.timeVolume.entrySet().toString());


//        ServerController.start();
    }
}