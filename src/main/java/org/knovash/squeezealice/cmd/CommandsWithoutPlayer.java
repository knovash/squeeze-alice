package org.knovash.squeezealice.cmd;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Tasker;
import org.knovash.squeezealice.player.ActionsAsync;

import static org.knovash.squeezealice.Main.finish;
import static org.knovash.squeezealice.Main.start;
import static org.knovash.squeezealice.web.SettingsTasker.settingsTasker;

@Log4j2
public class CommandsWithoutPlayer {

    public static String run(String action, String value, String body) {
        log.info(start);
        String response = action + "OK";
        switch (action) {
            case "stop_all":
                ActionsAsync.stopAll();
                break;
            case "remote_switch":
                ActionsAsync.remoteSwitchToNextPlayer(); // переключить пульт на следущий плеер
                break;
            case "ready": // Таскер ответ когда можно делать апдейт после завершения действий плееров
                response = Tasker.ready();
                break;
            //case "update_players":
            //    lmsPlayers.updatePlayers(); // ручное обновление
            //    break;
            //case "spotify_me":
            //    Spotify.me();
            //    break;
            case "its_alive":
                log.info("ITS ALIVE"); // для теста
                break;
            case "settings_get": // Tasker получает настройки
                settingsTasker.saveToFile();
                response = settingsTasker.settingsGet();
                break;
            case "settings_post": // Tasker присылает измененные настройки
                settingsTasker.settingsPost(body);
                break;
            case "tasker_ip": // Tasker присылает свой ip
                Tasker.taskerIpSet(body);
                break;
            default:
                response =null;
                break;
        }
        log.info(finish);
        return response;
    }
}