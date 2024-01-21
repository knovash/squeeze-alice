package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.spotify.spotify_pojo.Type;
import org.knovash.squeezealice.utils.JsonUtils;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class SwitchVoiceCommand {

    public static String action(String command, String playerName) {
        log.info("COMMAND: " + command);
        log.info("PLAYER NAME: " + playerName);
        String target;
        String answer = "повторите";
        if (playerName == null) return createResponse("настройте id алисы");
        if (command == null) return createResponse("не поняла");
        if (command.contains("включи")) {
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