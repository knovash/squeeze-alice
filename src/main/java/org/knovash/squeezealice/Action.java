package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.Player;
import org.knovash.squeezealice.lms.ServerLMS;

import static org.knovash.squeezealice.Main.serverLMS;

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
        String mode = Player.mode(player);
        if (mode.equals("play")) {
            log.info("PLAYER: " + player + " MODE: " + mode + " PLAY CONTINUE: ");
            Player.channel(player, channel);
        } else {
            log.info("PLAYER: " + player + "MODE: " + mode + " WAKE - PRESET - PLAY CHANNEL");
            Action.wake(player);
            Action.preset(player, "low");
            Player.channel(player, channel);
        }
    }

    // Алиса музыку громче\тише
    public static void volume(String name, String value) {
        log.info("VOLUME: (alice) " + value + " PLAYER: " + name);
        Player player = Player.playerByName(name);
        Integer volumePrevious = player.volumePrevious;
        Integer volumeCurrent = Integer.valueOf(value);
        Integer step = player.volumeStep;
        log.info("alice: " + volumeCurrent + " > alice last: " + volumePrevious);
        if (volumeCurrent > volumePrevious || volumeCurrent == 1) {
            log.info("VOLUME UP +" + step);
            player.volume("+" + step);
        }
        if (volumeCurrent < volumePrevious || volumeCurrent == 100) {
            log.info("VOLUME DN -" + step);
            player.volume("-" + step);
        }
        player.volumePrevious = volumeCurrent;
    }

    // Алиса включи музыку. Алиса включи колонку ---
    public static void turnon(String name) {
        Player player = Player.playerByName(name);
        log.info("TURN ON PLAYER: " + name);
        // обновить плееры
//        ServerLMS.updatePlayers();
        log.info("CHECK PLAYER: " + name + " " + serverLMS.players.stream().filter(player1 -> player1.getName().equals(name)).findFirst().orElse(null).getName());
        // если плеер на сервере
        if (player.mode().equals("play")) {
            log.info("continue playing");
        }
        // if mode not play - wake - set preset
        else {
            log.info("not play - wake - set preset - search playing - try sync - else play last - else play fav1");
        }
        player.wake();
        player.volume("4");

//        Action.wake(name);
//        Player.volumeSet(player, "3");
        // search playing
//        Action.playing();
        // playing yes - connect to playing
        // playing no - play last,fav1
    }

    public static void turnoff() {
        // Алиса выключи музыку
        serverLMS.players.stream().forEach(player -> player.pause());
    }

    public static void low() {
        // Алиса все тихо
        serverLMS.players.stream().forEach(player -> player.volume("5"));
    }

    public static void high() {
        // Алиса все громко
        serverLMS.players.stream().forEach(player -> player.volume("20"));
    }

    // Алиса обнови плеееры
    public static void updatePlayers() {
        serverLMS.updatePlayers();
    }

    public static void wake(String player) {
        log.info("PLAYER: " + player + " WAKE: ");
        Player.channel(player, "silence");
        // wait
//        Player.pause(player);
    }

    public static void preset(String player, String preset) {
        log.info("PLAYER: " + player + " PRESET: " + preset);
        Player.volume(player, "5");
    }

    public static Player playing() {
        return serverLMS.players
                .stream()
                .filter(player -> player.mode().equals("play"))
                .findFirst().orElse(null);
    }
}
