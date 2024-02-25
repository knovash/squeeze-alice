package org.knovash.squeezealice.spotify;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.spotify.spotify_pojo.Type;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_albums.SpotifyAlbums;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_artists.SpotifyArtists;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_playlist.Item;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_playlist.SpotifyPlaylists;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_tracks.SpotifyResponseTracks;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.knovash.squeezealice.Main.*;

@Log4j2
@Data
public class Spotify {

    public static String getLink(String target, Type type) {
        log.info("TARGET: " + target);
        log.info("TYPE: " + type);
        String link = null;
        String limit = "10";
        target = target.replace(" ", "%20");
        String uri = "https://api.spotify.com/v1/search?q=" + target + "&type=" + type + "&limit=" + limit;
        log.info("URI: " + uri);
        String json = SpotifyRequests.requestForLinkJson(uri);
        if (json == null) SpotifyAuth.requestRefresh();
        json = SpotifyRequests.requestForLinkJson(uri);
        if (json == null) return null;
        json = json.replace("\\\"", ""); //  фикс для такого "name" : "All versions of Nine inch nails \"Closer\"",
        switch (type.toString()) {
            case ("album"):
                log.info("ALBUM");
                SpotifyAlbums spotifyAlbums;
                spotifyAlbums = JsonUtils.jsonToPojo(json, SpotifyAlbums.class);
                spotifyAlbums.getAlbums().getItems().stream().forEach(item -> log.info(item));
                link = spotifyAlbums.getAlbums().items.get(0).external_urls.spotify.toString();
                break;
            case ("track"):
                log.info("TRACK");
                SpotifyResponseTracks spotifyResponseTracks;
                spotifyResponseTracks = JsonUtils.jsonToPojo(json, SpotifyResponseTracks.class);
                link = spotifyResponseTracks.tracks.items.get(0).external_urls.spotify.toString();
                break;
            case ("artist"):
                log.info("ARTIST");
                SpotifyArtists spotifyArtists;
                spotifyArtists = JsonUtils.jsonToPojo(json, SpotifyArtists.class);
                link = spotifyArtists.artists.items.get(0).external_urls.spotify.toString();
                break;
            case ("playlist"):
                log.info("PLAYLIST");
                SpotifyPlaylists spotifyPlaylists;
                spotifyPlaylists = JsonUtils.jsonToPojo(json, SpotifyPlaylists.class);
                Item item = spotifyPlaylists.playlists.items
                        .stream()
                        .peek(it -> log.info("PLAYLIST: " + it.name + " OWNER: " + it.owner.display_name))
                        .filter(it -> it.name.contains("This Is"))
                        .filter(it -> it.owner.display_name.contains("Spotify"))
                        .findFirst()
                        .orElse(spotifyPlaylists.playlists.items.get(0));
                log.info("FINAL PLAYLIST: " + item.name + " OWNER: " + item.owner.display_name);
                link = item.external_urls.spotify;
                break;
            default:
                log.info("ERROR");
                break;
        }
        return link;
    }

    public static String getClientIdHidden() {
        if (SpotifyAuth.client_id == null || SpotifyAuth.client_id == "") return "empty";
        return SpotifyAuth.client_id.substring(0, 4) + "----";
    }

    public static String getClientSecretHidden() {
        if (SpotifyAuth.client_secret == null || SpotifyAuth.client_secret == "") return "empty";
        return SpotifyAuth.client_secret.substring(0, 4) + "----";
    }

    public static String getPlayerState() { // получить состояние плеера спотифай
//  https://unicorn-neutral-badly.ngrok-free.app/cmd?action=spoti_state
//  https://developer.spotify.com/documentation/web-api/reference/get-information-about-the-users-current-playback
        String uri = "https://api.spotify.com/v1/me/player";
        Header[] headers = {
                new BasicHeader("Authorization", SpotifyAuth.bearer_token)
        };
        String responseBody = SpotifyRequests.requestHttpClient(uri, headers);
        if (responseBody.equals("401")) { // no auth - try  get refresh token
//            SpotifyAuth.runRequestRefresh();
            responseBody = SpotifyRequests.requestHttpClient(uri, headers);
        }
        log.info("PLAYER STATE CONT: " + responseBody);
        return responseBody;
    }

    public static String transfer(Player player) {
        log.info("TRANSFER START " + player.name + " " + player.mac);
        lmsPlayers.updateMac();
        String mac = player.mac;
        mac = mac.replace(":", "%3A");
        log.info("MAC " + player.name + " " + mac);
        String url = "http://" + lmsIP + ":" + lmsPort + "/plugins/spotty/index.html?index=10.1&" + "player=" + mac + "&sess=";
        requestHttpUrlConnectionGet("GET", url);
        log.info("TRANSFER FINISH");
        return "переключаю spotify на " + player.name;
    }

    public static void requestHttpUrlConnectionGet(String method, String url) {
        log.info("METHOD: " + method + " URL: " + url);
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod(method);
            con.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("FINISH");
    }
}