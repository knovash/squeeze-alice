package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.knovash.squeezealice.Main.*;

@Log4j2
public class Action {

    // Алиса, музыку громче\тише
    public static void volume(Player player, String value) {
        Integer step;
        Integer volumeAlicePrevious = player.volume_alice_previous;
        Integer volumeAliceCurrent = Integer.valueOf(value);
        Integer playerCurrentVolume = Integer.valueOf(player.volume());
        if (playerCurrentVolume == null) {
            log.info("LMS NO RESPONSE");
            return;
        }
        step = player.volume_step;
        if (playerCurrentVolume < 10) step = 1 + Math.round(playerCurrentVolume / 2);
        log.info("VOLUME: (alice) " + value + " PLAYER: " + player.name + " current=" + volumeAliceCurrent + " last=" + volumeAlicePrevious + " low=" + player.volume_alice_low + " hi=" + player.volume_alice_high);
        if ((volumeAliceCurrent > volumeAlicePrevious) || (volumeAliceCurrent.equals(player.volume_alice_high))) {
            player.volume("+" + step);
        }
        if (playerCurrentVolume > 1 && ((volumeAliceCurrent < volumeAlicePrevious) || (volumeAliceCurrent.equals(player.volume_alice_low)))) {
            player.volume("-" + step);
        }
        player.volume_alice_previous = volumeAliceCurrent;
    }

    // Алиса, включи канал
    // Если колонка играла канал включится без изменения громкости и группы,
    // Если колонка не играла установить громкость приемлемой для данного времени,
    public static void channel(Player player, Integer channel) {
        log.info("CHANNEL: " + channel + " PLAYER: " + player.name);
//        timeExpired(player);
        if (player.mode().equals("play")) {
            log.info("PLAYER: " + player.name + " PLAY CHANNEL: " + channel);
            player
                    .play(channel)
                    .saveLastTimeIfPlay();
            return;
        }
        log.info("PLAYER: " + player.name + " UNSYNC, WAKE, PLAY CHANNEL: " + channel);
        player
                .unsync()
                .wakeAndSet()
                .play(channel)
                .saveLastTimeIfPlay();
    }

    // Алиса, включи музыку/колонку
    // играет    +  нет играющей  = продолжить играть
    // играет    +  есть играющей = подключить к играющей
    // не играет +  нет играющей  = вэйк, сет, ластплэй
    // не играет +  есть играющей = вэйк, сет, подключить к играющей
    public static void turnOnMusicSpeaker(Player player) {
        log.info("TURN ON SPEAKER");
        String mode = player.mode();
        timeExpired(player);
        Player playing = Server.playingPlayer(player.name);
        // играет    +  нет играющей  = продолжить играть
        if (mode.equals("play") && playing == null) {
            log.info("STILL PLAYING");
        }
        // играет    +  есть играющей = подключить к играющей
        if (mode.equals("play") && playing != null) {
            log.info("SYNC TO PLAYING");
            player.sync(playing.name).saveLastTime();
        }
        // не играет +  нет играющей  = вэйк, сет, ластплэй
        if (!mode.equals("play") && playing == null) {
            log.info("WAKE, SET, PLAY LAST");
            player
                    .unsync()
                    .wakeAndSet()
                    .playLast()
                    .saveLastTime();
        }
        // не играет +  есть играющей = вэйк, сет, подключить к играющей
        if (!Objects.equals(mode, "play") && playing != null) {
            log.info("WAKE, SET, SYNC TO PLAYING");
            player
                    .unsync()
                    .wakeAndSet()
                    .sync(playing.name)
                    .saveLastTime();
        }
        player.saveLastTimeIfPlay();
    }

    // Алиса, выключи музыку - выключиться музыка везде
    public static void turnOffMusic() {
        log.info("TURN OFF MUSIC pause all players");
        server.players.forEach(Player::pause);
    }

    // Алиса, выключи колонку - отключить и остановить колонку
    public static void turnOffSpeaker(Player player) {
        log.info("TURN OFF SPEAKER");
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
        Fluent.getUriGetStatus(uri);
    }

    public static boolean timeExpired(Player player) {
//        return false;
        log.info("CHECK IF TIME EXPIRED");
        long delay = 10;
        if (player.lastPlayTime == null) return true;
        LocalTime playerTime = player.lastPlayTime.truncatedTo(MINUTES);
        LocalTime nowTime = LocalTime.now().truncatedTo(MINUTES);
        log.info("PLAYER LAST TIME: " + playerTime);
        log.info("NOW TIME: " + nowTime);
        log.info("TIME DIFFERENCE: " + playerTime.until(nowTime, MINUTES));
        long diff = playerTime.until(nowTime, MINUTES);
        log.info("LONG DIFFERENCE: " + diff);
        log.info("EXP BOOL: " + (diff>delay));
        return (diff>delay);
    }
}