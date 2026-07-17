package org.knovash.squeezealice.cmd;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Tasker;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.player.ActionsAsync;
import org.knovash.squeezealice.player.ActionsSync;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class SwitchNoPlayerCommand {

    public static String run(String action) {
        log.info(start);
        String response = null;
        switch (action) { // комманды выполняемые без плеера
            case "stop_all":
                Tasker.ready = "no";
                ActionsAsync.stopAll();
                log.info(ActionsSync.answer);
                response = "stop all";
                break;
            case "remote_switch":
                String name = ActionsAsync.remoteSwitch();
                response = "Remote switch to: " + name;
                break;
            case "ready": // Таскер ответ когда можно делать апдейт после завершения действий плееров
                response = Tasker.ready();
                break;
            case "update_players":
                lmsPlayers.updatePlayers(); // ручное обновление
                response = "update players";
                break;
            case "spotify_me":
                Spotify.me();
                response = "spotify_me";
                break;
            case "its_alive":
                log.info("ITS ALIVE TO ALL");
//                lmsPlayers.players.get(0).soundAll("beep_long",false,false);
                response = "ITS ALIVE TO ALL";
                break;
            default:
//                log.info("NO PLAYER COMMAND ACTION NOT FOUND: " + action);
                break;
        }
        log.info(finish);
        return response;
    }
}