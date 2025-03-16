package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.*;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.spotify.Spotify;
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
    public static String artist;
    public static String album;
    public static String track;

    public static Context actionMock(Context context) {
        log.info("SWITCH VOICE COMMAND MOCK");
        String answer = "проверка связи";
        log.info("ANSWER MOCK:------- " + answer);

        String sss = "{\n" +
                "  \"response\": {\n" +
                "    \"text\": \"Сейчас играет техно\",\n" +
                "    \"tts\": \"Сейчас играет техно\",\n" +
                "    \"end_session\": false\n" +
                "  },\n" +
                "  \"version\": \"1.0\"\n" +
                "}";
        context.bodyResponse = sss;

//        context.bodyResponse = createResponse(answer);
        context.code = 200;
//        log.info("CONTEXT ANSWER: " + context);
        return context;
    }

    public static Context action(Context context) {
        log.info("SWITCH VOICE COMMAND");
        String answer = switchVoiceCommand(context);
        log.info("ANSWER: " + answer);
        context.bodyResponse = createResponse(answer);
        context.code = 200;
        log.info("CONTEXT ANSWER: " + context);
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
        String json = JsonUtils.pojoToJson(alice);
        json = json.replaceAll("\\n", "");
        return json;
    }


    public static String switchVoiceCommand(Context context) {
        String body = context.body;
        String command = JsonUtils.jsonGetValue(body, "command");
        String clientId = JsonUtils.jsonGetValue(body, "client_id");
        log.info("COMMAND: " + command);
        log.error("COMMAND: " + command);
        if (clientId.contains("browser")) clientType = "browser";
        else clientType = "speaker";
        log.info("CLIENT_ID: " + clientId);
        log.info("CLIENT_TYPE: " + clientType);
        if (command == null) return "я не поняла команду";
        aliceId = JsonUtils.jsonGetValue(body, "application_id");
//        log.info("ALICE ID: " + aliceId);
//        lmsPlayers.lastAliceId = aliceId;
        return switchVoice(aliceId, command);
    }

    public static String switchVoice(String roomId, String command) {
        log.info("SWITCH VOICE COMMAND: " + " " + command);
//      НАСТРОЙКА
        if (command.equals(""))
            return "Я умею управлять плеерами подключенными в Logitech Media Server. Спросите у навыка," +
                    "что играет и я отвечу что сейчас играет или подскажу как настроить плееры";
        if (command.contains("помощь") || command.contains("Помощь"))
            return "У вас локально должен быть установлен Logitech Media Server и приложение навыка, например в докере";
        if (command.contains("что ты умеешь") || command.contains("Что ты умеешь"))
            return "Я умею управлять плеерами подключенными в Logitech Media Server";


        if (command.contains("найди сервер")) return searchServer();
        if (command.contains("найди колонки")) return searchPlayers();
        if (command.matches("это комната.*с колонкой.*")) return selectRoomWithSpeaker(command);
//        ретурн должен возвращать текс ответа Answer
        if (command.contains("это комната")) return selectRoomByCommand(command);

        room = Main.idRooms.get(roomId);
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

        if (command.matches("(выбери|включи) колонку.*")) {
            log.info("SELECT PLAYER выбери колонку ");
            return selectPlayerByCommand(command);
        }
        Device device = SmartHome.getDeviceByCorrectRoom(room);
        String firstPlayername = "";
        if (lmsPlayers.players.size() > 0) firstPlayername = lmsPlayers.players.get(0).name;
        if (device == null)
            return "устройство не найдено. скажите навыку, выбери колонку и название колонки, например " + firstPlayername;
        Player player = lmsPlayers.getPlayerByDeviceId(device.id);
        if (config.lmsIp == null) return "медиасервер не найден";
        if (player == null) return "в комнате " + room + " колонка еще не выбрана. " +
                "скажите навыку, выбери колонку и название колонки, например " + firstPlayername;

        if (command.matches("(включи|подключи) пульт")) return connectBtRemote(command, player);
        if (command.contains("где пульт")) return whereBtRemote();
//      БАЗА
        if (command.contains("что играет")) return whatsPlaying(player);
        if (command.contains("какая громкость")) return whatsVolume(player);
//      ИЗБРАННОЕ
        log.info("TRY FAVORITES");
        if ((command.matches("(включи )?(канал|избранное) .*"))) return channelPlayByName(command, player);
        if (command.matches("добавь( в)? избранное")) return channelAdd(player);
        //      СИНХРОНИЗАЦИЯ
        log.info("TRY SYNC");
        if (command.matches("переключи.*сюда")) return syncSwitchToHere(player);
        if (command.matches("(включи )?отдельно")) return syncSeparateOn(player);
        if (command.matches("(включи )?вместе")) return syncSeparateOff(player);
        if ((command.matches("(включи )?(рандом|шафл|shuffle|random)"))) {
            String.valueOf(player.shuffleOn());
            return "включаю рандом";
        }
        if ((command.matches("(выключи )?(рандом|шафл|shuffle|random)"))) {
            String.valueOf(player.shuffleOff());
            return "выключаю рандом";
        }

//      СПОТИФАЙ
//        включи кровосток
//        включи трэк усынови бомжа
//        включи кровосток трэк наука
        log.info("TRY SPOTY");
        if (command.matches("(включи )?(дальше|следующий)")) return playlistNextTrack(player);
        if (command.contains("включи альбом")) return spotifyPlayAlbum(command, player);
        if (command.contains("включи") && command.contains("альбом") && !command.contains("включи альбом"))
            return spotifyPlayArtistAlbum(command, player);
        if (command.contains("включи") && (command.contains("трэк") || command.contains("трек")))
            return spotifyPlayTrack(command, player);
        if (command.contains("включи") && !(command.contains("альбом") ||
                command.contains("трэк") ||
                command.contains("трек") ||
                command.contains("канал") ||
                command.contains("random") ||
                command.contains("shuffle") ||
                command.contains("шафл") ||
                command.contains("рандом")))
            return spotifyPlayArtist(command, player);
        return "я не поняла команду";
    }

    public static String selectRoomWithSpeaker(String command) {
        log.info("START");
        String targetRoom = command
                .replaceAll(".*комната", "")
                .replaceAll("с колонкой.*", "")
                .replaceAll("\\s", "");
        String correctRoom = Utils.getCorrectRoomName(targetRoom);
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
        Player playerNew = selectPlayerInRoom(player, room, false);
        if (playerNew != null) playerNew.turnOnMusic();
        return "это комната " + room + " с колонкой " + player;
    }

    public static String selectRoomByCommand(String command) {
        log.info("SELECT ROOM BY COMMAND: " + command);
        String target = command
                .replaceAll(".*комната\\S*\\s", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        target = Utils.getCorrectRoomName(target);
        log.info("TARGET: " + target);
        if (target == null) return "нет такой комнаты";
        room = target;
        log.info("ROOM: " + room);

//        selectRoomByCorrectRoom] - TRY WRITE TO rooms.json ID: {null=Веранда}
        selectRoomByCorrectRoom(target);

        log.info("SELECT ROOM OK");
        String whithPlayerName = "";
        Player player = lmsPlayers.getPlayerByCorrectRoom(room);
        if (player != null) whithPlayerName = ". с колонкой " + player.name;
        else whithPlayerName = ". колонка в комнате еще не выбрана ";
        return "это комната " + room + whithPlayerName;
    }

    public static void selectRoomByCorrectRoom(String target) {
        log.info("START SELECT ROOM: " + target);
        idRooms.put(aliceId, target);
        JsonUtils.mapToJsonFile(idRooms, "rooms.json");
    }

    public static String selectPlayerByCommand(String command) {
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
        Device device = SmartHome.getDeviceByCorrectRoom(roomName);
        if (device == null) {
            log.info("CREATE NEW DEVICE IN SMART HOME ROOM: " + roomName);
            SmartHome.create(roomName, null);
        } else log.info("DEVICE EXISTS IN ROOM: " + device.room);

        log.info("SELECT PLAYER IN ROOM " + roomName + " BY PLAYER IN COMMAND: " + playerName);
        List<String> playerNames = lmsPlayers.players.stream().map(player -> player.name).collect(Collectors.toList());
        log.info("PLAYERS IN LMS: " + playerNames);
        Player playerNew = lmsPlayers.getPlayerByCorrectName(playerName);

        if (!playerNew.status(1)) {
            log.info("PLAYER OFFLINE " + playerName);
            return null;
        }
        if (playerNew != null) log.info("PLAYER NEW: " + playerNew.name);
        Player playerNow = lmsPlayers.getPlayerByCorrectRoom(roomName);
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
            playerNew.deviceId = SmartHome.getDeviceByCorrectRoom(roomName).id;
        }
        lmsPlayers.write();
        if (start) {
            CompletableFuture.runAsync(() -> {
                log.info("TURN ON NEW PLAYER " + playerNew.name);
//                Actions.turnOnMusic(playerNew);
                playerNew.turnOnMusic();
                if (playerNow != null) {
                    log.info("STOP CURRENT PLAYER " + playerNow.name);
                    playerNow.unsync().pause();
                }
            });
        }
        return playerNew;
    }

    public static String correctPlayerName(String player) {
        log.info("START: " + player);
        List<String> players = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        player = Utils.convertCyrilic(player);
//        String correctPlayer = Levenstein.getNearestElementInList(player, players);
        String correctPlayer = Levenstein.getNearestElementInListWord(player, players);
//        String correctPlayer = Levenstein.search(player, players);
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
            log.info("LMS IP " + config.lmsIp);
            if (config.lmsIp != null) lmsPlayers.updateServerStatus();
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
        if (player == null) return "плеер не найден";
        if (player.status() == null) return "медиасервер не отвечает";
//        if (player.status() != true) return "медиасервер не отвечает";
        if (!player.connected) return "плеер " + player.name + "  не подключен к медиасерверу";
        if (player.title == null) return "медиасервер не отвечает";
        log.info("TITLE: " + player.title);
        String separate = "";
        if (player.separate) separate = "отдельно ";
        List<String> separatePlayers = lmsPlayers.getSeparatePlayers(player);
        String separateAnswer = "";
        if (separatePlayers.size() != 0) separateAnswer = ", отдельно " + String.join(", ", separatePlayers);

//        log.info("SPOTIFY TRY GET CURRENT TITLE");
//        String spotyCurrentName = Spotify.getCurrentTitle();
//        log.info("SPOTIFY CURRENT TITLE: " + spotyCurrentName);
//        String spotyAnswer = "";
//        if (spotyCurrentName != null) spotyAnswer = ". на спотифай играет " + spotyCurrentName;

        if (player.mode.equals("play")) {
            answer = "сейчас на " + player.name + " играет " + separate + player.title + " громкость " + player.volume;
        }

        if (!player.mode.equals("play")) {
            Player playing = lmsPlayers.getPlayingPlayer(player.name);
            if (playing != null) {
                playing.status();
                answer = "сейчас на " + player.name + " не играет " + separate + player.title +
                        ". на " + playing.name + " играет " + separate + playing.title;
            } else answer = "сейчас на " + player.name + " не играет " + separate + player.title;
        }
//        answer = answer + separateAnswer + spotyAnswer;
        answer = answer + separateAnswer;
        log.info("ANSWER: " + answer);
        return answer;
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
            player.ifExpiredOrNotPlayUnsyncWakeSet();
            player.playChannel(index);
            return "";
        });
        return answer;
    }

    private static String channelAdd(Player player) {
        String answer = "";
        log.info("FAVORITES ADD");
        String addTitle;
        addTitle = player.favoritesAdd();
        answer = "добавила в избранное, " + addTitle;
        return answer;
    }

    //      СПОТИФАЙ
    public static String spotifyPlayArtist(String command, Player player) {
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        String link;
        link = Spotify.getLinkArtist(target);
        log.info("LINK: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.runAsync(() -> {
//            player.shuffleOn();
            player.playPath(link);
        });
        String answer = "включаю " + artist;
        return answer;
    }

    public static String spotifyPlayTrack(String command, Player player) {
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("трэк", "").replaceAll("трек", "")
                .replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        String link = Spotify.getLinkTrack(target);
        log.info("LINK: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.runAsync(() -> player.playPath(link));
        String answer = "включаю " + artist + ", " + track;
        return answer;
    }

    public static String spotifyPlayAlbum(String command, Player player) {
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("альбом", "")
                .replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        String link = Spotify.getLinkAlbum(target);
        log.info("LINK: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.runAsync(() -> {
//            player.shuffleOff();
            player.playPath(link);
        });
        String answer = "включаю " + artist + ", " + album;
        return answer;
    }

    public static String spotifyPlayArtistAlbum(String command, Player player) {

        String artist = command.replaceAll(".*включи ", "")
                .replaceAll(" альбом.*", "");

        String album = command.replaceAll(".*альбом ", "");

        log.info("ARTIST: " + artist);
        log.info("ALBUM: " + album);

        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("альбом", "")
                .replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        String link = Spotify.getLinkAlbum(target);
        log.info("LINK: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.runAsync(() -> {
            player.shuffleOff();
            player.playPath(link);
        });
        String answer = "сейчас, мой господин, включаю " + SwitchVoiceCommand.artist + ", " + target;
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

//    private static String repeat(String command) {
//        String answer;
//        command = command.replaceAll("повтори", "");
//        log.info("REPEAT: " + command);
//        answer = "повторяю, " + command;
//        return answer;
//    }

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