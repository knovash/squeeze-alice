package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.time.LocalTime;
import java.util.Objects;

import static org.knovash.squeezealice.Main.server;

@Log4j2
public class Action {

    // Алиса музыку громче\тише
    public static void volume(Player player, String value) {
//        log.info("VOLUME: (alice) " + value + " PLAYER: " + player.name);
        Integer volumeAlicePrevious = player.volumeAlicePrevious;
        Integer volumeAliceCurrent = Integer.valueOf(value);
        Integer step = player.volumeStep;
        log.info("VOLUME: (alice) " + value + " PLAYER: " + player.name + " current=" + volumeAliceCurrent + " last=" + volumeAlicePrevious + " low=" + player.volumeAliceLow + " hi=" + player.volumeAliceHigh);
        if ((volumeAliceCurrent > volumeAlicePrevious) || (volumeAliceCurrent.equals(player.volumeAliceHigh))) {
//            log.info("VOLUME UP +" + step);
            player.volume("+" + step);
        }
        if ((volumeAliceCurrent < volumeAlicePrevious) || (volumeAliceCurrent.equals(player.volumeAliceLow))) {
//            log.info("VOLUME DN -" + step);
            player.volume("-" + step);
        }
        player.volumeAlicePrevious = volumeAliceCurrent;
    }

    // Алиса, включи канал
    // Если колонка играла канал включится без изменения громкости и группы,
    // Если колонка не играла установить громкость приемлемой для данного времени,
    public static void channel(Player player, Integer channel) {
        log.info("CHANNEL: " + channel + " PLAYER: " + player.name);
        if (player.mode().equals("play")) {
            log.info("PLAYER: " + player + " PLAY CHANNEL: " + channel);
            player.play(channel);
            return;
        }
        log.info("PLAYER: " + player.name + "UNSYNC, WAKE, PLAY CHANNEL: " + channel);
        player
                .unsync()
                .wakeAndSet()
                .play(channel);
    }

    // Алиса, включи музыку
    // если колонка играет - ничего. продолжит играть
    // если колонка не играет - разбудить, установить громкость,
    // найти играющую - если есть подключиться к ней
    // если нет играющей - играть последнее, если нет то избранное1
    // TODO проверить что синхронизировано, все вэйк и  пресет
    public static void turnOnMusic(Player player) {
        log.info("TURN ON PLAYER: " + player.name);
        if (player.mode().equals("play")) {
            log.info("STILL PLAYING");
            return;
        }
        log.info("not play - wake - set preset - search playing - try sync - else play last - else play fav1");
        player
                .unsync()
                .wakeAndSet();

        Player playing = Server.playingPlayer(); // найти играющую - если есть подключиться к ней

        if (playing != null) {
            log.info("SYNC TO PLAYING: " + playing.name);
            player.sync(playing.name);
            return;
        }
        if (player.path() != null) { // играть путь из плеера
            player.play(player.path());
            return;
        }
        if (Player.lastPath != null) { // играть последнее игравшее
            player.play(Player.lastPath);
            return;
        }

        if (Player.lastChannel != null) { // играть последнее игравшее
            player.play(Player.lastChannel);
            return;
        }
        if (Favorites.checkExists(1)) {  // играть первое избранное
            player.play(1);
        }

    }

    // Алиса, выключи музыку - выключиться музыка везде
    public static void turnOffMusic() {
        server.players.forEach(Player::pause);
    }

    // Алиса, включи колонку - подключить к играющей или играть последнее
    //        играет + нет играющей         =   продолжить играть
    //        играет + есть играющей        =   подключить к играющей
    //        не играет + нет играющей      =   вэйк, пресет, ластплэй
    //        не играет + есть играющей     =   вэйк, пресет, подключить к играющей
    //TODO проверить что синхронизировано, все вэйк и  пресет
    public static void turnOnSpeaker(Player player) {
        String mode = player.mode();
        Player playing = Server.playingPlayer();
        if (Objects.equals(mode, "play") && playing == null) {
            log.info("STILL PLAYING");
        }
        if (Objects.equals(mode, "play") && playing != null) {
            log.info("SYNC TO PLAYING");
            player.sync(playing.name);
        }
        if (!Objects.equals(mode, "play") && playing == null) {
            log.info("WAKE PLAY LAST");
            player.wakeAndSet();
            player.play(Player.lastPath);
        }
        if (!Objects.equals(mode, "play") && playing != null) {
            log.info("WAKE SYNC TO PLAYING");
            player.wakeAndSet();
            player.sync(playing.name);
        }
    }

    // Алиса выключи колонку - отключить и остановить колонку
    public static void turnOffSpeaker(Player player) {
        player.unsync().pause();
//        player.pause();
    }

    // Алиса все тихо
    public static void allLow() {
        LocalTime.now();
        server.players.stream().forEach(player -> player.volume("5"));
    }

    // Алиса все громко
    public static void allHigh() {
        server.players.stream().forEach(player -> player.volume("20"));
    }

    public static void preset(String player, String preset) {
        log.info("PLAYER: " + player + " PRESET: " + preset);
        String volume = Preset.volume();
    }
}