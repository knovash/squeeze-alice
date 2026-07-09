package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class ActionsSync {

    public static String answer = null;

    // =========================================================================
    // СПОТИФАЙ (синхронные версии)
    // =========================================================================

    public static String spotifyPlayArtist(String command, Player player, Boolean say) {
        String target = command
                .replaceAll(".*включи\\S*\\s", "")
                .replaceAll(".*Включи\\S*\\s", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("\nGET LINK BY ARTIST: " + target);
        String link = Spotify.getLinkArtist(target);
        log.info("\nPLAY LINK: " + link);

        if (link == null) {
            player.say("ошибка Spotify", true);
            return "настройте спотифай";
        }
        if (say) player.say("включаю " + Spotify.nameForSay, false); // если от алисы то не говорить в лмс
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

    public static String whatsPlaying(Player player, Boolean sayAtPlayer) {
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

        String atPlayer = "";
        if (sayAtPlayer) atPlayer = "на " + player.name;

        if (player.mode.equals("play")) {
            answer = "сейчас " + atPlayer + " играет " + separate + title + ", громкость " + player.volume;
        }
        if (!player.mode.equals("play")) {
            answer = "сейчас " + atPlayer + " не играет " + separate + title;
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

    public static String sayMyName(Player player) {
        log.info("SAY MY NAME");
        String textFromFile;
        try {
            textFromFile = Files.readString(Paths.get("/root/say.txt"));
        } catch (IOException e) {
            log.error("Не удалось прочитать /say.txt: " + e.getMessage());
            textFromFile = "[текст из файла недоступен]";
        }
        String answer = "на " + lmsPlayers.btPlayerName + " включаю " + textFromFile;
        return answer;
    }


    public static String sayMyText() {
        log.info("SAY MY TEXT: " + sayText);
        String answer = sayText;
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
        String correctRoom = Utils.roomNameByNearest(targetRoom);
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
        target = Utils.roomNameByNearest(target);
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

        } else log.info("DEVICE ROOM: " + device.room + " ID: " + device.id);

        log.info("SEARCH NEW PLAYER: " + playerName);
        log.info("LMS PLAYERS: " + lmsPlayers.players.stream().filter(Objects::nonNull).map(player -> player.name).collect(Collectors.toList()));
        Player playerNew = lmsPlayers.playerByName(playerName); // поиск нового плеера по имени плеера для комнаты и проверка что доступен
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

                // если пульт был подключен к заменяемой колонке то пульт переключить к новой
                if (playerNow.name.equals(lmsPlayers.btPlayerName)) lmsPlayers.btPlayerName = playerName;

                log.info("CHANGE PLAYER " + playerNow.name + " TO " + playerNew.name + " IN ROOM " + roomName);
                answer = "в комнате " + roomName + " изменена колонка " + playerNow.name + " на " + playerName;
            }

            roomsAndPlayers.put(playerNew.name, playerNew.room); // сохранить файл соответствия плееров в комнатах
            Utils.writeRoomsAndPlayers();

            lmsPlayers.write();

            log.info("LMS PLAYERS: " + lmsPlayers.players.stream().filter(Objects::nonNull).map(p -> p.name).collect(Collectors.toList()));
            log.info("YANDEX DEVICES: " + SmartHome.devices.stream().filter(Objects::nonNull).map(d -> d.room + " " + d.name + " ext:" + d.external_id + " id:" + d.id).collect(Collectors.toList()));

        }

        if (start) {
            log.info("TURN ON NEW PLAYER " + playerNew.name);
            playerNew.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null);
            if (!playerNew.playing)
                if (playerNow.isPlaying()) playerNew.syncTo(playerNow.name);
                else
                    playerNew.syncToPlayingOrPlayLast();

            answer = answer + ". включаю";
        } else log.info("NOT START PLAY (flag start false)");

        if (playerNow != null) {
            log.info("STOP PREVIOUS PLAYER IN ROOM " + playerNow.name);
            playerNow.unsync().pause();
        }
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
//        String answer;
        log.info("CONNECT BT REMOTE");
        log.info("COMMAND: " + command);
        command = command
                .replaceAll("включи", "")
                .replaceAll("подключи", "")
                .replaceAll("пульт ", "")
                .replaceAll(" к ", "")
                .replaceAll(" в ", "")
                .replaceAll(" на ", "");
//                .replaceAll(" ", "");
        command = command.trim();
        log.info("COMMAND: _" + command + "_");
        String playerName = null;

        String room = Utils.roomNameByNearest(command);
        Player playerNew = null;
        if (room == null) playerNew = lmsPlayers.playerByNearestName(command);

        if (playerNew != null) {
            player = playerNew;
        }

        log.info("ROOM: " + room);
        if (room != null) { // если в команде было название комнаты то подключить пульт к плееру в комнате из команды

            log.info("PLAYER BY PLAYERNAME: " + player);
            if (player == null) player = lmsPlayers.playerByNearestRoom(command);
            if (player == null) {
                answer = "плеер не найден";
                return answer;
            }
        }
        // если в команде небыло комнаты то подключить пульт в этой комнате

        lmsPlayers.btPlayerName = player.name;
        lmsPlayers.btPlayerAliceId = "";
        log.info("BT PLAYER NAME: " + lmsPlayers.btPlayerName);
        lmsPlayers.write();
        answer = "пульт подключен к " + player.name;
        return answer;
    }

    public static void connectBtRemoteToPlayer(Player player) {
        lmsPlayers.btPlayerName = player.name;

        log.info("BT PLAYER NAME: " + lmsPlayers.btPlayerName);
        lmsPlayers.write();
    }
    public static String remoteSwitch() {
        lmsPlayers.wakeUpAll();
        log.info("ALL WAKE UP FINISHED ----------------");

        // Получаем текущего плеера по имени
        String remoteNow = lmsPlayers.btPlayerName;
        Player playerNow = lmsPlayers.playerByName(remoteNow);
        int size = lmsPlayers.players.size();

        if (size == 0) {
            log.warn("No players available");
            return null;
        }

        // Если текущий плеер не найден, берём первого подключённого (или null)
        if (playerNow == null) {
            for (Player p : lmsPlayers.players) {
                if (p.connected) {
                    playerNow = p;
                    break;
                }
            }
            if (playerNow == null) {
                log.warn("No connected player found");
                return null;
            }
            // Если нашли, обновляем индекс
        }
        int indexNow = lmsPlayers.players.indexOf(playerNow);
        int startIndex = indexNow;
        int indexNew = (indexNow + 1) % size; // следующий по кругу
        Player playerNew = null;
        // Циклически ищем подключённый плеер, начиная со следующего
        while (indexNew != startIndex) {
            Player candidate = lmsPlayers.players.get(indexNew);
            if (candidate.connected) {
                playerNew = candidate;
                break;
            }
            indexNew = (indexNew + 1) % size;
        }
        // Если не нашли подключённый (все отключены), пытаемся оставить текущий, если он подключён
        if (playerNew == null) {
            if (playerNow.connected) {
                playerNew = playerNow;
            } else {
                log.warn("No connected players found");
                return null;
            }
        }

        // Переключаемся на найденный плеер
        lmsPlayers.btPlayerName = playerNew.name;
        log.info("BT PLAYER SWITCH TO: " + lmsPlayers.btPlayerName);

        if ("play".equals(playerNew.mode)) {
            playerNew.pause().play();
        } else {
            playerNew.sound("beep_long", true);
        }

        lmsPlayers.write();
        return lmsPlayers.btPlayerName;
    }
//    public static String remoteSwitch() {
//        lmsPlayers.wakeUpAll();
//        String remoteNow = lmsPlayers.btPlayerName;
//        Player playerNow = lmsPlayers.playerByName(remoteNow);
//        int indexNow = lmsPlayers.players.indexOf(playerNow);
//        int indexNew = indexNow + 1;
//        int size = lmsPlayers.players.size();
//        if (indexNow == size - 1) indexNew = 0;
//        Player playerNew = lmsPlayers.players.get(indexNew);
//        if(!playerNew.connected){indexNew = indexNew+1} // и далее повторить пока плееер не найдется подклченный playerNew.connected
//
//        lmsPlayers.btPlayerName = playerNew.name;
//        log.info("BT PLAYER SWITCH TO: " + lmsPlayers.btPlayerName);
//        if ("play".equals(playerNew.mode)) { // если этот плеер играет
//            playerNew.pause().play();
//        } else playerNew.sound("beep_long", true);
//        lmsPlayers.write();
//        return lmsPlayers.btPlayerName;
//    }

    public static String whereBtRemote() {
        log.info("WHERE BT REMOTE");
        answer = "пульт подключен к " + lmsPlayers.btPlayerName;
        return answer;
    }

    public static String volumeLimitSet(Player player, String command) {
        log.info(">>> PLAYER: " + player);
        log.info(">>> COMMAND: " + command);
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


}