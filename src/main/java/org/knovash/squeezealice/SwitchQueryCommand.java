package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.spotify.Spotify;
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
        log.info("ACTION: " + action);
        String playerInQuery = queryParams.get("player");
        log.info("PLAYER IN QUERY: " + playerInQuery);
        Player player = lmsPlayers.getPlayerByNameInQuery(playerInQuery);
        log.info("PLAYER BY NAME IN QUERY: " + player);
        String value = queryParams.get("value");
        log.info("VALUE: " + value);
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
                log.info("CASE TOGGLE MUSIC " + player);
                response = Actions.toggleMusic(player);
                break;
            case ("toggle_music_all"):
            case ("play_pause_all"):
                log.info("CASE TOGGLE MUSIC ALL");
                response = Actions.toggleMusicAll(player);
                break;
            case ("stop_all"):
            case ("pause_all"):
                log.info("CASE STOP MUSIC ALL");
                response = Actions.stopMusicAll();
                break;
            case ("next"):
                player.next().status();
                response = player.name + " - Next - " + player.title;
                break;
            case ("prev"):
                player.prev().status();
                response = player.name + " - Prev - " + player.title;
                break;
            case ("next_track"):
                player.nextTrack().status();
                response = player.name + " - Next track - " + player.title;
                break;
            case ("prev_track"):
                player.prevTrack().status();
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
                player.volumeSet("-3");
                response = "VOLUME DN";
                break;
            case ("volume_up"):
                player.volumeSet("+3");
                response = "VOLUME UP";
                break;
            case ("separate_on"):
                player.separate_on();
                response = "SEPARATE ON";
                break;
            case ("alone_on"):
                player.alone_on();
                response = "ALONE ON";
                break;
            case ("separate_alone_off"):
                player.separate_alone_off();
                response = "ALONE OK";
                break;
            case ("transfer"):
                Spotify.transfer(player);
                response = "TRANSFER";
                break;
//            case ("get_rooms"):
//                Utils.listDevicesRooms();
//                response = Utils.listDevicesRooms().toString();
//                break;

//            case ("log"): // WEB HOME
//                log.info("SHOW LOG");
//                response = Utils.logLastLines(queryParams);
//                break;
            case ("silence"): // TASKER
                log.info("SILENCE");
                player.playSilence();
                response = "SILENCE COMPLETE";
                break;
            case ("change_value"): // TASKER
                log.info("CHANGE PLAYER VALUE");
                Utils.changePlayerValue(queryParams);
                response = "VALUE COMPLETE";
                break;
            case ("state_devices"):
                log.info("STATE ALICE DEVICES");
                response = JsonUtils.pojoToJson(SmartHome.devices);
                break;
            case ("state_players"):
                log.info("STATE LMS PLAYERS");
                response = JsonUtils.pojoToJson(lmsPlayers);
                break;
//            case ("time_volume_get"):
//                log.info("SEND TIME AND VOLUME");
//                response = Utils.timeVolumeGet(player);
//                break;
//            case ("time_volume_set"):
//                log.info("CHANGE TIME AND VOLUME");
//                response = Utils.timeVolumeSet(player, queryParams);
//                break;
//            case ("time_volume_del"):
//                log.info("DELETE TIME AND VOLUME");
//                response = Utils.timeVolumeDel(player, queryParams);
//                break;
            case ("spotify_save_creds"):
                log.info("CREDENTIALS SPOTIFY");
                SpotifyAuth.save(queryParams);
                response = PageSpotify.page();
                break;
            case ("cred_yandex"):
                log.info("CREDENTIALS YANDEX");
                Yandex.writeCredentialsYandex(queryParams);
                response = PageYandex.page();
                break;
            case ("yandex_save_client_id"):
                log.info("YANDEX SAVE CLIENT ID");
                Yandex.saveClientId(queryParams);
                response = PageYandex.page();
                break;
            case ("yandex_save_token"):
                log.info("YANDEX SAVE TOKEN");
                Yandex.saveToken(queryParams);
                response = PageYandex.page();
                break;

//      WEB PLAYERS
            case ("player_save"):
                log.info("PLAYER SAVE");
                lmsPlayers.playerSave(queryParams);
                lmsPlayers.writePlayers();
                response = PagePlayers.page();
                break;
            case ("player_remove"):
                log.info("PLAYER REMOVE");
                lmsPlayers.playerRemove(queryParams);
                lmsPlayers.writePlayers();
                response = PagePlayers.page();
                break;
            case ("restart"):
                log.info("RESTART SERVER");
                Utils.restart();
                response = "RESTART";
                break;
            case ("reset_players"):
                log.info("RESET PLAYERS");
                lmsPlayers.resetPlayers();
                response = "RESET PLAYERS";
                break;
            case ("reboot"):
                log.info("REBOOT SERVER");
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