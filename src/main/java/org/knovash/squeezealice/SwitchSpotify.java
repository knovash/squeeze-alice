package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.spotify.spotify_pojo.PlayerState;

import java.util.HashMap;

@Log4j2
public class SwitchSpotify {

    public static Context action(Context context) {
        HashMap<String, String> queryParams = context.queryMap;
        context.bodyResponse = "\nBAD REQUEST NO ACTION IN QUERY";
        if (!queryParams.containsKey("action")) return context;
        context.code = 200;
        String action = queryParams.get("action");
        String value = queryParams.get("value");
        String response = null;

        switch (action) {
            case "play":
                Spotify.play();
                response = "Spotify play";
                break;
            case "pause":
                Spotify.pause();
                response = "Spotify pause";
                break;
            case "next":
                Spotify.next();
                response = "Spotify next track";
                break;
            case "prev":
                Spotify.prev();
                response = "Spotify previous track";
                break;
            case "volume":
                Spotify.volume(value);
                response = "Spotify volume " + value;
                break;
            case "volume_up":
                Spotify.volumeUp();
                response = "Spotify volume -";
                break;
            case "volume_dn":
                Spotify.volumeDn();
                response = "Spotify volume +";
                break;
            case "status":
                response = "Spotify status: " + Spotify.requestPlayerStateBool();
                break;
            case "transfer":
                Player player = new Player();
//                Spotify.transfer(player);
                response = "Spotify transfer to: " + value;
                break;
            case "currentlyPlaying":
                response = "Spotify currently playing: " + Spotify.currentlyPlaying();
                break;
            case "currentlyPlayingDetails":
                response = "Spotify currently playing details: " + Spotify.currentlyPlayingDetails();
                break;

            case "playAlbum":
                response = "Spotify currently playing details: " + Spotify.getLinkAlbum(value);
                break;
            case "playTrack":
                response = "Spotify currently playing details: " + Spotify.getLinkTrack(value);
                break;
            case "playPlaylist":
                response = "Spotify currently playing details: " + Spotify.getLinkPlaylist(value);
                break;

            default:
                log.info("ACTION NOT FOUND: " + action);
                response = "ACTION NOT FOUND: " + action;
                break;
        }

        if (response != null) {
            context.bodyResponse = response;
        } else {
            context.bodyResponse = "No response";
        }
        return context;
    }
}