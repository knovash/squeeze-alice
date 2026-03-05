package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class ActionsSync {

    // =========================================================================
    // СПОТИФАЙ (синхронные версии)
    // =========================================================================

    public static String spotifyPlayArtist(String command, Player player) {
        String target = command
                .replaceAll(".*включи\\S*\\s", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("\nGET LINK BY ARTIST: " + target);
        String link = Spotify.getLinkArtist(target);
        log.info("\nPLAY LINK: " + link);
        if (link == null) return "настройте спотифай";
        player
                .ifExpiredAndNotPlayingUnsyncWakeSet(null)
                .playPath(link)
                .syncAllOtherPlayingToThis();
        lmsPlayers.afterAll();
        return "включаю " + target;
    }

    // =========================================================================
    // ПЕРЕКЛЮЧЕНИЕ МУЗЫКИ (синхронное)
    // =========================================================================

    public static String syncSwitchToHere(Player player) {
        log.info("SWITCH TO " + player.name);
        Spotify.currentlyPlaying = null;
        boolean spotifyPlaying = Spotify.currentlyPlaying != null && Spotify.currentlyPlaying.is_playing;
        player.switchToHereAsync(spotifyPlaying);
        if (Spotify.currentlyPlaying != null && Spotify.currentlyPlaying.is_playing) {
            log.info("Spotify is playing. Transfer to player");
            return "переключаю spotify на " + player.name;
        } else {
            log.info("Transfer LMS player to player");
            return "переключаю музыку на " + player.name;
        }
    }

    // =========================================================================
    // ИНФОРМАЦИЯ О ПЛЕЕРЕ
    // =========================================================================

    public static String whatsPlaying(Player player) {
        String answer = "";
        log.info("WHATS PLAYING ON " + player.name);
        if (player == null) return "плеер не найден";
        if (!player.connected) return "плеер " + player.name + "  не подключен к медиасерверу";
        String title = player.title();

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
        if (player.mode.equals("play")) {
            answer = "сейчас на " + player.name + " играет " + separate + title + " громкость " + player.volume;
        }
        if (!player.mode.equals("play")) {
            answer = "сейчас на " + player.name + " не играет " + separate + title;
        }
        answer = answer + answerOtherInGroup + answerPlayingSeparate;
        log.info("ANSWER: " + answer);
        return answer;
    }

    public static String whatsVolume(Player player) {
        String answer;
        log.info("VOLUME");
        String volume = player.volumeGet();
        if (volume == null) return "медиасервер не отвечает";
        answer = "сейчас на " + player.name + " громкость " + volume;
        return answer;
    }

    // =========================================================================
    // ИЗБРАННОЕ (синхронное)
    // =========================================================================

    public static String channelAdd(Player player) {
        String answer = "";
        log.info("FAVORITES ADD");
        String addTitle = player.favoritesAdd();
        answer = "добавила в избранное, " + addTitle;
        return answer;
    }

    // =========================================================================
    // УПРАВЛЕНИЕ КОМНАТАМИ И ПЛЕЕРАМИ (синхронные версии)
    // =========================================================================

    public static String selectRoomWithSpeaker(String command, String aliceId) {
        log.info("START");
        String targetRoom = command
                .replaceAll(".*комната", "")
                .replaceAll("с колонкой.*", "")
                .replaceAll("\\s", "");
        String correctRoom = Utils.getCorrectRoomName(targetRoom);
        if (correctRoom == null) return "нет такой комнаты";
        String room = correctRoom;
        String player = command
                .replaceAll(".*с колонкой", "")
                .replaceAll("\\s", "");
        player = Utils.convertCyrilic(player);
        player = correctPlayerName(player);
        if (player == null) return "нет такой колонки";
        log.info("SELECT ROOM: " + room);
        selectRoomByCorrectRoom(room, aliceId);
        log.info("SELECT PLAYER: " + player);
        Player playerNew = selectPlayerInRoom(player, room, false);
        if (playerNew != null) playerNew.turnOnMusic(null);
        return "это комната " + room + " с колонкой " + player;
    }

    public static String selectRoomByCommand(String command, String aliceId) {
        log.info("SELECT ROOM BY COMMAND: " + command);
        String target = command
                .replaceAll(".*комната\\S*\\s", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        target = Utils.getCorrectRoomName(target);
        log.info("TARGET: " + target);
        if (target == null) return "нет такой комнаты";
        String room = target;
        log.info("ROOM: " + room);

        selectRoomByCorrectRoom(target, aliceId);

        log.info("SELECT ROOM OK");
        String whithPlayerName = "";
        Player player = lmsPlayers.playerByRoom(room);
        if (player != null) whithPlayerName = ". с колонкой " + player.name;
        else whithPlayerName = ". колонка в комнате еще не выбрана ";
        return "это комната " + room + whithPlayerName;
    }

    public static void selectRoomByCorrectRoom(String target, String aliceId) {
        log.info("START SELECT ROOM: " + target);
        idRooms.put(aliceId, target);
        JsonUtils.mapToJsonFile(idRooms, config.fileRooms);
    }

    public static String selectPlayerByCommand(String command, String room) {
        log.info("SELECT PLAYER BY COMMAND: " + command);
        String target = command
                .replaceAll(".*колонку\\S*\\s", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");

        log.info("TARGET: " + target);
        target = correctPlayerName(target);
        log.info("TARGET: " + target);
        if (target == null) return "нет такой колонки";

        Player player = selectPlayerInRoom(target, room, true);

        if (player == null) {
            log.info("PLAYER OFFLINE: " + target);
            return "колонка недоступна. " + target + " в комнате " + room;
        } else {
            log.info("PLAYER SELECTED: " + target);
            return "выбрана колонка " + target +
                    " в комнате " + room;
        }
    }

    public static Player selectPlayerInRoom(String playerName, String roomName, Boolean start) {
        log.info("CHECK IF DEVICE EXISTS IN SMART HOME " + roomName);
        if (playerName == null || roomName == null) {
            log.info("ERROR NULL");
            return null;
        }
        Device device = smartHome.deviceByRoom(roomName);
        if (device == null) {
            log.info("CREATE NEW DEVICE IN SMART HOME ROOM: " + roomName);
            smartHome.create(roomName, playerName);
            smartHome.write();
        } else log.info("DEVICE EXISTS IN ROOM: " + device.room);

        log.info("SELECT PLAYER IN ROOM " + roomName + " BY PLAYER IN COMMAND: " + playerName);
        List<String> playerNames = lmsPlayers.players.stream().map(player -> player.name).collect(Collectors.toList());
        log.info("PLAYERS IN LMS: " + playerNames);
        Player playerNew = lmsPlayers.playerByName(playerName);

        if (!playerNew.connected) {
            log.info("PLAYER OFFLINE " + playerName);
            return null;
        }
        if (playerNew != null) log.info("PLAYER NEW: " + playerNew.name);
        Player playerNow = lmsPlayers.playerByRoom(roomName);
        if (playerNow != null) log.info("PLAYER NOW: " + playerNow.name);

        if (playerNow != null) {
            log.info("SWAP PLAYERS IN ROOM: " + roomName + " NOW: " + playerNow.name + " ID: " + playerNow.deviceId +
                    " <- NEW: " + playerNew.name + " ID: " + playerNew.deviceId);
            String playerNowDeviceId = playerNow.deviceId;
            log.info("ROOM: " + roomName + " ID: " + playerNowDeviceId);
            playerNow.room = null;
            playerNow.deviceId = null;
            playerNew.room = roomName;
            playerNew.deviceId = playerNowDeviceId;
        } else {
            log.info("SWAP PLAYERS IN ROOM: " + roomName + " NOW: --- " +
                    " <- NEW: " + playerNew.name + " ID: " + playerNew.deviceId);
            playerNew.room = roomName;
            playerNew.deviceId = SmartHome.deviceByRoom(roomName).id;
        }
        lmsPlayers.write();
        if (start) {
            log.info("TURN ON NEW PLAYER " + playerNew.name);
            playerNew.turnOnMusic(null);
            if (playerNow != null) {
                log.info("STOP CURRENT PLAYER " + playerNow.name);
                playerNow.unsync().pause();
            }
        }
        return playerNew;
    }

    public static String correctPlayerName(String player) {
        log.info("START: " + player);
        List<String> players = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        player = Utils.convertCyrilic(player);
        String correctPlayer = Levenstein.getNearestElementInListWord(player, players);
        if (correctPlayer == null) log.info("ERROR PLAYER NOT EXISTS IN LMS ");
        log.info("CORRECT PLAYER: " + player + " -> " + correctPlayer);
        return correctPlayer;
    }

    // =========================================================================
    // BLUETOOTH ПУЛЬТ (синхронные версии)
    // =========================================================================

    public static String connectBtRemote(String command, Player player) {
        String answer;
        log.info("CONNECT BT REMOTE");
        log.info("COMMAND: " + command);
        command = command.replaceAll(".*пульт", "");
        log.info("COMMAND: _" + command + "_");
        String playerName = null;
        if (!command.equals("\\s*") && !command.equals("")) {
            List<String> pll = lmsPlayers.players.stream()
                    .filter(p -> p.connected)
                    .map(p -> p.name)
                    .collect(Collectors.toList());
            playerName = Levenstein.search(command, pll);
            log.info("PLAYER NAME: " + playerName);
            if (playerName != null) player = lmsPlayers.playerByName(playerName);
            if (player == null) return "плеер не найден " + command;

            String roomName = Levenstein.search(command, rooms);
            log.info("ROOM NAME: " + roomName);
            if (roomName != null) player = lmsPlayers.playerByRoom(roomName);
            if (player == null) return "плеер не найден " + command;
        }
        lmsPlayers.btPlayerName = player.name;
        lmsPlayers.write();
        answer = "пульт подключен к " + player.name;
        return answer;
    }

    public static String whereBtRemote() {
        String answer;
        log.info("WHERE BT REMOTE");
        answer = "пульт подключен к " + lmsPlayers.btPlayerName;
        return answer;
    }
}