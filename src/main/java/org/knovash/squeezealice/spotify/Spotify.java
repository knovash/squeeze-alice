package org.knovash.squeezealice.spotify;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.lms.PlayerStatus.PlaylistLoop;
import org.knovash.squeezealice.spotify.spotify_pojo.*;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_artists.SpotifyArtists;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;

import java.util.UUID;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.Main.hive;
import static org.knovash.squeezealice.spotify.SpotifyRequests.requestWithRefreshGet;

@Log4j2
@Data
public class Spotify {

    public static PlayerState playerState = new PlayerState();
    public static CurrentlyPlaying currentlyPlaying = new CurrentlyPlaying();
    public static Boolean active = false;
    public static String lastPath;
    public static String lastTitle;

    public static String me() {
        log.info("SPOTIFY INFO ME");
        String uri = "https://api.spotify.com/v1/me";
        log.info("URI: " + uri);
        String body = SpotifyRequests.requestGet(uri);
        log.info("SPOTY ME BODY: " + body);
        return body;
    }

    public static String getLinkArtist(String target) {
        log.info("ARTIST: " + target);
        target = target.replace(" ", "%20");
        String url = "https://api.spotify.com/v1/search?q=" + target + "&type=" + "artist" + "&limit=" + "3" + "&market=ES";
        String json = requestWithRefreshGet(url);
        if (json == null) return null;
        json = json.replace("\\\"", ""); //  фикс для такого "name" : "All versions of Nine inch nails \"Closer\"",
        SpotifyArtists spotifyArtists = JsonUtils.jsonToPojo(json, SpotifyArtists.class);
        String uri = spotifyArtists.artists.items.get(0).uri;
        SwitchVoiceCommand.artist = spotifyArtists.artists.items.get(0).name;
        log.info("ARTIST URI: " + uri);
        return uri;
    }

    public static String getLinkTrack(String target) {
        log.info("TRACK: " + target);
        target = target.replace(" ", "%20");
        String uri = "https://api.spotify.com/v1/search?q=" + target + "&type=" + "track" + "&limit=" + "5" + "&market=ES";
        String json = requestWithRefreshGet(uri);
        if (json == null) return null;
        json = json.replace("\\\"", ""); //  фикс для такого "name" : "All versions of Nine inch nails \"Closer\"",
        SpotifySearchTrack spotifySearchTrack = JsonUtils.jsonToPojo(json, SpotifySearchTrack.class);
        spotifySearchTrack.tracks.items.forEach(it -> log.info("TRACK: " + it.artists.get(0).name + " - " + it.name));
        String link = spotifySearchTrack.tracks.items.get(0).uri;
        SwitchVoiceCommand.artist = spotifySearchTrack.tracks.items.get(0).artists.get(0).name;
        SwitchVoiceCommand.track = spotifySearchTrack.tracks.items.get(0).name;
        log.info("ARTIST: " + SwitchVoiceCommand.artist);
        log.info("TRACK: " + SwitchVoiceCommand.track);
        log.info("URI: " + link);
        return link;
    }

    public static String getLinkAlbum(String target) {
        log.info("ALBUM TARGET: " + target);
        target = target.replace(" ", "%20");
        String uri = "https://api.spotify.com/v1/search?q=" + target + "&type=" + "album" + "&limit=" + "5" + "&market=ES";
        String json = requestWithRefreshGet(uri);
        if (json == null) return null;
        json = json.replace("\\\"", ""); //  фикс для такого "name" : "All versions of Nine inch nails \"Closer\"",
        SpotifySearchAlbum spotifySearchAlbum = JsonUtils.jsonToPojo(json, SpotifySearchAlbum.class);
        spotifySearchAlbum.albums.items.forEach(it -> log.info("ALBUM: " + it.artists.get(0).name + " - " + it.name));
        String link = spotifySearchAlbum.albums.items.get(0).uri;
        SwitchVoiceCommand.artist = spotifySearchAlbum.albums.items.get(0).artists.get(0).name;
        SwitchVoiceCommand.album = spotifySearchAlbum.albums.items.get(0).name;
        log.info("ARTIST: " + SwitchVoiceCommand.artist);
        log.info("ALBUM: " + SwitchVoiceCommand.album);
        log.info("URI: " + link);
        return link;
    }

//    public static String getClientIdHidden() {
//        if (SpotifyAuth.client_id == null || SpotifyAuth.client_id == "") return "empty";
//        return SpotifyAuth.client_id.substring(0, 4) + "----";
//    }
//
//    public static String getClientSecretHidden() {
//        if (SpotifyAuth.client_secret == null || SpotifyAuth.client_secret == "") return "empty";
//        return SpotifyAuth.client_secret.substring(0, 4) + "----";
//    }

    public static void requestCurrentlyPlaying() {
        String uri = "https://api.spotify.com/v1/me/player/currently-playing";
        log.info("SPOTIFY REQUEST CURRENTLY PLAYING " + uri);
//        log.info("REQUEST TRY");
        String json = requestWithRefreshGet(uri);
        log.info("REQUEST OK");
        log.info("REQUEST JSON " + json);
        if (json == null) {
            log.info("JSON NULL RETURN");
            currentlyPlaying = null;
            return;
        }
        log.info("REPLACE TRY");
        json = json.replace("\\\"", ""); //  фикс для такого "name" : "All versions of Nine inch nails \"Closer\"",
        log.info("REPLACE OK");
        currentlyPlaying = JsonUtils.jsonToPojo(json, CurrentlyPlaying.class);
        log.info("CURRENT OK");
    }

    public static String getNameById(String id) {
        log.info("ID: " + id);
        String name = "";
        if (id.contains("album")) {
            log.info("ALBUM");
            id = id.replace("spotify:album:", "");
            String uri = "https://api.spotify.com/v1/albums/" + id + "?fields=name,artists.name";
            log.info("URI: " + uri);
            String json = requestWithRefreshGet(uri);
            log.info("JSON: " + json);
            if (json == null) return null;
            AlbumsArtistTitle albumsArtistTitle = JsonUtils.jsonToPojo(json, AlbumsArtistTitle.class);
            String artistName = albumsArtistTitle.artists.get(0).name;
            String albumName = albumsArtistTitle.name;
            name = artistName + " - " + albumName;
        }
        if (id.contains("artist")) {
            log.info("ARTIST");
            id = id.replace("spotify:artist:", "");
            String uri = "https://api.spotify.com/v1/artists/" + id + "?fields=name";
            log.info("URI: " + uri);
            String json = requestWithRefreshGet(uri);
            log.info("JSON: " + json);
            if (json == null) return null;
            name = JsonUtils.jsonGetValue(json, "name");
        }
        if (id.contains("playlist")) {
            log.info("PLAYLIST");
            id = id.replace("spotify:playlist:", "");
            String uri = "https://api.spotify.com/v1/playlists/" + id + "?fields=name";
            log.info("URI: " + uri);
            String json = requestWithRefreshGet(uri);
            log.info("JSON: " + json);
            if (json == null) return null;
            name = JsonUtils.jsonGetValue(json, "name");
        }
        log.info("NAME: " + name);
        return name;
    }

    public static void pause() {
        String uri = "https://api.spotify.com/v1/me/player/pause";
        log.info("SPOTIFY PAUSE " + uri);
        SpotifyRequests.requestWithRetryPut(uri);
    }

    public static void play() {
        String uri = "https://api.spotify.com/v1/me/player/play";
        log.info("SPOTIFY PLAY " + uri);
        SpotifyRequests.requestWithRetryPut(uri);
    }

    public static void next() {
        String uri = "https://api.spotify.com/v1/me/player/next";
        log.info("SPOTIFY PLAY " + uri);
        SpotifyRequests.requestWithRetryPost(uri);
    }

    public static void prev() {
        String uri = "https://api.spotify.com/v1/me/player/previous";
        log.info("SPOTIFY PLAY " + uri);
        SpotifyRequests.requestWithRetryPost(uri);
    }

    public static void volumeSetAbs(String value) {
        log.info("SPOTIFY VOLUME SET ABSOLUTE: " + value);
        String uri = "https://api.spotify.com/v1/me/player/volume?volume_percent=" + value;
        log.info("URI: " + uri);
        String body = SpotifyRequests.requestPutHttpClient(uri);
        log.info("SPOTY VOLUME BODY: " + body);
        log.info("FINISH\n");
    }

    public static void volumeRelOrAbs(String value) {
        log.info("SPOTIFY VOLUME SET RELATIVE: " + value);

        if (value.contains("-") || value.contains("+")) {
            int current = playerState.device.volume_percent;
            log.info("SPOTIFY VOLUME CURRENT: " + value);
            if (value.contains("-")) {
                int delta = Integer.parseInt(value.replace("-", ""));
                log.info("SPOTIFY VOLUME DELTA: " + delta);
                value = String.valueOf(current - delta);
                if (current - delta < 1) value = "1";
            }
            if (value.contains("+")) {
                int delta = Integer.parseInt(value.replace("+", ""));
                log.info("SPOTIFY VOLUME DELTA: " + delta);
                value = String.valueOf(current + delta);
                if (current + delta > 100) value = "100";
            }
        }

        String uri = "https://api.spotify.com/v1/me/player/volume?volume_percent=" + value;
        log.info("URI: " + uri);
        String body = SpotifyRequests.requestPutHttpClient(uri);
        log.info("SPOTY VOLUME BODY: " + body);
        log.info("FINISH\n");
    }

    public static void volumeGeneral(String value, Boolean relative) {
        log.info("PLAYER NOT PLAYING");
//        if (Spotify.ifPlaying()) {
        log.info("SPOTIFY IF PLAYING");
        if (relative != null && relative.equals(true)) {
            log.info("VOLUME rel: " + value);
            if (value.contains("-")) {
                Spotify.volumeRelOrAbs(value);
            } else {
                Spotify.volumeRelOrAbs("+" + value);
            }
        }
        if (relative != null && relative.equals(false)) {
            log.info("VOLUME abs: " + value);
            Spotify.volumeRelOrAbs(value);
        } else {
            log.info("PLAYER PLAYING. SPOTY VOLUME SKIP");
        }
//        }
    }

    public static PlayerState requestPlayerState() {
        String uri = "https://api.spotify.com/v1/me/player/";
        log.info("PLAYER STATE " + uri);
        String json = requestWithRefreshGet(uri);
        log.info(json);
        if (json == null) return null;
        json = json.replace("\\\"", ""); //  фикс для такого "name" : "All versions of Nine inch nails \"Closer\"",
        PlayerState playerState = JsonUtils.jsonToPojo(json, PlayerState.class);
        if (playerState == null) return null;
        log.info("IS PLAYING: " + playerState.is_playing + "VOLUME: " + playerState.device.volume_percent + "\n");
        return playerState;
    }

    public static Boolean ifPlaying() {
//        log.info("SPOTIFY IF PLAYING");
        playerState = requestPlayerState();
        if (playerState == null) {
            log.info("SPOTIFY NOT PLAYING");
            return false;
        }
        log.info("SPOTIFY IS PLAYING");
        return playerState.is_playing;
    }

    public static String getCurrentTitle() {
        log.info("SPOTY GET CURRENT TITLE");
        requestCurrentlyPlaying();
        if (currentlyPlaying == null) return null;
        if (!currentlyPlaying.is_playing) return null;
        String id;
        String name = "";
        String json;
        String uri = currentlyPlaying.context.uri;
        String type = currentlyPlaying.context.type;
        log.info("SPOTY REQUEST BY TYPE: " + type + " URI: " + uri);
        if (type.equals("playlist")) {
            id = uri.replaceAll("spotify.*:", "");
            log.info("SPOTY ID: " + id);
            uri = "https://api.spotify.com/v1/playlists/" + id;
            log.info("SPOTY REQUEST " + uri);
            json = requestWithRefreshGet(uri);
            if (json == null) return null;
            json = json.replace("\\\"", ""); //  фикс для такого "name"
            PlayerPlaylist playerPlaylist = JsonUtils.jsonToPojo(json, PlayerPlaylist.class);
            name = playerPlaylist.name;
        }
        if (type.equals("artist")) {
//            id = uri.replaceAll("spotify:artist:", "");
            id = uri.replaceAll("spotify.*:", "");
            log.info("SPOTY ID: " + id);
            uri = "https://api.spotify.com/v1/artists/" + id;
            log.info("SPOTY REQUEST " + uri);
            json = requestWithRefreshGet(uri);
            if (json == null) return null;
            json = json.replace("\\\"", ""); //  фикс для такого "name"
            PlayerArtist playerArtist = JsonUtils.jsonToPojo(json, PlayerArtist.class);
            name = playerArtist.name;
        }
        if (type.equals("album")) {
//            id = uri.replaceAll("spotify:album:", "");
            id = uri.replaceAll("spotify.*:", "");
            log.info("SPOTY ID: " + id);
            uri = "https://api.spotify.com/v1/albums/" + id;
            log.info("SPOTY REQUEST " + uri);
            json = requestWithRefreshGet(uri);
            if (json == null) return null;
            json = json.replace("\\\"", ""); //  фикс для такого "name"
            PlayerAlbum playerAlbum = JsonUtils.jsonToPojo(json, PlayerAlbum.class);
            name = playerAlbum.name;
        }
        log.info("SPOTY TITLE: " + name);
        return name;
    }

    public static Boolean transfer(Player player) {
        log.info("SPOTIFY START TRANSFER TO " + player.name);
        if (config.spotifyToken == null) {
            log.info("ERROR SPOTIFY TOKEN NULL");
            return false;
        }
        requestCurrentlyPlaying();
        log.info("currentlyPlaying: " + currentlyPlaying);
        if (currentlyPlaying == null || !currentlyPlaying.is_playing) {
            log.info("SPOTIFY NOT PLAY. STOP TRANSFER");
            return false;
        }

        log.info("PLAY OK " + currentlyPlaying + " " + currentlyPlaying.is_playing);
        String name;
        String playingUri;
        if (currentlyPlaying.context == null) {
            log.info("CONTEXT NULL");
            log.info(" TITLE: " + currentlyPlaying.item.name);
            log.info("RUN TRANSFER. TYPE: " + currentlyPlaying.item.type);
            name = currentlyPlaying.item.name;
            playingUri = currentlyPlaying.item.uri;
        } else {
            log.info("CONTEXT OK");
            log.info(" TITLE: " + currentlyPlaying.item.name);
            log.info("RUN TRANSFER. TYPE: " + currentlyPlaying.context.type);
            log.info(" TRACK NUMBER: " + currentlyPlaying.item.track_number);
            name = currentlyPlaying.item.name;
            playingUri = currentlyPlaying.context.uri;
        }
        lastPath = playingUri;
        lastTitle = Spotify.getNameById(lastPath);
        log.info("LAST PATH: " + lastPath);
        log.info("LAST TITLE: " + lastTitle);
        player
                .ifExpiredAndNotPlayingUnsyncWakeSet(null) // transfer
                .playPath(playingUri)
                .waitFor(1000)
                .pause();
        Integer index = currentlyPlaying.item.track_number - 1;
        log.info("TYPE: " + currentlyPlaying.context.type);
        if (!currentlyPlaying.context.type.equals("album")) {
            log.info("PLAYER STATUS FOR PLAYLIST");
            player.waitFor(3000);
//            player.status(); //transfer
//        Player.playerStatus.result.playlist_loop.stream()
//                .forEach(playlistLoop -> log.info("-----" + playlistLoop.playlist_index + " " + playlistLoop.title + " = " + name));
            log.info("FILTER INDEX BY NAME: " + name);
            log.info("LOOP: " + player.playerStatus.result.playlist_loop);
            if (player.playerStatus.result.playlist_loop != null) {
                PlaylistLoop playlistLoop = player.playerStatus.result.playlist_loop.stream()
                        .peek(pl -> log.info("ALL: " + pl.playlist_index + " " + pl.title + " = " + name))
                        .filter(pl -> pl.title.equals(name))
                        .peek(pl -> log.info("FILTER: " + pl.playlist_index + " " + pl.title + " = " + name))
                        .findFirst()
                        .orElse(null);
                index = 0;
                if (playlistLoop != null) index = playlistLoop.playlist_index;
            }
        }
        log.info("INDEX: " + index + " TITLE: " + name);
        player.playTrackNumber(String.valueOf(index));
        player.syncAllOtherPlayingToThis();
        Utils.sleep(5);
        Spotify.pause();
        return true;
    }

    public static void ifExpiredRunRefersh() {
        long timeNow = System.currentTimeMillis();
        long expiresAt = config.spotifyTokenExpiresAt;
        boolean result = timeNow > expiresAt;
        log.info("EXPIRES AT: " + config.spotifyTokenExpiresAt + " TIME NOW: " + timeNow + " EXPIRED : " + result);
        if (result) {
            log.info("\nSPOTIFY TOKEN EXPIRED. REQUEST REFRESH TOKEN");
            requestRefreshToken();
        }
    }

    public static void requestRefreshToken() {
        log.info("SPOTIFY REQUEST REFRESH TOKEN");
        String sessionId = UUID.randomUUID().toString();
        hive.publishAndWaitForResponse("from_local_request", null, 10, "token_spotify_refresh", sessionId, config.spotifyRefreshToken);
    }
}