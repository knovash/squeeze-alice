package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.SmartHome;
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
    public static String clientType = "speaker"; // если запрос с колонки то завершить диалог чтобы не висеть в навыке, если диалог в браузере то не завершать
    public static String artist;
    public static String album;
    public static String track;
    public static String saveToFileJson = "data/rooms.json";

    public static Context action(Context context) {
        String answer = processVoiceContext(context);
        context.bodyResponse = createResponse(answer);
//        context.bodyResponse = answer;
        context.code = 200;
        return context;
    }

    public static String createResponse(String text) {
        AliceVoiceResponsePojo alice = new AliceVoiceResponsePojo();
        AliceVoiceResponsePojo.ResponseAlice responseAlice = new AliceVoiceResponsePojo.ResponseAlice();
        responseAlice.text = text;
        responseAlice.end_session = true;
// если запрос с колонки то завершить диалог чтобы не висеть в навыке, если диалог в браузере то не завершать
        if (clientType.equals("browser")) responseAlice.end_session = false;
        log.info("END SESSION: " + responseAlice.end_session);
        alice.version = "1.0";
        alice.response = responseAlice;
        String json = "";
//        json = JsonUtils.pojoToJson(alice);
//        json = json.replaceAll("\\n", "");
        json = text;
        return json;
    }

    public static String processVoiceContext(Context context) {
        String body = context.body;
        String command = JsonUtils.jsonGetValue(body, "command");
        String clientId = JsonUtils.jsonGetValue(body, "client_id");
//        log.info("COMMAND: " + command);
// если запрос с колонки то завершить диалог чтобы не висеть в навыке, если диалог в браузере то не завершать
        if (clientId.contains("browser")) clientType = "browser";
        else clientType = "speaker";
        log.info("COMMAND: " + command + " CLIENT_TYPE: " + clientType);
        if (command == null) return "я не поняла команду";
//        определить идентификатор application_id колонки с алисой для привязки к комнате где она находится
        aliceId = JsonUtils.jsonGetValue(body, "application_id");
        return processCommand(aliceId, command);
    }

    public static String processCommand(String roomId, String command) {
//        log.info("SWITCH VOICE COMMAND: " + " " + command);
        clientType = "browser";
// показывать сообщение при запуске диалога в браузере
        if (command.equals(""))
            return "Я умею управлять плеерами подключенными в Lyrion Music Server. \n"
                    + "Скажите Алисе:\n"
                    + "Алиса, включи(выключи) музыку \n"
                    + "Алиса, музыку громче(тише) \n"
                    + "Алиса, переключи канал";
// Алиса, навык, помощь/помоги/подскажи
        if (command.contains("помощь") || command.contains("Помощь") || command.contains("помоги") || command.contains("подскажи"))
            return "У вас локально должен быть установлен Lyrion Music Server и приложение навыка";
// Алиса, навык, что ты умеешь/можешь
        if (command.contains("что ты умеешь") || command.contains("Что ты умеешь") || command.contains("Что ты можешь"))
            return "Я умею управлять плеерами подключенными в Lyrion Music Server";

// УСТАНОВКА СВЯЗИ КОМНАТЫ И КОЛОНКИ
        clientType = "speaker";
// Алиса, навык, это комната с колонкой радиотехника
        if (command.matches("это комната.*с колонкой.*")) return selectRoomWithSpeaker(command);
// Алиса, навык, это комната гостиная
        if (command.contains("это комната")) return selectRoomByCommand(command);

// определение комнаты по идентификатору колонки с Алисой application_id
        room = Main.idRooms.get(roomId);
        log.info("ROOM: " + room + " COMMAND: " + " " + command);
        String firstRoomname = "";
        if (SmartHome.devices.size() > 0) firstRoomname = SmartHome.devices.get(0).room;
// если комната еще не определена, просьба обратиться к навыку для определения комнаты
        if (room == null) {
            log.info("UNKNOWN ROOM");
            return "скажите навыку, это комната и название комнаты, например " + firstRoomname;
        }

// далее команды выполняемые только если комната определена !!!

// Алиса, навык, какая комната
        if (command.contains("какая комната")) return VoiceActions.whatIsTheRoom(room);
// Алиса, навык, выбери/включи колонку радиотехника
        if (command.matches("(выбери|включи) колонку.*")) return selectPlayerByCommand(command, room);

// определение плеера в комнате
        Device device = SmartHome.getDeviceByCorrectRoom(room);
        String firstPlayername = "";
        if (lmsPlayers.players.size() > 0) firstPlayername = lmsPlayers.players.get(0).name;
        if (device == null)
            return "устройство не найдено. скажите навыку, выбери колонку и название колонки, например " + firstPlayername;
        Player player = lmsPlayers.playerByDeviceId(device.id);

// Алиса, навык, помощь/помоги/подскажи
        if (command.contains("привет") || command.contains("подключи") || command.contains("настрой")) {
            if (config.lmsIp == null) return "медиасервер не найден";
            if (room == null) {
                log.info("UNKNOWN ROOM");
                return "скажите навыку, это комната и название комнаты";
            }
            if (player == null) return "в комнате " + room + " колонка еще не выбрана. " +
                    "скажите навыку, выбери колонку и название колонки, например " + firstPlayername;
            return "в комнате " + room + " выбрана колонка " + player.name + ". можете спросить навык, что играет ";
        }
        if (config.lmsIp == null) return "медиасервер не найден";
        if (player == null) return "в комнате " + room + " колонка еще не выбрана. " +
                "скажите навыку, выбери колонку и название колонки, например " + firstPlayername;

// команды для bt пульта
        if (command.matches("(включи|подключи) пульт")) return connectBtRemote(command, player);
        if (command.contains("где пульт")) return whereBtRemote();

// команды узнать состояние плеера. ожидают ответ и возвращают его Алисе
        if (command.contains("что играет")) return whatsPlaying(player);
        if (command.contains("какая громкость")) return whatsVolume(player);

// далее команды которые надо запускать паралельно для быстрого ответа Алисы.
// Алиса отвечает не дожидаясь ответа сервера и выполнения команды
// после завершения параллельного процесса выполняется запрос для обновления виджетов таскера

        if ((command.matches("(включи )?(канал|избранное) .*"))) return VoiceActions.channelPlayByName(command, player);
        if (command.matches("добавь( в)? избранное")) return channelAdd(player);
        if (command.matches("переключи.*сюда")) return VoiceActions.syncSwitchToHere(player);
        if (command.matches("(включи )?отдельно")) return VoiceActions.separateOn(player);
        if (command.matches("(включи )?вместе")) return VoiceActions.separateAllOff(player);
        if ((command.matches("(включи )?(рандом|шафл|shuffle|random)"))) {
            player.shuffleOn();
            return "включаю рандом";
        }
        if ((command.matches("(выключи )?(рандом|шафл|shuffle|random)"))) {
            player.shuffleOff();
            return "выключаю рандом";
        }

// СПОТИФАЙ
// включи кровосток
// включи трэк усынови бомжа
// включи кровосток трэк наука
        log.info("SPOTIFY");
        if (command.matches("(включи )?(дальше|следующий)")) return playlistNextTrack(player);
//        if (command.contains("включи альбом")) return spotifyPlayAlbum(command, player);
//        if (command.contains("включи") && command.contains("альбом") && !command.contains("включи альбом"))
//            return spotifyPlayArtistAlbum(command, player);
//        if (command.contains("включи") && (command.contains("трэк") || command.contains("трек")))
//            return spotifyPlayTrack(command, player);

//        Алиса, навык, включи depechemode
        if (command.contains("включи") && !(command.contains("альбом") ||
                command.contains("трэк") ||
                command.contains("трек") ||
                command.contains("канал") ||
                command.contains("random") ||
                command.contains("shuffle") ||
                command.contains("шафл") ||
                command.contains("рандом"))) {


            CompletableFuture.runAsync(() -> spotifyPlayArtist(command, player))
                    .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());


            return "включаю";
        }
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
        if (playerNew != null) playerNew.turnOnMusic(null);
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

// selectRoomByCorrectRoom] - TRY WRITE TO rooms.json ID: {null=Веранда}
        selectRoomByCorrectRoom(target);

        log.info("SELECT ROOM OK");
        String whithPlayerName = "";
        Player player = lmsPlayers.playerByCorrectRoom(room);
        if (player != null) whithPlayerName = ". с колонкой " + player.name;
        else whithPlayerName = ". колонка в комнате еще не выбрана ";
        return "это комната " + room + whithPlayerName;
    }

    public static void selectRoomByCorrectRoom(String target) {
        log.info("START SELECT ROOM: " + target);
        idRooms.put(aliceId, target);
        JsonUtils.mapToJsonFile(idRooms, SwitchVoiceCommand.saveToFileJson);
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

        Device device = SmartHome.getDeviceByCorrectRoom(roomName);
        if (device == null) {
            log.info("CREATE NEW DEVICE IN SMART HOME ROOM: " + roomName);
            SmartHome.create(roomName, null);
            SmartHome.write();
        } else log.info("DEVICE EXISTS IN ROOM: " + device.room);

        log.info("SELECT PLAYER IN ROOM " + roomName + " BY PLAYER IN COMMAND: " + playerName);
        List<String> playerNames = lmsPlayers.players.stream().map(player -> player.name).collect(Collectors.toList());
        log.info("PLAYERS IN LMS: " + playerNames);
        Player playerNew = lmsPlayers.playerByCorrectName(playerName);

        if (!playerNew.status(1)) {
            log.info("PLAYER OFFLINE " + playerName);
            return null;
        }
        if (playerNew != null) log.info("PLAYER NEW: " + playerNew.name);
        Player playerNow = lmsPlayers.playerByCorrectRoom(roomName);
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
                playerNew.turnOnMusic(null);
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
// String correctPlayer = Levenstein.getNearestElementInList(player, players);
        String correctPlayer = Levenstein.getNearestElementInListWord(player, players);
// String correctPlayer = Levenstein.search(player, players);
        if (correctPlayer == null) log.info("ERROR PLAYER NOT EXISTS IN LMS " + room);
        log.info("CORRECT PLAYER: " + player + " -> " + correctPlayer);
        return correctPlayer;
    }

//    private static String searchPlayers() {
//        String answer;
//        log.info("BEFORE UPDATE " + lmsPlayers.playersNamesOnLine.toString());
//        lmsPlayers.updateLmsPlayers();
//        log.info("AFTER UPDATE " + lmsPlayers.playersNamesOnLine.toString());
//        answer = "найдено плееров " + lmsPlayers.playersNamesOnLine.size() + ", " + String.join(", ", lmsPlayers.playersNamesOnLine);
//        return answer;
//    }

//    private static String searchServer() {
//        String answer;
//        log.info("SEARCH SERVER");
//// CompletableFuture.runAsync(() -> {
//// Utils.searchLmsIp();
//// log.info("LMS IP " + config.lmsIp);
//// if (config.lmsIp != null) lmsPlayers.updateLmsPlayers();
//// });
//        answer = "сейчас найду";
//        return answer;
//    }

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
            if (playerName != null) player = lmsPlayers.playerByCorrectName(playerName);
            if (player == null) return "плеер не найден " + command;

            String roomName = Levenstein.search(command, rooms);
            log.info("ROOM NAME: " + roomName);
            if (roomName != null) player = lmsPlayers.playerByCorrectRoom(roomName);
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

    // БАЗА
    public static String whatsPlaying(Player player) {
        String answer = "";
        log.info("WHATS PLAYING ON " + player.name);
        if (player == null) return "плеер не найден";
        if (player.status() == null) return "медиасервер не отвечает";
        if (!player.connected) return "плеер " + player.name + "  не подключен к медиасерверу";
        String title = player.title();
        if (title == null) return "медиасервер не отвечает"; // whatsPlaying
// если с плеером все хорошо записываем тайтл что играет
//        log.info("TITLE: " + player.title);

// находим группы плееров которые играют вместе или отдельно, включая отделенные false
        player.playingPlayersNamesNotInCurrentGroup(false);

        String separate = "";
        if (player.separate) separate = "отдельно ";
        List<String> separatePlayers;
// в ответ плееры которые играют вместе с этим в группе
        String answerOtherInGroup = "";
        if (lmsPlayers.playersNamesInCurrentGroup.size() > 0)
            answerOtherInGroup = ", вместе " + String.join(", ", lmsPlayers.playersNamesInCurrentGroup);
// в ответ плееры которые играют не в группе с этим
        String answerPlayingSeparate = "";
        if (lmsPlayers.playingPlayersNamesNotInCurrentGrop.size() != 0)
            answerPlayingSeparate = ", отдельно играет " + String.join(", ", lmsPlayers.playingPlayersNamesNotInCurrentGrop);
// в ответ плеер сейчас играет
        if (player.mode.equals("play")) {
            answer = "сейчас на " + player.name + " играет " + separate + title + " громкость " + player.volume; // whatsPlaying
        }
// в ответ плеер сейчас не играет
        if (!player.mode.equals("play")) {
            answer = "сейчас на " + player.name + " не играет " + separate + title; // whatsPlaying
        }

        answer = answer + answerOtherInGroup + answerPlayingSeparate;
        log.info("ANSWER: " + answer);
        return answer;

// log.info("SPOTIFY TRY GET CURRENT TITLE");
// String spotyCurrentName = Spotify.getCurrentTitle();
// log.info("SPOTIFY CURRENT TITLE: " + spotyCurrentName);
// String spotyAnswer = "";
// if (spotyCurrentName != null) spotyAnswer = ". на спотифай играет " + spotyCurrentName;
    }

    private static String whatsVolume(Player player) {
        String answer;
        log.info("VOLUME");
        String volume = player.volumeGet();
//        if (volume == null) return createResponse("медиасервер не отвечает");
        if (volume == null) return "медиасервер не отвечает";
        answer = "сейчас на " + player.name + " громкость " + volume;
        return answer;
    }

// ИЗБРАННОЕ


    private static String channelAdd(Player player) {
        String answer = "";
        log.info("FAVORITES ADD");
        String addTitle;
        addTitle = player.favoritesAdd();
        answer = "добавила в избранное, " + addTitle;
        return answer;
    }

    // СПОТИФАЙ
    public static String spotifyPlayArtist(String command, Player player) {
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        String link;
        link = Spotify.getLinkArtist(target);
        log.info("LINK: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.runAsync(() -> {
// player.shuffleOn();
            player.ifExpiredAndNotPlayingUnsyncWakeSet(null)
                    .playPath(link);
        });
        String answer = "включаю " + artist;
// Requests.autoRemoteRefresh(); // spotifyPlayArtist
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
// player.shuffleOff();
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
        CompletableFuture.runAsync(() -> player.ctrlNextTrack().saveLastTimePathAutoremoteRequest());
        answer = "включаю следующий";
        return answer;
    }

}