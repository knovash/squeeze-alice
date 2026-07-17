package org.knovash.squeezealice.cmd;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Tasker;
import org.knovash.squeezealice.player.ActionsAsync;
import org.knovash.squeezealice.player.ActionsSync;
import org.knovash.squeezealice.yandex.YandexTTS;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.cmd.SwitchRemoteVoiceCommand.remoteVoiceCommandSwithc;

@Log4j2
public class SwitchPlayerCommand {

    public static String run(String action, Player player, String value) {
        log.info(start);
        String response = null;
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
//                player.say("следующий канал", false, true);
                ActionsAsync.ctrlNextChannel(player);
                response = player.name + " - next channel";
                break;
            case "prev_channel":
//                player.say("предыдущий канал", false, true);
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
//                player.say("преключаю музыку на " + player.name, false, true);
               log.info("RUN SWITCH ------------");
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
            case "remote_connect":
                lmsPlayers.btPlayerName = player.name;
                log.info("BT PLAYER NAME: " + lmsPlayers.btPlayerName);
                lmsPlayers.write();
                response = "bt player - " +lmsPlayers.btPlayerName;
                break;

            // ========== Информационные запросы ==========
            case "speak":
                player.say(value, true, true);
                response = "speak";
                break;
            case "signal":
                player.signal(value);
                response = "signal";
                break;
            case "whats_playing":
                String watsPlaying = ActionsSync.whatsPlaying(player, false);
                log.info("WHATS PLAYING: " + watsPlaying);
                player.say(watsPlaying, true, true);
                response = "whats_playing";
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
            case "voice": // через бт голосовой пульт
                Tasker.ready = "no";
                log.info("VOICE " + value);
                remoteVoiceCommandSwithc(value, player);
                response = "VOICE: " + value;
                break;
            // ========== Неизвестная команда ==========
            default:
//                log.info("PLAYER COMMAND ACTION NOT FOUND: " + action);
                break;
        }
        log.info(finish);
        return response;
    }
}