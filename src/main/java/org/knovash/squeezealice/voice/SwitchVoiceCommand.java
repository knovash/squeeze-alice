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
    //    public static String clientType = "speaker"; // ВОЗМОЖНО ИСПОЛЬЗУЕТСЯ НА СТОРОНЕ ОБЛАКА если запрос с колонки то завершить диалог чтобы не висеть в навыке, если диалог в браузере то не завершать
    public static String artist;
    public static String album;
    public static String track;
    public static String saveToFileJson = "data/rooms.json";
    public static String playlist;

    public static Context action(Context context) {
        String answer = createAnswer(context);
//        обрабатывает котекст и возвращает просто текст ответа для алисы
        log.info("ANSWER: " + answer);
// кладет текст в контекст запроса алисы
        context.bodyResponse = answer;
//        context.bodyResponse = createResponse(answer);

        log.info("bodyResponse: " + context.bodyResponse.toString());
//        context.bodyResponse = answer;
        context.code = 200;
        return context;
    }
//Облачный прокси-сервис, который принимает HTTP-запросы от Алисы и отправляет MQTT-сообщения, получает этот ответ,
// формирует из него полноценный JSON-ответ согласно требованиям Яндекс.Диалогов (добавляет version, session,
// response.end_session и т.д.) и отправляет его обратно Алисе.
//    public static String createResponse(String text) { // возможно метод ненужен
//        log.info("TEXT: " + text);
//        AliceVoiceResponsePojo alice = new AliceVoiceResponsePojo();
//        AliceVoiceResponsePojo.ResponseAlice responseAlice = new AliceVoiceResponsePojo.ResponseAlice();
//        responseAlice.text = text;
//        responseAlice.end_session = true;
//// если запрос с колонки то завершить диалог чтобы не висеть в навыке, если диалог в браузере то не завершать
//        if (clientType.equals("browser")) responseAlice.end_session = false;
//        log.info("END SESSION: " + responseAlice.end_session);
//        alice.version = "1.0";
//        alice.response = responseAlice;
//        String json = "";
////        json = JsonUtils.pojoToJson(alice);
////        json = json.replaceAll("\\n", "");
//        json = text;
//        return json;
//    }

    public static String createAnswer(Context context) {
//        сюда приходит весь запрос от алисы
//        log.info("CONTEXT: " + context.toJson().toString());
        String body = context.body;
        String command = JsonUtils.jsonGetValue(body, "command");


//         ВОЗМОЖНО ИСПОЛЬЗУЕТСЯ НА СТОРОНЕ ОБЛАКА
// если запрос с колонки то завершить диалог чтобы не висеть в навыке, если диалог в браузере то не завершать
//        String clientId = JsonUtils.jsonGetValue(body, "client_id");
//        if (clientId.contains("browser")) clientType = "browser";
//        else clientType = "speaker";
//        log.info("COMMAND: " + command + " CLIENT_TYPE: " + clientType);

        if (command == null) return "я не поняла команду";
//        определить идентификатор application_id колонки с алисой для привязки к комнате где она находится
        aliceId = JsonUtils.jsonGetValue(body, "application_id");
        return processCommand(aliceId, command);
//        возвращает просто текст ответа
    }

    public static String processCommand(String roomId, String command) {
        String DEFAULT = "Я умею управлять плеерами подключенными в Lyrion Music Server. \n" +
                "Скажите Алисе:\n" +
                ", включи или выключи музыку \n" +
                ", музыку громче или тише \n" +
                ", переключи канал";
        String HELP = "У вас локально должен быть установлен Lyrion Music Server и приложение навыка";
        String CAPABILITIES = "Я умею управлять плеерами подключенными в Lyrion Music Server";

        String cmd = command.trim().toLowerCase();

        // Базовые команды без зависимостей
        if (cmd.isEmpty()) return DEFAULT;
        if (cmd.contains("помощь") || cmd.contains("помоги") || cmd.contains("подскажи")) return HELP;
        if (cmd.contains("что ты умеешь") || cmd.contains("что ты можешь")) return CAPABILITIES;

        // Определение типа клиента ВОЗМОЖНО ИСПОЛЬЗУЕТСЯ НА СТОРОНЕ ОБЛАКА
//        clientType = cmd.isEmpty() ? "browser" : "speaker";

        if (config.lmsIp == null) return "медиасервер не найден";
        // Обработка команд привязки комнаты
        if (cmd.startsWith("это комната")) {
            if (cmd.contains("с колонкой"))
                return selectRoomWithSpeaker(command);
            return selectRoomByCommand(command);
        }

        // Получение текущей комнаты
        String room = Main.idRooms.get(roomId);
        log.info("ROOM: {} COMMAND: {}", room, command);

        // Проверка инициализации комнаты
        if (room == null) return "скажите навыку, это комната и название комнаты";
        // Команды управления комнатой
        if (cmd.matches("(выбери|включи) колонку.*")) return selectPlayerByCommand(command, room);

        // Обновление состояния плееров
        lmsPlayers.updateLmsPlayers();

        // Поиск устройства и плеера
        Device device = SmartHome.getDeviceByCorrectRoom(room);
        if (device == null) return "устройство не найдено. скажите навыку, выбери колонку и название колонки";
        Player player = lmsPlayers.playerByDeviceId(device.id);
        if (player == null)
            return "в комнате " + room + " колонка еще не выбрана. скажите навыку, выбери колонку и название колонки";

        // Обработка медиа-команд
        if (cmd.contains("что играет")) return whatsPlaying(player);
        if (cmd.contains("какая громкость")) return whatsVolume(player);
        if (cmd.matches("(включи )?(канал|избранное) .*")) return VoiceActions.channelPlayByName(command, player);
        if (cmd.matches("добавь( в)? избранное")) return channelAdd(player);
        if (cmd.matches("переключи.*сюда")) return VoiceActions.syncSwitchToHere(player);
        if (cmd.matches("(включи )?отдельно")) return VoiceActions.separateOn(player);
        if (cmd.matches("(включи )?вместе")) return VoiceActions.separateAllOff(player);
        if (cmd.matches("(включи )?(рандом|шафл|shuffle|random)")) return VoiceActions.shuffleOn(player);
        if (cmd.matches("(выключи )?(рандом|шафл|shuffle|random)")) return VoiceActions.shuffleOff(player);
        if (cmd.matches("(включи )?(повтор)")) return VoiceActions.repeatOn(player);
        if (cmd.matches("(выключи )?(повтор)")) return VoiceActions.repeatOff(player);
        if (cmd.matches("(включи )?(дальше|следующий)")) return playlistNextTrack(player);
        if (cmd.matches("(включи|подключи) пульт")) return connectBtRemote(command, player);
        if (cmd.contains("где пульт")) return whereBtRemote();
        if (cmd.startsWith("включи альбом")) return VoiceActions.playAlbum(player, command);
        if (cmd.startsWith("включи трек")) return VoiceActions.playTrack(player, command);
        if (cmd.startsWith("включи плейлист")) return VoiceActions.playPlaylist(player, command);
        if (cmd.startsWith("включи")) return VoiceActions.playArtist(player, command);
        return "Я не поняла команду";
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
        Player playerNew = selectPlayerInRoom(player, room, false); // selectRoomWithSpeaker
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

        Player player = selectPlayerInRoom(target, room, true); // selectPlayerByCommand

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
        Device device = smartHome.getDeviceByCorrectRoom(roomName);
        if (device == null) {
            log.info("CREATE NEW DEVICE IN SMART HOME ROOM: " + roomName);
//            SmartHome.create(roomName, null); // selectPlayerInRoom
            smartHome.create(roomName, playerName); // selectPlayerInRoom
            smartHome.write();
        } else log.info("DEVICE EXISTS IN ROOM: " + device.room);

        log.info("SELECT PLAYER IN ROOM " + roomName + " BY PLAYER IN COMMAND: " + playerName);
        List<String> playerNames = lmsPlayers.players.stream().map(player -> player.name).collect(Collectors.toList());
        log.info("PLAYERS IN LMS: " + playerNames);
        Player playerNew = lmsPlayers.playerByCorrectName(playerName);

        if (!playerNew.connected) { // selectPlayerInRoom
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
        String correctPlayer = Levenstein.getNearestElementInListWord(player, players);
        if (correctPlayer == null) log.info("ERROR PLAYER NOT EXISTS IN LMS " + room);
        log.info("CORRECT PLAYER: " + player + " -> " + correctPlayer);
        return correctPlayer;
    }

    private static String connectBtRemote(String command, Player player) {
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
//            playerName = Levenstein.search(command, lmsPlayers.playersNamesOnLine);
            playerName = Levenstein.search(command, pll);
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
        log.info("\nWHATS PLAYING ON " + player.name);
        if (player == null) return "плеер не найден";
        if (!player.connected) return "плеер " + player.name + "  не подключен к медиасерверу";
        String title = null;
        try {
            title = player.requestTitle();
            log.info("requestTitle: " + title);
            if (title == null) title = player.playlistNameShort;
            log.info("playlistNameShort: " + title);
        } catch (Exception e) {

            log.info("ERROR: TITLE");
        }
        if (title == null || "".equals(title) || "unknown".equals(title)) title = "ничего";
// находим группы плееров которые играют вместе или отдельно, включая отделенные false
        player.playingPlayersNamesNotInCurrentGroup(false); // whatsPlaying
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
        if (player.mode.equals("play")) { // whatsPlaying
            answer = "сейчас на " + player.name + " играет " + separate + title + " громкость " + player.volume; // whatsPlaying
        }
// в ответ плеер сейчас не играет
        if (!player.mode.equals("play")) {  // whatsPlaying
            answer = "сейчас на " + player.name + " не играет " + separate + title; // whatsPlaying
        }
        answer = answer + answerOtherInGroup + answerPlayingSeparate;
        log.info("ANSWER: " + answer);
        return answer;
    }

    private static String whatsVolume(Player player) {
        String answer;
        log.info("VOLUME");
        String volume = player.volume;
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
        String answer = "включаю " + target;
        return answer;
    }

    public static String spotifyPlayTrack(String command, Player player) {
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("трэк", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        String link = Spotify.getLinkTrack(target);
        log.info("LINK: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.runAsync(() -> player.playPath(link));
        String answer = "включаю " + target;
        return answer;
    }

    public static String spotifyPlayAlbum(String command, Player player) {
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("альбом", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        String link = Spotify.getLinkAlbum(target);
        log.info("LINK: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.runAsync(() -> {
// player.shuffleOff();
            player.playPath(link);
        });
        String answer = "включаю " + target;
        return answer;
    }

    public static String spotifyPlayPlaylist(String command, Player player) {
        String target = command.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("плэйлист", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        String link = Spotify.getLinkPlaylist(target);
        log.info("LINK: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.runAsync(() -> {
// player.shuffleOff();
            player.playPath(link);
        });
        String answer = "включаю " + target;
        return answer;
    }

//    public static String spotifyPlayArtistAlbum(String command, Player player) {
//
//        String artist = command.replaceAll(".*включи ", "")
//                .replaceAll(" альбом.*", "");
//
//        String album = command.replaceAll(".*альбом ", "");
//
//        log.info("ARTIST: " + artist);
//        log.info("ALBUM: " + album);
//
//        String target = command.replaceAll(".*включи\\S*\\s", "")
//                .replaceAll("альбом", "")
//                .replaceAll("\"", "").replaceAll("\\s\\s", " ");
//        log.info("TARGET: " + target);
//        String link = Spotify.getLinkAlbum(target);
//        log.info("LINK: " + link);
//        if (link == null) return "настройте спотифай";
//        CompletableFuture.runAsync(() -> {
//            player.shuffleOff();
//            player.playPath(link);
//        });
//        String answer = "сейчас, мой господин, включаю " + SwitchVoiceCommand.artist + ", " + target;
//        return answer;
//    }

    private static String playlistNextTrack(Player player) { // дальше, следующий
        String answer;
        log.info("NEXT TRACK");
//        CompletableFuture.runAsync(() -> player.ctrlNextTrack().saveLastTimePathAutoremoteRequest());
        CompletableFuture.runAsync(() -> player.ctrlNextTrack());
        answer = "включаю следующий";
        return answer;
    }

}