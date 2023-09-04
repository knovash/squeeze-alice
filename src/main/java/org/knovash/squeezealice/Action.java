package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.Player;
import org.knovash.squeezealice.lms.ServerLMS;

@Log4j2
public class Action {

    //ACTION    PLAYER      VALUE
    //channel   homepod     1/2/3.../9
    //volume    homepod     1/2/.../100
    //preset    homepod     low/high
    //turnon    homepod
    //turnoff   homepod
    //spotify   homepod
    //sync      homepod
    //unsync    homepod

    // Алиса включи канал
    public static void channel(String player, String channel) {
        log.info("CHANNEL: " + channel + " PLAYER: " + player);
        // включи канал -- на плеере --
        // если плеер играет - включить канал --
        // если плеер не играет - wake, unsync, preset, play channel
        String mode = Player.mode(player);
        if (mode.equals("play")) {
            log.info("PLAYER: " + player + "MODE: " + mode + " PLAY CHANNEL");
            Player.play(player, channel);
        } else {
            log.info("PLAYER: " + player + "MODE: " + mode + " WAKE - PRESET - PLAY CHANNEL");
//            Action.wake(player);
            Player.volumeSet(player, "5");
            Player.play(player, channel);
        }
    }

    // Алиса музыку громче\тише
    public static void volume(String player, String value) {
        log.info("VOLUME: " + value + " PLAYER: " + player);
        Player.volumeSet(player, value);
    }

    // Алиса все тихо
    public static void low() {
        log.info("LOW ALL");
        ServerLMS.players.stream().forEach(p -> p.volumeSet("5"));
    }

    // Алиса все громко
    public static void high() {
        log.info("HIGH ALL");
        ServerLMS.players.stream().forEach(p -> p.volumeSet("10"));
    }

    // Алиса включи музыку. Алиса включи колонку ---
    public static void turnOnMusic(String player) {
        log.info("TURN ON PLAYER: " + player);
        // update players - check player on server
        ServerLMS.updatePlayers();
        log.info("CHECK PLAYER: " + player + " " + ServerLMS.players.stream().filter(player1 -> player1.getName().equals(player)).findFirst().orElse(null).getName());
        // if mode play then continue play
        String mode = Player.mode(player);
        log.info(player + "MODE: " + mode);
        if (mode.equals("play")) {
            log.info("continue playing");
        }
        // if mode not play - wake - set preset
        else {
            log.info("not play - wake - set preset - search playing - try sync - else play last - else play fav1");
        }
        Action.wake(player);
//        Player.volumeSet(player, "3");
        // search playing
        // playing yes - connect to playing
        // playing no - play last,fav1


    }

    // Алиса выключи музыку. Алиса выключи колонку ---
    public static void off(String player) {

    }

    // Алиса обнови плеееры
    public static void updatePlayers() {
        ServerLMS.updatePlayers();
    }

    // Алиса обнови избранное
    public static void updateFavorites() {
        ServerLMS.updateFavorites();
    }

    public static void wake(String player) {
        log.info("WAKE play silence to player " + player);
        Player.play(player, "silence");
        // wait
//        Player.pause(player);

    }

    public static String searchPlaying() {
        String playingName = ServerLMS.players
                .stream()
                .map(player -> player.getName())
                .filter(name -> Player.mode(name) == "play")
                .findFirst().orElse("no");
        return playingName;
    }

    public static void stopAll() {
        // Алиса выключи музыку
        ServerLMS.players.stream().forEach(player -> player.pause());
    }

    public static void presetLow() {
        // Алиса все тихо
        ServerLMS.players.stream().forEach(player -> player.volumeSet("5"));
    }

    public static void presetHigh() {
        // Алиса все громко
        ServerLMS.players.stream().forEach(player -> player.volumeSet("20"));
    }
}
