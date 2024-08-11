package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class Actions {

//    public static void actionPlayChannel(Player player, Integer channel) {
//        log.info("CHANNEL: " + channel + " PLAYER: " + player.name);
//        player
//                .ifNotPlayUnsyncWakeSet() // mode? sync- volume
//                .playChannel(channel) // playlist item
//                .saveLastTime()
//                .saveLastChannel(channel)
//                .saveLastPath() // path
//                .syncAllOtherPlayingToThis(); // получить mode каждого
//        lmsPlayers.write();
//    }

//    public static void playSpotify(Player player, String path) {
//        log.info("SPOTIFY LINK PLAYER: " + player.name);
//        player
//                .ifNotPlayUnsyncWakeSet()
//                .playPath(path)
//                .saveLastTime()
////                .saveLastPathLink(link)
//                .waitFor(1000)
//                .syncAllOtherPlayingToThis();
//        lmsPlayers.write();
//    }

    public static void turnOnMusic(Player player) {
        log.info("TURN ON MUSIC PLAYER: " + player.name);
        player.ifNotPlayUnsyncWakeSet();
        if (player.separate) {
            log.info("PLAYER IS SEPARATED - PLAY LAST");
            player.playLast(); // include last path & time
            lmsPlayers.write();
            return;
        }
        Player playing = lmsPlayers.getPlayingPlayer(player.name);
        if (playing == null) {
            log.info("NO PLAYING. PLAY LAST");
            player.playLast();
        } else {
            log.info("SYNC TO PLAYING " + playing);
            player.syncTo(playing.name); //.saveLastPath().saveLastTime();
        }
        lmsPlayers.write();
    }

    public static void turnOffMusic(Player player) {
        log.info("TURN OFF SPEAKER " + player.name + " SEPARATE " + player.separate);

        if (player.name.equals("Spotify")) {
            log.info("ITS SPOTIFY");
            return;
        }

        player
                .unsync()
                .pause()
                .saveLastTime()
                .saveLastPath()
        ;
        lmsPlayers.write();
    }

    public static String toggleMusic(Player player) {
        log.info("START TOGGLE MUSIC PLAYER: " + player.name);
        player.status(); // TODO двойной запрос состояния плеера
        String mode;
        if (player.playing) {
            turnOffMusic(player);
            mode = player.name + " - Stop - " + player.title;
        } else {
            turnOnMusic(player);
            mode = player.name + " - Play - " + player.title;
        }
        return mode;
    }

    public static String toggleMusicAll(Player player) {
        player.status(); // TODO двойной запрос состояния плеера
        String mode;
        if (player.playing) {
            lmsPlayers.players.forEach(p -> turnOffMusic(p));
            mode = "All players - Stop";
        } else {
            turnOnMusic(player);
            mode = "All players - Play";
        }
        return mode;
    }

    public static String stopMusicAll() {
        lmsPlayers.players.forEach(p -> turnOffMusic(p));
        return "All players - Stop";
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