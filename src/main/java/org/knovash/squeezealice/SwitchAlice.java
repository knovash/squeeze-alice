package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.pojo.alice.Alice;
import org.knovash.squeezealice.pojo.alice.ResponseAlice;
import org.knovash.squeezealice.pojo.spotify.Type;
import org.knovash.squeezealice.utils.JsonUtils;

@Log4j2
public class SwitchAlice {

    public static String action(String command, String playerName) {
        log.info("COMMAND: " + command);
        log.info("PLAYER NAME: " + playerName);
        String target;
        String answer = "повторите";
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
            Server.playerByName(playerName).shuffleon().play(link);
            return createResponse(answer);
        }
        if (command.contains("какая") && command.contains("громкость")) {
            log.info("VOLUME");
            String volume = Server.playerByName(playerName).volume();
            if (volume == null)  return createResponse("медиасервер не отвечает");
            answer = "сейчас на " + playerName + " громкость " + volume;
            return createResponse(answer);
        }
        if (command.contains("что") && command.contains("играет")) {
            log.info("WATS PLAYING");
            String playlist = Server.playerByName(playerName).playlistname();
            log.info("PLAYLIST: " + playlist);
            if (playlist == null) playlist = Server.playerByName(playerName).artistname();
            log.info("PLAYLIST: " + playlist);
            if (playlist == null)  return createResponse("медиасервер не отвечает");
            log.info("PLAYLIST: " + playlist);
            answer = "сейчас на " + playerName + " играет " + playlist;
            return createResponse(answer);
        }
        if (command.contains("дальше") || command.contains("следующий")) {
            log.info("NEXT TRACK");
            Server.playerByName(playerName).nexttrack();
            answer = "включаю следующий";
            return createResponse(answer);
        }
        answer = createResponse(answer);
        return answer;
    }

    public static String createResponse(String text) {
        Alice alice = new Alice();
        ResponseAlice responseAlice = new ResponseAlice();
        responseAlice.text = text;
        responseAlice.end_session = true;
        alice.version = "1.0";
        alice.response = responseAlice;
        String response = JsonUtils.pojoToJson(alice);
        log.info("RESPONSE JSON: " + response);
        return response;
    }
}