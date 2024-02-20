package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.knovash.squeezealice.Main.*;

@Log4j2
public class Actions {

    // Алиса, музыку громче\тише для Tasker. переделать для up dn
    public static void volumeByQuery(Player player, String value) {
        player.volume(value);
    }

    // Алиса, включи канал
    // Если колонка играла канал включится без изменения громкости и группы,
    // Если колонка не играла установить громкость приемлемой для данного времени,
    public static void channel(Player player, Integer channel) {
        log.info("CHANNEL: " + channel + " PLAYER: " + player.name);
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
        Player playing = lmsPlayers.getPlayingPlayer(player.name);
        // играет    +  нет играющей  = продолжить играть
        if (mode.equals("play") && playing == null) {
            log.info("STILL PLAYING");
        }
        // играет    +  есть играющей = подключить к играющей
        if (mode.equals("play") && playing != null) {
            log.info("SYNC TO PLAYING");
            player
//                    .sync(playing.name)
                    .play(playing.path()) // времнная замена синхронизации di.fm пока не починят
                    .saveLastTime();
//  https://forums.slimdevices.com/forum/user-forums/logitech-media-server/1673928-logitech-media-server-8-4-0-released?p=1675699#post1675699
//  https://github.com/Logitech/slimserver/issues/993
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
//                    .sync(playing.name)
                    .play(playing.path()) // времнная замена синхронизации пока не починят
                    .saveLastTime();
        }
        player.saveLastTimeIfPlay();
    }

    // Алиса, выключи музыку - выключиться музыка везде
    public static void turnOffMusic() {
        log.info("TURN OFF MUSIC pause all players");
        lmsPlayers.players.forEach(Player::pause);
    }

    // Алиса, выключи колонку - отключить и остановить колонку
    public static void turnOffSpeaker(Player player) {
        log.info("TURN OFF SPEAKER");
        player
                .unsync()
                .pause();
    }

    public static String toggleMusic(Player player) {
        // переключить play/pause для кнопки на пульте
        String mode = player.mode();
        if (mode.equals("play")) turnOffSpeaker(player);
        else turnOnMusicSpeaker(player);
        return "MUSIC TOGGLE: " + mode;
    }

    // Алиса, все тихо/громко НЕИСПОЛЬЗУЕТЬСЯ
    public static void allLowOrHigh(String mute) {
        switch (mute) {
            case ("1"):
                log.info("ALL LOW");
                lmsPlayers.players.forEach(player -> player.volume(String.valueOf(player.volume_low)));
                break;
            case ("0"):
                log.info("ALL HIGH");
                lmsPlayers.players.forEach(player -> player.volume(String.valueOf(player.volume_high)));
                break;
        }
    }

    public static boolean timeExpired(Player player) {
        log.info("CHECK IF TIME EXPIRED");
        long delay = 10;
        if (player.lastPlayTimeStr == null) return true;
        LocalTime playerTime = LocalTime.parse(player.lastPlayTimeStr).truncatedTo(MINUTES);
        LocalTime nowTime = LocalTime.now().truncatedTo(MINUTES);
        log.info("PLAYER LAST TIME: " + playerTime);
        log.info("NOW TIME: " + nowTime);
        log.info("TIME DIFFERENCE: " + playerTime.until(nowTime, MINUTES));
        long diff = playerTime.until(nowTime, MINUTES);
        log.info("LONG DIFFERENCE: " + diff);
        log.info("EXP BOOL: " + (diff > delay));
        return (diff > delay);
    }

    public static void favoritePrev(Player player, HashMap<String, String> parameters) {
        Integer time = Integer.valueOf(parameters.get("time"));
        player.play(1);
        player.timeVolume.remove(time);
    }

    public static void favoriteNext(Player player, HashMap<String, String> parameters) {
        Integer time = Integer.valueOf(parameters.get("time"));
        player.timeVolume.remove(time);
    }
}