package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.web.PagePlayers;
import org.knovash.squeezealice.web.PageSpotify;
import org.knovash.squeezealice.web.PageYandex;

import java.util.HashMap;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class SwitchQueryCommand {

    public static Context action(Context context) {
        HashMap<String, String> queryParams = context.queryMap;
        log.info("QUERY: " + queryParams);
        String response;
        context.json = "BAD REQUEST NO ACTION IN QUERY";
        if (!queryParams.containsKey("action")) return context;
        context.code = 200;
        String action = queryParams.get("action");
        String playerInQuery = queryParams.get("player");
        Player player = lmsPlayers.getPlayerByNameInQuery(playerInQuery);
        log.info("PLAYER: " + player);
        String value = queryParams.get("value");
        Yandex.sayBeep();
        switch (action) {
            case ("channel"):
                player.playChannel(Integer.valueOf(value));
                response = "PLAY CHANNEL " + value;
                break;
            case ("play"):
                Actions.turnOnMusic(player);
                response = "PLAY";
                break;
            case ("toggle_music"):
            case ("play_pause"):
                response = Actions.toggleMusic(player);
                break;
            case ("toggle_music_all"):
            case ("play_pause_all"):
                response = Actions.toggleMusicAll(player);
                break;
            case ("stop_all"):
            case ("pause_all"):
                response = Actions.stopMusicAll();
                break;
            case ("next"):
                response = Actions.next(player);
                break;
            case ("prev"):
                response = Actions.prev(player);
                break;
            case ("next_track"):
                player.nextTrack().status(50);
                response = player.name + " - Next track - " + player.title;
                break;
            case ("prev_track"):
                player.prevTrack().status(50);
                response = player.name + " - Next track - " + player.title;
                break;
            case ("next_channel"):
                player.nextChannel();
                response = "NEXT";
                break;
            case ("prev_channel"):
                player.prevChannel();
                response = "PREV";
                break;
            case ("volume_dn"):
                player.volumeGeneral(player, "-3", true);
                response = "VOLUME DN";
                break;
            case ("volume_up"):
                player.volumeGeneral(player, "3", true);
                response = "VOLUME UP";
                break;
            case ("separate_on"):
                player.separateOn();
                response = "SEPARATE ON";
                break;
            case ("separate_off"):
                player.separateOff();
                response = "SEPARATE OFF";
                break;
            case ("transfer"):
            case ("switch_here"):
                player.switchToHere();
                response = "SWITCH HERE";
                break;

//            WEB
            case ("state_devices"):
                response = JsonUtils.pojoToJson(SmartHome.devices);
                break;
            case ("state_players"):
                response = JsonUtils.pojoToJson(lmsPlayers);
                break;
            case ("spotify_save_creds"):
                SpotifyAuth.save(queryParams);
                response = PageSpotify.page();
                break;
            case ("cred_yandex"):
                Yandex.writeCredentialsYandex(queryParams);
                response = PageYandex.page();
                break;
            case ("yandex_save_client_id"):
                Yandex.saveClientId(queryParams);
                response = PageYandex.page();
                break;
            case ("yandex_save_token"):
                Yandex.saveToken(queryParams);
                response = PageYandex.page();
                break;
            case ("player_save"):
                lmsPlayers.playerSave(queryParams);
                response = PagePlayers.page();
                break;
            case ("player_remove"):
                lmsPlayers.playerRemove(queryParams);
                response = PagePlayers.page();
                break;
            case ("restart"):
                Utils.restart();
                response = "RESTART";
                break;
            case ("reboot"):
                Utils.reboot();
                response = "REBOOT";
                break;
            default:
                log.info("ACTION NOT FOUND: " + action);
                response = "ACTION NOT FOUND: " + action;
                break;
        }
        context.json = response;
        return context;
    }
}