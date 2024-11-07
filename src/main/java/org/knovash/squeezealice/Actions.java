package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.spotify.Spotify;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class Actions {

    public static void turnOnMusic(Player player) {
        log.info("TURN ON MUSIC PLAYER: " + player.name);
//        CompletableFuture.runAsync(() -> {
        player.ifExpiredOrNotPlayUnsyncWakeSet();
        if (player.separate) {
            log.info("PLAYER IS SEPARATED - PLAY LAST");
            player.playLast();
            lmsPlayers.write();
            return;
        }
        Player playing = lmsPlayers.getPlayingPlayer(player.name);
        if (playing == null) {
            log.info("NO PLAYING. PLAY LAST");
            player.playLast();
//                player.syncAllOtherPlayingToThis();
        } else {
            log.info("SYNC TO PLAYING " + playing);
            player.syncTo(playing.name); //.saveLastPath().saveLastTime();
        }
        lmsPlayers.write();
//        });
        Requests.autoRemoteRefresh();
    }

    public static void turnOffMusic(Player player) {
        log.info("TURN OFF SPEAKER " + player.name + " SEPARATE " + player.separate);
        player
                .unsync()
                .pause()
                .saveLastTime()
                .saveLastPath();
        lmsPlayers.write();
        Requests.autoRemoteRefresh();
    }

    public static String queryToggleMusic(Player player) {
        log.info("TOGGLE MUSIC PLAYER: " + player.name);
//        if (Spotify.ifPlaying()) {
//            Spotify.pause();
//            Spotify.active = true;
//            return "";
//        }
        player.status();
        String status;
        if (player.playing) {
            player.turnOffMusic();
            player.status();
            player.title();
            status = player.name + " - Stop - " + player.title;
        } else {
            player.turnOnMusic()
                    .syncAllOtherPlayingToThis();
            player.status();
            player.title();
            status = player.name + " - Play - " + player.title;
        }
        log.info("STATUS: " + status);
        Requests.autoRemoteRefresh();
        return status;
    }

    public static String queryToggleMusicAll(Player player) {
        player.status();
        String status;
        if (player.playing) {
            lmsPlayers.players.forEach(p -> p.turnOffMusic());
            status = "All players - Stop";
        } else {
            player.turnOnMusic().syncAllOtherPlayingToThis();
            status = "All players - Play";
        }
        Requests.autoRemoteRefresh();
        return status;
    }

    public static String queryStopMusicAll() {
        log.info("STOP ALL");
        lmsPlayers.updateServerStatus();
        lmsPlayers.players.stream()
                .filter(player -> player.connected)
                .peek(p -> log.info(p.name + " " + p.connected))
                .forEach(p -> p.stop());
        Requests.autoRemoteRefresh();
        return "All players - Stop";
    }

    public static String queryNext(Player player) {
        player.next().status(50);
        Requests.autoRemoteRefresh();
        return player.name + " - Next - " + player.title;

    }

    public static String queryPrev(Player player) {
        player.prev().status(50);
        Requests.autoRemoteRefresh();
        return player.name + " - Prev - " + player.title;
    }
}