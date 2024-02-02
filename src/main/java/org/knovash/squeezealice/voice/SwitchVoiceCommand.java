package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.provider.SmartHome;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.spotify.spotify_pojo.Type;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;

import java.util.List;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class SwitchVoiceCommand {

    public static String action(String command, String playerName, Context context) {
        log.info("COMMAND: " + command);
        log.info("PLAYER NAME: " + playerName);
        String target;
        String answer = "повторите";
        if (command == null) return createResponse("не поняла");

        // привязка Алисы к колоке в комнате
        if (command.contains("комната")) {
            log.info("ACTION ROOM CONNECT");
            target = command.replaceAll(".*комната\\S*\\s", "")
                    .replaceAll("\"", "")
                    .replaceAll("\\s\\s", " ");
            log.info("room request: " + target);
            // найти девайс с комнатой полученой от Алисы
            Device device = SmartHome.getByDeviceRoom(target);
            log.info("ROOM DEVICE: " + device);
            if (device == null) {
                // ненайдена
                log.info("NOT FOUND ROOM: " + target);
                answer = "ой, не найдена комната " + target;
            } else {
                // найдена
                log.info("ROOM: " + device.room);
                log.info("ALICE ID:" + Player.lastAliceId);
                playerName = device.customData.lmsName;
                Player player = lmsPlayers.getPlayerByName(playerName);
                player.alice_id = Player.lastAliceId;
                answer = "это комната " + device.room + " с колонкой" + playerName;
                lmsPlayers.write();

            }
            return createResponse(answer);
        }

        if (playerName == null) return createResponse("повторите и скажите: это комната - название");

//        включи Spotify <name playlist it is ...>
        if (command.contains("включи") && !command.contains("канал")) {
            target = command.replaceAll(".*включи\\S*\\s", "")
                    .replaceAll("\"", "")
                    .replaceAll("\\s\\s", " ");
            answer = "сейчас, мой господин, включаю " + target;
            log.info("TARGET: " + target);
            String link = Spotify.search(target, Type.playlist);
            log.info("LINK " + link);
            if (link == null && Spotify.bearerToken == null) {
                answer = "настройте спотифай";
                return createResponse(answer);
            }
            if (link == null) {
                answer = "ничего не нашла";
                return createResponse(answer);
            }
            lmsPlayers.getPlayerByName(playerName).shuffleon().play(link);
            return createResponse(answer);
        }
//        включи канал <number> or <name>
        if (command.contains("включи") && command.contains("канал")) {
            target = command.replaceAll(".*канал\\S*\\s", "")
                    .replaceAll("\"", "")
                    .replaceAll("\\s\\s", " ");
            log.info("TARGET: " + target);
            List<String> pl = lmsPlayers.favorites();
            pl.forEach(n -> log.info(n));
            String favName = Levenstein.searchInList(target, pl);
            log.info("FAVNAME: " + favName);
            answer = "сейчас, мой господин, включаю " + favName;
            int index = pl.indexOf(favName);
            log.info("FAVINDEX: " + index);
            lmsPlayers.getPlayerByName(playerName).play(index+1);
            return createResponse(answer);
        }

//        какая громкость
        if (command.contains("какая") && command.contains("громкость")) {
            log.info("VOLUME");
            String volume = lmsPlayers.getPlayerByName(playerName).volume();
            if (volume == null) return createResponse("медиасервер не отвечает");
            answer = "сейчас на " + playerName + " громкость " + volume;
            return createResponse(answer);
        }

        if (command.contains("что") && command.contains("играет")) {
            log.info("WATS PLAYING");
            String playlist = lmsPlayers.getPlayerByName(playerName).playlistname();

            String mode = lmsPlayers.getPlayerByName(playerName).mode();
            log.info("PLAYLIST: " + playlist);
            if (playlist == null) playlist = lmsPlayers.getPlayerByName(playerName).artistname();
            log.info("PLAYLIST: " + playlist);
            if (playlist == null) return createResponse("медиасервер не отвечает");
            log.info("PLAYLIST: " + playlist);
            if (mode.equals("play")) {
                answer = "сейчас на " + playerName + " играет " + playlist;
            } else {
                answer = "сейчас на " + playerName + " не играет " + playlist;
            }
            return createResponse(answer);
        }
        if (command.contains("дальше") || command.contains("следующий")) {
            log.info("NEXT TRACK");
            lmsPlayers.getPlayerByName(playerName).nexttrack();
            answer = "включаю следующий";
            return createResponse(answer);
        }
        answer = createResponse(answer);
        return answer;
    }

    public static String createResponse(String text) {
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
}