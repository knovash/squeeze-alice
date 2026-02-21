package org.knovash.squeezealice.spotify;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.lms.PlayerStatus.PlaylistLoop;
import org.knovash.squeezealice.spotify.spotify_pojo.*;
import org.knovash.squeezealice.spotify.spotify_pojo.spotify_artists.SpotifyArtists;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Log4j2
public class Spotify {

    // Статические поля оставлены для совместимости (но их использование внутри класса сведено к минимуму)
    public static PlayerState playerState = new PlayerState();
    public static CurrentlyPlaying currentlyPlaying = new CurrentlyPlaying();
    public static Boolean active = false;

    // ------- МЕТОД, КОТОРЫЙ НЕ ИЗМЕНЯТЬ (me) -------

    public static String me() { // нужен только для получения имени пользователя показать на web
        log.info("SPOTIFY INFO ME");
        String uri = "https://api.spotify.com/v1/me";
        log.info("URI: " + uri);
        String body = SpotifyRequests.requestGet(uri);  // использует старый requestGet
        log.info("SPOTY ME BODY: " + body);
        return body;
    }

    // ------- МЕТОДЫ ПОИСКА (НЕ ИЗМЕНЕНЫ) -------

    public static String getLinkArtist(String target) {
        log.info("ARTIST: " + target);
        try {
            String encodedQuery = URLEncoder.encode(target, StandardCharsets.UTF_8);
            String url = "https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=artist&limit=3&market=ES";
            String json = SpotifyRequests.requestGet(url);
            if (json == null) return null;
            json = json.replace("\\\"", ""); // Осторожно: может повредить JSON
            SpotifyArtists spotifyArtists = JsonUtils.jsonToPojo(json, SpotifyArtists.class);
            if (spotifyArtists.artists.items.isEmpty()) {
                log.warn("No artists found for: " + target);
                return null;
            }
            String uri = spotifyArtists.artists.items.get(0).uri;
            SwitchVoiceCommand.artist = spotifyArtists.artists.items.get(0).name;
            log.info("ARTIST URI: " + uri);
            return uri;
        } catch (Exception e) {
            log.error("Encoding error", e);
            return null;
        }
    }

    public static String getLinkTrack(String target) {
        log.info("TRACK: " + target);
        try {
            String encodedQuery = URLEncoder.encode(target, StandardCharsets.UTF_8);
            String url = "https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=track&limit=5&market=ES";
            String json = SpotifyRequests.requestGet(url);
            if (json == null) return null;
            json = json.replace("\\\"", "");
            SpotifySearchTrack spotifySearchTrack = JsonUtils.jsonToPojo(json, SpotifySearchTrack.class);
            if (spotifySearchTrack.tracks.items.isEmpty()) {
                log.warn("No tracks found for: " + target);
                return null;
            }
            spotifySearchTrack.tracks.items.forEach(it -> log.info("TRACK: " + it.artists.get(0).name + " - " + it.name));
            String link = spotifySearchTrack.tracks.items.get(0).uri;
            SwitchVoiceCommand.artist = spotifySearchTrack.tracks.items.get(0).artists.get(0).name;
            SwitchVoiceCommand.track = spotifySearchTrack.tracks.items.get(0).name;
            log.info("ARTIST: " + SwitchVoiceCommand.artist);
            log.info("TRACK: " + SwitchVoiceCommand.track);
            log.info("URI: " + link);
            return link;
        } catch (Exception e) {
            log.error("Encoding error", e);
            return null;
        }
    }

    public static String getLinkAlbum(String target) {
        log.info("ALBUM TARGET: " + target);
        try {
            String encodedQuery = URLEncoder.encode(target, StandardCharsets.UTF_8);
            String url = "https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=album&limit=5&market=ES";
            String json = SpotifyRequests.requestGet(url);
            if (json == null) return null;
            json = json.replace("\\\"", "");
            SpotifySearchAlbum spotifySearchAlbum = JsonUtils.jsonToPojo(json, SpotifySearchAlbum.class);
            if (spotifySearchAlbum.albums.items.isEmpty()) {
                log.warn("No albums found for: " + target);
                return null;
            }
            spotifySearchAlbum.albums.items.forEach(it -> log.info("ALBUM: " + it.artists.get(0).name + " - " + it.name));
            String link = spotifySearchAlbum.albums.items.get(0).uri;
            SwitchVoiceCommand.artist = spotifySearchAlbum.albums.items.get(0).artists.get(0).name;
            SwitchVoiceCommand.album = spotifySearchAlbum.albums.items.get(0).name;
            log.info("ARTIST: " + SwitchVoiceCommand.artist);
            log.info("ALBUM: " + SwitchVoiceCommand.album);
            log.info("URI: " + link);
            return link;
        } catch (Exception e) {
            log.error("Encoding error", e);
            return null;
        }
    }

    public static String getLinkPlaylist(String target) {
        log.info("PLAYLIST TARGET: " + target);
        try {
            String encodedQuery = URLEncoder.encode(target, StandardCharsets.UTF_8);
            // Исправлен тип на playlist
            String url = "https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=playlist&limit=5&market=ES";
            String json = SpotifyRequests.requestGet(url);
            if (json == null) return null;
            json = json.replace("\\\"", "");
            // Используем новый класс для плейлистов
            SpotifySearchPlaylist spotifySearchPlaylist = JsonUtils.jsonToPojo(json, SpotifySearchPlaylist.class);
            if (spotifySearchPlaylist.playlists.items.isEmpty()) {
                log.warn("No playlists found for: " + target);
                return null;
            }
            spotifySearchPlaylist.playlists.items.forEach(it ->
                    log.info("PLAYLIST: " + it.owner.display_name + " - " + it.name));
            String link = spotifySearchPlaylist.playlists.items.get(0).uri;
            SwitchVoiceCommand.artist = spotifySearchPlaylist.playlists.items.get(0).owner.display_name;
            SwitchVoiceCommand.playlist = spotifySearchPlaylist.playlists.items.get(0).name;
            log.info("OWNER: " + SwitchVoiceCommand.artist);
            log.info("PLAYLIST: " + SwitchVoiceCommand.playlist);
            log.info("URI: " + link);
            return link;
        } catch (Exception e) {
            log.error("Encoding error", e);
            return null;
        }
    }

    // ------- ОПТИМИЗИРОВАННЫЕ МЕТОДЫ -------

    /**
     * Получить информацию о текущем треке.
     * @return объект CurrentlyPlaying или null
     */
    public static CurrentlyPlaying getCurrentlyPlaying() {
        String json = SpotifyRequests.requestGet("https://api.spotify.com/v1/me/player/currently-playing");
        if (json == null) return null;
        json = json.replace("\\\"", "");
        CurrentlyPlaying cp = JsonUtils.jsonToPojo(json, CurrentlyPlaying.class);
        currentlyPlaying = cp; // для совместимости
        return cp;
    }

    // Для обратной совместимости
    public static String currentlyPlaying() {
        CurrentlyPlaying cp = getCurrentlyPlaying();
        if (cp == null) return "ERROR";
        log.info("Spotify is playing: {}", cp.is_playing);
        return JsonUtils.pojoToJson(cp);
    }

    public static String currentlyPlayingDetails() {
        CurrentlyPlaying cp = getCurrentlyPlaying();
        if (cp == null || cp.item == null) return "Nothing playing";
        String albumName = cp.item.album.name;
        String artistName = cp.item.artists.get(0).name;
        String artistType = cp.item.artists.get(0).type;
        String playingType = cp.currently_playing_type;
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
    public static boolean transfer(Player player) {
        log.info("Starting transfer to player {}", player.name);

        CurrentlyPlaying playing = getCurrentlyPlaying();
        if (playing == null || !playing.is_playing) {
            log.info("Spotify is not playing, transfer aborted");
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
                .ifExpiredAndNotPlayingUnsyncWakeSet(null)
                .playPath(playingUri)
                .waitFor(1000)
                .pause();

        if (playing.context != null && "playlist".equals(playing.context.type)) {
            player.waitFor(3000);
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
        player.syncAllOtherPlayingToThis();

        Utils.sleep(5);
        pause();

        log.info("Transfer completed");
        return true;
    }
}