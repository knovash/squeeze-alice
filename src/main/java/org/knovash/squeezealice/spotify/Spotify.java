package org.knovash.squeezealice.spotify;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.spotify.spotify_pojo.CurrentlyPlaying;
import org.knovash.squeezealice.spotify.spotify_pojo.PlayerArtist;
import org.knovash.squeezealice.spotify.spotify_pojo.PlayerPlaylist;
import org.knovash.squeezealice.spotify.spotify_pojo.Type;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_albums.SpotifyAlbums;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_artists.SpotifyArtists;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_playlist.Item;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_playlist.SpotifyPlaylists;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_tracks.SpotifyResponseTracks;
import org.knovash.squeezealice.utils.JsonUtils;

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

        String json = getJson(uri);

//        String json = SpotifyRequests.requestForLinkJson(uri);
//        if (json == null) SpotifyAuth.requestRefresh();
//        json = SpotifyRequests.requestForLinkJson(uri);

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

    public static CurrentlyPlaying getCurrentlyPlaying() {
        String uri = "https://api.spotify.com/v1/me/player/currently-playing";
        log.info("URI: " + uri);
        String body = SpotifyRequests.requestHttpClient(uri);
//        log.info("BODY: " + body);
//        body = body.replace("\\\"", ""); //  ф
        if (body.contains("204")) return null;
        String json = getJson(uri);
//        String json = SpotifyRequests.requestForLinkJson(uri);
//        if (json == null) SpotifyAuth.requestRefresh();
//        json = SpotifyRequests.requestForLinkJson(uri);
        if (json == null) return null;
//        log.info("JSON: " + json);
        json = json.replace("\\\"", ""); //  фикс для такого "name" : "All versions of Nine inch nails \"Closer\"",
        CurrentlyPlaying currentlyPlaying = JsonUtils.jsonToPojo(json, CurrentlyPlaying.class);
        log.info("CURRENTLY PLAYING: " + currentlyPlaying);
        return currentlyPlaying;
    }

    public static String getCurrentName(CurrentlyPlaying currentlyPlaying) {
        String id;
        String name = "";
        String json;
        String uri = currentlyPlaying.context.uri;
        String type = currentlyPlaying.context.type;
//      "uri": "spotify:playlist:37i9dQZF1DWTvNyxOwkztu"
//      "uri": "spotify:artist:5K4W6rqBFWDnAN6FQUkS6x"
        if (type.equals("playlist")) {
            id = uri.replaceAll("spotify:playlist:", "");
            uri = "https://api.spotify.com/v1/playlists/" + id;
            json = getJson(uri);
            json = json.replace("\\\"", ""); //  фикс для такого "name"
            PlayerPlaylist playerPlaylist = JsonUtils.jsonToPojo(json, PlayerPlaylist.class);
            name = playerPlaylist.name;
        }
        if (type.equals("artist")) {
            id = uri.replaceAll("spotify:artist:", "");
            uri = "https://api.spotify.com/v1/artists/" + id;
            json = getJson(uri);
            json = json.replace("\\\"", ""); //  фикс для такого "name"
            PlayerArtist playerArtist = JsonUtils.jsonToPojo(json, PlayerArtist.class);
            name = playerArtist.name;
        }
        return name;
    }

    public static String getJson(String uri) {
        String json = SpotifyRequests.requestForLinkJson(uri);
        if (json == null) SpotifyAuth.requestRefresh();
        json = SpotifyRequests.requestForLinkJson(uri);
        if (json == null) return null;
        return json;
    }

    public static String transfer(Player player) {
        log.info("TRANSFER START " + player.name);
        CurrentlyPlaying currentlyPlaying = getCurrentlyPlaying();
        if (currentlyPlaying == null) return "Spotifi не играет";
        log.info("CURRENT TYPE: " + currentlyPlaying.context.type);
        log.info("CURRENT URI: " + currentlyPlaying.context.uri);
        String playingUri = currentlyPlaying.context.uri;
//        String name = getCurrentName(currentlyPlaying);
//        log.info("CURRENT NAME: " + name);
        player.playPath(playingUri);
        player.syncAllOtherPlayingToThis();
        log.info("TRANSFER FINISH");
        return "переключаю spotify на " + player.name;
    }
}