package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.voice.ActionsAsync;

import java.util.HashMap;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class SwitchQueryCommand {

    public static Context action(Context context) {
        HashMap<String, String> queryParams = context.queryMap;
        context.bodyResponse = "BAD REQUEST NO ACTION IN QUERY";
        if (!queryParams.containsKey("action")) return context;

        context.code = 200;
        String action = queryParams.get("action");
        String playerName = queryParams.get("player");
        String room = queryParams.get("room");
        String value = queryParams.get("value");
        String response = "null";
        log.info("PLAYER: " + playerName + " ROOM: " + room + " ACTION: " + action + " VALUE: " + value);

        Player player = null;
        if (playerName == null && room != null) player = lmsPlayers.playerByNearestRoom(room);
        if (playerName != null && playerName.equals("btremote")) playerName = lmsPlayers.btPlayerName;
        if (playerName != null) player = lmsPlayers.playerByNearestName(playerName);

        log.info("PLAYER: " + player);

        lmsPlayers.fastUpdateServer(); // обновить перед всеми командами с пульта или планшета /cmd

        // Управление с пульта или виджетов таскер через http запрос
        // Респонс для отображения действия на телевизоре или планшете

        switch (action) {
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
                ActionsAsync.turnOnChannel(player, value);
                response = player.name + " - play channel " + value;
                break;
            case "play":
            case "turn_on_music":
                ActionsAsync.turnOnMusic(player);
                response = player.name + " - play";
                break;
            case "play_pause":
            case "toggle_music":
                ActionsAsync.toggleMusic(player);
                response = player.name + " - toggle music";
                break;
            case "stop_all":
                ActionsAsync.stopAll();
                response = "stop all";
                break;
            case "next":
                ActionsAsync.nextChannelOrTrack(player);
                response = player.name + " - next";
                break;
            case "prev":
                ActionsAsync.prevChannelOrTrack(player);
                response = player.name + " - prev";
                break;
            case "next_track":
                ActionsAsync.nextTrack(player);
                response = player.name + " - next track";
                break;
            case "prev_track":
                ActionsAsync.prevTrack(player);
                response = player.name + " - prev track";
                break;
            case "next_channel":
                ActionsAsync.ctrlNextChannel(player);
                response = player.name + " - next channel";
                break;
            case "prev_channel":
                ActionsAsync.prevChannel(player);
                response = player.name + " - prev channel";
                break;
            case "switch_here":
                ActionsAsync.switchHere(player);
                response = player.name + " - switch here";
                break;

            // ========== Дополнительные опции (режимы, избранное) ==========
            case "separate_on":
                ActionsAsync.separateOn(player);
                response = player.name + " - separate on";
                break;
            case "separate_off":
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

            // ========== Информационные запросы ==========
            case "title":
                response = player.title();
                break;
            case "get_room_player": // Таскер по названию виджета вернуть комнату и плеер при активации нового виджета
                response = Tasker.playerNameByWidgetName(value);
                break;
            case "get_refresh_json": // Таскер для виджетов иконок плееров
                response = Tasker.forTaskerWidgetsRefreshJson(player, value);
                break;

            // ========== Утилитные действия (обновление, внешние сервисы) ==========
            case "update_players":
                lmsPlayers.fastUpdateServer(); // ручное обновление
                response = "update players";
                break;
            case "spotify_me":
                Spotify.me();
                response = "spotify_me";
                break;

            // ========== Неизвестная команда ==========
            default:
                log.info("ACTION NOT FOUND: " + action);
                response = "ACTION NOT FOUND: " + action;
                break;
        }


        if (response != "null") {
            context.bodyResponse = response;
            return context;
        }
        context.bodyResponse = response;
        return context;
    }
}