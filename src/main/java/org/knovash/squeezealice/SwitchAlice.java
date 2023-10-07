package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.pojo.alice.Alice;
import org.knovash.squeezealice.pojo.alice.ResponseAlice;
import org.knovash.squeezealice.pojo.spotify.Type;

@Log4j2
public class SwitchAlice {

    public static String action(String command) {
        log.info("COMMAND: " + command);
        String target;
        String answer = "йоу!";
        if (command.contains("включи")) {
            target = command.replaceAll(".*включи\\S*\\s", "")
                    .replaceAll("\"", "")
                    .replaceAll("\\s\\s", " ");
            answer = "сейчас, мой господин, включаю " + target;
            log.info("TARGET: " + target);
            String link = Spotify.search(target, Type.playlist);
            log.info("LINK " + link);
            Server.playerByName("HomePod").shuffleon().play(link);
        }
        if (command.contains("какая") && command.contains("громкость")) {
            log.info("VOLUME");
            String volume = Server.playerByName("HomePod").volume();
            answer = "мой господин, сейчас громкость " + volume;
        }
        if (command.contains("что") && command.contains("играет")) {
            log.info("WATS PLAYING");
            String playlist = Server.playerByName("HomePod").playlistname();
            if (playlist == null)  playlist = Server.playerByName("HomePod").artistname();
            answer = "мой господин, сейчас играет " + playlist;
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