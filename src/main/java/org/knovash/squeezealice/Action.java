package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.util.Objects;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class Action {

    // Алиса, музыку громче\тише
    public static void volume(Player player, String value) {
        Integer playerCurrentVolume;
        Integer volumeAlicePrevious = player.volume_alice_previous;
        Integer volumeAliceCurrent = Integer.valueOf(value);
        Integer step = player.volume_step;
        playerCurrentVolume = Integer.valueOf(player.volume());
        if (playerCurrentVolume < 5) step = 2;
        log.info("VOLUME: (alice) " + value + " PLAYER: " + player.name + " current=" + volumeAliceCurrent + " last=" + volumeAlicePrevious + " low=" + player.volume_alice_low + " hi=" + player.volume_alice_high);
        if ((volumeAliceCurrent > volumeAlicePrevious) || (volumeAliceCurrent.equals(player.volume_alice_high))) {
            player.volume("+" + step);
        }
        if ((volumeAliceCurrent < volumeAlicePrevious) || (volumeAliceCurrent.equals(player.volume_alice_low))) {
            player.volume("-" + step);
        }
        player.volume_alice_previous = volumeAliceCurrent;
        playerCurrentVolume = Integer.valueOf(player.volume());
        if (playerCurrentVolume == 0) player.volume("1");
    }

    // Алиса, музыку громче\тише
//    public static void volume(Player player, String value) {
//        Integer volumeAlicePrevious = player.volume_alice_previous;
//        Integer volumeAliceCurrent = Integer.valueOf(value);
//        Integer step = player.volume_step;
//        log.info("VOLUME: (alice) " + value + " PLAYER: " + player.name + " current=" + volumeAliceCurrent + " last=" + volumeAlicePrevious + " low=" + player.volume_alice_low + " hi=" + player.volume_alice_high);
//        if ((volumeAliceCurrent > volumeAlicePrevious) || (volumeAliceCurrent.equals(player.volume_alice_high))) {
//            player.volume("+" + step);
//        }
//        if ((volumeAliceCurrent < volumeAlicePrevious) || (volumeAliceCurrent.equals(player.volume_alice_low))) {
//            player.volume("-" + step);
//        }
//        player.volume_alice_previous = volumeAliceCurrent;
//    }

    // Алиса, включи канал
    // Если колонка играла канал включится без изменения громкости и группы,
    // Если колонка не играла установить громкость приемлемой для данного времени,
    public static void channel(Player player, Integer channel) {
        log.info("CHANNEL: " + channel + " PLAYER: " + player.name);
        if (player.mode().equals("play")) {
            log.info("PLAYER: " + player.name + " PLAY CHANNEL: " + channel);
            player.play(channel);
            return;
        }
        log.info("PLAYER: " + player.name + " UNSYNC, WAKE, PLAY CHANNEL: " + channel);
        player
                .unsync()
                .wakeAndSet()
                .play(channel);
    }

    // Алиса, включи музыку/колонку
    // играет    +  нет играющей  = продолжить играть
    // играет    +  есть играющей = подключить к играющей
    // не играет +  нет играющей  = вэйк, сет, ластплэй
    // не играет +  есть играющей = вэйк, сет, подключить к играющей
    public static void turnOnMusicSpeaker(Player player) {
        String mode = player.mode();
        Player playing = Server.playingPlayer(player.name);
        // играет    +  нет играющей  = продолжить играть
        if (Objects.equals(mode, "play") && playing == null) {
            log.info("STILL PLAYING");
        }
        // играет    +  есть играющей = подключить к играющей
        if (Objects.equals(mode, "play") && playing != null) {
            log.info("SYNC TO PLAYING");
            player.sync(playing.name);
        }
        // не играет +  нет играющей  = вэйк, сет, ластплэй
        if (!Objects.equals(mode, "play") && playing == null) {
            log.info("WAKE, SET, PLAY LAST");
            player
                    .unsync()
                    .wakeAndSet()
                    .playLast();
        }
        // не играет +  есть играющей = вэйк, сет, подключить к играющей
        if (!Objects.equals(mode, "play") && playing != null) {
            log.info("WAKE, SET, SYNC TO PLAYING");
            player
                    .unsync()
                    .wakeAndSet()
                    .sync(playing.name);
        }
    }

    // Алиса, выключи музыку - выключиться музыка везде
    public static void turnOffMusic() {
        log.info("TURN OFF MUSIC pause all players");
        server.players.forEach(Player::pause);
    }

    // Алиса, выключи колонку - отключить и остановить колонку
    public static void turnOffSpeaker(Player player) {
        player
                .unsync()
                .pause();
    }

    // Алиса, все тихо/громко
    public static void allLowOrHigh(String mute) {
        switch (mute) {
            case ("1"):
                log.info("ALL LOW");
                server.players.forEach(player -> player.volume(String.valueOf(player.volume_low)));
                break;
            case ("0"):
                log.info("ALL HIGH");
                server.players.forEach(player -> player.volume(String.valueOf(player.volume_high)));
                break;
        }
    }

    // Алиса, включи Спотифай
    public static void turnOnSpotify(Player player) {
        log.info("TURN ON SPOTIFY " + player.name + " MAC " + player.id);
        String mac = player.id;
        mac = mac.replace(":", "%3A");
        String uri = "http://" + lmsIP + ":9000/plugins/spotty/index.html?index=10.1&player=" + mac + "&sess=";
        Fluent.postQueryGetStatus(uri);
    }
}