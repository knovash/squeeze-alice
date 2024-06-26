package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.*;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.spotify.spotify_pojo.Type;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Levenstein;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.Main.rooms;

@Log4j2
public class SwitchVoiceCommand {

    public static String alice_id;
    public static String room;

    public static Context action(Context context) {
        log.info("");
        context.json = switchVoiceCommand(context);
        context.code = 200;
        return context;
    }

    public static String switchVoiceCommand(Context context) {
        String body = context.body;
        String command = JsonUtils.jsonGetValue(body, "command");
        log.info("COMMAND: " + command); // текст из диалога
        alice_id = JsonUtils.jsonGetValue(body, "application_id");
        room = SmartHome.getRoomByAliceId(alice_id);
        log.info("ROOM: " + room); // текст из диалога
        if (command == null)
            return createResponse("я не поняла команду");
        if (command.contains("обнови"))
            return createResponse(updatePlayers());
        if (command.contains("выбери колонку"))
            return createResponse(selectPlayerInRoom(command));
        if (command.contains("где пульт"))
            return createResponse(whereRemote());

        if (command.contains("комната")) // это комната {название}
            return createResponse(thisRoomIsName(command));
        if (room == null) return createResponse("скажите навыку: это комната и название комнаты");

        Device device = SmartHome.getDeviceByRoom(room);
        if (device == null) return createResponse("я не нашла комнату " + room + " в доме");

        Player player = device.takePlayer();
        if (player == null)
            return createResponse("я не нашла колонку " + device.takePlayerName() + " в комнате " + room);

        lmsPlayers.lastAliceId = alice_id;
        if (command.contains("какая колонка"))
            return createResponse(whatsPlayer(player));
        if (command.contains("включи пульт") || command.contains("подключи пульт"))
            return createResponse(connectBtRemote(player));
        if (command.contains("отдельно"))
            return createResponse(separate_on(player));
        if (command.contains("только тут"))
            return createResponse(alone_on(player));
        if (command.contains("переключи сюда") || command.contains("переключи музыку сюда") || command.contains("переключи музыку"))
            return createResponse(swithToHere(player));
        if (command.contains("вместе"))
            return createResponse(separate_alone_off(player));
        if ((command.contains("spotify") || command.contains("спотифай")))
            return createResponse(Spotify.transfer(player));
        if ((command.contains("канал")))
            return createResponse(channel(command, player));
        if (command.contains("включи") && // включи {исполнитель}
                !(command.contains("альбом") || command.contains("канал") || command.contains("избранное")))
            return createResponse(spotifyPlayArtist(command, player));
        if (command.contains("включи альбом"))
            return createResponse(spotifyPlayAlbum(command, player));
        if (command.contains("громкость"))
            return createResponse(volume(player));
        if (command.contains("что играет"))
            return createResponse(whatsPlaying(player));
        if (command.contains("дальше") || command.contains("следующий"))
            return createResponse(next(player));
        if (command.contains("добавь в избранное") || command.contains("добавь избранное"))
            return createResponse(favoritesAdd(player));
        return createResponse("я не поняла команду");
    }

    public static String createResponse(String text) { // TODO
        AliceVoiceResponsePojo alice = new AliceVoiceResponsePojo();
        AliceVoiceResponsePojo.ResponseAlice responseAlice = new AliceVoiceResponsePojo.ResponseAlice();
        responseAlice.text = text;
        responseAlice.end_session = true;
        alice.version = "1.0";
        alice.response = responseAlice;
        return JsonUtils.pojoToJson(alice);
    }

    private static String selectPlayerInRoom(String command) {
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

        Player playerNow = lmsPlayers.getPlayerByRoom(room);
        String id = playerNow.deviceId;
        log.info("ROOM: " + room + " ID: " + id);

        playerNow.roomPlayer = null;
        playerNow.deviceId = null;
        playerNew.roomPlayer = room;
        playerNew.deviceId = id;

        CompletableFuture.supplyAsync(() -> {
            Actions.turnOnMusic(playerNew);
            playerNow.unsync().pause();
//            SmartHome.write();
            return "";
        });
        return "выбрана колонка " + playerNewName + " в комнате " + SmartHome.getRoomByPlayerName(playerNewName);
    }

    private static String thisRoomIsName(String command) {
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
            log.info("PLAYER NAME: " + playerName);
            log.info("ALICE ID: " + alice_id);
            log.info("ROOM: " + room);
            answer = "это комната " + room + " с колонкой " + playerName;
            log.info("ANSWER: " + answer);
            log.info("ROOMS: " + Main.rooms);
            Main.rooms.put(room, alice_id);
            log.info("ROOMS: " + Main.rooms);
            JsonUtils.mapToJsonFile(rooms, "rooms.json");
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
            Actions.playSpotify(player, link);
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
            Actions.playSpotify(player, link);
            return "";
        });
        answer = "сейчас, мой господин, включаю " + target;
        return answer;
    }

    private static String channel(String command, Player player) {
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
            Actions.playChannel(player, index);
            return "";
        });
        return answer;
    }

    private static String volume(Player player) {
        String answer;
        log.info("VOLUME");
        String volume = player.volumeGet();
        if (volume == null) return createResponse("медиасервер не отвечает");
        answer = "сейчас на " + player.name + " громкость " + volume;
        return answer;
    }

    public static String whatsPlaying(Player player) {
        String answer = "";
        log.info("WATS PLAYING");
        player.status();
        lmsPlayers.update();
        String title = player.title;

        if (title == null) return createResponse("медиасервер не отвечает");
        log.info("PLAYLIST: " + title);
        String separate = "";
        if (player.separate) separate = "отдельно ";

        List<String> separatePlayers =
                lmsPlayers.players.stream()
                        .peek(p -> log.info(p.name + " separate " + p.separate))
                        .filter(p -> p.separate)
                        .filter(p -> !p.name.equals(player.name))
                        .peek(p -> log.info(p.name + " filter separate " + p.separate))
                        .map(p -> {
                            if (p.playing) return " играет " + p.name;
                            return " не играет " + p.name;
                        })
                        .collect(Collectors.toList());
        log.info("SEPARATE PLAYERS: " + separatePlayers);
        String separateAnswer = "";
        if (separatePlayers.contains(player.name)) separatePlayers.remove(player.name);
        if (separatePlayers.size() != 0) separateAnswer = ", отдельно " + String.join(", ", separatePlayers);
        String mode = player.status.result.mode;
        int volume = player.status.result.mixer_volume;
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
        answer = answer + separateAnswer;
        return answer;
    }

    private static String next(Player player) { // дальше, следующий
        String answer;
        log.info("NEXT TRACK");
        CompletableFuture.supplyAsync(() -> {
            player.nextTrack();
            return "";
        });
        answer = "включаю следующий";
        return answer;
    }

    private static String separate_on(Player player) {
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

    private static String alone_on(Player player) {
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

    private static String swithToHere(Player player) {
        String answer;
        log.info("SWITCH TO HERE");
        player.status();
        CompletableFuture.supplyAsync(() -> {
            player.play()
                    .stopAllOther()
                    .status();
            return "";
        });
        answer = "переключаю музыку на " + player.name + ". " + player.title;
        return answer;
    }

    private static String separate_alone_off(Player player) {
        String answer;
        log.info("SEPARATE ALONE OFF");
        player.separate_alone_off();
        player.waitFor(1000).status(); // неуспевает обновить current_title
//        player.waitFor(500).status();
        answer = "включаю вместе " + player.name + " " + player.title;
        return answer;
    }

    private static String updatePlayers() {
        String answer;
        log.info("BEFORE UPDATE " + lmsPlayers.playersNamesOnLine.toString());
        lmsPlayers.update();
        log.info("AFTER UPDATE " + lmsPlayers.playersNamesOnLine.toString());
        answer = "найдено плееров " + lmsPlayers.playersNamesOnLine.size() + ", " + String.join(", ", lmsPlayers.playersNamesOnLine);
        return answer;
    }

    private static String favoritesAdd(Player player) {
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

    private static String whereRemote() {
        String answer;
        log.info("WHERE BT REMOTE");
        answer = "пульт подключен к " + lmsPlayers.btPlayerName;
        return answer;
    }

    private static String whatsPlayer(Player player) {
        String answer;
        log.info("WHATS PLAYER");
        answer = "сейчас подключена колонка " + player.name;
        return answer;
    }

}