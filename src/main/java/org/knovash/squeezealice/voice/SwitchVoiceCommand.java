package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.*;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.spotify.spotify_pojo.Type;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class SwitchVoiceCommand {

    public static String aliceId;
    public static String room;
    public static String clientType = "speaker";

    public static Context action(Context context) {
        String answer = switchVoiceCommand(context);
        context.json = createResponse(answer);
        context.code = 200;
        return context;
    }

    public static String createResponse(String text) {
        AliceVoiceResponsePojo alice = new AliceVoiceResponsePojo();
        AliceVoiceResponsePojo.ResponseAlice responseAlice = new AliceVoiceResponsePojo.ResponseAlice();
        responseAlice.text = text;
        responseAlice.end_session = true;
        if (clientType.equals("browser")) responseAlice.end_session = false;
        alice.version = "1.0";
        alice.response = responseAlice;
        return JsonUtils.pojoToJson(alice);
    }

    public static String switchVoiceCommand(Context context) {
        String body = context.body;
        String command = JsonUtils.jsonGetValue(body, "command");
        String clientId = JsonUtils.jsonGetValue(body, "client_id");
        log.info("COMMAND: " + command);

        if (clientId.contains("browser")) clientType = "browser";
        else clientType = "speaker";

        log.info("CLIENT_ID: " + clientId);
        log.info("CLIENT_TYPE: " + clientType);

        if (command == null) return "я не поняла команду";
        aliceId = JsonUtils.jsonGetValue(body, "application_id");
//        lmsPlayers.lastAliceId = aliceId;

//      НАСТРОЙКА
        if (command.contains("найди сервер")) return searchServer();
        if (command.contains("найди колонки")) return searchPlayers();
        if (command.contains("что с колонками") || command.contains("какие колонки")) return searchPlayers();
        if (command.matches("это комната.*с колонкой.*")) return selectRoomWithSpeaker(command);
        if (command.contains("это комната")) return selectRoomByCommand(command);
        room = Main.idRooms.get(aliceId);
        log.info("ROOM: " + room);
        String firstRoomname = "";
        if (SmartHome.devices.size() > 0) firstRoomname = SmartHome.devices.get(0).room;
        if (room == null) {
            log.info("UNKNOWN ROOM");
            return "скажите навыку, это комната и название комнаты, например " + firstRoomname;
        }

        if (command.contains("какая комната")) {
            log.info("КАКАЯ КОМНАТА");
            String playerNameInRoom = ". колонка еще не выбрана. скажите выбери колонку и название";
            Player playerInRoom = lmsPlayers.getPlayerByCorrectRoom(room);
            log.info("PLAYER IN ROOM: " + playerInRoom);
            if (playerInRoom != null)
                playerNameInRoom = ". с колонкой " + playerInRoom.name;
            String remoteInRoom = ". пульт не подключен";
            if (lmsPlayers.btPlayerName != null)
                remoteInRoom = ". пульт подключен к " + lmsPlayers.btPlayerName;
            return "это комната " + room + playerNameInRoom + remoteInRoom;
        }

        if (command.contains("выбери колонку")) return selectPlayerByCommand(command);
        Device device = SmartHome.getDeviceByCorrectRoom(room);
        String firstPlayername = "";
        if (lmsPlayers.players.size() > 0) firstPlayername = lmsPlayers.players.get(0).name;
        if (device == null)
            return "устройство не найдено. скажите навыку, выбери колонку и название колонки, например " + firstPlayername;
        Player player = lmsPlayers.getPlayerByDeviceId(device.id);
        if (lmsIp == null) return "медиасервер не найден";
        if (player == null) return "в комнате " + room + " колонка еще не выбрана. " +
                "скажите навыку, выбери колонку и название колонки, например " + firstPlayername;
        if (command.contains("включи пульт") || command.contains("подключи пульт"))
            return connectBtRemote(command, player);
        if (command.contains("где пульт")) return whereBtRemote();
//      БАЗА
        if (command.contains("что играет") || command.contains("что с музыкой")) return whatsPlaying(player);

        if (command.contains("что в группе")) return watsInGroup(player);


        if (command.contains("какая громкость")) return whatsVolume(player);
//      ИЗБРАННОЕ
        if ((command.contains("канал"))) return channelPlayByName(command, player);
        if (command.contains("добавь в избранное")) return channelAdd(player);

        //      СИНХРОНИЗАЦИЯ
        if (command.matches("переключи.*сюда")) return syncSwitchToHere(player);
        if (command.contains("отдельно")) return syncSeparateOn(player);
        if (command.contains("вместе")) return syncSeparateOff(player);

//      СПОТИФАЙ
        if (command.contains("включи") && !(command.contains("альбом") || command.contains("канал")))
            return spotifyPlayArtist(command, player);
        if (command.contains("включи альбом")) return spotifyPlayAlbum(command, player);
        if (command.contains("дальше") || command.contains("следующий")) return playlistNextTrack(player);

        return "я не поняла команду";
    }

    public static String selectRoomWithSpeaker(String command) {
        log.info("START");
        String targetRoom = command
                .replaceAll(".*комната", "")
                .replaceAll("с колонкой.*", "")
                .replaceAll("\\s", "");
        String correctRoom = correctRoomName(targetRoom);
        if (correctRoom == null) return "нет такой комнаты";
        room = correctRoom;
        String player = command
                .replaceAll(".*с колонкой", "")
                .replaceAll("\\s", "");
        player = Utils.convertCyrilic(player);
        player = correctPlayerName(player);
        if (player == null) return "нет такой колонки";
        log.info("SELECT ROOM: " + room);
        selectRoomByCorrectRoom(room);
        log.info("SELECT PLAYER: " + player);
        selectPlayerByCorrectPlayer(player);
        return "это комната " + room + " с колонкой " + player;
    }

    public static String selectRoomByCommand(String command) {
        log.info("SELECT ROOM BY COMMAND: " + command);
        String target = command
                .replaceAll(".*комната\\S*\\s", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        target = correctRoomName(target);
        if (target == null) return "нет такой комнаты";
        selectRoomByCorrectRoom(target);
        String whithPlayerName = "";
        Player player = lmsPlayers.getPlayerByCorrectRoom(room);
        if (player != null) whithPlayerName = ". с колонкой " + player.name;
        else whithPlayerName = ". колонка в комнате еще не выбрана ";
        return "это комната " + room + whithPlayerName;
    }

    public static void selectRoomByCorrectRoom(String target) {
        idRooms.put(aliceId, target);
        JsonUtils.mapToJsonFile(idRooms, "rooms.json");
        log.info("ADD TO ROOMS: " + target + " WHITH ID: " + aliceId);
        log.info("WRITE FILE rooms.json");
    }

    public static String selectPlayerByCommand(String command) {
        log.info("SELECT PLAYER BY COMMAND: " + command);
        String target = command
                .replaceAll(".*колонку\\S*\\s", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        target = correctPlayerName(target);
        if (target == null) return "нет такой колонки";
        selectPlayerByCorrectPlayer(target);
        return "выбрана колонка " + target +
                " в комнате " + room;
    }

    public static void selectPlayerByCorrectPlayer(String target) {
        log.info("CHECK IF DEVICE EXISTS IN SMART HOME " + room);
        Device device = SmartHome.getDeviceByCorrectRoom(room);
        if (device == null) {
            log.info("CREATE NEW DEVICE IN SMART HOME ROOM: " + room);
            SmartHome.create(room, null);
        } else log.info("DEVICE EXISTS IN ROOM: " + device.room);

        log.info("SELECT PLAYER IN ROOM " + room + " BY PLAYER IN COMMAND: " + target);
        List<String> playerNames = lmsPlayers.players.stream().map(player -> player.name).collect(Collectors.toList());
        log.info("PLAYERS IN LMS: " + playerNames);
        Player playerNew = lmsPlayers.getPlayerByCorrectName(target);
        if (playerNew != null) log.info("PLAYER NEW: " + playerNew.name);
        Player playerNow = lmsPlayers.getPlayerByCorrectRoom(room);
        if (playerNow != null) log.info("PLAYER NOW: " + playerNow.name);
        CompletableFuture.supplyAsync(() -> {
            if (playerNow != null) {
                log.info("SWAP PLAYERS IN ROOM: " + room + " NOW: " + playerNow.name + " ID: " + playerNow.deviceId +
                        " <- NEW: " + playerNew.name + " ID: " + playerNew.deviceId);
                String playerNowDeviceId = playerNow.deviceId;
                log.info("ROOM: " + room + " ID: " + playerNowDeviceId);
                playerNow.room = null;
                playerNow.deviceId = null;
                playerNew.room = room;
                playerNew.deviceId = playerNowDeviceId;
            } else {
                log.info("SWAP PLAYERS IN ROOM: " + room + " NOW: --- " +
                        " <- NEW: " + playerNew.name + " ID: " + playerNew.deviceId);
                playerNew.room = room;
                playerNew.deviceId = SmartHome.getDeviceByCorrectRoom(room).id;
            }
            lmsPlayers.write();
            log.info("TURN ON NEW PLAYER " + playerNew.name);
            Actions.turnOnMusic(playerNew);
            if (playerNow != null) {
                log.info("STOP CURRENT PLAYER " + playerNow.name);
                playerNow.unsync().pause();
            }

            return "";
        });
    }

    public static String correctRoomName(String target) {
        log.info("START: " + target);
//        String correctRoom = Levenstein.getNearestElementInList(target, rooms);
        String correctRoom = Levenstein.search(target, rooms);
        if (correctRoom == null) {
            log.info("ERROR ROOM NOT EXISTS IN YANDEX SMART HOME " + target);
            return null;
        }
        room = correctRoom;
        log.info("CORRECT ROOM: " + target + " -> " + correctRoom);
        return correctRoom;
    }

    public static String correctPlayerName(String player) {
        log.info("START: " + player);
        List<String> players = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        player = Utils.convertCyrilic(player);
//        String correctPlayer = Levenstein.getNearestElementInList(player, players);
        String correctPlayer = Levenstein.search(player, players);
        if (correctPlayer == null) log.info("ERROR PLAYER NOT EXISTS IN LMS " + room);
        log.info("CORRECT PLAYER: " + player + " -> " + correctPlayer);
        return correctPlayer;
    }

    private static String searchPlayers() {
        String answer;
        log.info("BEFORE UPDATE " + lmsPlayers.playersNamesOnLine.toString());
        lmsPlayers.updateServerStatus();
        log.info("AFTER UPDATE " + lmsPlayers.playersNamesOnLine.toString());
        answer = "найдено плееров " + lmsPlayers.playersNamesOnLine.size() + ", " + String.join(", ", lmsPlayers.playersNamesOnLine);
        return answer;
    }

    private static String searchServer() {
        String answer;
        log.info("SEARCH SERVER");
        CompletableFuture.runAsync(() -> {
            Utils.searchLmsIp();
            log.info("LMS IP " + lmsIp);
            if (lmsIp != null) lmsPlayers.updateServerStatus();
        });
        answer = "сейчас найду";
        return answer;
    }

    private static String connectBtRemote(String command, Player player) {
        String answer;
        log.info("CONNECT BT REMOTE");
        log.info("COMMAND: " + command);
        command = command.replaceAll(".*пульт", "");
        log.info("COMMAND: _" + command + "_");
        String playerName = null;


        if (!command.equals("\\s*") && !command.equals("")) {
            playerName = Levenstein.search(command, lmsPlayers.playersNamesOnLine);
            log.info("PLAYER NAME: " + playerName);
            if (playerName != null) player = lmsPlayers.getPlayerByCorrectName(playerName);
            if (player == null) return "плеер не найден " + command;

            String roomName = Levenstein.search(command, rooms);
            log.info("ROOM NAME: " + roomName);
            if (roomName != null) player = lmsPlayers.getPlayerByCorrectRoom(roomName);
            if (player == null) return "плеер не найден " + command;
        }


        lmsPlayers.btPlayerInQuery = player.nameInQuery;
        lmsPlayers.btPlayerName = player.name;
        lmsPlayers.write();
        answer = "пульт подключен к " + player.name;
        return answer;
    }

    private static String whereBtRemote() {
        String answer;
        log.info("WHERE BT REMOTE");
        answer = "пульт подключен к " + lmsPlayers.btPlayerName;
        return answer;
    }

    //      БАЗА
    public static String whatsPlaying(Player player) {
        String answer = "";
        log.info("WHATS PLAYING ON " + player.name);
        if (player.status() == null) return "медиасервер не работает";
        player.title();
        String title = player.title;
        if (player.playerStatus.result.player_connected == 0)
            return "плеер " + player.name + "  не подключен к медиасерверу";
        if (title == null) return "медиасервер не отвечает";
        log.info("TITLE: " + title);
        String separate = "";
        if (player.separate) separate = "отдельно ";
        List<String> separatePlayers = lmsPlayers.getSeparatePlayers(player);
        String separateAnswer = "";
        if (separatePlayers.size() != 0) separateAnswer = ", отдельно " + String.join(", ", separatePlayers);
        String mode = player.playerStatus.result.mode;
        int volume = player.playerStatus.result.mixer_volume;

//        watsInGroup(player);

        log.info("SPOTIFY TRY GET CURRENT TITLE");
        String spotyCurrentName = Spotify.getCurrentTitle();
        log.info("SPOTIFY CURRENT TITLE: " + spotyCurrentName);
        String spotyAnswer = "";
        if (spotyCurrentName != null) spotyAnswer = ". на спотифай играет " + spotyCurrentName;
        if (mode.equals("play")) {
            answer = "сейчас на " + player.name + " играет " + separate + title + " громкость " + volume;
        }
        if (!mode.equals("play")) {
            Player playing = lmsPlayers.getPlayingPlayer(player.name);
            if (playing != null) {
                playing.status();
                answer = "сейчас на " + player.name + " не играет " + separate + title +
                        ". на " + playing.name + " играет " + separate + playing.title;
            } else answer = "сейчас на " + player.name + " не играет " + separate + title;
        }
        answer = answer + separateAnswer + spotyAnswer;
        log.info("ANSWER: " + answer);
        return answer;
    }

    private static String watsInGroup(Player player) {
        log.info("WATS IN GROUP START PLAYER: " + player.name);
        List<String> group = player.syncgroups();
        log.info(group);
        log.info(player.name);
        log.info(group.contains(player.name));
        group.stream()
                .peek(n -> log.info("----- " + n))
                .map(m -> m.split(","))
                .filter(n -> n.equals("HomePod"))
                .peek(n -> log.info("--- " + n))
                .collect(Collectors.toList());

        if (group != null) {
            if (group.contains(player.name)) {
                log.info("GROUPE CONTAINS PLAYER " + player.name + " - " + group);
                group.remove(player.name);
                return player.name + " в группе c " + group;
            } else {

                log.info("GROUPE NOT CONTAINS PLAYER " + player.name + " - " + group);
                return player.name + " не в группе из " + group;
            }
        } else {
            log.info("GROUPE IS NULL");
            return "группы нет";
        }
    }

    private static String whatsVolume(Player player) {
        String answer;
        log.info("VOLUME");
        String volume = player.volumeGet();
        if (volume == null) return createResponse("медиасервер не отвечает");
        answer = "сейчас на " + player.name + " громкость " + volume;
        return answer;
    }

    //      ИЗБРАННОЕ
    private static String channelPlayByName(String command, Player player) {
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
        answer = "сейчас, мой господин, включаю канал " + index + ", " + channel;
        log.info("INDEX: " + index);
        CompletableFuture.supplyAsync(() -> {
//            Actions.actionPlayChannel(player, index);
            player.playChannel(index);
            return "";
        });
        return answer;
    }

    private static String channelAdd(Player player) {
        String answer;
        log.info("FAVORITES ADD");
        String addTitle;
        addTitle = player.favoritesAdd();
        answer = "добавила в избранное " + addTitle;
        return answer;
    }

    //      СПОТИФАЙ
    public static String spotifyPlayArtist(String command, Player player) {
        String answer;
        String target;
        target = command.replaceAll(".*включи\\S*\\s", "").replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET SPOTIFY: " + target);
        String link = Spotify.getLink(target, Type.playlist);
        log.info("LINK SPOTIFY: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.runAsync(() -> {
            player.playPath(link);
        });
        answer = "сейчас, мой господин, включаю " + target;
        return answer;
    }

    public static String spotifyPlayAlbum(String command, Player player) {
        String answer;
        String target;
        target = command.replaceAll(".*включи\\S*\\s", "").replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET SPOTIFY: " + target);
        String link = Spotify.getLink(target, Type.album);
        log.info("LINK SPOTIFY: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.runAsync(() -> player.playPath(link));
        answer = "сейчас, мой господин, включаю " + target;
        return answer;
    }

    private static String playlistNextTrack(Player player) { // дальше, следующий
        String answer;
        log.info("NEXT TRACK");
        CompletableFuture.runAsync(() -> player.nextTrack());
        answer = "включаю следующий";
        return answer;
    }

    //      СИНХРОНИЗАЦИЯ
    private static String syncSwitchToHere(Player player) {
        log.info("SWITCH TO HERE");
        String answer = "переключаю музыку на " + player.name;
        CompletableFuture.runAsync(() -> player.switchToHere());
        return answer;
    }

    private static String syncSeparateOn(Player player) {
        String answer;
        log.info("SEPARATE ON");
        player.status();
        CompletableFuture.runAsync(() -> player.separateOn());
        return "включаю отдельно " + player.name;
    }

    private static String syncSeparateOff(Player player) {
        String answer = "";
        if (player.separate) answer = "включаю вместе " + player.name;
        else answer = "включаю все вместе";
        log.info("SEPARATE OFF");
        CompletableFuture.runAsync(() -> player.separateOff());
        return answer;
    }

//    public static String patternmathcroom(String command) {
//        Pattern pattern = Pattern.compile("(?<=комната )[a-zA-Zа-яА-Я]*");
//        Matcher matcher = pattern.matcher(command);
//        String room;
//        if (!matcher.find()) return "";
//        room = matcher.group();
//        room = Levenstein.getNearestElementInList(room, rooms);
//        if (room == null) log.info("ERROR ROOM NOT EXISTS IN YANDEX SMART HOME");
//        return room;
//    }
}