package org.knovash.squeezealice;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.knovash.squeezealice.requests.Type;
import org.knovash.squeezealice.requests.spotifyalbums.SpotifyResponseAlbums;
import org.knovash.squeezealice.requests.spotifyartists.SpotifyResponseArtists;
import org.knovash.squeezealice.requests.spotifyplaylist.SpotifyResponsePlaylists;
import org.knovash.squeezealice.requests.spotifytracks.SpotifyResponseTracks;

import java.io.IOException;
import java.util.Base64;

@Log4j2
@Data
public class Spotify {

    public static String bearerToken;

    public static String getBearerToken(String clientId, String clientSecret) {
        log.info("clientId: " + clientId + " clientSecret: " + clientSecret);
        Response response;
        String token;
        String clientIdSecret = clientId + ":" + clientSecret;
        String base64 = Base64.getEncoder().encodeToString(clientIdSecret.getBytes());
        log.info("base64: " + base64);
        String json = null;
        try {
            response = Request.Post("https://accounts.spotify.com/api/token?grant_type=client_credentials")
                    .setHeader("Authorization", "Basic " + base64)
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .execute();
            json = response.returnContent().asString();
            log.info("json: " + json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        token = JsonUtils.jsonGetValue(json, "access_token");
        log.info("token: " + token);
        bearerToken = "Bearer " + token.replace("\"", "");
        log.info("bearerToken: " + bearerToken);
        return bearerToken;
    }

    public static String action(String uri) {
        log.info("uri: " + uri);
        Response response = null;
        String json = null;
        try {
            response = Request.Get(uri)
                    .setHeader("Authorization", bearerToken)
                    .execute();
//            log.info("response: " + response.returnContent().asString());
            json = response.returnContent().asString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    public static String search(String q, Type type) {
//        String q = "kraftwerk";
//        String type = "album";
//        String type = "track";
//        String type = "artist";
//        String type = "playlist";
        String link = null;
        String limit = "3";
        String uri = "https://api.spotify.com/v1/search?q=" + q + "&type=" + type + "&limit=" + limit;
        log.info("URI: " + uri);
        String json = Spotify.action(uri);
        log.info("JSON\n" + json);
        switch (type.toString()) {
            case ("album"):
                SpotifyResponseAlbums spotifyResponseAlbums;
                spotifyResponseAlbums = JsonUtils.jsonToPojo(json, SpotifyResponseAlbums.class);
                spotifyResponseAlbums.getAlbums().getItems().stream().forEach(item -> log.info(item));
                log.info("SSS: " + spotifyResponseAlbums.getAlbums().items.get(0).external_urls.spotify.toString());
                log.info("SSS: " + spotifyResponseAlbums.getAlbums().items.get(1).external_urls.spotify.toString());
                log.info("SSS: " + spotifyResponseAlbums.getAlbums().items.get(2).external_urls.spotify.toString());
                link = spotifyResponseAlbums.getAlbums().items.get(0).external_urls.spotify.toString();
                break;
            case ("track"):
                SpotifyResponseTracks st;
                st = JsonUtils.jsonToPojo(json, SpotifyResponseTracks.class);
                log.info("ST\n" + st);
                log.info("SSS: " + st.tracks.items.get(0).external_urls.spotify.toString());
                log.info("SSS: " + st.tracks.items.get(1).external_urls.spotify.toString());
                log.info("SSS: " + st.tracks.items.get(2).external_urls.spotify.toString());
                link = st.tracks.items.get(0).external_urls.spotify.toString();
                break;
            case ("artist"):
                SpotifyResponseArtists sa;
                sa = JsonUtils.jsonToPojo(json, SpotifyResponseArtists.class);
                log.info("ST\n" + sa);
                log.info("SSS: " + sa.artists.items.get(0).external_urls.spotify.toString());
                log.info("SSS: " + sa.artists.items.get(1).external_urls.spotify.toString());
                log.info("SSS: " + sa.artists.items.get(2).external_urls.spotify.toString());
                link = sa.artists.items.get(0).external_urls.spotify.toString();
                break;
            case ("playlist"):
                SpotifyResponsePlaylists sp;
                sp = JsonUtils.jsonToPojo(json, SpotifyResponsePlaylists.class);
                log.info("ST\n" + sp);
                log.info("SSS: " + sp.playlists.items.get(0).external_urls.spotify.toString());
                log.info("SSS: " + sp.playlists.items.get(1).external_urls.spotify.toString());
                log.info("SSS: " + sp.playlists.items.get(2).external_urls.spotify.toString());
                link = sp.playlists.items.get(0).external_urls.spotify.toString();
                break;

            default:
                log.info("ACTION NOT FOUND: ");

                break;
        }
        return link;
    }
}