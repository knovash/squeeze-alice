package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class Actions {

    public static void playChannel(Player player, Integer channel) {
        log.info("CHANNEL: " + channel + " PLAYER: " + player.name);
        player
                .ifNotPlayUnsyncWakeSet()
                .playChannel(channel)
                .saveLastTime()
                .saveLastChannel(channel)
                .saveLastPath()
                .syncAllOtherPlayingToThis();
        lmsPlayers.write();
    }

    public static void playSpotify(Player player, String link) {
        log.info("SPOTIFY LINK PLAYER: " + player.name);
        player
                .ifNotPlayUnsyncWakeSet()
                .playPath(link)
                .saveLastTime()
                .saveLastPathLink(link)
                .waitFor(1000)
                .syncAllOtherPlayingToThis();
        lmsPlayers.write();
    }

    public static void turnOnMusic(Player player) {
        log.info("TURN ON MUSIC PLAYER: " + player.name);
        player.ifNotPlayUnsyncWakeSet();
        if (player.separate) {
            log.info("PLAYER IS SEPARATED - PLAY LAST");
            player
                    .playLast()
                    .saveLastPath()
                    .saveLastTime();
            lmsPlayers.write();
            return;
        }
        Player playing = lmsPlayers.getPlayingPlayer(player.name);
        if (playing == null) {
            log.info("NO PLAYING - PLAY LAST");
            player.playLast();
        } else {
            log.info("SYNC TO PLAYING " + playing);
            player.sync(playing.name);
        }
        player
                .saveLastPath()
                .saveLastTime();
        lmsPlayers.write();
    }

    public static void turnOffMusic(Player player) {
        log.info("TURN OFF SPEAKER");
        player
                .unsync()
                .pause()
                .saveLastTime()
                .saveLastPath();
        lmsPlayers.write();
    }

    public static String toggleMusic(Player player) {
        String mode = player.mode(); // TODO двойной запрос состояния плеера
        if (mode.equals("play")) {
            turnOffMusic(player);
            mode = "STOP " + player.name;
        } else {
            turnOnMusic(player);
            mode = "PLAY " + player.name;
        }
        return "TOGGLE " + mode;
    }

    public static String toggleMusicAll(Player player) {
        String mode = player.mode();
        if (mode.equals("play")) {
            lmsPlayers.players.forEach(p -> turnOffMusic(p));
            mode = "STOP " + player.name;
        } else {
            turnOnMusic(player);
            mode = "PLAY " + player.name;
        }
        return "TOGGLE ALL " + mode;
    }

    public static String stopMusicAll() {
        lmsPlayers.players.forEach(p -> turnOffMusic(p));
        return "STOP ALL";
    }

    public static boolean timeExpired(Player player) {
        long delay = lmsPlayers.delayExpire;
        log.info("CHECK IF LAST PLAY TIME EXPIRED");
        if (player.lastPlayTime == null) return true;
        LocalTime playerTime = LocalTime.parse(player.lastPlayTime).truncatedTo(MINUTES);
        LocalTime nowTime = LocalTime.now().truncatedTo(MINUTES);
        log.info("PLAYER LAST TIME: " + playerTime + " NOW TIME: " + nowTime + " DIFF: " + playerTime.until(nowTime, MINUTES) + " DELAY " + delay);
        long diff = playerTime.until(nowTime, MINUTES);
        if (diff < 0) return true;
        log.info("EXPIRED: " + (diff > delay));
        return (diff > delay);
    }
}