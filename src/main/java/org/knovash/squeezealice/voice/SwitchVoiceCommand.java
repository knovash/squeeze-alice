package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.spotify.spotify_pojo.Type;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Levenstein;

import java.util.List;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class SwitchVoiceCommand {

    private static String alice_id;
    private static String room;

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
        Device device = SmartHome.getDeviceByRoomLevenstein(room);
        Player player = lmsPlayers.getPlayerByName(device.customData.lmsName);
//        Player player = lmsPlayers.getPlayerByAliceId(alice_id);
        Player.lastAliceId = alice_id;

        if (command == null)
            return createResponse("я не поняла команду");
        if (command.contains("комната"))
            return createResponse(thisRoomIsName(command));
        if (command.contains("выбери колонку") || command.contains("выбери плеер"))
            return createResponse(selectPlayerInRoom(command));

        if (player == null)
            return createResponse("скажите навыку: это комната и название комнаты");
        if (command.contains("включи") && (command.contains("spotify") || command.contains("спотифай")))
            return createResponse(Spotify.transfer(player));
        if (command.contains("включи") && (command.contains("канал") || command.contains("избранное")))
            return createResponse(channel(command, player));
        if (command.contains("включи") && !(command.contains("канал") || command.contains("избранное")))
            return createResponse(spotify(command, player));
        if (command.contains("громкость"))
            return createResponse(volume(player));
        if (command.contains("что") && command.contains("играет"))
            return createResponse(whatsPlaying(player));
        if (command.contains("дальше") || command.contains("следующий"))
            return createResponse(next(player));
        return createResponse("я не поняла команду");
    }

    private static String createResponse(String text) {
        AliceVoiceResponsePojo alice = new AliceVoiceResponsePojo();
        AliceVoiceResponsePojo.ResponseAlice responseAlice = new AliceVoiceResponsePojo.ResponseAlice();
        responseAlice.text = text;
        responseAlice.end_session = true;
        alice.version = "1.0";
        alice.response = responseAlice;
        String response = JsonUtils.pojoToJson(alice);
        log.info("RESPONSE JSON: " + response);
        return response;
    }

    private static String selectPlayerInRoom(String command) {
//        "выбери колонку <target>"
        log.info("ACTION SELECT PLAYER IN ROOM");
        String answer;
        String target = command.replaceAll(".*колонку\\S*\\s", "").replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("room request: " + target);
        List<String> playerNames = lmsPlayers.players.stream().map(player -> player.name).collect(Collectors.toList());
        String playerNewName = Levenstein.getNearestElementInList(target, playerNames); // найти имя плеера по имени из фразы
        if (playerNewName == null) return "нет такого плеера";
        log.info("найдено имя НОВОГО плеера " + playerNewName);

        String playerNowName = SmartHome.getLmsNameByAliceId(alice_id);
        log.info("найдено имя плеера СЕЙЧАС " + playerNowName);
        Player playerNow = lmsPlayers.getPlayerByName(playerNowName); // плеер сейчас в комнате
        Player playerNew = lmsPlayers.getPlayerByName(playerNewName); // взять плеер по имени из фразы

//        playerNew.sync(playerNow.name);
        log.info("ВКЛЮЧИТЬ НОВЫЙ ПЛЕЕР " + playerNew.name);
        playerNew.play();
        log.info("ВыКЛЮЧИТЬ СЕЙЧАС ПЛЕЕР " + playerNow.name);
        playerNow.unsync().pause();



        log.info("заменить плеер " + playerNow.name + " на " + playerNew.name);


        Device deviceNow = SmartHome.getDeviceByRoom(room);
        Device deviceNew = SmartHome.getDeviceByLmsName(playerNewName);
        log.info("deviceNow " + deviceNow.customData.lmsName + " " +deviceNow.id  + " " +deviceNow.room);
        log.info("deviceNew " + deviceNew.customData.lmsName + " " +deviceNew.id  + " " +deviceNew.room);
        String idNow = deviceNow.id;
        String roomNow = deviceNow.room;
        String idNew = deviceNew.id;
        String roomNew = deviceNew.room;
        log.info("сейчас ид " + idNow);
        log.info("сейчас рум " + roomNow);
        log.info("новый ид " + idNew);
        log.info("новый рум " + roomNew);

        SmartHome.logListStringDevices();
        SmartHome.devices.remove(deviceNew);
        SmartHome.devices.remove(deviceNow);
        SmartHome.logListStringDevices();

        deviceNew.id = idNow;
        deviceNow.id = idNew;
        deviceNew.room = roomNow;
        deviceNow.room = roomNew;

        SmartHome.devices.add(deviceNew);
        SmartHome.devices.add(deviceNow);
        SmartHome.logListStringDevices();


        return "выбран плеер " + playerNewName + " в комнате " + SmartHome.getRoomByPlayerName(playerNewName);
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
            log.info("ROOM: " + device.room);
            log.info("ALICE ID:" + Player.lastAliceId);
            String playerName = device.customData.lmsName;
            lmsPlayers.getPlayerByName(playerName).alice_id = Player.lastAliceId;
            answer = "это комната " + device.room + " с колонкой" + playerName;

            SmartHome.rooms.put(room, alice_id);

            lmsPlayers.write();
        }
        return answer;
    }

    private static String spotify(String command, Player player) {
        String answer;
        String target;
        target = command.replaceAll(".*включи\\S*\\s", "").replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET SPOTIFY: " + target);
        String link = Spotify.getLink(target, Type.playlist);
        log.info("LINK SPOTIFY: " + link);
        if (link == null && SpotifyAuth.bearer_token == null) {
            answer = "настройте спотифай";
            return answer;
        }
        if (link == null) {
            answer = "ничего не нашла";
            return answer;
        }
        player.shuffleon().play(link);
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
        String favName = Levenstein.getNearestElementInList(target, playlist);

        if (favName == null) return "повторите";
        log.info("FAVNAME: " + favName);
        int index = playlist.indexOf(favName) + 1;
        answer = "сейчас, мой господин, включаю канал " + index + ", " + favName;
        log.info("FAVINDEX: " + index);
        player.play(index);
        return answer;
    }

    private static String volume(Player player) {
        String answer;
        log.info("VOLUME");
        String volume = player.volume();
        if (volume == null) return createResponse("медиасервер не отвечает");
        answer = "сейчас на " + player.name + " громкость " + volume;
        return answer;
    }

    private static String whatsPlaying(Player player) {
        String answer;
        log.info("WATS PLAYING");
        String playlist = player.playlistname();
        String mode = player.mode();
        log.info("PLAYLIST: " + playlist);
        if (playlist == null) playlist = player.artistname();
        log.info("PLAYLIST: " + playlist);
        if (playlist == null) return createResponse("медиасервер не отвечает");
        log.info("PLAYLIST: " + playlist);
        if (mode.equals("play")) {
            answer = "сейчас на " + player.name + " играет " + playlist + " громкость " + player.volume();
        } else {
            answer = "сейчас на " + player.name + " не играет " + playlist;
        }
        return answer;
    }

    private static String next(Player player) {
        String answer;
        log.info("NEXT TRACK");
        player.nexttrack();
        answer = "включаю следующий";
        return answer;
    }


}