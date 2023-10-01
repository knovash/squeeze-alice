package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.requests.spotify.Type;

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
        return answer;
    }
}