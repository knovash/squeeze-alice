package org.knovash.squeezealice.volumio;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.SmartHome;

import java.util.Objects;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class Volumio {

    public static String volumioPlayerName = "Volumio";


    public static void createPlayer() {
        log.info("VOLUMIO TURN ON");

        lmsPlayers.logPlayersNames();

        if (lmsPlayers.playerByName(volumioPlayerName) != null) {
            log.info("ALREADY CREATED");
            return;
        }

        VolumioPlayer volumioPlayer = new VolumioPlayer("volumio");
        volumioPlayer.baseUrl = "http://" + config.volumioIp;
        volumioPlayer.room = null;
        volumioPlayer.name = volumioPlayerName;
        volumioPlayer.playing = false;
        volumioPlayer.mode = "stop";
        volumioPlayer.connected = true;
        volumioPlayer.separateFlagTrue();
        log.info("------PLAYER VOLUMIO: " + volumioPlayer);
        log.info("------PLAYER VOLUMIO: " + volumioPlayer.getClass().getName());
        lmsPlayers.players.add(volumioPlayer);
        log.info("VOLUMIO " + lmsPlayers.playerByName("volumio"));

        lmsPlayers.logPlayersNames();
    }

    public static void removePlayer() {
        log.info("VOLUMIO TURN OFF");

        lmsPlayers.logPlayersNames();
        lmsPlayers.playerByName(volumioPlayerName).remove();
        lmsPlayers.logPlayersNames();

    }

}
