package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;
import org.knovash.squeezealice.web.PagePlayers;
import org.knovash.squeezealice.web.PageSpotify;
import org.knovash.squeezealice.web.PageYandex;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.Main.rooms;
//import static org.knovash.squeezealice.provider.Yandex.runScenario;

@Log4j2
public class SwitchQueryCommand {

    public static Context action(Context context) {
        HashMap<String, String> queryParams = context.queryMap;
        log.info("QUERY: " + queryParams);

        context.bodyResponse = "BAD REQUEST NO ACTION IN QUERY";
        if (!queryParams.containsKey("action")) return context;
        context.code = 200;
        String action = queryParams.get("action");
        String playerInQuery = queryParams.get("player");
        String roomInQuery = queryParams.get("room");
        log.info("PLAYER: " + playerInQuery + " ROOM: " + roomInQuery);
        Player player = null;
        if (playerInQuery != null) {
            if (playerInQuery.equals("btremote")) {
//                runScenario("уведомление клик");
                log.info("BT PLAYER: " + lmsPlayers.btPlayerName);
                playerInQuery = lmsPlayers.btPlayerName;
            }
            player = lmsPlayers.getPlayerByNearestName(playerInQuery);
            if (player == null) player = lmsPlayers.getPlayerByNearestRoom(playerInQuery);
        }

        String value = queryParams.get("value");
        String playerName;
        String roomName;
        log.info("START SWITCH CASE action: " + action);


        String response = "null";
        if (player != null)
            log.info("SWITCH PLAYER NOT NULL");
            switch (action) {
                case ("channel"):
                    response = player.playChannelRelativeOrAbsolute(value, false);
                    break;
                case ("play"):
                    Player finalPlayer = player;
                    CompletableFuture.runAsync(() -> finalPlayer.turnOnMusic().syncAllOtherPlayingToThis());
                    response = "PLAY";
                    break;
                case ("toggle_music"):
                case ("play_pause"):
                    response = Actions.queryToggleMusic(player);
                    break;
                case ("toggle_music_all"):
                case ("play_pause_all"):
                    response = Actions.queryToggleMusicAll(player);
                    break;
                case ("next"):
                    response = Actions.queryNext(player);
                    break;
                case ("prev"):
                    response = Actions.queryPrev(player);
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
                    response = player.volumeRelativeOrAbsolute("-3", true);
                    break;
                case ("volume_up"):
                    response = player.volumeRelativeOrAbsolute("3", true);
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
                case ("shuffle_on"):
                    player.shuffleOn();
                    response = "SHUFFLE ON";
                    break;
                case ("shuffle_off"):
                    player.shuffleOff();
                    response = "SHUFFLE OFF";
                    break;
                case ("favorites_add"):
                    log.info("CASE FAVORITES ADD");
                    player.favoritesAdd();
                    response = "FAVORITES ADD";
                    break;
                case ("get_last"):
                    response = lmsPlayers.getLastTitle(player);
                    break;
                case ("get_players"):
                    List<String> sss = rooms.stream()
                            .map(r -> r + ":" + lmsPlayers.getPlayerByCorrectRoom(r))
                            .collect(Collectors.toList());
                    log.info(sss);
                    String lll = sss.toString();
                    player.prevTrack().status(50);
                    response = lll;
                    break;
                default:
                    log.info("ACTION NOT FOUND: " + action);
                    response = "ACTION NOT FOUND: " + action;
                    break;
            }
        if (response != "null") {
            context.bodyResponse = response;
            return context;
        }

        log.info("SWITCH PLAYER NULL");
        switch (action) {
            case ("stop_all"):
            case ("pause_all"):
                response = Actions.queryStopMusicAll();
                break;
            case ("get_player"):
                response = lmsPlayers.getPlayerByNearestRoom(value).name;
                break;
            case ("get_room_player"):
                response = lmsPlayers.getPlayerNameByWidgetName(value);
                break;
            case ("get_super_refresh"):
                log.info("TRY SUPER RESRESH -----------");
//                response = lmsPlayers.getSuperRefresh();
                break;
            case ("select"):
                log.info("SSSS  " + roomInQuery + " " + playerInQuery);
                roomName = Utils.getCorrectRoomName(roomInQuery);
                playerName = Utils.getCorrectPlayerName(playerInQuery);
                log.info("DDDD " + roomName + " " + playerName);
                if (roomName != null && playerName != null) {
                    SwitchVoiceCommand.selectPlayerInRoom(playerName, roomName, true);
                    response = "SELECT OK";
                } else
                    response = "SELECT ERROR";
                break;
//            WEB
            case ("state_devices"):
                response = JsonUtils.pojoToJson(SmartHome.devices);
                break;
            case ("state_players"):
                response = JsonUtils.pojoToJson(lmsPlayers);
                break;
//            case ("spotify_save_creds"):
//                SpotifyAuth.save(queryParams);
//                response = PageSpotify.page();
//                break;
//            case ("cred_yandex"):
//                Yandex.writeCredentialsYandex(queryParams);
//                response = PageYandex.page();
//                break;
            case ("yandex_save_client_id"):
                Yandex.saveClientId(queryParams);
                response = PageYandex.page();
                break;

            case ("yandex_get_bearer"):
                Yandex.getBearerToken();
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
//            case ("delay_expire_save"):
//                lmsPlayers.delayExpireSave(queryParams);
//                response = PagePlayers.page();
//                break;
            case ("autoremote_save"):
                lmsPlayers.autoremoteSave(queryParams);
                response = PagePlayers.page();
                break;
//            case ("alt_sync_save"):
//                lmsPlayers.altSyncSave(queryParams);
//                response = PagePlayers.page();
//                break;
//            case ("last_this_save"):
//                lmsPlayers.lastThisSave(queryParams);
//                response = PagePlayers.page();
//                break;
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
            case ("widget"):
                response = Utils.widget();
                break;
            default:
                log.info("ACTION NOT FOUND: " + action);
                response = "ACTION NOT FOUND: " + action;
                break;
        }
        context.bodyResponse = response;
        return context;
    }
}