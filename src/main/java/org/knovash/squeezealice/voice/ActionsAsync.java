package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class ActionsAsync {

    // =========================================================================
    // ОСНОВНОЕ УПРАВЛЕНИЕ ВОСПРОИЗВЕДЕНИЕМ (асинхронные обёртки)
    // =========================================================================

    public static String turnOnMusic(Player player) {
        log.info("PLAY ARTIST");
        CompletableFuture.runAsync(() -> player.turnOnMusic(null))
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "включаю";
    }

    public static String turnOnChannel(Player player, String value) {
        log.info("PLAY ARTIST");
        CompletableFuture.runAsync(() -> player
                        .ifExpiredAndNotPlayingUnsyncWakeSetVolume(null)
                        .playChannel(value))
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "включаю";
    }

    public static String ctrlNextChannel(Player player) {
        log.info("PLAY ARTIST");
        CompletableFuture.runAsync(() -> player.ctrlNextChannel())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "включаю";
    }

    public static String channelPlayByName(String command, Player player) {
        String answer;
        String target;
        target = command.replaceAll(".*(канал|избранное)\\S*\\s", "").replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET LMS CHANNEL: " + target);

        answer = "включаю канал ";
        log.info("INDEX: " );
        CompletableFuture.runAsync(() -> {
                    List<String> playlist = player.favorites();
                    playlist.forEach(n -> log.info(n));
                    String channel = Levenstein.searchTitleInFavorites(target, playlist);
                    log.info("CHANNEL: " + channel);
                    int index = playlist.indexOf(channel) + 1;
                    channel = channel.replaceAll(":.*", "");

            player
                        .ifExpiredAndNotPlayingUnsyncWakeSetVolume(null)
                        .playChannel(String.valueOf(index));}
        )
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return answer;
    }

    public static String toggleMusic(Player player) {
        log.info("TOGGLE MUSIC");
        CompletableFuture.runAsync(() -> player.toggleMusic())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return player.name + " - play/pause";
    }

    public static String stopAll() {
        log.info("STOP ALL PLAYERS");
        CompletableFuture.runAsync(() -> lmsPlayers.turnOffMusicAll())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "All players - Stop";
    }

    public static String nextChannelOrTrack(Player player) {
        log.info("NEXT CHANNEL/TRACK");
        CompletableFuture.runAsync(() -> player.ctrlNextChannelOrTrack())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return player.name + " - Next";
    }

    public static String prevChannelOrTrack(Player player) {
        log.info("PREV CHANNEL/TRACK");
        CompletableFuture.runAsync(() -> player.ctrlPrevChannelOrTrack())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return player.name + " - Prev";
    }

    public static String nextTrack(Player player) {
        log.info("NEXT TRACK");
        CompletableFuture.runAsync(() -> player.ctrlNextTrack())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return player.name + " - Next track";
    }

    public static String prevTrack(Player player) {
        log.info("PREV TRACK");
        CompletableFuture.runAsync(() -> player.ctrlPrevTrack())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return player.name + " - Prev track";
    }

    public static String prevChannel(Player player) {
        log.info("PREV CHANNEL");
        CompletableFuture.runAsync(() -> player.ctrlPrevChannel())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return player.name + " - Prev channel";
    }

    public static String switchHere(Player player) {
        log.info("SWITCH HERE");
        Spotify.currentlyPlaying(); // узнать играет ли спотифай, для ответа
        CompletableFuture.runAsync(() -> {
            ActionsSync.syncSwitchToHere(player);
            lmsPlayers.afterAll();
        });
        if (Spotify.currentlyPlaying != null && Spotify.currentlyPlaying.is_playing)
            return "переключаю spotify на " + player.name;
        else
            return "переключаю музыку на " + player.name;
    }

    // =========================================================================
    // ДОПОЛНИТЕЛЬНОЕ УПРАВЛЕНИЕ (режимы, избранное)
    // =========================================================================

    public static String shuffleOn(Player player) {
        log.info("SHUFFLE ON");
        CompletableFuture.runAsync(() -> player.shuffleOn());
        return "включаю рандом";
    }

    public static String shuffleOff(Player player) {
        log.info("SHUFFLE OFF");
        CompletableFuture.runAsync(() -> player.shuffleOff());
        return "выключаю рандом";
    }

    public static String repeatOn(Player player) {
        log.info("REPEAT ON");
        CompletableFuture.runAsync(() -> player.repeatOn());
        return "включаю повтор";
    }

    public static String repeatOff(Player player) {
        log.info("REPEAT OFF");
        CompletableFuture.runAsync(() -> player.repeatOff());
        return "выключаю повтор";
    }

    public static String separateOn(Player player) {
        log.info("SEPARATE ON");
        CompletableFuture.runAsync(() -> player.separateOn())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "включаю отдельно " + player.name;
    }

    public static String separateOff(Player player) {
        log.info("SEPARATE OFF");
        CompletableFuture.runAsync(() -> player.separateOff())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "соединяю все играющие вместе";
    }

    public static String onlyHere(Player player) {
        log.info("ONLY HERE");
        CompletableFuture.runAsync(() -> player.onlyHere())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "включаю только тут";
    }

    public static String favoritesAdd(Player player) {
        log.info("FAVORITES ADD");
        CompletableFuture.runAsync(() -> player.favoritesAdd());
        return "FAVORITES ADD";
    }

    // =========================================================================
    // УПРАВЛЕНИЕ SPOTIFY (асинхронные обёртки)
    // =========================================================================

    public static String playArtist(Player player, String command) {
        log.info("PLAY ARTIST");
        CompletableFuture.runAsync(() -> ActionsSync.spotifyPlayArtist(command, player))
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "включаю";
    }

    public static String playAlbum(Player player, String command) {
        log.info("PLAY ALBUM");
        CompletableFuture.runAsync(() -> spotifyPlayAlbum(command, player))
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "включаю";
    }

    public static String playTrack(Player player, String command) {
        log.info("PLAY TRACK");
        CompletableFuture.runAsync(() -> spotifyPlayTrack(command, player))
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "включаю";
    }

    public static String playPlaylist(Player player, String command) {
        log.info("PLAY PLAYLIST");
        CompletableFuture.runAsync(() -> spotifyPlayPlaylist(command, player))
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "включаю";
    }

    // Эти методы уже асинхронные, оставляем как есть
    public static String spotifyPlayTrack(String command, Player player) {
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("трэк", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        String link = Spotify.getLinkTrack(target);
        log.info("LINK: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.runAsync(() -> player.playPath(link));

        String answer = "включаю " + target;
        Utils.sleep(1000);
        return answer;
    }

    public static String spotifyPlayAlbum(String command, Player player) {
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("альбом", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        String link = Spotify.getLinkAlbum(target);
        log.info("LINK: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.runAsync(() -> {
            player.playPath(link);
        });
        String answer = "включаю " + target;
        return answer;
    }

    public static String spotifyPlayPlaylist(String command, Player player) {
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("плэйлист", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        String link = Spotify.getLinkPlaylist(target);
        log.info("LINK: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.runAsync(() -> {
            player.playPath(link);
        });
        String answer = "включаю " + target;
        return answer;
    }

    // =========================================================================
    // УПРАВЛЕНИЕ КОМНАТАМИ И ПЛЕЕРАМИ (асинхронные обёртки)
    // =========================================================================

    public static String selectRoomWithSpeaker(String command, String aliceId) {
        log.info("SELECT ROOM WITH SPEAKER");
        CompletableFuture.runAsync(() -> {
            ActionsSync.selectRoomWithSpeaker(command, aliceId);
            lmsPlayers.afterAll();
        });
        return "выполняю выбор комнаты с колонкой";
    }

    public static String selectRoomByCommand(String command, String aliceId) {
        log.info("SELECT ROOM BY COMMAND");
        CompletableFuture.runAsync(() -> {
            ActionsSync.selectRoomByCommand(command, aliceId);
        });
        return "выполняю выбор комнаты";
    }

    public static String selectPlayerByCommand(String command, String room) {
        log.info("SELECT PLAYER BY COMMAND: " + command + " IN ROOM: " + room);
        ActionsSync.answer = "пытаюсь подключить колонку в комнате " + room;
        CompletableFuture.runAsync(() -> {
            ActionsSync.selectPlayerByCommand(command, room, false);
        });
        Utils.sleep(1000);
        return ActionsSync.answer;
    }

    public static String runPlayerByCommand(String command, String room) {
        log.info("SELECT PLAYER BY COMMAND");
        CompletableFuture.runAsync(() -> {
            ActionsSync.selectPlayerByCommand(command, room, true);
        });
        Utils.sleep(1000);
        log.info(ActionsSync.answer);
        if (ActionsSync.answer != null) return ActionsSync.answer;
        return "пытаюсь включить колонку в комнате " + room;
    }

    // =========================================================================
    // BLUETOOTH ПУЛЬТ (асинхронные обёртки)
    // =========================================================================

    public static String connectBtRemote(String command, Player player) {
        log.info("CONNECT BT REMOTE");
        CompletableFuture.runAsync(() -> {
            ActionsSync.connectBtRemote(command, player);
            lmsPlayers.afterAll();
        });
        return "подключаю пульт";
    }

    public static String whereBtRemote() {
        // Этот метод просто читает состояние, можно оставить синхронным
        return ActionsSync.whereBtRemote();
    }

    // =========================================================================
    // ИНФОРМАЦИОННЫЕ ЗАПРОСЫ (можно оставить синхронными, т.к. они быстрые)
    // =========================================================================


    public static String channelAdd(Player player) {
        return ActionsSync.channelAdd(player);
    }
}