package org.knovash.squeezealice;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.knovash.squeezealice.requests.spotify.Type;
import org.knovash.squeezealice.requests.spotify.spotifyalbums.SpotifyAlbums;
import org.knovash.squeezealice.requests.spotify.spotifyartists.SpotifyArtists;
import org.knovash.squeezealice.requests.spotify.spotifyplaylist.Item;
import org.knovash.squeezealice.requests.spotify.spotifyplaylist.SpotifyPlaylists;
import org.knovash.squeezealice.requests.spotify.spotifytracks.SpotifyResponseTracks;

import java.io.IOException;
import java.util.Base64;
import java.util.stream.Collectors;

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

    public static String uri(String uri) {
        log.info("uri: " + uri);
        Response response = null;
        String json = null;

        try {
            response = Request.Get(uri)
                    .setHeader("Authorization", bearerToken)
                    .execute();
            json = response.returnContent().asString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    public static String search(String q, Type type) {

        String clientId = "f45a18e2bcfe456dbd9e7b73e74514af";
        String clientSecret = "5c3321b4ae7e43ab93a2ce4ec1b4cf48";
        Spotify.getBearerToken(clientId, clientSecret);

        log.info("Q: " + q);
        log.info("TYPE: " + type);
        String link = null;
        String limit = "10";
        q = q.replace(" ", "%20");
        String uri = "https://api.spotify.com/v1/search?q=" + q + "&type=" + type + "&limit=" + limit;
        log.info("URI: " + uri);
        String json = Spotify.uri(uri);
        switch (type.toString()) {
            case ("album"):
                log.info("ALBUM");
                SpotifyAlbums spotifyAlbums;
                spotifyAlbums = JsonUtils.jsonToPojo(json, SpotifyAlbums.class);
                spotifyAlbums.getAlbums().getItems().stream().forEach(item -> log.info(item));
                log.info("SSS: " + spotifyAlbums.getAlbums().items.get(0).external_urls.spotify.toString());
                link = spotifyAlbums.getAlbums().items.get(0).external_urls.spotify.toString();
                break;
            case ("track"):
                log.info("TRACK");
                SpotifyResponseTracks st;
                st = JsonUtils.jsonToPojo(json, SpotifyResponseTracks.class);
                log.info("ST\n" + st);
                log.info("SSS: " + st.tracks.items.get(0).external_urls.spotify.toString());
                link = st.tracks.items.get(0).external_urls.spotify.toString();
                break;
            case ("artist"):
                log.info("ARTIST");
                SpotifyArtists sa;
                sa = JsonUtils.jsonToPojo(json, SpotifyArtists.class);
                log.info("ST\n" + sa);
                log.info("SSS: " + sa.artists.items.get(0).external_urls.spotify.toString());
                link = sa.artists.items.get(0).external_urls.spotify.toString();
                break;
            case ("playlist"):
                log.info("PLAYLIST");
                SpotifyPlaylists sp;
                sp = JsonUtils.jsonToPojo(json, SpotifyPlaylists.class);
                log.info("ST\n" + sp);
                Item item = sp.playlists.items
                        .stream()
                        .peek(it -> log.info("\nPLAYLIST:" +
                                "\nNAME: " + it.name +
                                "\nOWNER: " + it.owner.display_name +
                                "\nURL: " + it.external_urls.spotify.toString()))
                        .filter(it -> it.name.contains("This Is"))
                        .findFirst()
                        .orElse(null);
                if (item == null) item = sp.playlists.items.get(0);


                log.info("FINAL OWNER: " + item.owner.display_name);
                log.info("FINAL NAME: " + item.name);
                log.info("FINAL URL: " + item.external_urls.spotify);
                link = item.external_urls.spotify;
                break;

            default:
                log.info("DEF");
                break;
        }
        log.info("SKIP");
        return link;
    }
}