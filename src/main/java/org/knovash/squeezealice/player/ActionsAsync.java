package org.knovash.squeezealice.player;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.Tasker;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.yandex.Yandex;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.Main.smartHome;
import static org.knovash.squeezealice.web.SettingsVoice.KeyToggle.VOICE_TOGGLE_LMS_SAY_CHANNEL_NAME;

@Log4j2
public class ActionsAsync {


    // Spotify
    public static String spotifyPlayCommand(Player player, String command, String type) {
        log.info("PLAY SPOTIFY " + type + " COMMAND: " + command);
        String target = extractTargetFromCommand(command);
        log.info("TARGET: " + target);
        // Запускаем асинхронный поиск в зависимости от типа
        CompletableFuture<Spotify.GetLinkResult> future;
        switch (type) {
            case "artist":
                future = CompletableFuture.supplyAsync(() -> Spotify.getLinkArtist(target));
                break;
            case "album":
                future = CompletableFuture.supplyAsync(() -> Spotify.getLinkAlbum(target));
                break;
            case "track":
                future = CompletableFuture.supplyAsync(() -> Spotify.getLinkTrack(target));
                break;
            case "playlist":
                future = CompletableFuture.supplyAsync(() -> Spotify.getLinkPlaylist(target));
                break;
            default:
                return "ошибка";
        }
        try {
            // Ждём результат не более 1 секунды
            Spotify.GetLinkResult result = future.get(1, SECONDS);
            String link = result.uri;
            String name = result.name;
            String answer = "включаю " + name;
            log.info("LINK: " + link);
            log.info("NAME: " + name);
            log.info("ANSWER: " + answer);
            // Воспроизведение в фоне, чтобы не задерживать ответ
            CompletableFuture.runAsync(() -> {
                if (VOICE_TOGGLE_LMS_SAY_CHANNEL_NAME.value) {
                    player.say(answer, false, true);
                }
                player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false);
                player.playPath(link);
                // обновить Таскер и Яндекс
                Tasker.sendRequestToTaskerRunRefreshAsync(player.name, player.room);
                Yandex.sendDevicesStatesAsync();
            });
            return answer;
        } catch (TimeoutException e) {
            // Не успели за 1 секунду – отвечаем без названия, а воспроизведение отложим
            String fallbackAnswer = "Включаю Spotify";
            // Подписываемся на завершение поиска (без таймаута)
            future.thenAcceptAsync(result -> {
                String link = result.uri;
                String name = result.name;
                String fullAnswer = "включаю " + name;
                // Если включена голосовая подсказка, произносим полное название
                if (VOICE_TOGGLE_LMS_SAY_CHANNEL_NAME.value) {
                    player.say(fullAnswer, false, true);
                }
                player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false);
                player.playPath(link);
                Tasker.sendRequestToTaskerRunRefreshAsync(player.name, player.room);
                Yandex.sendDevicesStatesAsync();
            }).exceptionally(ex -> {
                log.error("Failed to get Spotify link after timeout", ex);
                // Здесь можно добавить дополнительную обработку ошибки воспроизведения
                return null;
            });
            return fallbackAnswer;
        } catch (Exception e) {
            log.error("Error getting Spotify link", e);
            return "Ошибка Spotify";
        }
    }

    // Раз Два это комната ...
    // вернуть ответ: это комната ... с колонкой ...
    public static String selectRoomWithSpeaker(String command, String aliceId) {
        log.info("SELECT ROOM WITH SPEAKER");
        return AsyncExecutor.executeWithTimeout(() -> ActionsSync.selectRoomWithSpeaker(command, aliceId));
    }

    // Раз Два это комната ...
    // вернуть ответ: это комната ... с колонкой ...
    public static String selectRoomByCommand(String command, String aliceId) {
        log.info("SELECT ROOM BY COMMAND");
        return AsyncExecutor.executeWithTimeout(() -> ActionsSync.selectRoomByCommand(command, aliceId), 2, SECONDS);
    }

    // Раз Два выбери колонку ... или включи колонку ...
    //вернуть ответ: в комнате ... выбрана колонка ... или включаю колонку ...
    public static String selectNewPlayerInRoomByCommand(String command, String room, boolean turnOn) {
        log.info("SELECT PLAYER BY COMMAND: " + command + " IN ROOM: " + room);
        return AsyncExecutor.executeWithTimeout(() -> {
            String playerName = ActionsSync.getPlayerNameInRoomByCommand(command);
            return ActionsSync.selectNewPlayerInRoom(playerName, room, turnOn);
        }, 2, SECONDS);
    }

    // Пульт - подключи пульт в ...
    public static String connectBtRemote(String command, Player player) {
        log.info("REMOTE CONNECT COMMAND: " + command + " PLAYER: " + player);
        return AsyncExecutor.executeWithTimeout(() -> ActionsSync.remoteSetFromCommand(command, player));
    }

    // Раз Два что играет?
    // вернуть ответ: сейчас на ... играет ... , громкость ...
    public static String whatsPlayingAnswerForAlice(Player player, boolean addAtPlayerName) {
        log.info("WHATS PLAYING " + player.name);
        return AsyncExecutor.executeWithTimeout(() -> ActionsSync.whatsPlaying(player, addAtPlayerName), 2, SECONDS);
    }

    // Раз Два лимит установить - должен вернуть ответ
    public static String volumeLimitSet(Player player, String command) {
        log.info("VOLUME LIMIT SET ASYNC");
        return AsyncExecutor.executeWithTimeout(() -> ActionsSync.volumeLimitSet(player, command), 2, SECONDS);
    }

    // Раз Два какая громкость - должен вернуть ответ
    public static String whatsVolume(Player player) {
        log.info("WHATS VOLUME ASYNC");
        return AsyncExecutor.executeWithTimeout(() -> {
            String volume = player.volumeGet();
            if (volume != null)
                return "сейчас на " + player.name + " громкость " + volume + ", ограничение " + player.volume_high;
            else return "Не удалось получить громкость";
        }, 2, SECONDS);

        //CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
        //    String volume = player.volumeGet();
        //    if (volume != null)
        //        return "сейчас на " + player.name + " громкость " + volume + ", ограничение " + player.volume_high;
        //    else return "Не удалось получить громкость";
        //});
        //try {
        //    return future.get(1, SECONDS);
        //} catch (TimeoutException e) {
        //    return "Не успела выполнить команду";
        //} catch (Exception e) {
        //    return "Ошибка выполнения команды";
        //}
    }


    // Девайс - Переключи сюда
    public static void switchHere(Player player) {
        CompletableFuture.runAsync(() -> {
            ActionsSync.switchToHere(player);

            Tasker.sendRequestToTaskerRunRefreshAsync(player.name, player.room);
            Yandex.sendDevicesStatesAsync();
            CompletableFuture.runAsync(() -> Yandex.sendDeviceOtherState(SmartHome.deviceByRoom(player.room), "on_off", "on", "false"));
        });
    }

    // Девайс - Отдельно/Вместе
    // Таскер - Отдельно/Вместе
    public static void separateSet(Player player, String action) {
        if ("Вместе".equals(action)) player.separateOff();
        if ("Отдельно".equals(action)) player.separateOn();

        Tasker.sendRequestToTaskerRunRefreshAsync(player.name, player.room);
        Device deviceSepareteOn = smartHome.deviceByName("Отдельно", player.room);
        Device deviceSepareteOff = smartHome.deviceByName("Вместе", player.room);
        CompletableFuture.runAsync(() -> Yandex.sendDeviceOtherState(deviceSepareteOn, "on_off", "on", String.valueOf(player.separate))); // Отдельно
        CompletableFuture.runAsync(() -> Yandex.sendDeviceOtherState(deviceSepareteOff, "on_off", "on", String.valueOf(!player.separate))); // Вместе
    }

    // Девайс - Рандом
    // Таскер - Рандом
    public static void shuffleSet(Player player, boolean stateForRun) {
        if (stateForRun) player.shuffleOn();
        else player.shuffleOff();

        Device deviceShuffle = smartHome.deviceByName("Рандом", player.room);
        CompletableFuture.runAsync(() -> Yandex.sendDeviceOtherState(deviceShuffle, "on_off", "on", String.valueOf(stateForRun)));
    }

    // Девайс - Повтор
    // Таскер - Повтор
    public static void repeatSet(Player player, boolean stateForRun) {
        if (stateForRun) player.repeatOn();
        else player.repeatOff();

        Device deviceRepeat = smartHome.deviceByName("Повтор", player.room);
        CompletableFuture.runAsync(() -> Yandex.sendDeviceOtherState(deviceRepeat, "on_off", "on", String.valueOf(stateForRun)));
    }


    // сказать через LMS - где подключен пульт
    public static void whereRemoteSayToLms(Player player) {
        CompletableFuture.runAsync(() -> player.say("пульт подключен к " + lmsPlayers.btPlayerName, true, true));
    }

    // сказать через LMS - что играет
    public static void whatsPlayingSayToLms(Player player, boolean addAtPlayerName) {
        //if (TOGGLE_WATSPLAYING_TO_LMS.value)
        if (false)
            CompletableFuture.runAsync(() -> {
                player.say(ActionsSync.whatsPlaying(player, addAtPlayerName), true, true); // сказать через колонку LMS
            });
    }


    // Таскер или Пульт
    public static void turnOnMusic(Player player) {
        log.info("TURN ON MUSIC" + player.name);
        CompletableFuture.runAsync(() -> {
            player.turnOnMusic(null);

            Yandex.sendDevicesStatesAsync();
            Tasker.sendRequestToTaskerRunRefreshAsync(player.name, player.room);
        });
    }

    // Таскер или Пульт
    public static void toggleMusic(Player player) {
        log.info("TOGGLE MUSIC " + player.name);
        CompletableFuture.runAsync(() -> {
            player.toggleMusic();

            Yandex.sendDevicesStatesAsync();
            Tasker.sendRequestToTaskerRunRefreshAsync(player.name, player.room);
        });
    }

    // Таскер или Пульт
    public static void stopAll() {
        log.info("STOP ALL PLAYERS");
        lmsPlayers.turnOffMusicAll();

        Yandex.sendDevicesStatesAsync();
        Tasker.sendRequestToTaskerRunRefreshAsync("", "");
    }

    // Девайс - Дальше
    // Таскер или Пульт
    public static void nextTrack(Player player) {
        log.info("NEXT TRACK " + player.name);
        CompletableFuture.runAsync(() -> {
            player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false);
            player.ctrlNextTrack();

            Tasker.sendRequestToTaskerRunRefreshAsync(player.name, player.room);
            CompletableFuture.runAsync(() -> Yandex.sendDeviceOtherState(SmartHome.deviceByRoom(player.room), "on_off", "on", "false"));
            Yandex.sendDevicesStatesAsync();
        });
    }

    // Девайс - Назад
    // Таскер или Пульт
    public static void prevTrack(Player player) {
        log.info("PREV TRACK " + player.name);
        CompletableFuture.runAsync(() -> {
            player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false);
            player.ctrlPrevTrack();

            Tasker.sendRequestToTaskerRunRefreshAsync(player.name, player.room);
            CompletableFuture.runAsync(() -> Yandex.sendDeviceOtherState(SmartHome.deviceByRoom(player.room), "on_off", "on", "false"));
            Yandex.sendDevicesStatesAsync();
        });
    }

    public static void jumpTrack(Player player, String index) {
        log.info("JUMP TRACK " + player.name);
        CompletableFuture.runAsync(() -> {
            player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false);
            player.ctrlJumpTrack(index);

            Tasker.sendRequestToTaskerRunRefreshAsync(player.name, player.room);
            CompletableFuture.runAsync(() -> Yandex.sendDeviceOtherState(SmartHome.deviceByRoom(player.room), "on_off", "on", "false"));
            Yandex.sendDevicesStatesAsync();
        });
    }

    // Таскер или Пульт
    public static void prevChannel(Player player) {
        log.info("PREV CHANNEL " + player.name);
        CompletableFuture.runAsync(() -> {
            player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false);
            player.ctrlPrevChannel();

            Tasker.sendRequestToTaskerRunRefreshAsync(player.name, player.room);
            CompletableFuture.runAsync(() -> Yandex.sendDeviceOtherState(SmartHome.deviceByRoom(player.room), "on_off", "on", "false"));
            Yandex.sendDevicesStatesAsync();
        });
    }

    // Таскер или Пульт
    public static void nextChannel(Player player) {
        log.info("NEXT CHANNEL " + player.name);
        CompletableFuture.runAsync(() -> {
            player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false);
            player.ctrlNextChannel();

            Tasker.sendRequestToTaskerRunRefreshAsync(player.name, player.room);
            CompletableFuture.runAsync(() -> Yandex.sendDeviceOtherState(SmartHome.deviceByRoom(player.room), "on_off", "on", "false"));
            Yandex.sendDevicesStatesAsync();
        });
    }

    // Таскер или Пульт
    public static void prevChannelOrTrack(Player player) {
        log.info("PREV CHANNEL/TRACK " + player.name);
        CompletableFuture.runAsync(() -> {
            player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false);
            player.ctrlPrevChannelOrTrack();

            Tasker.sendRequestToTaskerRunRefreshAsync(player.name, player.room);
            CompletableFuture.runAsync(() -> Yandex.sendDeviceOtherState(SmartHome.deviceByRoom(player.room), "on_off", "on", "false"));
            Yandex.sendDevicesStatesAsync();
        });
    }

    // Таскер или Пульт
    public static void nextChannelOrTrack(Player player) {
        log.info("NEXT CHANNEL/TRACK " + player.name);
        CompletableFuture.runAsync(() -> {
            player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false);
            player.ctrlNextChannelOrTrack();

            Tasker.sendRequestToTaskerRunRefreshAsync(player.name, player.room);
            CompletableFuture.runAsync(() -> Yandex.sendDeviceOtherState(SmartHome.deviceByRoom(player.room), "on_off", "on", "false"));
            Yandex.sendDevicesStatesAsync();
        });
    }

    // Таскер или Пульт
    public static void playChannelIndex(Player player, String value) { // разбудить плеер и потом использовать playChannel(value)
        log.info("PLAY CHANNEL INDEX " + value + " PLAYER " + player.name);
        CompletableFuture.runAsync(() -> {
            // Сказать включаю канал через плеер LMS, если был в группе после уведомления надо вернуть в группу
            if (VOICE_TOGGLE_LMS_SAY_CHANNEL_NAME.value) {
                String channelName = player.favorites().get(Integer.parseInt(value) - 1);
                player.say("включаю канал " + value + " " + channelName, false, true);
            }
            player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false);
            player.playChannel(value);

            Tasker.sendRequestToTaskerRunRefreshAsync(player.name, player.room);
            Yandex.sendDevicesStatesAsync();
        });
    }

    // Таскер или Пульт
    public static void favoritesAdd(Player player) {
        log.info("FAVORITES ADD");
        CompletableFuture.runAsync(() -> player.favoritesAdd());

    }

    // Таскер или Пульт
    public static void remoteSwitchToNextPlayer() {
        log.info("REMOTE SWITCH TO NEXT PLAYER");
        CompletableFuture.runAsync(() -> ActionsSync.remoteSwitch());
    }


    // TODO плохо ищет на англиском, он получает текст на руском и делает транслит
    //public static String channelPlayByName(String command, Player player) {
    //    log.info("CHANNEL PLAY BY NAME");
    //    String target = command.replaceAll(".*(канал|избранное)\\S*\\s", "")
    //            .replaceAll("\"", "").replaceAll("\\s\\s", " ");
    //    log.info("TARGET LMS CHANNEL: " + target);
    //    ActionsSync.answer = "Не успела выполнить команду";
    //    CompletableFuture.runAsync(() -> {
    //        List<String> playlist = player.favorites();
    //        playlist.forEach(log::info);
    //        String latin = Utils.convertCyrilicToLatin(target);
    //        String channel = Levenstein.searchTitleInFavorites(latin, playlist);
    //        log.info("CHANNEL: " + channel);
    //        int index = playlist.indexOf(channel) + 1;
    //        channel = channel.replaceAll(":.*", "");
    //        player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false).playChannel(String.valueOf(index));
    //        ActionsSync.answer = "Включаю канал " + index + ", " + channel;
    //    });
    //    Utils.sleep(2000);
    //    return ActionsSync.answer;
    //}

//     PRIVATE  ------------

    private static String extractTargetFromCommand(String command) {
        log.info("COMMAND: " + command);
        if (command == null) return null;
        String target;
        target = command.trim()
                .toLowerCase();
        target = target
                .replaceAll(".*включи\\S*\\s", "")
                .replaceAll("альбом", "")
                .replaceAll("трэк", "")
                .replaceAll("плэйлист", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");
        log.info("TARGET: " + target);
        return target;
    }


}