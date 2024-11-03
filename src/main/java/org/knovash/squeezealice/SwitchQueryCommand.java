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
        String roomInQuery = queryParams.get("room");
        log.info("PLAYER: " + playerInQuery + " ROOM: " + roomInQuery);

        Player player;
        if (roomInQuery != null) player = lmsPlayers.getPlayerByNearestRoom(roomInQuery);
        else player = lmsPlayers.getPlayerByNameInQuery(playerInQuery);
        log.info("PLAYER: " + player);


        String value = queryParams.get("value");
        Yandex.sayBeep();
        String playerName;
        String roomName;
        switch (action) {
            case ("channel"):
//                response = Actions.queryChannelPlay(player, value);
                response = player.playChannelRelativeOrAbsolute(value, false);
                break;
            case ("play"):
                CompletableFuture.runAsync(() -> player.turnOnMusic().syncAllOtherPlayingToThis());
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
            case ("stop_all"):
            case ("pause_all"):
                response = Actions.queryStopMusicAll();
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
                player.volumeRelativeOrAbsolute("-3", true);
                response = "VOLUME DN";
                break;
            case ("volume_up"):
                player.volumeRelativeOrAbsolute("3", true);
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
            case ("get_player"):
                response = lmsPlayers.getPlayerByNearestRoom(value).name;
                break;
            case ("get_rooms_players"):
                response = lmsPlayers.roomsAndPlayersAllWidgets();
                break;
            case ("get_room_player"):
                response = lmsPlayers.getPlayerNameByWidgetName(value);
                break;
            case ("get_players_list"):
                response = lmsPlayers.playerVolumeModeTitle();
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
            case ("delay_expire_save"):
                lmsPlayers.delayExpireSave(queryParams);
                response = PagePlayers.page();
                break;
            case ("alt_sync_save"):
                lmsPlayers.altSyncSave(queryParams);
                response = PagePlayers.page();
                break;
            case ("last_this_save"):
                lmsPlayers.lastThisSave(queryParams);
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
            case ("widget"):
                response = Utils.widget();
                break;
            case ("test"):
                log.info("TEST");
//                player.syncAllOtherPlayingToThis();
                response = "TEST";
                break;
            case ("say"):
                response = Actions.queryToggleMusic(player);
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