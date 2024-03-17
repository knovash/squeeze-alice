package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.time.LocalTime;
import java.util.HashMap;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class Actions {

    // Алиса, музыку громче\тише для Tasker. переделать для up dn
    public static void volumeByQuery(Player player, String value) {
        player.volume(value);
    }

    // Алиса, включи канал (избранное LMS)
    // Если колонка играла канал включится без изменения громкости и группы,
    // Если колонка не играла установить громкость приемлемой для данного времени,
    public static void playChannel(Player player, Integer channel) {
        log.info("CHANNEL: " + channel + " PLAYER: " + player.name);
        if (player.mode().equals("play")) { // если играет - включить новый канал
            log.info("PLAYER: " + player.name + " PLAY CHANNEL: " + channel);
            player
                    .play(channel) // канал - цифра позиция в избранном LMS
                    .saveLastTime()
                    .saveLastPath()
                    .syncAllOtherPlayingToThis(); // пока сломана синхронизация в лмс для DI.FM
        } else {
            // если не играет - вэйк, громкость, включить новый канал
            log.info("PLAYER: " + player.name + " UNSYNC, WAKE, PLAY CHANNEL: " + channel);
            player
                    .unsync()
                    .wakeAndSet()
                    .play(channel) // канал - цифра позиция в избранном LMS
                    .saveLastTime()
                    .saveLastPath()
                    .syncAllOtherPlayingToThis();// пока сломана синхронизация в лмс для DI.FM
        }
    }

    // Алиса, навык, включи {исполнитель} (линк на плейлист в Spotify)
    public static void playSpotify(Player player, String link) {
        log.info("SPOTIFY LINK PLAYER: " + player.name);
        if (player.mode().equals("play")) {
            log.info("PLAYER: " + player.name + " PLAY LINK: " + link);
            player
                    .play(link) // линк - ссылка на плэйлист спотифай
                    .saveLastTime()
                    .saveLastPathLink(link)
                    .waitFor(2000)
                    .syncAllOtherPlayingToThis(); // подключить все играющие
        } else {
            log.info("PLAYER: " + player.name + " UNSYNC, WAKE, PLAY CHANNEL: " + link);
            player
                    .unsync()
                    .wakeAndSet()
                    .play(link) // линк - ссылка на плэйлист спотифай
                    .saveLastTime()
                    .saveLastPathLink(link)
                    .waitFor(2000)
                    .syncAllOtherPlayingToThis(); // подключить все играющие для СПОТИФАЙ
        }
    }


    // Алиса, включи музыку/колонку
    // играет    +  нет играющей  = продолжить играть
    // играет    +  есть играющей = подключить к играющей
    // не играет +  нет играющей  = вэйк, сет, ластплэй
    // не играет +  есть играющей = вэйк, сет, подключить к играющей
    public static void turnOnMusic(Player player) {
        log.info("TURN ON SPEAKER");
        String mode = player.mode();
        log.info("PLAYER: " + player.name + " mode: " + mode + " separate: " + player.separate);

        // отделен = играть, если пусто играть последнее
        if (player.separate) {
            log.info("PLAYER IS SEPARATED " + player.separate);
            if (!mode.equals("play")) {
                log.info("WAKE, SET, PLAY LAST");
                player
                        .wakeAndSet()
                        .playLast()
                        .saveLastPath()
                        .saveLastTime();
            }
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
//            player.syncAllOtherPlayingToThis();
//            log.info("SYNC TO PLAYING: " + playing);
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

    public static String separate_on(Player player) {
        // отдельно от других
        log.info("SEPARATE ON");
        player.separate = true;
        player.unsync().play();
        lmsPlayers.write();
        return "SEPARATE: " + player.name + " " + player.separate;
    }

    public static String alone_on(Player player) {
        // только этот плеер
        log.info("ALONE ON");
        player.separate = true;
        player.unsync().play();
        lmsPlayers.players.stream()
                .filter(p -> !p.name.equals(player.name))
                .forEach(p -> p.unsync());
        lmsPlayers.write();
        return "ALONE: " + player.name + " " + player.separate;
    }

    public static String separate_alone_off(Player player) {
        // только этот плеер
        log.info("SEPARATE ALONE OFF");
        lmsPlayers.players.stream().forEach(p -> p.separate = false);
//        player.separate = false;
//        player.alone = false;
        Actions.turnOnMusic(player);
        lmsPlayers.write();
        return "ALONE: " + player.name + " " + player.separate;
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
        log.info("TIME DIFF MINUTES: " + playerTime.until(nowTime, MINUTES));
        long diff = playerTime.until(nowTime, MINUTES);
//        log.info("LONG DIFFERENCE: " + diff);
        if (diff < 0) return true;
//        if (nowTime.isAfter(LocalTime.of(22,00))) return true;
        log.info("EXPIRED: " + (diff > delay));
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