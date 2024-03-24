package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class Actions2 {

    public static void playChannel(Player player, Integer channel) {
        log.info("CHANNEL: " + channel + " PLAYER: " + player.name);
        player
                .ifNotPlayUnsyncWakeSet()
                .playChannel(channel)
                .saveLastTime()
                .saveLastChannel(channel)
                .saveLastPath()
                .syncAllOtherPlayingToThis();
    }

    public static void playSpotify(Player player, String link) {
        log.info("SPOTIFY LINK PLAYER: " + player.name);
        player
                .ifNotPlayUnsyncWakeSet()
                .playPath(link)
                .saveLastTime()
                .saveLastPathLink(link)
                .waitFor(2000)
                .syncAllOtherPlayingToThis();
    }

    public static void turnOnMusic(Player player) {
        log.info("TURN ON SPEAKER");
        String mode = player.mode();
        log.info("PLAYER: " + player.name + " mode: " + mode + " separate: " + player.separate);

        player.ifNotPlayUnsyncWakeSet();

        // отделен = играть, если пусто играть последнее
        if (player.separate) {
            log.info("PLAYER IS SEPARATED " + player.separate);
            player
//                    .ifNotPlayUnsyncWakeSet()
                    .playLast()
                    .saveLastPath()
                    .saveLastTime();
            return;
        }




        Player playing = lmsPlayers.getPlayingPlayer(player.name);
        log.info("PLAYING: " + playing);

        // играет    +  нет играющей  = продолжить играть
        if (mode.equals("play") && playing == null) {
            log.info("STILL PLAYING");
            return;
        }

        log.info("PLAYING: " + playing);
        // играет    +  есть играющей = подключить к играющей
        if (mode.equals("play") && playing != null) {
            log.info("SYNC ALL PLAYING TO THIS: " + playing);
            player
                    .sync(playing.name)
                    .saveLastPath()
                    .saveLastTime();
            return;
        }

        // не играет +  нет играющей  = вэйк, сет, ластплэй
        if (!mode.equals("play") && playing == null) {
            log.info("WAKE, SET, PLAY LAST, NO PLAYING: " + playing);
            player
                    .unsync()
                    .wakeAndSet()
                    .playLast()
                    .saveLastPath()
                    .saveLastTime();
            return;
        }
        // не играет +  есть играющей = вэйк, сет, подключить к играющей
        if (!mode.equals("play") && playing != null) {
            log.info("WAKE, SET, SYNC TO PLAYING: " + playing);
            player
                    .unsync()
                    .wakeAndSet()
                    .sync(playing.name)
                    .saveLastPath()
                    .saveLastTime();
            return;
        }
    }

    public static void turnOnMusicThis(Player player) {
        log.info("TURN ON SPEAKER THIS");
        String mode = player.mode();
        log.info("PLAYER: " + player.name + " mode: " + mode + " separate: " + player.separate);

        // отделен = играть, если пусто играть последнее
//        if (player.separate) {
//            log.info("PLAYER IS SEPARATED " + player.separate);
//            if (!mode.equals("play")) {
//                log.info("WAKE, SET, PLAY LAST");
//                player
//                        .wakeAndSet()
//                        .playLast()
//                        .saveLastPath()
//                        .saveLastTime();
//            }
//            return;
//        }

        Player playing = lmsPlayers.getPlayingPlayer(player.name);
        log.info("PLAYING: " + playing);

        // играет    +  нет играющей  = продолжить играть
        if (mode.equals("play") && playing == null) {
            log.info("STILL PLAYING");
            return;
        }

        log.info("PLAYING: " + playing);
        // играет    +  есть играющей = подключить к играющей
        if (mode.equals("play") && playing != null) {
            log.info("SYNC ALL PLAYING TO THIS: " + playing);
//            player.syncAllOtherPlayingToThis();
//            log.info("SYNC TO PLAYING: " + playing);
            player
//                    .sync(playing.name)
                    .syncAllOtherPlayingToThis()
                    .saveLastPath()
                    .saveLastTime();
            return;
        }

        // не играет +  нет играющей  = вэйк, сет, ластплэй
        if (!mode.equals("play") && playing == null) {
            log.info("WAKE, SET, PLAY LAST, NO PLAYING: " + playing);
            player
                    .unsync()
                    .wakeAndSet()
                    .playLast()
                    .saveLastPath()
                    .saveLastTime();
            return;
        }
        // не играет +  есть играющей = вэйк, сет, подключить к играющей
        if (!mode.equals("play") && playing != null) {
            log.info("WAKE, SET, SYNC TO PLAYING: " + playing);
            player
                    .unsync()
                    .wakeAndSet()
                    .sync(playing.name)
                    .saveLastPath()
                    .saveLastTime();
            return;
        }
    }

    // Алиса, выключи музыку - выключиться музыка везде
    public static void turnOffMusicAll() {
        log.info("TURN OFF MUSIC pause all players");
        lmsPlayers.players.forEach(Player::pause);
    }

    // Алиса, выключи колонку - отключить и остановить колонку
    public static void turnOffMusic(Player player) {
        log.info("TURN OFF SPEAKER");
        player
                .unsync()
                .pause();
    }

    public static String toggleMusic(Player player) {
        // переключить play/pause для кнопки на пульте
        String mode = player.mode();
        if (mode.equals("play")) turnOffMusic(player);
        else turnOnMusic(player);
        return "MUSIC TOGGLE: " + mode;
    }

    public static String toggleMusicAll(Player player) {
        // переключить play/pause для кнопки на пульте
        String mode = player.mode();
        if (mode.equals("play")) {
            lmsPlayers.players.stream().forEach(p -> p.unsync().pause());
        } else turnOnMusic(player);
        return "MUSIC TOGGLE: " + mode;
    }


    public static String alone_on(Player player) {
        // только этот плеер
        log.info("ALONE ON");
        player.separate = true;
        player
                .unsync()
                .play()
                .stopAllOther();
        lmsPlayers.write();
        return "ALONE: " + player.name + " " + player.separate;
    }

    public static String separate_alone_off(Player player) {
        // вместе
        // если играет отдельно подключить к играющим
        // если играет не отдельно подключить остальные ОТДЕЛЬНЫЕ сюда
        log.info("SEPARATE ALONE OFF");

        if (player.separate) {
            lmsPlayers.players.stream().forEach(p -> p.separate = false);
            Actions2.turnOnMusic(player);
        } else {
            lmsPlayers.players.stream().forEach(p -> p.separate = false);
            Actions2.turnOnMusicThis(player);
        }
        lmsPlayers.write();
        return "ALONE: " + player.name + " " + player.separate;
    }

    public static boolean timeExpired(Player player) {
        log.info("CHECK IF TIME EXPIRED");
        long delay = 10;
        if (player.lastPlayTime == null) return true;
        LocalTime playerTime = LocalTime.parse(player.lastPlayTime).truncatedTo(MINUTES);
        LocalTime nowTime = LocalTime.now().truncatedTo(MINUTES);
        log.info("PLAYER LAST TIME: " + playerTime);
        log.info("NOW TIME: " + nowTime);
        log.info("TIME DIFF MINUTES: " + playerTime.until(nowTime, MINUTES));
        long diff = playerTime.until(nowTime, MINUTES);
//        log.info("LONG DIFFERENCE: " + diff);
        if (diff < 0) return true;
//        if (nowTime.isAfter(LocalTime.of(22,00))) return true;
        log.info("EXPIRED: " + (diff > delay));
        return (diff > delay);
    }
}