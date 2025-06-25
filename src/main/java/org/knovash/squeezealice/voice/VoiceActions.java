package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.utils.Levenstein;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.voice.SwitchVoiceCommand.spotifyPlayArtist;

@Log4j2
public class VoiceActions {

    public static String playArtist(Player player, String command) {
        log.info("PLAY ARTIST");
        CompletableFuture.runAsync(() -> spotifyPlayArtist(command, player))
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "включаю";
    }

    //      СИНХРОНИЗАЦИЯ
    public static String syncSwitchToHere(Player player) {
        log.info("SWITCH TO HERE");
        CompletableFuture.runAsync(() -> player.switchToHere())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "переключаю музыку на " + player.name;
    }

    public static String shuffleOn(Player player) {
        log.info("SEPARATE ON");
        CompletableFuture.runAsync(() -> player.shuffleOn())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "включаю рандом";
    }

    public static String shuffleOff(Player player) {
        log.info("SEPARATE ON");
        CompletableFuture.runAsync(() -> player.shuffleOff())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "выключаю рандом";
    }

    public static String separateOn(Player player) {
        log.info("SEPARATE ON");
        CompletableFuture.runAsync(() -> player.separateOn())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "включаю отдельно " + player.name;
    }

    public static String separateAllOff(Player player) {
        log.info("SEPARATE OFF");
        CompletableFuture.runAsync(() -> player.separateOffAll())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "соединяю все играющие вместе";
    }

    public static String separateOff(Player player) {
        log.info("SEPARATE OFF");
        CompletableFuture.runAsync(() -> player.separateOff())
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return "соединяю все играющие вместе";
    }

//    ВОСПРОИЗВЕДЕНИЕ

    public static String channelPlayByName(String command, Player player) {
        String answer;
        String target;
        target = command.replaceAll(".*(канал|избранное)\\S*\\s", "").replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET LMS CHANNEL: " + target);
        List<String> playlist = lmsPlayers.favorites();
        playlist.forEach(n -> log.info(n));
        String channel = Levenstein.searchTitleInFavorites(target, playlist);
        if (channel == null) return "повторите";
        log.info("CHANNEL: " + channel);
        int index = playlist.indexOf(channel) + 1;
        channel = channel.replaceAll(":.*", "");
        answer = "включаю канал " + index + ", " + channel;
        log.info("INDEX: " + index);
        CompletableFuture.runAsync(() -> player
                        .ifExpiredAndNotPlayingUnsyncWakeSet(null)
                        .playChannel(index))
                .thenRunAsync(() -> lmsPlayers.afterAll());
        return answer;
    }
}
