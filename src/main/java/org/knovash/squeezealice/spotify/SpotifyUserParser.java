package org.knovash.squeezealice.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SpotifyUserParser {
    public static SpotifyUser spotifyUser;

    @JsonIgnoreProperties(ignoreUnknown = true) // Игнорировать неизвестные поля
    public static class SpotifyUser {
        @JsonProperty("display_name")
        private String displayName;

        @JsonProperty("email")
        private String email;

        // Остальные поля можно опустить, так как они не нужны

        public String getDisplayName() {
            if (spotifyUser == null) return null;
            return displayName;
        }

        public String getEmail() {
            if (spotifyUser == null) return null;
            return email;
        }
    }

    public static SpotifyUser parseUserInfo(String jsonBody) {
        if (jsonBody == null) return null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            spotifyUser = mapper.readValue(jsonBody, SpotifyUser.class);
            String name = spotifyUser.getDisplayName();
            String email = spotifyUser.getEmail();

            System.out.println("Name: " + name);
            System.out.println("Email: " + email);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return spotifyUser;
    }
}