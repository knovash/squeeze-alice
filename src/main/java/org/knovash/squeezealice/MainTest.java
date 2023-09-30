package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.ResourceBundle;

@Log4j2
public class MainTest {

    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");

        log.info("  ---+++===[ START ]===+++---");
        String clientId = "f45a18e2bcfe456dbd9e7b73e74514af";
        String clientSecret = "5c3321b4ae7e43ab93a2ce4ec1b4cf48";
        Spotify.getBearerToken(clientId, clientSecret);
        log.info("  ---+++===[ ALBUM ]===+++---");
        Spotify.action("https://api.spotify.com/v1/search?q=techno&type=album&limit=5");
        log.info("  ---+++===[ TRACK ]===+++---");
        Spotify.action("https://api.spotify.com/v1/search?q=track%3A$1+$2+$3&type=track&limit=5");


    }
}