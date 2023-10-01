package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SwitchAlice {

    public static String action(String command) {
        log.info("COMMAND: " + command);
        String target = "иди в жопу";
        String answer = "иди в жопу";
        if (command.contains("включи")) {
            target = command.replaceAll(".*включи ", "")
                    .replaceAll("\"", "")
              .replaceAll("\\s\\s", " ");
            answer = "сейчас, мой господин, включаю " + target;
            log.info("TARGET: " + target);
            Spotify.action("https://api.spotify.com/v1/search?q="+target+"&type=album&limit=5");
        }
        return answer;
    }
}