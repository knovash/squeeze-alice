package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class Actions {

    public static void turnOnMusic(Player player) {
        log.info("TURN ON MUSIC PLAYER: " + player.name);
        player.ifNotPlayUnsyncWakeSet();
        if (player.separate) {
            log.info("PLAYER IS SEPARATED - PLAY LAST");
            player.playLast(); // include last path & time
            lmsPlayers.writePlayers();
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
        lmsPlayers.writePlayers();
    }

    public static void turnOffMusic(Player player) {
        log.info("TURN OFF SPEAKER " + player.name + " SEPARATE " + player.separate);
        player
                .unsync()
                .pause()
                .saveLastTime()
                .saveLastPath();
        lmsPlayers.writePlayers();
    }

    public static String toggleMusic(Player player) {
        log.info("START TOGGLE MUSIC PLAYER: " + player.name);
        player.status(); // TODO двойной запрос состояния плеера
        String status;
        if (player.playing) {
            turnOffMusic(player);
            status = player.name + " - Stop - " + player.title;
        } else {
            turnOnMusic(player);
            status = player.name + " - Play - " + player.title;
        }
        return status;
    }

    public static String toggleMusicAll(Player player) {
        player.status(); // TODO двойной запрос состояния плеера
        String status;
        if (player.playing) {
            lmsPlayers.players.forEach(p -> turnOffMusic(p));
            status = "All players - Stop";
        } else {
            turnOnMusic(player);
            status = "All players - Play";
        }
        return status;
    }

    public static String stopMusicAll() {
        lmsPlayers.players.forEach(p -> turnOffMusic(p));
        return "All players - Stop";
    }
}