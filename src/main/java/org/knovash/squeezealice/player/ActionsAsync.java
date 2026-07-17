package org.knovash.squeezealice.player;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Tasker;
import org.knovash.squeezealice.utils.levenstein.Levenstein;
import org.knovash.squeezealice.utils.Utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.player.ActionsSync.*;

@Log4j2
public class ActionsAsync {

    // =========================================================================
    // ЕДИНЫЙ АСИНХРОННЫЙ ИСПОЛНИТЕЛЬ (приватный)
    // =========================================================================


    public static String runAsync(String initialAnswer, Runnable action) {
        return runAsync(initialAnswer, action, null);
    }

    public static String runAsync(String initialAnswer, Runnable action, Integer delay) {
        if (delay == null) delay = 100;
        Tasker.ready = "no";
        ActionsSync.answer = initialAnswer;
        CompletableFuture.runAsync(() -> {
            long start = System.nanoTime();
            try {
                action.run();
                lmsPlayers.afterAsync();
            } finally {
                long duration = System.nanoTime() - start;
                log.info("TIME: " + duration / 1000000);
            }
        });
        Utils.sleep(delay);
        return ActionsSync.answer;
    }

    // =========================================================================
    // 1. МЕТОДЫ, ИСПОЛЬЗУЕМЫЕ В HandlePathAlice (все асинхронные) для голосового навыка
    // =========================================================================

    // ---- 1.1 Управление комнатами и плеерами ----
    public static String selectRoomWithSpeaker(String command, String aliceId) {
        log.info("SELECT ROOM WITH SPEAKER");
        return runAsync("Сейчас выберу комнату с колонкой", () -> {
            ActionsSync.selectRoomWithSpeaker(command, aliceId);
        });
    }

    public static String selectRoomByCommand(String command, String aliceId) {
        log.info("SELECT ROOM BY COMMAND");
        return runAsync("Сейчас выберу комнату", () -> {
            ActionsSync.selectRoomByCommand(command, aliceId);
        });
    }

    public static String selectPlayerByCommand(String command, String room) {
        log.info("SELECT PLAYER BY COMMAND: " + command + " IN ROOM: " + room);
        return runAsync("Сейчас подключу колонку в комнате " + room, () -> {
            ActionsSync.selectPlayerByCommand(command, room, false);
        });
    }

    public static String runPlayerByCommand(String command, String room) {
        log.info("RUN PLAYER BY COMMAND");
        return runAsync("Сейчас включу колонку в комнате " + room, () -> {
            ActionsSync.selectPlayerByCommand(command, room, true);
        });
    }

    // ---- 1.2 Управление воспроизведением (переключение, режимы) ----
    // Used in SwitchPlayerCommand
    public static String switchHere(Player player) {
        log.info(start);
        return runAsync("Сейчас переключу музыку на " + player.name, () -> {
            ActionsSync.syncSwitchToHere(player);
            log.info(finish);
        });
    }

    // Used in SwitchPlayerCommand
    public static String separateOn(Player player) {
        log.info("SEPARATE ON");
        return runAsync("Сейчас включу отдельно " + player.name, () -> {
            player.separateOn();
            ActionsSync.answer = "Включаю отдельно " + player.name;
        });
    }

    // Used in SwitchPlayerCommand
    public static String separateOff(Player player) {
        log.info("SEPARATE OFF");
        return runAsync("Сейчас соединю все плееры вместе", () -> {
            player.separateOff();
            ActionsSync.answer = "Соединяю все плееры вместе";
        });
    }

    // Used in SwitchPlayerCommand
    public static String onlyHere(Player player) {
        log.info("ONLY HERE");
        return runAsync("Сейчас включу музыку только на " + player.name, () -> {
            player.onlyHere();
            ActionsSync.answer = "Включаю музыку только на " + player.name;
        });
    }

    // Used in SwitchPlayerCommand
    public static String shuffleOn(Player player) {
        log.info("SHUFFLE ON");
        return runAsync("Сейчас включу рандом", () -> {
            player.shuffleOn();
            ActionsSync.answer = "Включаю рандом";
        });
    }

    // Used in SwitchPlayerCommand
    public static String shuffleOff(Player player) {
        log.info("SHUFFLE OFF");
        return runAsync("Сейчас выключу рандом", () -> {
            player.shuffleOff();
            ActionsSync.answer = "Выключаю рандом";
        });
    }

    // Used in SwitchPlayerCommand
    public static String repeatOn(Player player) {
        log.info("REPEAT ON");
        return runAsync("Сейчас включу повтор", () -> {
            player.repeatOn();
            ActionsSync.answer = "Включаю повтор";
        });
    }

    // Used in SwitchPlayerCommand
    public static String repeatOff(Player player) {
        log.info("REPEAT OFF");
        return runAsync("Сейчас выключу повтор", () -> {
            player.repeatOff();
            ActionsSync.answer = "Выключаю повтор";
        });
    }

    // Used in SwitchPlayerCommand (also in HandlePathAlice)
    public static String nextChannelOrTrack(Player player) {
        log.info("NEXT CHANNEL/TRACK " + player);
        return runAsync("Сейчас переключу следующий на " + player.name, () -> {
            player.ctrlNextChannelOrTrack();
            ActionsSync.answer = "Переключаю следующий на " + player.name;
        });
    }

    // ---- 1.3 Spotify ----
    public static String playArtist(Player player, String command) {
        log.info("PLAY ARTIST");
        return runAsync("Сейчас включу spotify", () -> {
            ActionsSync.spotifyPlayArtist(command, player, false);
        }, 2000);
    }

    public static String playAlbum(Player player, String command) {
        log.info("PLAY ALBUM");
        return runAsync("Сейчас включу альбом", () -> {
            spotifyPlayAlbum(command, player, false);
        });
    }

    public static String playTrack(Player player, String command) {
        log.info("PLAY TRACK");
        return runAsync("Сейчас включу трек", () -> {
            spotifyPlayTrack(command, player, false);
        });
    }

    public static String playPlaylist(Player player, String command) {
        log.info("PLAY PLAYLIST");
        return runAsync("Сейчас включу плейлист", () -> {
            spotifyPlayPlaylist(command, player, false);
        });
    }

    // ---- 1.4 Bluetooth-пульт ----
    public static String connectBtRemote(String command, Player player) {
        log.info("COMMAND: " + command + " PLAYER: " + player);
        return runAsync("Сейчас подключу пульт", () -> {
            ActionsSync.connectBtRemote(command, player);
        });
    }

    public static String whereBtRemote() {
        log.info("WHERE BT REMOTE ASYNC");
        return runAsync("Сейчас проверю, где пульт", () -> {
            String result = ActionsSync.whereBtRemote();
            ActionsSync.answer = result;
        });
    }

    // ---- 1.5 Избранное (каналы) ----
    public static String channelAdd(Player player) {
        log.info("CHANNEL ADD");
        return runAsync("Сейчас добавлю в избранное", () -> {
            String result = ActionsSync.channelAdd(player);
            ActionsSync.answer = result;
        });
    }

    public static String channelPlayByName(String command, Player player) {
        log.info("CHANNEL PLAY BY NAME");
        String target = command.replaceAll(".*(канал|избранное)\\S*\\s", "")
                .replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET LMS CHANNEL: " + target);
        return runAsync("Сейчас включу канал " + target, () -> {
            List<String> playlist = player.favorites();
            playlist.forEach(log::info);

            String latin = Utils.convertCyrilicToLatin(target);

            String channel = Levenstein.searchTitleInFavorites(latin, playlist);
            log.info("CHANNEL: " + channel);
            int index = playlist.indexOf(channel) + 1;
            channel = channel.replaceAll(":.*", "");
            player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null).playChannel(String.valueOf(index));
            ActionsSync.answer = "Включаю канал " + index + ", " + channel;
        });
    }

    // ---- 1.6 Информационные запросы ----
    public static String whatsPlaying(Player player, boolean sayAtPlayer) {
        log.info("WHATS PLAYING ASYNC");
        return runAsync("Сейчас проверю, что играет", () -> {
            String result = ActionsSync.whatsPlaying(player, sayAtPlayer);
            ActionsSync.answer = result;
        }, 1000);
    }

    public static String volumeLimitSet(Player player, String command) {
        log.info("VOLUME LIMIT SET ASYNC");
        return runAsync("Сейчас установлю ограничение громкости", () -> {
            String result = ActionsSync.volumeLimitSet(player, command);
            ActionsSync.answer = result;
        });
    }

    public static String whatsVolume(Player player) {
        log.info("WHATS VOLUME ASYNC");
        return runAsync("Сейчас проверю громкость", () -> {
            String result = ActionsSync.whatsVolume(player);
            ActionsSync.answer = result;
        });
    }

    // =========================================================================
    // 2. МЕТОДЫ, НЕ ИСПОЛЬЗУЕМЫЕ В HandlePathAlice (для возможного расширения)
    // =========================================================================

    // ---- 2.1 Управление воспроизведением (дополнительное) ----
    // Used in SwitchPlayerCommand
    public static String turnOnMusic(Player player) {
        log.info(start);
        log.info("TURN ON MUSIC " + player.name);
        String result = runAsync("Сейчас включу музыку", () -> {
            player.turnOnMusic(null);
            answer = "Включаю музыку";
        });
        return result;
    }

    // Used in SwitchPlayerCommand
    public static String toggleMusic(Player player) {
        log.info("TOGGLE MUSIC PLAYER: " + player);
        return runAsync("Сейчас переключу воспроизведение на " + player.name, () -> {
            player.toggleMusic();
            ActionsSync.answer = "Переключил воспроизведение на " + player.name;
        });
    }

    // Used in SwitchNoPlayerCommand
    public static String stopAll() {
        log.info("STOP ALL PLAYERS");
        return runAsync("Сейчас выключу всё", () -> {
            lmsPlayers.turnOffMusicAll();
            log.info("STOP ALL FINISH");
            ActionsSync.answer = "Выключаю всё";
        });
    }

    // Used in SwitchPlayerCommand
    public static String nextTrack(Player player) {
        log.info("NEXT TRACK");
        return runAsync("Сейчас переключу следующий трек на " + player.name, () -> {
            player.ctrlNextTrack();
            ActionsSync.answer = "Переключаю следующий трек на " + player.name;
        });
    }

    // Used in SwitchPlayerCommand
    public static String prevTrack(Player player) {
        log.info("PREV TRACK");
        return runAsync("Сейчас переключу предыдущий трек на " + player.name, () -> {
            player.ctrlPrevTrack();
            ActionsSync.answer = "Переключаю предыдущий трек на " + player.name;
        });
    }

    // Used in SwitchPlayerCommand
    public static String ctrlNextChannel(Player player) {
        log.info("CTRL NEXT CHANNEL");
        return runAsync("Сейчас переключу канал", () -> {
            player.ctrlNextChannel();
            ActionsSync.answer = "Включаю следующий";
        });
    }

    // Used in SwitchPlayerCommand
    public static String prevChannel(Player player) {
        log.info("PREV CHANNEL");
        return runAsync("Сейчас переключу предыдущий канал на " + player.name, () -> {
            player.ctrlPrevChannel();
            ActionsSync.answer = "Переключаю предыдущий канал на " + player.name;
        });
    }

    // Used in SwitchPlayerCommand
    public static String prevChannelOrTrack(Player player) {
        log.info("PREV CHANNEL/TRACK");
        return runAsync("Сейчас переключу предыдущий на " + player.name, () -> {
            player.ctrlPrevChannelOrTrack();
            ActionsSync.answer = "Переключаю предыдущий на " + player.name;
        });
    }

    // Used in SwitchPlayerCommand
    public static String turnOnChannel(Player player, String value) {
        log.info(start);
        return runAsync("Сейчас включу канал", () -> {
            int vv = Integer.parseInt(value) - 1;
            String channelName = player.favorites().get(vv);
            log.info("CHANNEL NAME GET FROM FAVORITES: " + channelName);
            player.say("включаю канал " + value + " " + channelName, false, true);
//  если был в группе после уведомления надо вернуть в группу
            player
                    .ifExpiredAndNotPlayingUnsyncWakeSetVolume(null)
                    .volumeByTimeSet()
                    .playChannel(value);
            ActionsSync.answer = "Включаю канал " + value;
        });
    }

    // ---- 2.2 Избранное (дополнительное) ----
    // Used in SwitchPlayerCommand
    public static String favoritesAdd(Player player) {
        log.info("FAVORITES ADD");
        return runAsync("Сейчас добавлю в избранное", () -> {
            player.favoritesAdd();
            ActionsSync.answer = "Добавляю в избранное";
        });
    }

    // ---- 2.3 Bluetooth (дополнительное) ----
    // Used in SwitchNoPlayerCommand
    public static String remoteSwitch() {
        log.info("REMOTE SWITCH");
        return runAsync("Сейчас переключу пульт", () -> {
            String btname = ActionsSync.remoteSwitch();
            ActionsSync.answer = "Пульт переключен на " + btname;
        });
    }
}