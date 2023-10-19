package org.knovash.squeezealice;

import org.knovash.squeezealice.Spotify;

import java.util.HashMap;

public class SpotifyUtils {

    public static String credentials(HashMap<String, String> parameters) {
        String id = parameters.get("id");
        String secret = parameters.get("secret");
        if (id == null || secret == null) return "CRED ERROR";
        Spotify.createCredFile(id, secret);
        return "CRED SET";
    }
}
