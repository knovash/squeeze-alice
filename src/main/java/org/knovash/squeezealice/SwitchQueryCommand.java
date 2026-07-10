package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.voice.ActionsAsync;
import org.knovash.squeezealice.voice.ActionsSync;

import java.util.HashMap;
import java.util.Map;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class SwitchQueryCommand {

    public static Context action(Context context) {
        log.info(start);
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


        switch (action) { // БЕЗ ПЛЕЕРА
            case "stop_all":
                Tasker.ready = "no";
                ActionsAsync.stopAll();
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
        Player player = lmsPlayers.playerByPlayerNameOrRoomName(playerName, room);
        if (room == null && player != null) room = player.room;
        log.info("PLAYER: " + player);
        log.info("ROOM: " + room);
        log.info("COMMAND TO PLAYER: " + player);
//        -----------------------------------------------

        if (response.equals("null") && player != null && player.connected)
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
                    ActionsAsync.turnOnChannel(player, value);
                    response = player.name + " - play channel " + value;
                    break;
                case "voice": // через бт голосовой пульт
                    Tasker.ready = "no";
                    log.info("VOICE " + value);
//                    ActionsSync.spotifyPlayArtist(value, player, true);
                    remoteVoiceCommandSwithc(value, player);

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
                    player.say("предыдущий канал", false, true);
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
                    player.say("преключаю музыку на " + player.name, false, true);
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
                    player.say(value, true, true);
                    break;
                case "signal":
                    player.sound(value, false, true);
                    break;
                case "wats_playing":
                    String watsPlaying = ActionsSync.whatsPlaying(player, false);
                    log.info("WATS PLAYING: " + watsPlaying);
                    player.say(watsPlaying, true, true);
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


    private static String remoteVoiceCommandSwithc(String command, Player player) {

        // Обработка всех команд, требующих плеер (обернуто в try-catch для устойчивости)
        try {
//            if (command.contains("что играет"))
//                return ActionsSync.whatsPlaying(player, true);
//            if (command.contains("лимит"))
//                return ActionsSync.volumeLimitSet(player, command);
//            if (command.contains("какая громкость"))
//                return ActionsSync.whatsVolume(player);
//            if (command.matches("(включи )?(канал|избранное) .*"))
//                return ActionsAsync.channelPlayByName(command, player);
//            if (command.matches("добавь( в)? избранное"))
//                return ActionsAsync.channelAdd(player);
//            if (command.matches("переключи.*сюда"))
//                return ActionsAsync.switchHere(player);
//            if (command.matches("(включи )?отдельно"))
//                return ActionsAsync.separateOn(player);
//            if (command.matches("(включи )?вместе"))
//                return ActionsAsync.separateOff(player);
//            if (command.matches("(включи )?только тут"))
//                return ActionsAsync.onlyHere(player);
//            if (command.matches("(включи )?(рандом|шафл|shuffle|random)"))
//                return ActionsAsync.shuffleOn(player);
//            if (command.matches("(выключи )?(рандом|шафл|shuffle|random)"))
//                return ActionsAsync.shuffleOff(player);
//            if (command.matches("(включи )?(повтор)"))
//                return ActionsAsync.repeatOn(player);
//            if (command.matches("(выключи )?(повтор)"))
//                return ActionsAsync.repeatOff(player);
//            if (command.matches("(включи )?(дальше|следующий)")) {
//                ActionsAsync.nextChannelOrTrack(player);
//                return "включаю следующий";
//            }

            if (command.matches("^(?:подключи пульт(?: (?:к|в|на).*)?|включи пульт)$"))
                return ActionsAsync.connectBtRemote(command, player);
            if (command.contains("где пульт")) {
               String answer = ActionsSync.whereBtRemote();
                player.say(answer,true,true);
                return answer;
            }
            if (command.startsWith("включи альбом"))
                return ActionsSync.spotifyPlayAlbum(command, player, true);
            if (command.startsWith("включи трек"))
                return ActionsSync.spotifyPlayTrack(command, player, true);
            if (command.startsWith("включи плейлист"))
                return ActionsAsync.playPlaylist(player, command);
            if (command.startsWith("включи"))
                return ActionsSync.spotifyPlayArtist(command, player, true);

        } catch (Exception e) {
            log.error("Ошибка при обработке команды '{}': {}", command, e.getMessage(), e);
            return "Произошла внутренняя ошибка, попробуйте позже";
        }
        return "";
    }
}