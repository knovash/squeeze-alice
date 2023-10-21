package org.knovash.squeezealice.spotify;

import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
@Log4j2
public class SpotifyUtils {

    public static String credentials(HashMap<String, String> parameters) {
        String id = parameters.get("id");
        String secret = parameters.get("secret");
        Spotify.client_id = id;
        Spotify.client_secret = secret;
        log.info(Spotify.client_id);
        log.info(Spotify.client_secret);
        if (id == null || secret == null) return "CRED ERROR";
        Spotify.createCredFile(id, secret);
        return "CRED SET";
    }
}
