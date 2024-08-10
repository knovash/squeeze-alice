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

    public static Context action(Context context) {
        String answer = switchVoiceCommand(context);
        context.json = createJsonResponse(answer);
        context.code = 200;
        return context;
    }

    public static String switchVoiceCommand(Context context) {
        String body = context.body;
        String command = JsonUtils.jsonGetValue(body, "command");
        log.info("COMMAND: " + command);
        if (command == null) return "я не поняла команду";
        aliceId = JsonUtils.jsonGetValue(body, "application_id");
        lmsPlayers.lastAliceId = aliceId;

//      НАСТРОЙКА
        if (command.contains("найди сервер")) return searchServer();
        if (command.contains("найди колонки")) return searchPlayers();
        if (command.matches("это комната.*с колонкой.*")) return selectRoomWithSpeaker(command);
        if (command.contains("это комната")) return selectRoom(command);
//        room = SmartHome.getRoomByAliceId(aliceId);
        room = Main.roomsNew.get(aliceId);
        log.info("ROOM: " + room);
        String firstRoomname = "";
        if (SmartHome.devices.size() > 0) firstRoomname = SmartHome.devices.get(0).room;
        if (room == null) return "скажите навыку, это комната и название комнаты, например " + firstRoomname;

        if (command.contains("выбери колонку")) return selectPlayer(command);

        Device device = SmartHome.getDeviceByRoom(room);
        String firstPlayername = "";
        if (lmsPlayers.players.size() > 0) firstPlayername = lmsPlayers.players.get(0).name;
        if (device == null)
            return "устройство не найдено. скажите навыку, выбери колонку и название колонки, например " + firstPlayername;
//        Player player = device.lmsGetPlayerByDeviceId();

        Player player = lmsPlayers.getPlayerByDeviceId(device.id);
        if (lmsIp == null) return "медиасервер не найден";
        if (player == null) return "скажите навыку, выбери колонку и название колонки, например " + firstPlayername;
        if (command.contains("включи пульт")) return connectBtRemote(player);
        if (command.contains("где пульт")) return whereBtRemote();

//      БАЗА
        if (command.contains("что играет")) return whatsPlaying(player);
        if (command.contains("какая громкость")) return whatsVolume(player);

//      ИЗБРАННОЕ
        if ((command.contains("канал"))) return channelPlayByName(command, player);
        if (command.contains("добавь в избранное")) return channelAdd(player);

//      СПОТИФАЙ
        if (command.contains("включи") && !(command.contains("альбом") || command.contains("канал")))
            return spotifyPlayArtist(command, player);
        if (command.contains("включи альбом")) return spotifyPlayAlbum(command, player);
        if (command.contains("дальше") || command.contains("следующий")) return playlistNextTrack(player);

//      СИНХРОНИЗАЦИЯ
        if (command.matches("переключи.*сюда")) return syncToHere(player);
        if (command.contains("только тут")) return syncAlone(player);
        if (command.contains("отдельно")) return syncSeparate(player);
        if (command.contains("вместе")) return syncTogether(player);

        return "я не поняла команду";
    }

    public static String createJsonResponse(String text) { // TODO
        AliceVoiceResponsePojo alice = new AliceVoiceResponsePojo();
        AliceVoiceResponsePojo.ResponseAlice responseAlice = new AliceVoiceResponsePojo.ResponseAlice();
        responseAlice.text = text;
        responseAlice.end_session = true;
        alice.version = "1.0";
        alice.response = responseAlice;
        return JsonUtils.pojoToJson(alice);
    }

    private static String selectPlayer(String command) {
        log.info("ACTION SELECT PLAYER IN ROOM");
        String target = command.replaceAll(".*колонку\\S*\\s", "").replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("target: " + target);
        List<String> playerNames = lmsPlayers.players.stream().map(player -> player.name).collect(Collectors.toList());
        log.info("players names in lms: " + playerNames);
        String playerNewName = Levenstein.getNearestElementInListW(target, playerNames); // найти имя плеера по имени из фразы
        if (playerNewName == null) return "нет такой колонки";
        log.info("найдено имя НОВОГО плеера " + playerNewName);
        Player playerNew = lmsPlayers.getPlayerByName(playerNewName);
        log.info("найден НОВЫЙ плеер " + playerNew.name);
//        room = playerNew.roomPlayer;
//        CompletableFuture.supplyAsync(() -> {
        Player playerNow = lmsPlayers.getPlayerByRoom(room);
        if (playerNow != null) {
            String id = playerNow.deviceId;
            log.info("ROOM: " + room + " ID: " + id);
            playerNow.roomPlayer = null;
            playerNow.deviceId = null;
            playerNew.roomPlayer = room;
            playerNew.deviceId = id;
        } else {
            playerNew.roomPlayer = room;
            playerNew.deviceId = SmartHome.getDeviceIdByRoom(room);
        }

        CompletableFuture.supplyAsync(() -> {
            Actions.turnOnMusic(playerNew);
            if (playerNow != null) playerNow.unsync().pause();
//            SmartHome.write();
            return "";
        });
        return "выбрана колонка " + playerNewName + " в комнате " + SmartHome.getRoomByPlayerName(playerNewName);
    }

    private static String selectRoomWithSpeaker(String command) {
        log.info("ACTION ROOM CONNECT");
        String answer;
//        это комната гостиная с колонкой хомпод
        String room = command
                .replaceAll(".*комната", "")
                .replaceAll("с колонкой.*", "")
                .replaceAll("\\s", "")
//                .replaceAll("\"", "")
//                .replaceAll("\\s\\s", " ")
                ;
        log.info("room request: " + room);

        String player = command
                .replaceAll(".*с колонкой", "")
                .replaceAll("\\s", "")
//                .replaceAll("\"", "")
//                .replaceAll("\\s\\s", " ")
                ;
        log.info("speeaker request: " + player);

        selectRoom(room);
        answer = selectPlayer(player);

        return answer;
    }

    private static String selectRoom(String command) {
        log.info("ACTION ROOM CONNECT");
        String answer;
        String room = command.replaceAll(".*комната\\S*\\s", "").replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("room request: " + room);
        // найти девайс с комнатой полученой от Алисы
        Device device = SmartHome.getDeviceByRoomLevenstein(room);
        log.info("ROOM DEVICE: " + device);
        if (device == null) {
            // ненайдена
            log.info("NOT FOUND ROOM: " + room);
            answer = "ой, не найдена комната " + room;
        } else {
            // найдена
            log.info("ROOM ID: " + device.id);
            String playerName = device.takePlayerName();
            if (playerName == null) playerName = ". колонка в комнате еще не выбрана";
            else playerName = ". с колонкой " + playerName;
            log.info("PLAYER NAME: " + playerName);
            log.info("ALICE ID: " + aliceId);
            log.info("ROOM: " + room);
            answer = "это комната " + room + playerName;
            log.info("ANSWER: " + answer);
            log.info("ROOMS NEW: " + roomsNew);
            roomsNew.put(aliceId, room);
            log.info("ROOMS NEW: " + roomsNew);
            JsonUtils.mapToJsonFile(roomsNew, "rooms_new.json");
//            SmartHome.write();
            log.info("FINISH");
        }
        return answer;
    }

    private static String spotifyPlayArtist(String command, Player player) {
        String answer;
        String target;
        target = command.replaceAll(".*включи\\S*\\s", "").replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET SPOTIFY: " + target);
        String link = Spotify.getLink(target, Type.playlist);
        log.info("LINK SPOTIFY: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.supplyAsync(() -> {
//            Actions.playSpotify(player, link);
            player.playPath(link);
            return "";
        });
        answer = "сейчас, мой господин, включаю " + target;
        return answer;
    }

    private static String spotifyPlayAlbum(String command, Player player) {
        String answer;
        String target;
        target = command.replaceAll(".*включи\\S*\\s", "").replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET SPOTIFY: " + target);
        String link = Spotify.getLink(target, Type.album);
        log.info("LINK SPOTIFY: " + link);
        if (link == null) return "настройте спотифай";
        CompletableFuture.supplyAsync(() -> {
//            Actions.playSpotify(player, link);
            player.playPath(link);
            return "";
        });
        answer = "сейчас, мой господин, включаю " + target;
        return answer;
    }

    private static String channelPlayByName(String command, Player player) {
        String answer;
        String target;
        target = command.replaceAll(".*(канал|избранное)\\S*\\s", "").replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET LMS CHANNEL: " + target);
        List<String> playlist = lmsPlayers.favorites();
        playlist.forEach(n -> log.info(n));
        String channel = Levenstein.getNearestElementInList(target, playlist);
        if (channel == null) return "повторите";
        log.info("CHANNEL: " + channel);
        int index = playlist.indexOf(channel) + 1;
        answer = "сейчас, мой господин, включаю канал " + index + ", " + channel;
        log.info("INDEX: " + index);
        CompletableFuture.supplyAsync(() -> {
//            Actions.actionPlayChannel(player, index);
            player.playChannel(index);
            return "";
        });
        return answer;
    }

    private static String whatsVolume(Player player) {
        String answer;
        log.info("VOLUME");
        String volume = player.volumeGet();
        if (volume == null) return createJsonResponse("медиасервер не отвечает");
        answer = "сейчас на " + player.name + " громкость " + volume;
        return answer;
    }

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

    private static String playlistNextTrack(Player player) { // дальше, следующий
        String answer;
        log.info("NEXT TRACK");
        CompletableFuture.supplyAsync(() -> {
            player.nextTrack();
            return "";
        });
        answer = "включаю следующий";
        return answer;
    }

    private static String syncSeparate(Player player) {
        String answer;
        log.info("SEPARATE ON");
        player.status();
        CompletableFuture.supplyAsync(() -> {
            player
                    .separate_on()
                    .status();
            return "";
        });
        answer = "включаю отдельно " + player.name + ". " + player.title;
        return answer;
    }

    private static String syncAlone(Player player) {
        String answer;
        log.info("ALONE ON");
        player.status();
        CompletableFuture.supplyAsync(() -> {
            player
                    .alone_on()
                    .status();
            return "";
        });
        answer = "включаю только тут на " + player.name + ". " + player.title;
        return answer;
    }

    private static String syncToHere(Player player) {
        log.info("SWITCH TO HERE");
        String answer = "переключаю музыку на " + player.name;
        CompletableFuture.supplyAsync(() -> {
            log.info("TRY TRANSFER IF SPOTIFY PLAY");
            if (!Spotify.transfer(player)) {
                log.info("SPOTIFY NOT PLAY. TRY SEARCH FOR PLAYING");
                player.status();
                Player playing = lmsPlayers.getPlayingPlayer(player.name);
                if (playing != null) {
                    Actions.turnOnMusic(player);
                    player.stopAllOther().status();
                }
            }
            return "";
        });
        return answer;
    }

    private static String syncTogether(Player player) {
        String answer;
        log.info("SEPARATE ALONE OFF");
        player.separate_alone_off();
        player.waitFor(1000).status(); // неуспевает обновить current_title
//        player.waitFor(500).status();
        answer = "включаю вместе " + player.name + " " + player.title;
        return answer;
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
        CompletableFuture.supplyAsync(() -> {
            Utils.searchLmsIp();
            log.info("LMS IP " + lmsIp);
            if (lmsIp != null) lmsPlayers.updateServerStatus();
            return "";
        });
        answer = "сейчас найду";
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

    private static String connectBtRemote(Player player) {
        String answer;
        log.info("CONNECT BT REMOTE");
        lmsPlayers.btPlayerInQuery = player.nameInQuery;
        lmsPlayers.btPlayerName = player.name;
        answer = "пульт подключен к " + player.name;
        return answer;
    }

    private static String whereBtRemote() {
        String answer;
        log.info("WHERE BT REMOTE");
        answer = "пульт подключен к " + lmsPlayers.btPlayerName;
        return answer;
    }
}