package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.utils.Levenstein;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class VoiceActions {

    public static String whatIsTheRoom(String room) {
//        if (command.contains("какая комната")) {
        log.info("КАКАЯ КОМНАТА");
        String playerNameInRoom = ". колонка еще не выбрана. скажите выбери колонку и название";
        Player playerInRoom = lmsPlayers.playerByCorrectRoom(room);
        log.info("PLAYER IN ROOM: " + playerInRoom);
        if (playerInRoom != null)
            playerNameInRoom = ". с колонкой " + playerInRoom.name;
        String remoteInRoom = ". пульт не подключен";
        if (lmsPlayers.btPlayerName != null)
            remoteInRoom = ". пульт подключен к " + lmsPlayers.btPlayerName;
        return "это комната " + room + playerNameInRoom + remoteInRoom;
    }

    //      СИНХРОНИЗАЦИЯ
    public static String syncSwitchToHere(Player player) {
        log.info("SWITCH TO HERE");
        CompletableFuture.runAsync(() -> player.switchToHere())
                .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());
        return "переключаю музыку на " + player.name;
    }

    public static String separateOn(Player player) {
        log.info("SEPARATE ON");
        CompletableFuture.runAsync(() -> player.separateOn())
                .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());
        return "включаю отдельно " + player.name;
    }

    public static String separateAllOff(Player player) {
        log.info("SEPARATE OFF");
        CompletableFuture.runAsync(() -> player.separateOffAll())
                .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());
        return "соединяю все играющие вместе";
    }

    public static String separateOff(Player player) {
        log.info("SEPARATE OFF");
        CompletableFuture.runAsync(() -> player.separateOff())
                .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());
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
        CompletableFuture.runAsync(() -> player.playChannelRelativeOrAbsolute(String.valueOf(index), false))
                .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());
        return answer;
    }
}
