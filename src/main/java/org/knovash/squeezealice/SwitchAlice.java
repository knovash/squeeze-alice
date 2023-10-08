package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.pojo.alice.Alice;
import org.knovash.squeezealice.pojo.alice.ResponseAlice;
import org.knovash.squeezealice.pojo.spotify.Type;
import org.knovash.squeezealice.utils.JsonUtils;

@Log4j2
public class SwitchAlice {

    public static String action(String command, String playerName) {
        String currentPlayer = playerName;
        log.info("COMMAND: " + command);

        log.info("COMMAND: " + playerName);
        String target;
        String answer = "слушаю, мой господин";
        if (command.contains("включи")) {
            target = command.replaceAll(".*включи\\S*\\s", "")
                    .replaceAll("\"", "")
                    .replaceAll("\\s\\s", " ");
            if (target.contains("плэйлист")) {

                answer = "мой господин, на " + playerName + " включаю плэйлист " + target;
                return answer;
            }

            answer = "сейчас, мой господин, включаю " + target;
            log.info("TARGET: " + target);
            String link = Spotify.search(target, Type.playlist);
            log.info("LINK " + link);
            Server.playerByName(currentPlayer).shuffleon().play(link);
        }
        if (command.contains("какая") && command.contains("громкость")) {
            log.info("VOLUME");
            String volume = Server.playerByName(currentPlayer).volume();
            answer = "сейчас на " + playerName + " громкость " + volume;
        }
        if (command.contains("что") && command.contains("играет")) {
            log.info("WATS PLAYING");
            String playlist = Server.playerByName(currentPlayer).playlistname();
            if (playlist == null) playlist = Server.playerByName(currentPlayer).artistname();
            answer = "сейчас на " + playerName + " играет " + playlist;
        }

        if (command.contains("дальше") || command.contains("следующий")) {
            log.info("NEXT TRACK");
            Server.playerByName(currentPlayer).nexttrack();
            answer = "включаю следующий";
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