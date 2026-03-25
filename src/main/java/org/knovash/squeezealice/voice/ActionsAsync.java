package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.voice.ActionsSync.*;

@Log4j2
public class ActionsAsync {

    // =========================================================================
    // ОСНОВНОЕ УПРАВЛЕНИЕ ВОСПРОИЗВЕДЕНИЕМ (асинхронные обёртки)
    // =========================================================================

    public static String turnOnMusic(Player player) {
        log.info("TURN ON MUSIC");
        ActionsSync.answer = "Пытаюсь включить музыку";
        CompletableFuture.runAsync(() -> {
            player.turnOnMusic(null);
            lmsPlayers.afterAsync();
            ActionsSync.answer = "Включаю музыку";
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String turnOnChannel(Player player, String value) {
        log.info("TURN ON CHANNEL");
        ActionsSync.answer = "Пытаюсь включить канал";
        CompletableFuture.runAsync(() -> {
            player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null).playChannel(value);
            lmsPlayers.afterAsync();
            ActionsSync.answer = "Включаю канал " + value;
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String ctrlNextChannel(Player player) {
        log.info("CTRL NEXT CHANNEL");
        ActionsSync.answer = "Пытаюсь переключить канал";
        CompletableFuture.runAsync(() -> {
            player.ctrlNextChannel();
            lmsPlayers.afterAsync();
            ActionsSync.answer = "Включаю следующий";
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String channelPlayByName(String command, Player player) {
        log.info("CHANNEL PLAY BY NAME");
        String target = command.replaceAll(".*(канал|избранное)\\S*\\s", "")
                .replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET LMS CHANNEL: " + target);
        ActionsSync.answer = "Пытаюсь включить канал " + target;
        CompletableFuture.runAsync(() -> {
            List<String> playlist = player.favorites();
            playlist.forEach(log::info);
            String channel = Levenstein.searchTitleInFavorites(target, playlist);
            log.info("CHANNEL: " + channel);
            int index = playlist.indexOf(channel) + 1;
            channel = channel.replaceAll(":.*", "");
            player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null).playChannel(String.valueOf(index));
            lmsPlayers.afterAsync();
            ActionsSync.answer = "Включаю канал " + channel;
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String toggleMusic(Player player) {
        log.info("TOGGLE MUSIC");
        ActionsSync.answer = "Пытаюсь переключить воспроизведение на " + player.name;
        CompletableFuture.runAsync(() -> {
            player.toggleMusic();
            lmsPlayers.afterAsync();
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String stopAll() {
        log.info("STOP ALL PLAYERS");
        ActionsSync.answer = "Пытаюсь выключить всё";
        CompletableFuture.runAsync(() -> {
            lmsPlayers.turnOffMusicAll();
            lmsPlayers.afterAsync();
            ActionsSync.answer = "Выключаю всё";
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String nextChannelOrTrack(Player player) {
        log.info("NEXT CHANNEL/TRACK " + player);
        ActionsSync.answer = "Пытаюсь переключить следующий трек/канал на " + player.name;
        CompletableFuture.runAsync(() -> {
            player.ctrlNextChannelOrTrack();
            lmsPlayers.afterAsync();
            ActionsSync.answer = "Переключаю следующий трек/канал на " + player.name;
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String prevChannelOrTrack(Player player) {
        log.info("PREV CHANNEL/TRACK");
        ActionsSync.answer = "Пытаюсь переключить предыдущий трек/канал на " + player.name;
        CompletableFuture.runAsync(() -> {
            player.ctrlPrevChannelOrTrack();
            lmsPlayers.afterAsync();
            ActionsSync.answer = "Переключаю предыдущий трек/канал на " + player.name;
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String nextTrack(Player player) {
        log.info("NEXT TRACK");
        ActionsSync.answer = "Пытаюсь переключить следующий трек на " + player.name;
        CompletableFuture.runAsync(() -> {
            player.ctrlNextTrack();
            lmsPlayers.afterAsync();
            ActionsSync.answer = "Переключаю следующий трек на " + player.name;
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String prevTrack(Player player) {
        log.info("PREV TRACK");
        ActionsSync.answer = "Пытаюсь переключить предыдущий трек на " + player.name;
        CompletableFuture.runAsync(() -> {
            player.ctrlPrevTrack();
            lmsPlayers.afterAsync();
            ActionsSync.answer = "Переключаю предыдущий трек на " + player.name;
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String prevChannel(Player player) {
        log.info("PREV CHANNEL");
        ActionsSync.answer = "Пытаюсь переключить предыдущий канал на " + player.name;
        CompletableFuture.runAsync(() -> {
            player.ctrlPrevChannel();
            lmsPlayers.afterAsync();
            ActionsSync.answer = "Переключаю предыдущий канал на " + player.name;
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String switchHere(Player player) {
        log.info("SWITCH HERE");
        Spotify.currentlyPlaying(); // узнать играет ли спотифай, для ответа
        ActionsSync.answer = "Пытаюсь переключить музыку на " + player.name;
        CompletableFuture.runAsync(() -> {
            ActionsSync.syncSwitchToHere(player);
            lmsPlayers.afterAsync();
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    // =========================================================================
    // ДОПОЛНИТЕЛЬНОЕ УПРАВЛЕНИЕ (режимы, избранное)
    // =========================================================================

    public static String shuffleOn(Player player) {
        log.info("SHUFFLE ON");
        ActionsSync.answer = "Пытаюсь включить рандом";
        CompletableFuture.runAsync(() -> {
            player.shuffleOn();
            ActionsSync.answer = "Включаю рандом";
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String shuffleOff(Player player) {
        log.info("SHUFFLE OFF");
        ActionsSync.answer = "Пытаюсь выключить рандом";
        CompletableFuture.runAsync(() -> {
            player.shuffleOff();
            ActionsSync.answer = "Выключаю рандом";
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String repeatOn(Player player) {
        log.info("REPEAT ON");
        ActionsSync.answer = "Пытаюсь включить повтор";
        CompletableFuture.runAsync(() -> {
            player.repeatOn();
            ActionsSync.answer = "Включаю повтор";
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String repeatOff(Player player) {
        log.info("REPEAT OFF");
        ActionsSync.answer = "Пытаюсь выключить повтор";
        CompletableFuture.runAsync(() -> {
            player.repeatOff();
            ActionsSync.answer = "Выключаю повтор";
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String separateOn(Player player) {
        log.info("SEPARATE ON");
        ActionsSync.answer = "Пытаюсь включить отдельно " + player.name;
        CompletableFuture.runAsync(() -> {
            player.separateOn();
            lmsPlayers.afterAsync();
            ActionsSync.answer = "Включаю отдельно " + player.name;
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String separateOff(Player player) {
        log.info("SEPARATE OFF");
        ActionsSync.answer = "Пытаюсь соединить все плееры вместе";
        CompletableFuture.runAsync(() -> {
            player.separateOff();
            lmsPlayers.afterAsync();
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String onlyHere(Player player) {
        log.info("ONLY HERE");
        ActionsSync.answer = "Пытаюсь включить музыку только на " + player.name;
        CompletableFuture.runAsync(() -> {
            player.onlyHere();
            lmsPlayers.afterAsync();
            ActionsSync.answer = "Включаю музыку только на " + player.name;
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String favoritesAdd(Player player) {
        log.info("FAVORITES ADD");
        ActionsSync.answer = "Пытаюсь добавить в избранное";
        CompletableFuture.runAsync(() -> {
            player.favoritesAdd();
            ActionsSync.answer = "Добавляю в избранное";
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    // =========================================================================
    // УПРАВЛЕНИЕ SPOTIFY (асинхронные обёртки)
    // =========================================================================

    public static String playArtist(Player player, String command) {
        log.info("PLAY ARTIST");
        ActionsSync.answer = "Пытаюсь включить spotify";
        CompletableFuture.runAsync(() -> {
            ActionsSync.spotifyPlayArtist(command, player);
            lmsPlayers.afterAsync();
            ActionsSync.answer = "Включаю spotify";
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String playAlbum(Player player, String command) {
        log.info("PLAY ALBUM");
        ActionsSync.answer = "Пытаюсь включить альбом";
        CompletableFuture.runAsync(() -> {
            spotifyPlayAlbum(command, player);
            lmsPlayers.afterAsync();
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String playTrack(Player player, String command) {
        log.info("PLAY TRACK");
        ActionsSync.answer = "Пытаюсь включить трек";
        CompletableFuture.runAsync(() -> {
            spotifyPlayTrack(command, player);
            lmsPlayers.afterAsync();
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String playPlaylist(Player player, String command) {
        log.info("PLAY PLAYLIST");
        ActionsSync.answer = "Пытаюсь включить плейлист";
        CompletableFuture.runAsync(() -> {
            spotifyPlayPlaylist(command, player);
            lmsPlayers.afterAsync();
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    // =========================================================================
    // УПРАВЛЕНИЕ КОМНАТАМИ И ПЛЕЕРАМИ (асинхронные обёртки)
    // =========================================================================

    public static String selectRoomWithSpeaker(String command, String aliceId) {
        log.info("SELECT ROOM WITH SPEAKER");
        ActionsSync.answer = "Пытаюсь выбрать комнату с колонкой";
        CompletableFuture.runAsync(() -> {
            ActionsSync.selectRoomWithSpeaker(command, aliceId);
            lmsPlayers.afterAsync();
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String selectRoomByCommand(String command, String aliceId) {
        log.info("SELECT ROOM BY COMMAND");
        ActionsSync.answer = "Пытаюсь выбрать комнату";
        CompletableFuture.runAsync(() -> {
            ActionsSync.selectRoomByCommand(command, aliceId);
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String selectPlayerByCommand(String command, String room) {
        log.info("SELECT PLAYER BY COMMAND: " + command + " IN ROOM: " + room);
        ActionsSync.answer = "Пытаюсь подключить колонку в комнате " + room;
        CompletableFuture.runAsync(() -> {
            ActionsSync.selectPlayerByCommand(command, room, false);
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String runPlayerByCommand(String command, String room) {
        log.info("RUN PLAYER BY COMMAND");
        ActionsSync.answer = "Пытаюсь включить колонку в комнате " + room;
        CompletableFuture.runAsync(() -> {
            ActionsSync.selectPlayerByCommand(command, room, true);
        });
        Utils.sleep(1000);
        log.info(ActionsSync.answer);
        return ActionsSync.answer;
    }

    // =========================================================================
    // BLUETOOTH ПУЛЬТ (асинхронные обёртки)
    // =========================================================================

    public static String connectBtRemote(String command, Player player) {
        log.info("CONNECT BT REMOTE");
        ActionsSync.answer = "Пытаюсь подключить пульт";
        CompletableFuture.runAsync(() -> {
            ActionsSync.connectBtRemote(command, player);
            lmsPlayers.afterAsync();
            ActionsSync.answer = "Подключаю пульт";
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    // Информационные запросы оставляем синхронными, так как они быстрые
    public static String whereBtRemote() {
        return ActionsSync.whereBtRemote();
    }

    public static String channelAdd(Player player) {
        return ActionsSync.channelAdd(player);
    }
}