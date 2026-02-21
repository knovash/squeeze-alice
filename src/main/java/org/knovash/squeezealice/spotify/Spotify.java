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

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.spotify.SpotifyRequests.requestWithRefreshGet;

@Log4j2
@Data
public class Spotify {

    public static PlayerState playerState = new PlayerState();
    public static CurrentlyPlaying currentlyPlaying = new CurrentlyPlaying();
    public static Boolean active = false;
//    public static String lastPath;
//    public static String lastTitle;

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

    public static String currentlyPlaying() {
        String uri = "https://api.spotify.com/v1/me/player/currently-playing";
        log.info(uri);
        currentlyPlaying = null;
        String json = requestWithRefreshGet(uri);
        if (json == null) {
            log.info("JSON NULL RETURN");
            currentlyPlaying = null;
            return "ERROR";
        }
        log.debug(json);
        currentlyPlaying = JsonUtils.jsonToPojo(json, CurrentlyPlaying.class);
        log.info("Spotify is playing: "+currentlyPlaying.is_playing);
        String jsonReplaced = json.replace("\\\"", ""); //  фикс для такого "name" : "All versions of Nine inch nails \"Closer\"",
        currentlyPlaying = JsonUtils.jsonToPojo(jsonReplaced, CurrentlyPlaying.class);
        return json;
    }

    public static String currentlyPlayingDetails() {
        currentlyPlaying();
        String albumName = currentlyPlaying.item.album.name;
        String artistName = currentlyPlaying.item.artists.get(0).name;
        String artistType = currentlyPlaying.item.artists.get(0).type;
        String playingType = currentlyPlaying.currently_playing_type;
        return albumName + " - " + artistName + " - " + artistType + " - " + playingType;
    }


    //    только в transfer
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


    public static void volumeUp() {
        volumeSet("+10");
    }

    public static void volumeDn() {
        volumeSet("-10");
    }

    public static void volume(String value) {
        log.info("SPOTIFY VOLUME SET : " + value);
        String uri = "https://api.spotify.com/v1/me/player/volume?volume_percent=" + value;
        log.info(uri);
        SpotifyRequests.requestPutHttpClient(uri);
    }

    public static void volumeSet(String value) {
        log.info("SPOTIFY VOLUME SET : " + value);
        if (value.contains("-") || value.contains("+")) {
            PlayerState state = Spotify.requestPlayerState();
            if (state == null) {
                log.info("SPOTIFY GET STATE ERROR");
                return;
            }
            int current = state.device.volume_percent;
            log.info("SPOTIFY VOLUME CURRENT: " + current);
            if (value.contains("-")) {
                int delta = Integer.parseInt(value.replace("-", ""));
                value = String.valueOf(current - delta);
                if (current - delta < 1) value = "1";
            }
            if (value.contains("+")) {
                int delta = Integer.parseInt(value.replace("+", ""));
                value = String.valueOf(current + delta);
                if (current + delta > 100) value = "100";
            }
        }
        volume(value);
    }


    public static PlayerState requestPlayerState() {
        String uri = "https://api.spotify.com/v1/me/player/";
        log.info("PLAYER STATE " + uri);
        String json = requestWithRefreshGet(uri);
//        log.info(json);
        if (json == null) return null;
        json = json.replace("\\\"", ""); //  фикс для такого "name" : "All versions of Nine inch nails \"Closer\"",
        PlayerState playerState = JsonUtils.jsonToPojo(json, PlayerState.class);
        if (playerState == null) return null;
        log.info("IS PLAYING: " + playerState.is_playing + "VOLUME: " + playerState.device.volume_percent + "\n");
        return playerState;
    }

    public static Boolean requestPlayerStateBool() {
        playerState = requestPlayerState();
        if (playerState == null) {
            log.info("SPOTIFY NOT PLAYING");
            return false;
        }
        log.info("SPOTIFY IS PLAYING");
        return playerState.is_playing;
    }


    public static Boolean transfer(Player player) {
        log.info("SPOTIFY START TRANSFER TO " + player.name);
        if (config.spotifyToken == null) {
            log.info("ERROR SPOTIFY TOKEN NULL");
            return false;
        }
        currentlyPlaying();
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
//        lastPath = playingUri;
//        lastTitle = Spotify.getNameById(lastPath);
//        log.info("LAST PATH: " + lastPath);
//        log.info("LAST TITLE: " + lastTitle);
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


}