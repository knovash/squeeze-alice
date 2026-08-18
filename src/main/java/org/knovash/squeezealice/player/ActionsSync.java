package org.knovash.squeezealice.player;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.utils.levenstein.Levenstein;
import org.knovash.squeezealice.yandex.Yandex;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class ActionsSync {

    public static String answer = null;

    // метод меняет плеер в комнате и возвращает ответ для Алисы
    public static String selectNewPlayerInRoom(String playerNewName, String roomName, boolean turnOn) {
        if (playerNewName == null || roomName == null) return null;

        // устройство Музыка еще небыло создано в комнате
        Device device = smartHome.deviceByRoom(roomName);
        if (device == null) {
            log.info("CREATE NEW DEVICE FOR YANDEX IN ROOM: " + roomName + " PLAYER: " + playerNewName);
            smartHome.create(roomName, null);
        } else log.info("DEVICE ROOM: " + device.room + " ID: " + device.id);

        Player playerNow = lmsPlayers.playerByRoom(roomName); // плеер сейчас в комнате
        Player playerNew = lmsPlayers.playerByName(playerNewName); // плеер новый из ЛМС
        if (playerNew == null) return "нет такого плеера";
        if (playerNew.equals(playerNow)) return "плеер уже подключен";
        if (!Boolean.TRUE.equals(playerNew.connected)) return "плеер недоступен";

        if (playerNow != null) {
            playerNow.room = null;
            if (playerNow.name.equals(lmsPlayers.btPlayerName)) lmsPlayers.btPlayerName = playerNewName;
            playerNow.unsync().pause();
        }
        playerNew.room = roomName;
        roomsAndPlayers.put(playerNew.name, playerNew.room);
        Utils.writeRoomsAndPlayers();
        lmsPlayers.write();

        if (turnOn) CompletableFuture.runAsync(() -> playerNew.turnOnMusic(null));

        if (playerNow != null)
            return "в комнате " + roomName + " изменена колонка " + playerNow.name + " на " + playerNewName;
        else
            return "подключена колонка " + playerNewName + " в комнате " + roomName;
    }

    // Раз Два это комната ... - привязка комнаты к AliceId
    public static String selectRoomWithSpeaker(String command, String aliceId) {
        log.info("START");
        String answer;
        String targetRoom = command
                .replaceAll(".*комната", "")
                .replaceAll("с колонкой.*", "")
                .replaceAll("\\s", "");
        String correctRoom = Utils.roomNameByNearest(targetRoom);
        if (correctRoom == null) {
            answer = "нет такой комнаты";
            return answer;
        }
        String room = correctRoom;
        String playerName = command
                .replaceAll(".*с колонкой", "")
                .replaceAll("\\s", "");
        playerName = Utils.convertCyrilicToLatin(playerName);
        playerName = getNearestPlayerNameFromLmsPlayers(playerName);
        if (playerName == null) {
            answer = "нет такой колонки";
            return answer;
        }
        log.info("SELECT ROOM: " + room);
        selectRoomByCorrectRoom(room, aliceId);
        log.info("SELECT PLAYER: " + playerName);
        answer = "это комната " + room + " с колонкой " + playerName;
        log.info("ANSWER: " + answer);
        selectNewPlayerInRoom(playerName, room, true);
        Player playerNew = lmsPlayers.playerByRoom(room);
        if (playerNew != null) playerNew.turnOnMusic(null);
        return answer;
    }

    // Раз Два это комната ... с колонкой ... - привязка комнаты к AliceId
    public static String selectRoomByCommand(String command, String aliceId) {
        log.info("SELECT ROOM BY COMMAND: " + command);
        String answer;
        String target = command
                .replaceAll(".*комната\\S*\\s", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        target = Utils.roomNameByNearest(target);
        log.info("TARGET: " + target);
        if (target == null) return "нет такой комнаты";
        String room = target;
        log.info("ROOM: " + room);

        selectRoomByCorrectRoom(target, aliceId);

        log.info("SELECT ROOM OK");
        Player player = lmsPlayers.playerByRoom(room);
        if (player != null) answer = "это комната " + room + " с колонкой " + player.name;
        else
            answer = "колонка в комнате еще не выбрана";
        return answer;
    }

    // Раз Два выбери колонку ...
    public static String getPlayerNameInRoomByCommand(String command) {
        log.info("SELECT PLAYER BY COMMAND: " + command);
        String playerInCommand = command
                .replaceAll(".*колонку\\S*\\s", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        String playerName = getNearestPlayerNameFromLmsPlayers(playerInCommand);
        log.info("NAME: " + playerInCommand + " TARGET: " + playerName);
        return playerName;
    }

    // Раз Два Лимит ... - установить лимит - ответ: Лимит установлен ...
    public static String volumeLimitSet(Player player, String command) {
        log.info(">>> PLAYER: " + player);
        log.info(">>> COMMAND: " + command);
        String answer; // сверху используется future.get
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+");
        java.util.regex.Matcher matcher = pattern.matcher(command);
        Integer volume = null;
        if (matcher.find()) volume = Integer.parseInt(matcher.group());
        log.info(">>> LIMIT: " + volume);
        if (volume != null && volume > 20 && volume < 100) {
            player.volume_high = volume;
            answer = player.name + ", ограничение громкости " + volume;
        } else
            answer = player.name + ", ошибка ограничения громкости. число должно быть от 20 до 100";
        return answer;
    }

    // Девайс-кнопка Переключи сюда
    public static void switchToHere(Player player) {
        log.info(start);
        if (player == null) return;
        log.info("SWITCH TO " + player.name);
        player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false);
        Spotify.checkSpotifyIsPlaying();
        if (Spotify.currentlyPlaying != null && Spotify.currentlyPlaying.is_playing) {
            log.info("SWITCH SPOTIFY TO " + player.name);
            Spotify.transferSpotifyToLms(player);
        } else {
            log.info("SWITCH MUSIC TO " + player.name);
            player.syncToPlayingOrPlayLast();
            player.stopOther();
        }
        log.info(finish);
    }


    // Пульт - голосовая команда - Подключи пульт ...
    public static String remoteSetFromCommand(String command, Player player) {
        log.info("COMMAND: " + command + " PLAYER: " + player);
        String target = command
                .replaceAll("включи", "")
                .replaceAll("подключи", "")
                .replaceAll("пульт ", "")
                .replaceAll(" к ", "")
                .replaceAll(" в ", "")
                .replaceAll(" на ", "")
                .trim();
        log.info("TARGET: _" + target + "_");

        Player playerByRoom = lmsPlayers.playerByPlayerNameOrRoomName(target, target);
        if (playerByRoom != null) player = playerByRoom;

        lmsPlayers.btPlayerName = player.name;
        lmsPlayers.btPlayerAliceId = ""; // сброс для следующих команд
        log.info("BT PLAYER NAME: " + lmsPlayers.btPlayerName);
        lmsPlayers.write();
        return "пульт подключен к " + lmsPlayers.btPlayerName;
    }

    // Пульт - кнопка - Переключи пульт на следующий плеер
    public static String remoteSwitch() {
        log.info("REMOTE SWITCH NEXT");
        log.info("SIZE: " + lmsPlayers.players.size() + " BT NOW: " + lmsPlayers.btPlayerName);
        if (lmsPlayers.players == null || lmsPlayers.players.size() < 2) return "";
        Player playerNow = lmsPlayers.playerByName(lmsPlayers.btPlayerName);
        log.info("PLAYER NOW: " + playerNow);
        if (playerNow == null) {
            lmsPlayers.btPlayerName = lmsPlayers.players.get(0).name;
            log.info("BT PLAYER SWITCH TO: " + lmsPlayers.btPlayerName);
            return lmsPlayers.btPlayerName;
        }
        List<Player> playersConnected = lmsPlayers.players.stream().filter(p -> p.connected).collect(Collectors.toList());
        log.info("SIZE: " + playersConnected.size() + " BT NOW: " + lmsPlayers.btPlayerName);
        int indexNext = playersConnected.indexOf(playerNow) + 1;
        if (indexNext > playersConnected.size() - 1) indexNext = 0;
        Player playerNext = playersConnected.get(indexNext);
        lmsPlayers.btPlayerName = playerNext.name;
        lmsPlayers.write();
        log.info("SIZE: " + lmsPlayers.players.size() + " PLAYER NOW: " + playerNow.name + " INDEX NEXT: " + indexNext);
        log.info("BT PLAYER SWITCH TO: " + lmsPlayers.btPlayerName);
        playerNext.say("пульт подключен к " + playerNext.name, true, true);
        return lmsPlayers.btPlayerName;
    }


    // возвращает ответ что играет и также кладет его в answer
    public static String whatsPlaying(Player player, Boolean addAtPlayerName) {
        log.info(start);
        String answer = "";
        log.info("WHATS PLAYING ON " + player.name);
        if (player == null) {
            answer = "плеер не найден";
            return answer;
        }
        lmsPlayers.checkUpdated();
        if (!player.connected) {
            answer = "плеер " + player.name + "  не подключен к медиасерверу";
            return answer;
        }

        String title = player.title(); // получить что играет - название плейлиста или исполнителя
        player.volumeGet(); // получить громкость

        if (title == null || "".equals(title) || "unknown".equals(title)) title = "ничего";
        Map<String, List<String>> pp = player.playingPlayersNameGroups(false);
        List<String> playersNamesInCurrentGroup = pp.get("inGroup");
        List<String> playingPlayersNamesNotInCurrentGrop = pp.get("notInGroup");
        String separate = "";
        if (player.separate) separate = "отдельно ";
        String answerOtherInGroup = "";
        if (playersNamesInCurrentGroup.size() > 0)
            answerOtherInGroup = ", вместе " + String.join(", ", playersNamesInCurrentGroup);
        String answerPlayingSeparate = "";
        if (playingPlayersNamesNotInCurrentGrop.size() != 0)
            answerPlayingSeparate = ", отдельно играет " + String.join(", ", playingPlayersNamesNotInCurrentGrop);

        String atPlayer = "";
        if (addAtPlayerName) atPlayer = "на " + player.name;

        if (player.mode.equals("play")) {
            answer = "сейчас " + atPlayer + " играет " + separate + title + ", громкость " + player.volume;
        }
        if (!player.mode.equals("play")) {
            answer = "сейчас " + atPlayer + " не играет " + separate + title;
        }
        answer = answer + answerOtherInGroup + answerPlayingSeparate;
        log.info("ANSWER: " + answer);
        log.info(finish);
        return answer;
    }


    // PRIVATE ------------------------

    private static void selectRoomByCorrectRoom(String target, String aliceId) {
        log.info("START SELECT ROOM: " + target);
        roomsAndAliceIds.put(aliceId, target);
        Utils.writeRoomsAndAliceIds();
    }

    private static String getNearestPlayerNameFromLmsPlayers(String player) {
        List<String> players = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        player = Utils.convertCyrilicToLatin(player);
        log.info("PLAYER " + player);
        log.info("PLAYERS " + players);
        String correctPlayer = Levenstein.getNearestElementInListWord(player, players);
        if (correctPlayer == null) log.info("ERROR PLAYER NOT EXISTS IN LMS ");
        log.info("PLAYER NAME " + player + " MATCH TO " + correctPlayer);
        return correctPlayer;
    }

}