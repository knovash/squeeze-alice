package org.knovash.squeezealice.cmd;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Tasker;
import org.knovash.squeezealice.player.ActionsAsync;
import org.knovash.squeezealice.yandex.Yandex;

import java.util.concurrent.CompletableFuture;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.cmd.CommandsFromVoiceRemote.remoteVoiceCommands;

@Log4j2
public class CommandsWithPlayer {

    public static String run(String action, Player player, String value) {
        log.info(start);
        log.info("ACTION: " + action + " VALUE: " + value + " PLAYER: " + player);
        String playerName = "unknown";
        if (player != null) playerName = player.name;
        String response = playerName + " " + action + " " + value;

        // Таскер для виджетов иконок плееров, даже если плеер null вернуть ответ
        if ("get_refresh_json".equals(action)) return Tasker.forTaskerWidgetsRefreshJson(player, value);

        if (player == null) response = "ERROR PLAYER NULL";

        switch (action) {
            case "volume_dn":
                CompletableFuture.runAsync(() -> {
//                player.volume("-3");
                    if (value == null) player.volumeSetLimited("-3");
                    player.volumeSetLimited("-" + value);
                });
                break;
            case "volume_up":
                CompletableFuture.runAsync(() -> {
//                player.volume("+3");
                    if (value == null) player.volumeSetLimited("+3");
                    player.volumeSetLimited("+" + value);
                });
                break;
            case "channel":
                ActionsAsync.playChannelIndex(player, value);
                break;
            case "play":
            case "turn_on_music":
                ActionsAsync.turnOnMusic(player);
                break;
            case "play_pause":
            case "toggle_music":
                ActionsAsync.toggleMusic(player);
                break;
            case "next":
                ActionsAsync.nextChannelOrTrack(player);
                break;
            case "prev":
                ActionsAsync.prevChannelOrTrack(player);
                break;
            case "next_track":
                ActionsAsync.nextTrack(player);
                break;
            case "prev_track":
                ActionsAsync.prevTrack(player);
                break;
            case "jump_track":
                ActionsAsync.jumpTrack(player, value);
                break;
            case "next_channel":
                ActionsAsync.nextChannel(player);
                break;
            case "prev_channel":
                ActionsAsync.prevChannel(player);
                break;
            case "forward":
                player.forward();
                break;
            case "rewind":
                player.rewind();
                break;
            case "switch_here":
                ActionsAsync.switchHere(player);
                break;
            case "separate_on":
                ActionsAsync.separateSet(player, "Отдельно");
                break;
            case "separate_off":
                ActionsAsync.separateSet(player, "Вместе");
                break;
            case "shuffle_on":
                ActionsAsync.shuffleSet(player, true);
                break;
            case "shuffle_off":
                ActionsAsync.shuffleSet(player, false);
                break;
            case "repeat_on":
                ActionsAsync.repeatSet(player, true);
                break;
            case "repeat_off":
                ActionsAsync.repeatSet(player, false);
                break;
            case "favorites_add":
                ActionsAsync.favoritesAdd(player);
                break;
            case "remote_connect":
                lmsPlayers.btPlayerName = player.name;
                log.info("BT PLAYER NAME: " + lmsPlayers.btPlayerName);
                lmsPlayers.write();
                break;
            case "speak":
                CompletableFuture.runAsync(() -> player.say(value, true, true));
                break;
            case "signal":
                CompletableFuture.runAsync(() -> player.signal(value));
                break;
            case "whats_playing": // сказать что играет через плеер LMS
                ActionsAsync.whatsPlayingSayToLms(player, false);
                break;
            case "get_room_player": // Таскер по названию виджета вернуть комнату и плеер при активации нового виджета
                return Tasker.playerNameByWidgetName(value);
            case "get_playlist": // Таскер для плейлиста
                return Tasker.forTaskerPlaylist(player, 100, true);
            case "voice": // голосовой поиск в Spotify через BT пульт. Включи альбом ...
                CompletableFuture.runAsync(() -> remoteVoiceCommands(value, player));
                break;
            default:
                response = "ERROR";
                break;
        }


        log.info(finish);
        return response;
    }
}