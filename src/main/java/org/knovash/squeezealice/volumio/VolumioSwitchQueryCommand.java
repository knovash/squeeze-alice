package org.knovash.squeezealice.volumio;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Tasker;
import org.knovash.squeezealice.spotify.Spotify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class VolumioSwitchQueryCommand {

    // Хранилище плееров: ключ - имя плеера, значение - объект VolumioPlayer
    private static final Map<String, VolumioPlayer> players = new ConcurrentHashMap<>();

    // Метод для получения или создания плеера по имени и базовому URL
    private static VolumioPlayer getPlayer(String playerName, String baseUrl) {
        return players.computeIfAbsent(playerName, name -> {
            VolumioPlayer p = new VolumioPlayer(name);
            p.baseUrl = "http://192.168.1.118";
            p.setRoom("main"); // можно задавать из конфига
            return p;
        });
    }

    public static Context action(Context context) {
        HashMap<String, String> queryParams = context.queryMap;
        context.bodyResponse = "BAD REQUEST NO ACTION IN QUERY";
        if (!queryParams.containsKey("action")) return context;

        String action = queryParams.get("action");
        String value = queryParams.get("value");
        String playerName = queryParams.getOrDefault("player", "volumio"); // имя плеера по умолчанию
        String baseUrl = queryParams.getOrDefault("baseUrl", "http://localhost:3000"); // URL по умолчанию

        // Временное захардкоженное значение (можно убрать, если baseUrl передаётся корректно)
        baseUrl = "http://192.168.1.126";

        log.info("ACTION: {} VALUE: {} PLAYER: {}", action, value, playerName);

        VolumioPlayer player = getPlayer(playerName, baseUrl);

        // Обновляем состояние перед выполнением (кроме информационных действий, где это не обязательно)
//        boolean isInfoAction = List.of("title", "get_room_player", "get_refresh_json", "ready", "update_players", "spotify_me").contains(action);
//        if (!isInfoAction) {
//            player.fetchState(); // для актуальности состояния
//        }

        // Если это не информационное действие, сбрасываем Tasker.ready (как в оригинале)
//        if (!isInfoAction) {
//            Tasker.ready = "no";
//        }

        String response;
        context.code = 200;

        log.info("------------");
        try {
            switch (action) {
                // Управление громкостью
                case "volume_dn":
                case "volume_down":
                    if (value != null && !value.isEmpty()) {
                        // Если передан параметр, уменьшаем на указанное значение (как абсолютная установка?)
                        // Но в базовых командах у нас только step-изменение, поэтому интерпретируем value как шаг
                        int step = Integer.parseInt(value);
                        for (int i = 0; i < step; i++) {
                            player.volumeDown();
                        }
                    } else {
                        player.volumeDown();
                    }
                    response = player.getName() + " - volume down";
                    break;
                case "volume_up":
                    if (value != null && !value.isEmpty()) {
                        int step = Integer.parseInt(value);
                        for (int i = 0; i < step; i++) {
                            player.volumeUp();
                        }
                    } else {
                        player.volumeUp();
                    }
                    response = player.getName() + " - volume up";
                    break;
                case "volume_set":
                    if (value != null && !value.isEmpty()) {
                        int vol = Integer.parseInt(value);
                        player.setVolume(String.valueOf(vol));
                        response = player.getName() + " - volume set to " + vol;
                    } else {
                        response = "Missing volume value";
                        context.code = 400;
                    }
                    break;
                case "mute":
                    player.mute();
                    response = player.getName() + " - mute";
                    break;
                case "unmute":
                    player.unmute();
                    response = player.getName() + " - unmute";
                    break;

                // Управление воспроизведением
                case "play":
                case "turn_on_music":
                    player.play();
                    response = player.getName() + " - play";
                    break;
                case "pause":
                case "stop_all": // stop_all интерпретируем как pause (остановка с сохранением позиции)
                    player.pause();
                    response = player.getName() + " - pause";
                    break;
                case "play_pause":
                case "toggle_music":
                    player.togglePlayPause();
                    response = player.getName() + " - toggle";
                    break;
                case "stop":
                    player.stop();
                    response = player.getName() + " - stop";
                    break;
                case "next":
                    player.ctrlNextTrack();
                    response = player.getName() + " - next";
                    break;
                case "prev":
                    player.ctrlPrevTrack();
                    response = player.getName() + " - prev";
                    break;

                // Информационные действия
                case "title":
                    response = player.getTitle();
                    break;
                case "get_room_player":
                    response = getRoomPlayerInfo(player, value);
                    break;
                case "get_refresh_json":
                    response = getRefreshJson(player, value);
                    break;
                case "ready":
                    response = Tasker.ready;
                    break;
                case "update_players":
                    response = "update players";
                    break;
                case "spotify_me":
                    Spotify.me();
                    response = "spotify_me";
                    break;

                // Действия, которые не поддерживаются в упрощённой версии
                case "channel":
                case "next_channel":
                case "prev_channel":
                case "next_track": // в базовых командах есть next/prev, которые переключают треки
                case "prev_track": // но next_track и prev_track можно оставить как синонимы next/prev
                    // Для совместимости можно обработать:
                    if (action.equals("next_track")) {
                        player.ctrlNextTrack();
                        response = player.getName() + " - next track";
                    } else if (action.equals("prev_track")) {
                        player.ctrlPrevTrack();
                        response = player.getName() + " - prev track";
                    } else {
                        log.warn("Action {} is not supported in simplified VolumioPlayer", action);
                        response = action + " not supported";
                        context.code = 400;
                    }
                    break;
                case "shuffle_on":
                case "shuffle_off":
                case "repeat_on":
                case "repeat_off":
                case "favorites_add":
                case "separate_on":
                case "separate_off":
                case "switch_here":
                    log.warn("Action {} is not supported in simplified VolumioPlayer", action);
                    response = action + " not supported";
                    context.code = 400;
                    break;
                default:
                    log.info("ACTION NOT FOUND: " + action);
                    response = "ACTION NOT FOUND: " + action;
                    context.code = 404;
                    break;
            }
        } catch (NumberFormatException e) {
            log.error("Invalid number format for value '{}' in action {}: {}", value, action, e.getMessage());
            response = "Invalid value format";
            context.code = 400;
        } catch (Exception e) {
            log.error("Error executing action {}: {}", action, e.getMessage());
            response = "ERROR: " + e.getMessage();
            context.code = 500;
        }

        // Устанавливаем ready обратно в "yes" после выполнения неинформационного действия
//        if (!isInfoAction && !action.equals("ready")) {
//            Tasker.ready = "yes";
//        }

        context.bodyResponse = response;
        return context;
    }

    private static String getRoomPlayerInfo(VolumioPlayer player, String widgetName) {
        if (player == null) return "unknown:unknown";
        return (player.getRoom() != null ? player.getRoom() : "unknown") + ":" + player.getName();
    }

    private static String getRefreshJson(VolumioPlayer player, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put("player", player.getName());
        data.put("room", player.getRoom());
        data.put("title", player.getTitle());
        data.put("volume", player.getVolume());
        data.put("playing", player.isPlaying());
        data.put("mode", player.getMode());
        data.put("connected", player.isConnected());
        try {
            return new ObjectMapper().writeValueAsString(data);
        } catch (Exception e) {
            log.error("Failed to generate refresh JSON: {}", e.getMessage());
            return "{}";
        }
    }
}