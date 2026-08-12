package org.knovash.squeezealice.spotify;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.lms.PlayerStatus.PlaylistLoop;
import org.knovash.squeezealice.player.ActionsSync;
import org.knovash.squeezealice.spotify.spotify_pojo.*;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_artists.SpotifyArtists;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class Spotify {

    // Статические поля оставлены для совместимости (но их использование внутри класса сведено к минимуму)
    public static PlayerState playerState = new PlayerState();
    public static CurrentlyPlaying currentlyPlaying = new CurrentlyPlaying();
    public static Boolean active = false;
    //public static String currentGetName;

    // ------- МЕТОД, КОТОРЫЙ НЕ ИЗМЕНЯТЬ (me) -------

    public static String me() {
        log.info("SPOTIFY INFO START");
        if (config.spotifyToken == null || config.spotifyToken.isEmpty()) {
            log.info("SPOTIFY NOT LOGGED IN");
            return null;
        }
        String uri = "https://api.spotify.com/v1/me";
        log.info("URI: " + uri);
        String body = SpotifyRequests.requestGet(uri);
        log.info("SPOTY ME BODY: " + body);
        return body;
    }

    // ------- МЕТОДЫ ПОИСКА (НЕ ИЗМЕНЕНЫ) -------
    public static class GetLinkResult {

        public final String uri;
        public final String name;

        public GetLinkResult(String uri, String name) {
            this.uri = uri;
            this.name = name;
        }
    }


    public static GetLinkResult getLinkArtist(String target) {
        log.debug("SPOTIFY GET LINK FOR TARGET: " + target);
        String encodedQuery = URLEncoder.encode(target, StandardCharsets.UTF_8);
        String type = "artist";
        String url = "https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=" + type + "&limit=3&market=ES";
        String json = SpotifyRequests.requestGet(url);
        if (json == null) return null;
        json = json.replace("\\\"", ""); // Осторожно: может повредить JSON
        SpotifyArtists result = JsonUtils.jsonToPojo(json, SpotifyArtists.class);
        if (result == null ||
                result.artists == null ||
                result.artists.items == null ||
                result.artists.items.isEmpty())
            return new GetLinkResult(null, null);
        String uri = result.artists.items.get(0).uri;
        String name = result.artists.items.get(0).name;
        return new GetLinkResult(uri, name);
    }

    public static GetLinkResult getLinkTrack(String target) {
        log.debug("SPOTIFY GET LINK FOR TARGET: " + target);
        String encodedQuery = URLEncoder.encode(target, StandardCharsets.UTF_8);
        String type = "track";
        String url = "https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=" + type + "&limit=3&market=ES";
        String json = SpotifyRequests.requestGet(url);
        if (json == null) return null;
        json = json.replace("\\\"", ""); // Осторожно: может повредить JSON
        SpotifySearchTrack result = JsonUtils.jsonToPojo(json, SpotifySearchTrack.class);
        if (result == null ||
                result.tracks == null ||
                result.tracks.items == null ||
                result.tracks.items.isEmpty())
            return null;
        String uri = result.tracks.items.get(0).uri;
        String name = result.tracks.items.get(0).artists.get(0).name + ", " + result.tracks.items.get(0).name;
        return new GetLinkResult(uri, name);
    }

    public static GetLinkResult getLinkAlbum(String target) {
        log.debug("SPOTIFY GET LINK FOR TARGET: " + target);
        String encodedQuery = URLEncoder.encode(target, StandardCharsets.UTF_8);
        String type = "album";
        String url = "https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=" + type + "&limit=3&market=ES";
        String json = SpotifyRequests.requestGet(url);
        if (json == null) return null;
        json = json.replace("\\\"", ""); // Осторожно: может повредить JSON
        SpotifySearchAlbum result = JsonUtils.jsonToPojo(json, SpotifySearchAlbum.class);
        if (result == null ||
                result.albums == null ||
                result.albums.items == null ||
                result.albums.items.isEmpty())
            return null;
        String uri = result.albums.items.get(0).uri;
        String name = result.albums.items.get(0).artists.get(0).name + ", " + result.albums.items.get(0).name;
        return new GetLinkResult(uri, name);
    }

    public static GetLinkResult getLinkPlaylist(String target) {
        log.debug("SPOTIFY GET LINK FOR TARGET: " + target);
        String encodedQuery = URLEncoder.encode(target, StandardCharsets.UTF_8);
        String type = "playlist";
        String url = "https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=" + type + "&limit=5&market=ES";
        String json = SpotifyRequests.requestGet(url);
        if (json == null) return null;
        json = json.replace("\\\"", ""); // Осторожно: может повредить JSON
        SpotifySearchPlaylist result = JsonUtils.jsonToPojo(json, SpotifySearchPlaylist.class);
        if (result == null ||
                result.playlists == null ||
                result.playlists.items == null ||
                result.playlists.items.isEmpty())
            return null;
        log.info("PLAYLISTS");
        log.info(result.playlists.items);
        String uri = result.playlists.items.get(0).uri;
        String name = result.playlists.items.get(0).name;
        return new GetLinkResult(uri, name);
    }

    public static CurrentlyPlaying getCurrentlyPlaying() {
        log.info(start);
        String json = SpotifyRequests.requestGet("https://api.spotify.com/v1/me/player/currently-playing");
        log.debug("SPOTIFY RESPONSE PLAYING: " + json);
        if (json == null) return null;
        json = json.replace("\\\"", "");
        CurrentlyPlaying cp = JsonUtils.jsonToPojo(json, CurrentlyPlaying.class);
        currentlyPlaying = cp; // для совместимости
        log.info(finish);
        return cp;
    }

    // Для обратной совместимости
    public static String currentlyPlaying() {
        log.info(start);
        log.info("CHECK SPOTIFY IS PLAYING...");
        Spotify.currentlyPlaying = null;
        CurrentlyPlaying cp = getCurrentlyPlaying();
        if (cp == null) return "ERROR";
        log.info("Spotify is playing: {}", cp.is_playing);
        Spotify.currentlyPlaying = cp;
        log.info(finish);
        return JsonUtils.pojoToJson(cp);
    }

    public static String currentlyPlayingDetails() {
        log.info(start);
        CurrentlyPlaying cp = getCurrentlyPlaying();
        if (cp == null || cp.item == null) return "Nothing playing";
        String albumName = cp.item.album.name;
        String artistName = cp.item.artists.get(0).name;
        String artistType = cp.item.artists.get(0).type;
        String playingType = cp.currently_playing_type;
        log.info(finish);
        return albumName + " - " + artistName + " - " + artistType + " - " + playingType;
    }

    // Управление воспроизведением
    public static void pause() {
        log.info("Pausing Spotify");
        SpotifyRequests.requestPut("https://api.spotify.com/v1/me/player/pause");
    }

    public static void play() {
        log.info("Resuming Spotify");
        SpotifyRequests.requestPut("https://api.spotify.com/v1/me/player/play");
    }

    public static void next() {
        log.info("Next track");
        SpotifyRequests.requestPost("https://api.spotify.com/v1/me/player/next");
    }

    public static void prev() {
        log.info("Previous track");
        SpotifyRequests.requestPost("https://api.spotify.com/v1/me/player/previous");
    }

    // Громкость
    public static void volumeUp() {
        volumeSet("+10");
    }

    public static void volumeDn() {
        volumeSet("-10");
    }

    public static void volume(String value) {
        try {
            int volume = Integer.parseInt(value);
            if (volume < 0 || volume > 100) {
                log.warn("Volume must be between 0 and 100");
                return;
            }
            String uri = "https://api.spotify.com/v1/me/player/volume?volume_percent=" + volume;
            SpotifyRequests.requestPut(uri);
        } catch (NumberFormatException e) {
            log.error("Invalid volume value: {}", value);
        }
    }

    public static void volumeSet(String value) {
        if (value == null) return;
        if (value.startsWith("+") || value.startsWith("-")) {
            PlayerState state = requestPlayerState();
            if (state == null || state.device == null) {
                log.warn("Cannot get current volume");
                return;
            }
            int current = state.device.volume_percent;
            try {
                int delta = Integer.parseInt(value);
                int newVolume = current + delta;
                if (newVolume < 1) newVolume = 1;
                if (newVolume > 100) newVolume = 100;
                volume(String.valueOf(newVolume));
            } catch (NumberFormatException e) {
                log.error("Invalid delta: {}", value);
            }
        } else {
            volume(value);
        }
    }

    // Состояние плеера
    public static PlayerState requestPlayerState() {
        String json = SpotifyRequests.requestGet("https://api.spotify.com/v1/me/player/");
        if (json == null) return null;
        json = json.replace("\\\"", "");
        PlayerState state = JsonUtils.jsonToPojo(json, PlayerState.class);
        playerState = state;
        return state;
    }

    public static Boolean requestPlayerStateBool() {
        PlayerState state = requestPlayerState();
        return state != null && state.is_playing;
    }

    // Получение имени по ID (используется в transfer)
    public static String getNameById(String id) {
        if (id == null) return "";
        String name = "";
        if (id.contains("album")) {
            String albumId = id.replace("spotify:album:", "");
            String uri = "https://api.spotify.com/v1/albums/" + albumId + "?fields=name,artists.name";
            String json = SpotifyRequests.requestGet(uri);
            if (json != null) {
                AlbumsArtistTitle album = JsonUtils.jsonToPojo(json, AlbumsArtistTitle.class);
                if (album.artists != null && !album.artists.isEmpty()) {
                    name = album.artists.get(0).name + " - " + album.name;
                }
            }
        } else if (id.contains("artist")) {
            String artistId = id.replace("spotify:artist:", "");
            String uri = "https://api.spotify.com/v1/artists/" + artistId + "?fields=name";
            String json = SpotifyRequests.requestGet(uri);
            if (json != null) {
                name = JsonUtils.jsonGetValue(json, "name");
            }
        } else if (id.contains("playlist")) {
            String playlistId = id.replace("spotify:playlist:", "");
            String uri = "https://api.spotify.com/v1/playlists/" + playlistId + "?fields=name";
            String json = SpotifyRequests.requestGet(uri);
            if (json != null) {
                name = JsonUtils.jsonGetValue(json, "name");
            }
        }
        log.info("Resolved name for {}: {}", id, name);
        return name;
    }


    // Трансфер на Squeezebox
    public static boolean transferSpotifyToLms(Player player) {
        log.info("Starting transfer to player {}", player.name);

        CurrentlyPlaying playing = getCurrentlyPlaying();
        if (playing == null) {
            log.warn("Spotify currently playing is null – nothing is playing or API error. Transfer aborted.");
            return false;
        }
        if (!playing.is_playing) {
            log.info("Spotify is paused/stopped (is_playing=false). Transfer aborted.");
            return false;
        }

        String playingUri;
        int trackIndex;

        if (playing.context == null) {
            playingUri = playing.item.uri;
            trackIndex = 0;
            log.info("Single track: {}", playing.item.name);
        } else {
            playingUri = playing.context.uri;
            trackIndex = playing.item.track_number - 1;
            log.info("Context: {} ({})", playing.context.type, playing.context.uri);
        }

        player
                .ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false)
                .playPathSpotify(playingUri)
                .waitFor(500)
                .pause();

        if (playing.context != null && "playlist".equals(playing.context.type)) {
            player.waitFor(500);
            if (player.playerStatus != null && player.playerStatus.result != null
                    && player.playerStatus.result.playlist_loop != null) {
                String targetTitle = playing.item.name;
                Optional<PlaylistLoop> match = player.playerStatus.result.playlist_loop.stream()
                        .filter(pl -> targetTitle.equals(pl.title))
                        .findFirst();
                if (match.isPresent()) {
                    trackIndex = match.get().playlist_index;
                    log.info("Found track in playlist at index {}", trackIndex);
                }
            }
        }

        player.playTrackNumber(String.valueOf(trackIndex));
        player.syncOtherPlayingNotInGroupToThis();

        Utils.sleep(5);
        pause();

        log.info("Transfer completed");
        return true;
    }

}