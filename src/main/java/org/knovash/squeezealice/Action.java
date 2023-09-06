package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import static org.knovash.squeezealice.Main.serverLMS;

@Log4j2
public class Action {

    // Алиса музыку громче\тише
    public static void volume(String name, String value) {
        log.info("VOLUME: (alice) " + value + " PLAYER: " + name);
        Player player = Player.instance(name);
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

    // Алиса включи канал
    // 1. проверить в черном списке - если да то ничего неделать
    // 2. обновить колонки на сервере
    // 3. проверить что колонка есть на сервере - если нет - ничего
    // 4. если колонка играет - включить канал
    // 5. если колонка не играет - разбудить, установить громкость,
    // 6. включить канал
    public static void channel(String name, Integer channel) {
        Player player = Player.instance(name);
        log.info("CHANNEL: " + channel + " PLAYER: " + player);
        if (player.black) return; // 1. проверить в черном списке - если да то ничего неделать
//        serverLMS.updatePlayers();  // 2. обновить колонки на сервере
        if (!serverLMS.players.contains(player))
            return;  // 3. проверить что колонка есть на сервере - если нет - ничего
        // 4. если колонка играет - включить канал
        if (player.mode().equals("play")) {
            log.info("PLAYER: " + player + " MODE: " + " PLAY CONTINUE: ");
            player.play(channel);
            return;
        }
        // 5. если колонка не играет - разбудить, установить громкость,
        log.info("PLAYER: " + player + "MODE: " + "mode" + " WAKE - PRESET - PLAY CHANNEL");
        player.wake();
        Action.preset(name, "low");
        player.play(channel);  // 6. включить канал
    }


    // Алиса включи музыку
    // проверить в черном списке - если да то ничего неделать
    // обновить колонки на сервере
    // проверить что колонка есть на сервере - если нет - ничего
    // если колонка играет - ничего. продолжит играть
    // если колонка не играет - разбудить, установить громкость,
    // найти играющую - если есть подключиться к ней
    // если нет играющей - играть последнее, если нет то избранное1
    public static void turnOnMusic(String name) {
        Player player = Player.instance(name);
        log.info("TURN ON PLAYER: " + name);
        if (player.black) return; // проверить в черном списке - если да то ничего неделать
//        serverLMS.updatePlayers();  // обновить колонки на сервере
        if (!serverLMS.players.contains(player)) return;  // проверить что колонка есть на сервере - если нет - ничего
        if (player.mode().equals("play")) return; // если колонка играет - ничего. продолжит играть
        // если колонка не играет - разбудить, установить громкость,
        log.info("not play - wake - set preset - search playing - try sync - else play last - else play fav1");
        player.wake();  // TODO
        player.volume("4"); // TODO
        Player playing = Action.playing(); // найти играющую - если есть подключиться к ней
        if (playing != null) {
            player.sync(playing.name); // подключиться к играющей
            return;
        }
        // если нет играющей - играть последнее, если нет то избранное1
        if (Player.pathLast != null) {
            Player.play(player.name, String.valueOf(Integer.valueOf(Player.pathLast)));  // играть последнее
        } else if (Favorites.checkExists(2)) {
            Player.play(player.name, 2);  // играть избранное 1
        }
    }

    // Алиса выключи музыку - выключиться музыка везде
    public static void turnOffMusic() {
        serverLMS.players.stream().forEach(player -> player.pause());
    }

    public static void turnOnSpeaker() {
        // Алиса включи колонку - подключить к играющей или играть последнее
    }

    public static void turnOffSpeaker() {
        // Алиса выключи колонку - отключить колонку
    }

    public static void allLow() {
        // Алиса все тихо
        serverLMS.players.stream().forEach(player -> player.volume("5"));
    }

    public static void allHigh() {
        // Алиса все громко
        serverLMS.players.stream().forEach(player -> player.volume("20"));
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

    public static void updatePlayers() {
        serverLMS.updatePlayers();
    }

    public static void updateFavorites() {
        serverLMS.updatePlayers();
    }
}
