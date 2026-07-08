package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.voice.ActionsAsync;
import org.knovash.squeezealice.voice.ActionsSync;
import org.knovash.squeezealice.yandex.Yandex;

import java.util.HashMap;
import java.util.Map;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class SwitchQueryCommand {

    public static Context action(Context context) {
        log.info(start);
        String aliceIdRoom = null;
        Player player = null;
//        -----------------------
        HashMap<String, String> queryParams = context.queryMap;
        context.bodyResponse = "BAD REQUEST NO ACTION IN QUERY";
        if (!queryParams.containsKey("action")) return context;

        context.code = 200;
        String action = queryParams.get("action");
        String playerName = queryParams.get("player");
        String room = queryParams.get("room");
        String value = queryParams.get("value");
        String volume = queryParams.get("volume");
        String response = "null";
        log.info("QUERY PARAMS: PLAYER: " + playerName + " ROOM: " + room + " ACTION: " + action + " VALUE: " + value + " VOLUME: " + volume);

        if (value != null) {
            value = value.toLowerCase();
            value = value.replace("+", " ");
        }
// -------------------------------------
        if (!"signal".equals(action)) lmsPlayers.updatePlayers();
        // обновить перед всеми командами с пульта или планшета /cmd
//        ----------------------------
        player = lmsPlayers.playerByPlayerNameOrRoomName(playerName,room);
        if (room == null && player != null) room = player.room;
        log.info("PLAYER: " + player);
//        if (room != null) aliceIdRoom = getAliceIdByRoom(room); // получить id по комнате
        log.info("ROOM: " + room);
//        log.info("ALICE ID: " + aliceIdRoom);
        log.info("COMMAND TO PLAYER: " + player);
//        -----------------------------------------------


        switch (action) { // БЕЗ ПЛЕЕРА
            case "stop_all":
                Tasker.ready = "no";
                ActionsAsync.stopAll();
                response = "stop all";
                break;
            case "remote_switch":
                String name = ActionsSync.remoteSwitch();
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
            case "say":
                ActionsSync.sayMyText();
                response = "say";
                break;
            default:
                log.info("ACTION 1 NOT FOUND: " + action);
//                response = "ACTION NOT FOUND: " + action;
                break;
        }

//        -----------------



if (response.equals("null") && player != null)
        switch (action) { // ТОЛЬКО С ПЛЕЕРОМ
            // ========== Основные операции управления плеером ==========
            case "volume_dn":
//                player.volume("-3");
                if (value == null) value = "-3";
                player.volumeSetLimited("-" + value);
                response = player.name + " - play volume " + value;
                break;
            case "volume_up":
//                player.volume("+3");
                if (value == null) value = "+3";
                player.volumeSetLimited("+" + value);
                response = player.name + " - play volume " + value;
                break;
            case "channel":
                Tasker.ready = "no";
//                int vv = Integer.parseInt(value) - 1;
//                String channelName = player.favorites().get(Integer.parseInt(String.valueOf(vv)));
//                player.say("включаю канал " + value + " " + channelName, false);
                ActionsAsync.turnOnChannel(player, value);
                response = player.name + " - play channel " + value;
                break;
            case "voice": // через бт голосовой пульт
                Tasker.ready = "no";
                log.info("VOICE " + value);
                ActionsSync.spotifyPlayArtist(value, player, true);
                response = "VOICE: " + value;
                break;
            case "play":
            case "turn_on_music":
                Tasker.ready = "no";
                ActionsAsync.turnOnMusic(player);
                response = player.name + " - play";
                break;
            case "play_pause":
            case "toggle_music":
                Tasker.ready = "no";
                ActionsAsync.toggleMusic(player);
                response = player.name + " - toggle music";
                break;

            case "next":
                Tasker.ready = "no";
                ActionsAsync.nextChannelOrTrack(player);
                response = player.name + " - next";
                break;
            case "prev":
                Tasker.ready = "no";
                ActionsAsync.prevChannelOrTrack(player);
                response = player.name + " - prev";
                break;
            case "next_track":
                Tasker.ready = "no";
                ActionsAsync.nextTrack(player);
                response = player.name + " - next track";
                break;
            case "prev_track":
                Tasker.ready = "no";
                ActionsAsync.prevTrack(player);
                response = player.name + " - prev track";
                break;
            case "next_channel":
                Tasker.ready = "no";
                ActionsAsync.ctrlNextChannel(player);
                response = player.name + " - next channel";
                break;
            case "prev_channel":
                Tasker.ready = "no";
                player.say("предыдущий канал", false);
                ActionsAsync.prevChannel(player);
                response = player.name + " - prev channel";
                break;
            case "forward":
                Tasker.ready = "no";
                player.forward();
                response = player.name + " - forward";
                break;
            case "rewind":
                Tasker.ready = "no";
                player.rewind();
                response = player.name + " - rewind";
                break;
            case "switch_here":
                Tasker.ready = "no";
                player.say("преключаю музыку на " + player.name, false);
                ActionsAsync.switchHere(player);
                response = player.name + " - switch here";
                break;

            // ========== Дополнительные опции (режимы, избранное) ==========
            case "separate_on":
                Tasker.ready = "no";
                ActionsAsync.separateOn(player);
                response = player.name + " - separate on";
                break;
            case "separate_off":
                Tasker.ready = "no";
                ActionsAsync.separateOff(player);
                response = player.name + " - separate off";
                break;
            case "shuffle_on":
                ActionsAsync.shuffleOn(player);
                response = player.name + " - shuffle on";
                break;
            case "shuffle_off":
                ActionsAsync.shuffleOff(player);
                response = player.name + " - shuffle off";
                break;
            case "favorites_add":
                ActionsAsync.favoritesAdd(player);
                response = player.name + " - favorites add";
                break;
            case "remote_connect":
                ActionsSync.connectBtRemoteToPlayer(player);
                response = player.name + " - favorites add";
                break;

            // ========== Информационные запросы ==========
            case "speak":
                player.say(value, true);
                break;
            case "signal":
                player.sound(value, false);
                break;

            case "its_alive":
                lmsPlayers.itsAlive();
                break;

            case "wats_playing":
                String watsPlaying = ActionsSync.whatsPlaying(player, false);
                log.info("WATS PLAYING: " + watsPlaying);
                player.say(watsPlaying, true);
                break;
            case "title":
                response = player.title();
                break;
            case "get_room_player": // Таскер по названию виджета вернуть комнату и плеер при активации нового виджета
                response = Tasker.playerNameByWidgetName(value);
                break;
            case "get_refresh_json": // Таскер для виджетов иконок плееров
                response = Tasker.forTaskerWidgetsRefreshJson(player, value);
                log.info("FINISH get_refresh_json");
                break;
            case "get_playlist": // Таскер для плейлиста
                response = Tasker.forTaskerPlaylist(player, 100);
                break;

            // ========== Неизвестная команда ==========
            default:
                log.info("ACTION NOT FOUND: " + action);
                response = "ACTION NOT FOUND: " + action;
                break;
        }


        if (response != "null") {
            context.bodyResponse = response;
            log.info(finish);
            return context;
        }
        context.bodyResponse = response;

        log.info(finish);
        return context;
    }


    public static String getAliceIdByRoom(String room) {
        log.info(start);
        log.info(Main.roomsAndAliceIds.entrySet());
        for (Map.Entry<String, String> entry : Main.roomsAndAliceIds.entrySet()) {
            log.info("ENTRY: " + entry + " ROOM: " + room);
            if (entry.getValue().equals(room)) {
                return entry.getKey(); // возвращаем id (хэш)
            }
        }
        return null; // комната не найдена
    }
}