package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Requests;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.spotify.spotify_pojo.Type;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Levenstein;

import java.util.List;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class SwitchVoiceCommand {

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
        String alice_id = JsonUtils.jsonGetValue(body, "application_id");
        Player player = lmsPlayers.getPlayerByAliceId(alice_id);
        Player.lastAliceId = alice_id;

//        List<String> voiceCommands = new ArrayList<>();
//        voiceCommands.add("включи спотифай");
//        voiceCommands.add("включи канал");
//        voiceCommands.add("включи избранное");
//        voiceCommands.add("какая громкость");
//        voiceCommands.add("громкость");
//        voiceCommands.add("что играет");
//        command = Levenstein.searchInList(command, voiceCommands);

        if (command == null)
            return createResponse("я не поняла команду");
        if (command.contains("комната"))
            return createResponse(thisRoomIsName(command));
        if (player == null)
            return createResponse("скажите навыку: это комната и название комнаты");
        if (command.contains("spotify") || command.contains("спотифай"))
            return createResponse(spotifyTransfer(player));
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

    private static String thisRoomIsName(String command) {
        log.info("ACTION ROOM CONNECT");
        String answer;
        String target = command.replaceAll(".*комната\\S*\\s", "").replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("room request: " + target);
        // найти девайс с комнатой полученой от Алисы
        Device device = SmartHome.getDeviceByRoom(target);
        log.info("ROOM DEVICE: " + device);
        if (device == null) {
            // ненайдена
            log.info("NOT FOUND ROOM: " + target);
            answer = "ой, не найдена комната " + target;
        } else {
            // найдена
            log.info("ROOM: " + device.room);
            log.info("ALICE ID:" + Player.lastAliceId);
            String playerName = device.customData.lmsName;
            lmsPlayers.getPlayerByName(playerName).alice_id = Player.lastAliceId;
            answer = "это комната " + device.room + " с колонкой" + playerName;
            lmsPlayers.write();
        }
        return answer;
    }

    private static String spotifyTransfer(Player player) {
//        http://192.168.1.110:9000/plugins/spotty/index.html?index=10&player=aa%3Aaa%3Acf%3Acd%3A7f%3A87&sess=
        String mac = player.mac;
//        String mac = "aa%3Aaa%3Acf%3Acd%3A7f%3A87";
        String uri = " http://" + lmsIP + ":" + lmsPort + "/plugins/spotty/index.html?index=10&player=" + mac + "&sess=";
        Requests.getByUriForStatus(uri);
        return "включаю spotify на " + player.name;
    }

    private static String spotify(String command, Player player) {
        String answer;
        String target;
        target = command.replaceAll(".*включи\\S*\\s", "").replaceAll("\"", "").replaceAll("\\s\\s", " ");
        log.info("TARGET SPOTIFY: " + target);
        String link = Spotify.search(target, Type.playlist);
        log.info("LINK SPOTIFY: " + link);
        if (link == null && Spotify.bearerToken == null) {
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
        String favName = Levenstein.searchInList(target, playlist);

        if (favName == null) return "повторите";
        log.info("FAVNAME: " + favName);
        int index = playlist.indexOf(favName) + 1;
        answer = "сейчас, мой господин, включаю канал " + index +", "+ favName;
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
            answer = "сейчас на " + player.name + " играет " + playlist;
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