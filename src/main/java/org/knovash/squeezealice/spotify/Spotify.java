package org.knovash.squeezealice.spotify;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.message.BasicHeader;
import org.knovash.squeezealice.spotify.spotify_pojo.SpotifyCredentials;
import org.knovash.squeezealice.spotify.spotify_pojo.Type;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_albums.SpotifyAlbums;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_artists.SpotifyArtists;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_playlist.Item;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_playlist.SpotifyPlaylists;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_tracks.SpotifyResponseTracks;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

@Log4j2
@Data
public class Spotify {

    public static String bearerToken;
    public static String client_id = "";
    public static String client_secret = "";
    public static SpotifyCredentials sc = new SpotifyCredentials();

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
            log.info("SPOTIFY BEARER TOKEN REQUEST ERROR try check credentials in spotify.json");
            return null;
//            throw new RuntimeException(e);
        }
        token = JsonUtils.jsonGetValue(json, "access_token");
        log.info("token: " + token);
        bearerToken = "Bearer " + token.replace("\"", "");
        log.info("bearerToken: " + bearerToken);
        return bearerToken;
    }

    public static String request(String uri) {
        log.info("uri: " + uri);
        Response response = null;
        String json = null;
        Header[] headers = {
                new BasicHeader("Authorization", SpotifyAuth.bearerToken)
        };
        try {
            response = Request.Get(uri)
                    .setHeaders(headers)
                    .execute();
            json = response.returnContent().asString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    public static String search(String q, Type type) {

        sc = JsonUtils.jsonFileToPojo("spotify.json", SpotifyCredentials.class);
        log.info("NO FILE");
        if (sc == null) Spotify.createCredFile();
        String bt = null;
        if (sc != null) bt = Spotify.getBearerToken(sc.clientId, sc.clientSecret);
        if (bt == null) return null;
        log.info("Q: " + q);
        log.info("TYPE: " + type);
        String link = null;
        String limit = "10";
        q = q.replace(" ", "%20");
        String uri = "https://api.spotify.com/v1/search?q=" + q + "&type=" + type + "&limit=" + limit;
        log.info("URI: " + uri);
        String json = Spotify.request(uri);
        switch (type.toString()) {
            case ("album"):
                log.info("ALBUM");
                SpotifyAlbums sl;
                sl = JsonUtils.jsonToPojo(json, SpotifyAlbums.class);
                sl.getAlbums().getItems().stream().forEach(item -> log.info(item));
                link = sl.getAlbums().items.get(0).external_urls.spotify.toString();
                break;
            case ("track"):
                log.info("TRACK");
                SpotifyResponseTracks st;
                st = JsonUtils.jsonToPojo(json, SpotifyResponseTracks.class);
                link = st.tracks.items.get(0).external_urls.spotify.toString();
                break;
            case ("artist"):
                log.info("ARTIST");
                SpotifyArtists sr;
                sr = JsonUtils.jsonToPojo(json, SpotifyArtists.class);
                link = sr.artists.items.get(0).external_urls.spotify.toString();
                break;
            case ("playlist"):
                log.info("PLAYLIST");
                SpotifyPlaylists sp;
                sp = JsonUtils.jsonToPojo(json, SpotifyPlaylists.class);
//                log.info("JSON " + json);
                log.info("ST\n" + sp);
                Item item = sp.playlists.items
                        .stream()
                        .peek(it -> log.info(
                                "PLAYLIST: " + it.name +
                                        " OWNER: " + it.owner.display_name +
                                        " URL: " + it.external_urls.spotify.toString()))
                        .filter(it -> it.name.contains("This Is"))
                        .filter(it -> it.owner.display_name.contains("Spotify"))
                        .findFirst()
                        .orElse(sp.playlists.items.get(0));
//                if (item == null) item = sp.playlists.items.get(0);
                log.info("FINAL PLAYLIST: " + item.name +
                        " FINAL OWNER: " + item.owner.display_name +
                        " FINAL URL: " + item.external_urls.spotify);
                link = item.external_urls.spotify;
                break;
            default:
                log.info("default");
                break;
        }
        return link;
    }

    public static void createCredFile() {
        SpotifyCredentials sc = new SpotifyCredentials();
        sc.setClientId("ClientId");
        sc.setClientSecret("ClientSecret");
        JsonUtils.pojoToJsonFile(sc, "spotify.json");
    }

    public static void createCredFile(String id, String secret) {
        SpotifyCredentials sc = new SpotifyCredentials();
        sc.setClientId(id);
        sc.setClientSecret(secret);
        JsonUtils.pojoToJsonFile(sc, "spotify.json");
    }

    public static String credentialsSpotify(HashMap<String, String> parameters) {
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

    public static String getClientIdHidden() {
        String hidden;
        if (client_id == null || client_id == "") {
            hidden = "empty";
        } else {
            hidden = client_id.substring(0, 4) + "----";
        }
        return hidden;
    }

    public static String getClientSecretHidden() {
        String hidden;
        if (client_secret == null || client_secret == "") {
            hidden = "empty";
        } else {
            hidden = client_secret.substring(0, 4) + "----";
        }
        return hidden;
    }

    public static void getPlayerState() {
//        curl --request GET \
//        --url https://api.spotify.com/v1/me/player \
//        --header 'Authorization: Bearer 1POdFZRZbvb...qqillRxMr2z'
        String json = request("https://api.spotify.com/v1/me/player");
        log.info("PLAYER STATE JSON: " + json);
//        ResponseState responseState = JsonUtils.jsonToPojo(json, ResponseState.class);
//        log.info("PLAYER STATE POJO: " + responseState);
        log.info("is_playing: " + JsonUtils.jsonGetValue(json, "is_playing"));
        log.info("currently_playing_type: " + JsonUtils.jsonGetValue(json, "currently_playing_type"));
        log.info("is_playing: " + JsonUtils.jsonGetValue(json, "is_playing"));
        log.info("is_playing: " + JsonUtils.jsonGetValue(json, "is_playing"));
    }
}