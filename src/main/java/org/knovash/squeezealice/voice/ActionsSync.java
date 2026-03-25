package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.yandex.Yandex;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class ActionsSync {

    public static String answer = null;

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
                .ifExpiredAndNotPlayingUnsyncWakeSetVolume(null)
                .playPath(link)
                .syncOtherPlayingNotInGroupToThis();
        lmsPlayers.afterAsync();
        return "включаю " + target;
    }

    public static String spotifyPlayAlbum(String command, Player player) {
        log.info("SPOTIFY PLAY ALBUM");
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("альбом", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        ActionsSync.answer = "Пытаюсь найти и включить альбом " + target;
        String link = Spotify.getLinkAlbum(target);
        log.info("LINK: " + link);
        if (link == null) {
            ActionsSync.answer = "Не удалось найти альбом " + target;
        } else {
            player.playPath(link);
            ActionsSync.answer = "Включаю альбом " + target;
        }
        return ActionsSync.answer;
    }

    public static String spotifyPlayTrack(String command, Player player) {
        log.info("SPOTIFY PLAY TRACK");
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("трэк", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        ActionsSync.answer = "Пытаюсь найти и включить трек " + target;
        String link = Spotify.getLinkTrack(target);
        log.info("LINK: " + link);
        if (link == null) {
            ActionsSync.answer = "Не удалось найти трек " + target;
        } else {
            player.playPath(link);
            ActionsSync.answer = "Включаю трек " + target;
        }
        return ActionsSync.answer;
    }

    public static String spotifyPlayPlaylist(String command, Player player) {
        log.info("SPOTIFY PLAY PLAYLIST");
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("плэйлист", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        ActionsSync.answer = "Пытаюсь найти и включить плейлист " + target;
        String link = Spotify.getLinkPlaylist(target);
        log.info("LINK: " + link);
        if (link == null) {
            ActionsSync.answer = "Не удалось найти плейлист " + target;
        } else {
            player.playPath(link);
            ActionsSync.answer = "Включаю плейлист " + target;
        }
        return ActionsSync.answer;
    }


    // =========================================================================
    // ПЕРЕКЛЮЧЕНИЕ МУЗЫКИ (синхронное)
    // =========================================================================

    public static String syncSwitchToHere(Player player) {
        if (player == null) return "ошибка, плеер не найден";
        player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null);
        log.info("SWITCH TO " + player.name);
        boolean spotifyPlaying = Spotify.currentlyPlaying != null && Spotify.currentlyPlaying.is_playing;
        log.info("Spotify check if playing");
        if (spotifyPlaying) {
            log.info("Spotify is playing. Transferring to player: {}", player.name);
            ActionsSync.answer = "Переключаю spotify на " + player.name;
            Spotify.transfer(player);
        } else {
            log.info("Spotify is not playing. Syncing to playing player and stopping others.");
            ActionsSync.answer = "Переключаю музыку на " + player.name;
            player.syncToPlayingOrPlayLast();
            player.stopOther();
        }
        return "преключаю музыку";
    }

    // =========================================================================
    // ИНФОРМАЦИЯ О ПЛЕЕРЕ
    // =========================================================================

    public static String whatsPlaying(Player player) {
        String answer = "";
        log.info("WHATS PLAYING ON " + player.name);
        if (player == null) return "плеер не найден";
        lmsPlayers.checkUpdated(); // TODO DEBUG
        if (!player.connected) return "плеер " + player.name + "  не подключен к медиасерверу";
        String title = player.title();
        player.volumeGet();

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
        player = playerNameMatch(player);
        if (player == null) return "нет такой колонки";
        log.info("SELECT ROOM: " + room);
        selectRoomByCorrectRoom(room, aliceId);
        log.info("SELECT PLAYER: " + player);
        ActionsSync.answer = "это комната " + room + " с колонкой " + player;
        Player playerNew = selectPlayerInRoom(player, room, false);
        if (playerNew != null) playerNew.turnOnMusic(null);
        return ActionsSync.answer;
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
        Player player = lmsPlayers.playerByRoom(room);
        if (player != null) ActionsSync.answer = "это комната " + room + " с колонкой " + player.name;
        else
            ActionsSync.answer = "колонка в комнате еще не выбрана";

        return ActionsSync.answer;
    }

    public static void selectRoomByCorrectRoom(String target, String aliceId) {
        log.info("START SELECT ROOM: " + target);
        roomsAndAliceIds.put(aliceId, target);
        Utils.writeRoomsAndAliceIds();
    }

    public static void selectPlayerByCommand(String command, String room, Boolean start) {
        log.info("SELECT PLAYER BY COMMAND: " + command);
        String name = command
                .replaceAll(".*колонку\\S*\\s", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        String target;
        target = playerNameMatch(name);

        log.info("NAME: " + name + " TARGET: " + target);
        if (target == null) {
            answer = "в медиасервере нет колонки " + name;
            return;
        }


        selectPlayerInRoom(target, room, start);

    }

    public static Player selectPlayerInRoom(String playerName, String roomName, Boolean start) {
        if (playerName == null || roomName == null) {
            log.info("PLAYER NAME OR ROOM IS NULL");
            answer = "колонка недоступна";
            return null;
        }
        Device device = smartHome.deviceByRoom(roomName);
//        log.info("GET DEVICE: ROOM:" + device.room + " EXTID " + device.external_id);
        if (device == null) { // если девайса с такой комнатой еще нет то создать девайс. надо будет подключить его в яндексе
            log.info("CREATE NEW DEVICE FOR YANDEX IN ROOM: " + roomName + " PLAYER: " + playerName);
            smartHome.create(roomName, null); // TODO добавить проверку что девайс создался
            smartHome.write();
        } else log.info("DEVICE ROOM: " + device.room + " ID: " + device.id);

        log.info("SEARCH NEW PLAYER: " + playerName);
        log.info("LMS PLAYERS: " + lmsPlayers.players.stream().filter(Objects::nonNull).map(player -> player.name + ":" + player.getClass().getName()).collect(Collectors.toList()));
        Player playerNew = lmsPlayers.playerByName(playerName); // поиск нового плеера по имени плеера для комнаты и проверка что доступен
        log.info("PLAYER NEW: " + playerNew.getClass().getName());
        log.info("PLAYER NEW: " + playerNew);
        lmsPlayers.checkUpdated(); // TODO DEBUG
        if (!Boolean.TRUE.equals(playerNew.connected)) { // если плеер недоступен остановить выбор плеера
            log.info("PLAYER NOT CONNECTED: {}", playerName);
            answer = "колонка " + playerName + " недоступна";
            return null;
        }

        Player playerNow = lmsPlayers.playerByRoom(roomName); // поиск текущего плеера в комнате по имени комнаты

        if (playerNow != null && playerNow.equals(playerNew)) { // если плееры одинаковы ничего не делать
            log.info("NEW PLAYER = NOW PLAYER");
            answer = "колонка " + playerName + " уже была подключена в комнате " + roomName;
            roomsAndPlayers.put(playerNew.name, playerNew.room); // сохранить файл соответствия плееров в комнатах
            Utils.writeRoomsAndPlayers();
            smartHome.write();
            lmsPlayers.write();
            return playerNow;
        } else {
            if (playerNow == null) { // привязка нового плеера к комнате
                playerNew.room = roomName;
//                playerNew.deviceId = device.id;
                log.info("ASSIGN NEW PLAYER: " + playerNew.name + " TO ROOM: " + roomName);
                answer = "подключена новая колонка " + playerName + " в комнате " + roomName;
            } else { // замена плеера в комнате
                playerNew.room = roomName;
                playerNow.room = null;
//                playerNow.unsync().pause();
                if (playerNow.playing) start = true; // если текущий плеер играл то новый тоже включить


                log.info("CHANGE PLAYER " + playerNow.name + " TO " + playerNew.name + " IN ROOM " + roomName);
                answer = "в комнате " + roomName + " изменена колонка " + playerNow.name + " на " + playerName;
            }

            roomsAndPlayers.put(playerNew.name, playerNew.room); // сохранить файл соответствия плееров в комнатах
            Utils.writeRoomsAndPlayers();

            smartHome.write();
            lmsPlayers.write();

            log.info("LMS PLAYERS: " + lmsPlayers.players.stream().filter(Objects::nonNull).map(p -> p.name).collect(Collectors.toList()));
            log.info("YANDEX DEVICES: " + SmartHome.devices.stream().filter(Objects::nonNull).map(d -> d.room).collect(Collectors.toList()));

        }

        if (start) {
            log.info("TURN ON NEW PLAYER " + playerNew.name);
            playerNew.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null);
            if (!playerNew.playing)
                if (playerNow.isPlaying())                playerNew.syncTo(playerNow.name);
            else
                playerNew.syncToPlayingOrPlayLast();

            answer = answer + ". включаю";
        }

        playerNow.unsync().pause();
        return playerNew;
    }

    public static String playerNameMatch(String player) {
        List<String> players = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        player = Utils.convertCyrilic(player);
        log.info("PLAYER " + player);
        log.info("PLAYERS " + players);
        String correctPlayer = Levenstein.getNearestElementInListWord(player, players);
        if (correctPlayer == null) log.info("ERROR PLAYER NOT EXISTS IN LMS ");
        log.info("PLAYER NAME " + player + " MATCH TO " + correctPlayer);
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

        lmsPlayers.checkUpdated(); // TODO DEBUG
        if (!command.equals("\\s*") && !command.equals("")) {
            List<String> pll = lmsPlayers.players.stream()
                    .filter(p -> p.connected)
                    .map(p -> p.name)
                    .collect(Collectors.toList());
            playerName = Levenstein.search(command, pll);
            log.info("PLAYER NAME: " + playerName);
            if (playerName != null) player = lmsPlayers.playerByName(playerName);
            if (player == null) return "плеер не найден " + command;

            String roomName = Levenstein.search(command, Yandex.rooms);
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