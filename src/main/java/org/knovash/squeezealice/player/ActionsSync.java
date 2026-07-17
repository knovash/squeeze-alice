package org.knovash.squeezealice.player;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.levenstein.Levenstein;
import org.knovash.squeezealice.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class ActionsSync {

    public static String answer = null;

    // =========================================================================
    // 1. МЕТОДЫ, ИСПОЛЬЗУЕМЫЕ В ActionsAsync (асинхронные обёртки)
    //    (вызываются из асинхронных методов, которые, в свою очередь,
    //     используются в HandlePathAlice, SwitchPlayerCommand и других)
    // =========================================================================

    // ---- 1.1 Управление комнатами и плеерами ----
    // Используется в ActionsAsync.selectRoomWithSpeaker -> HandlePathAlice
    // Также вызывается из SwitchNoPlayerCommand? Нет, только голосовой навык.
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
        player = Utils.convertCyrilicToLatin(player);
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

    // Используется в ActionsAsync.selectRoomByCommand -> HandlePathAlice
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

    // Используется в ActionsAsync.selectPlayerByCommand и ActionsAsync.runPlayerByCommand
    // -> HandlePathAlice (выбери колонку, включи колонку)
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

    // ---- 1.2 Переключение музыки (sync) ----
    // Используется в ActionsAsync.switchHere -> HandlePathAlice, SwitchPlayerCommand
    public static String syncSwitchToHere(Player player) {
        log.info(start);
        if (player == null) return "ошибка, плеер не найден";
        player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null);
        log.info("SWITCH TO " + player.name);
        Spotify.currentlyPlaying(); // TODO ВКЛЮЧИТЬ исправить
        boolean spotifyPlaying = Spotify.currentlyPlaying != null && Spotify.currentlyPlaying.is_playing;
        if (spotifyPlaying) {
            log.info("Spotify is playing. Transferring to player: {}", player.name);
            ActionsSync.answer = "Переключаю spotify на " + player.name;
            Spotify.transferSpotifyToLms(player);
        } else {
            log.info("Spotify is not playing. Syncing to playing player and stopping others.");
            ActionsSync.answer = "Переключаю музыку на " + player.name;
            player.syncToPlayingOrPlayLast();
            player.stopOther();
        }
        log.info(finish);
        return "преключаю музыку";
    }

    // ---- 1.3 Spotify ----
    // Используются в ActionsAsync.playArtist/Album/Track/Playlist -> HandlePathAlice

    public static String spotifyPlayLink(String link, Player player, Boolean say) {
        log.info("PLAY LINK: " + link);
        if (link == null) {
            player.say("ошибка Spotify", true, true);
            return "настройте спотифай";
        }
        if (say) player.say("включаю " + Spotify.nameForSay, false, true);
        player
                .ifExpiredAndNotPlayingUnsyncWakeSetVolume(null)
                .volumeByTimeSet()
                .playPathSpotify(link)
                .syncOtherPlayingNotInGroupToThis();
        player.waitSeconds(2); // реально надо ждать пока плейлист спотифая запустится и потом сохранять
        log.info("SAVE PLAYLIST: " + Spotify.nameForSay); // TODO сохранять надо но так неуспевает и сохраняет плейсист с у ведомлением а не то что в споти
        player.savePlaylist(Spotify.nameForSay);
        lmsPlayers.afterAsync();
        ActionsSync.answer = "включаю " + Spotify.nameForSay;
        return "включаю " + Spotify.nameForSay;
    }

    public static String spotifyPlayArtist(String command, Player player, Boolean say) {
        String target = command
                .replaceAll(".*включи\\S*\\s", "")
                .replaceAll(".*Включи\\S*\\s", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("GET LINK BY ARTIST: " + target);
        String link = Spotify.getLinkArtist(target);
        log.info("PLAY LINK: " + link);
        return spotifyPlayLink(link, player, say);
    }

    public static String spotifyPlayAlbum(String command, Player player, Boolean say) {
        log.info("SPOTIFY PLAY ALBUM");
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("альбом", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        ActionsSync.answer = "Пытаюсь найти и включить альбом " + target;
        String link = Spotify.getLinkAlbum(target);
        log.info("PLAY LINK: " + link);
        return spotifyPlayLink(link, player, say);
    }

    public static String spotifyPlayTrack(String command, Player player, Boolean say) {
        log.info("SPOTIFY PLAY TRACK");
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("трэк", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        ActionsSync.answer = "Пытаюсь найти и включить трек " + target;
        String link = Spotify.getLinkTrack(target);
        log.info("PLAY LINK: " + link);
        return spotifyPlayLink(link, player, say);
    }

    public static String spotifyPlayPlaylist(String command, Player player, Boolean say) {
        log.info("SPOTIFY PLAY PLAYLIST");
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("плэйлист", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        ActionsSync.answer = "Пытаюсь найти и включить плейлист " + target;
        String link = Spotify.getLinkPlaylist(target);
        log.info("PLAY LINK: " + link);
        return spotifyPlayLink(link, player, say);
    }

    // ---- 1.4 Bluetooth-пульт ----
    // Используется в ActionsAsync.connectBtRemote -> HandlePathAlice, SwitchPlayerCommand
    public static String connectBtRemote(String command, Player player) {
        log.info("COMMAND: " + command + " PLAYER: " + player);
        command = command
                .replaceAll("включи", "")
                .replaceAll("подключи", "")
                .replaceAll("пульт ", "")
                .replaceAll(" к ", "")
                .replaceAll(" в ", "")
                .replaceAll(" на ", "");
        command = command.trim();
        log.info("COMMAND: _" + command + "_");

        Player playerByRoom = lmsPlayers.playerByPlayerNameOrRoomName(command, null);
        if (playerByRoom != null) player = playerByRoom;

        lmsPlayers.btPlayerName = player.name;
        lmsPlayers.btPlayerAliceId = ""; // сброс для следующих команд
        log.info("BT PLAYER NAME: " + lmsPlayers.btPlayerName);
        lmsPlayers.write();
        answer = "пульт подключен к " + player.name;
        return answer;
    }

    // Используется в ActionsAsync.whereBtRemote -> HandlePathAlice
    public static String whereBtRemote() {
        log.info("WHERE BT REMOTE");
        answer = "пульт подключен к " + lmsPlayers.btPlayerName;
        return answer;
    }

    // Используется в ActionsAsync.remoteSwitch -> SwitchNoPlayerCommand
    public static String remoteSwitch() {
        log.info("REMOTE SWITCH NEXT");
        log.info("SIZE: " + lmsPlayers.players.size() + " BT NOW: " + lmsPlayers.btPlayerName);
        if (lmsPlayers.players == null || lmsPlayers.players.size() < 2) return "";
        lmsPlayers.wakeUpAll();
        log.info("ALL WAKE UP FINISHED");
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

    // ---- 1.5 Избранное (каналы) ----
    // Используется в ActionsAsync.channelAdd -> HandlePathAlice, SwitchPlayerCommand
    public static String channelAdd(Player player) {
        String answer = "";
        log.info("FAVORITES ADD");
        String addTitle = player.favoritesAdd();
        answer = "добавила в избранное, " + addTitle;
        return answer;
    }

    // ---- 1.6 Информационные запросы ----
    // Используется в ActionsAsync.whatsPlaying -> HandlePathAlice, SwitchPlayerCommand
    public static String whatsPlaying(Player player, Boolean sayAtPlayer) {
        log.info(start);
        String answer = "";
        log.info("WHATS PLAYING ON " + player.name);
        if (player == null) return "плеер не найден";
        lmsPlayers.checkUpdated();
        if (!player.connected) return "плеер " + player.name + "  не подключен к медиасерверу";

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
        if (sayAtPlayer) atPlayer = "на " + player.name;

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

    // Используется в ActionsAsync.volumeLimitSet -> HandlePathAlice
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

    // Используется в ActionsAsync.whatsVolume -> HandlePathAlice
    public static String whatsVolume(Player player) {
        String answer;
        log.info("VOLUME");
        String volume = player.volumeGet();
        if (volume == null) return "медиасервер не отвечает";
        answer = "сейчас на " + player.name + " громкость " + volume + ", ограничение " + player.volume_high;
        return answer;
    }

    // =========================================================================
    // 2. ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ (НЕ ВЫЗЫВАЮТСЯ НАПРЯМУЮ ИЗ ActionsAsync)
    //    но используются внутри вышеперечисленных или в других частях системы
    // =========================================================================

    // ---- 2.1 Вспомогательные методы для управления комнатами ----
    // Используется внутри selectRoomWithSpeaker, selectRoomByCommand
    private static void selectRoomByCorrectRoom(String target, String aliceId) {
        log.info("START SELECT ROOM: " + target);
        roomsAndAliceIds.put(aliceId, target);
        Utils.writeRoomsAndAliceIds();
    }

    // Используется внутри selectPlayerByCommand, selectRoomWithSpeaker
    public static Player selectPlayerInRoom(String playerName, String roomName, Boolean start) {
        // TODO удалить использование в lmsPlayers и сделать приватным
        if (playerName == null || roomName == null) {
            log.info("PLAYER NAME OR ROOM IS NULL");
            answer = "колонка недоступна";
            return null;
        }
        Device device = smartHome.deviceByRoom(roomName);
        if (device == null) {
            log.info("CREATE NEW DEVICE FOR YANDEX IN ROOM: " + roomName + " PLAYER: " + playerName);
            smartHome.create(roomName, null);
        } else log.info("DEVICE ROOM: " + device.room + " ID: " + device.id);

        log.info("SEARCH NEW PLAYER: " + playerName);
        log.info("LMS PLAYERS: " + lmsPlayers.players.stream().filter(Objects::nonNull).map(player -> player.name).collect(Collectors.toList()));
        Player playerNew = lmsPlayers.playerByName(playerName);
        log.info("PLAYER NEW: " + playerNew);
        lmsPlayers.checkUpdated();
        if (!Boolean.TRUE.equals(playerNew.connected)) {
            log.info("PLAYER NOT CONNECTED: {}", playerName);
            answer = "колонка " + playerName + " недоступна";
            return null;
        }

        Player playerNow = lmsPlayers.playerByRoom(roomName);

        if (playerNow != null && playerNow.equals(playerNew)) {
            log.info("NEW PLAYER = NOW PLAYER");
            answer = "колонка " + playerName + " уже была подключена в комнате " + roomName;
            roomsAndPlayers.put(playerNew.name, playerNew.room);
            Utils.writeRoomsAndPlayers();
            lmsPlayers.write();
            return playerNow;
        } else {
            if (playerNow == null) {
                playerNew.room = roomName;
                log.info("ASSIGN NEW PLAYER: " + playerNew.name + " TO ROOM: " + roomName);
                answer = "подключена новая колонка " + playerName + " в комнате " + roomName;
            } else {
                playerNew.room = roomName;
                playerNow.room = null;
                if (playerNow.playing) start = true;
                if (playerNow.name.equals(lmsPlayers.btPlayerName)) lmsPlayers.btPlayerName = playerName;
                log.info("CHANGE PLAYER " + playerNow.name + " TO " + playerNew.name + " IN ROOM " + roomName);
                answer = "в комнате " + roomName + " изменена колонка " + playerNow.name + " на " + playerName;
            }

            roomsAndPlayers.put(playerNew.name, playerNew.room);
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

    // Используется в selectRoomWithSpeaker, selectPlayerByCommand
    private static String playerNameMatch(String player) {
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