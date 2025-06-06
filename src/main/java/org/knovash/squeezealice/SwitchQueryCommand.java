package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.voice.VoiceActions;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class SwitchQueryCommand {

    private static Player player;

    public static Context action(Context context) {
        HashMap<String, String> queryParams = context.queryMap;
        context.bodyResponse = "BAD REQUEST NO ACTION IN QUERY";
        if (!queryParams.containsKey("action")) return context;
        context.code = 200;
        String action = queryParams.get("action");
        String playerInQuery = queryParams.get("player");
        String roomInQuery = queryParams.get("room");
        String value = queryParams.get("value");
        String response = "null";
        log.info("PLAYER: " + playerInQuery + " ROOM: " + roomInQuery);

// проверка что пришло, имя плеера или комнаты, если пришла комната - взять плеер по комнате
// найти плеер по похожему имени
// если плеер не найден попробовать взять плеер по похожему названию комнаты
        player = null;
        if (playerInQuery != null) {
            if (playerInQuery.equals("btremote")) playerInQuery = lmsPlayers.btPlayerName;
            player = lmsPlayers.playerByNearestName(playerInQuery);
            if (player == null) player = lmsPlayers.playerByNearestRoom(playerInQuery);
        }

        log.info("\nUPDATE LMS PLAYERS");
        lmsPlayers.updateLmsPlayers();

// управление с пульта или виджетов таскер
// респонс для отображения действия на телевизоре или планшете
        switch (action) {
            case ("volume_dn"): // TODO неиспользует Таскер
                response = player.volumeRelativeOrAbsolute("-3", true);
                break;
            case ("volume_up"): // TODO неиспользует Таскер
                response = player.volumeRelativeOrAbsolute("3", true);
                break;
            case ("channel"):
                CompletableFuture.runAsync(() -> player.playChannelRelativeOrAbsolute(value, false, null))
                        .thenRunAsync(() -> lmsPlayers.afterAll());
                response = player.name + " - play channel " + value;
                break;
            case ("play"): // TODO неиспользует Таскер
                CompletableFuture.runAsync(() -> player.turnOnMusic(null))
                        .thenRunAsync(() -> lmsPlayers.afterAll());
                response = player.name + " - play";
                break;
            case ("toggle_music"):
                CompletableFuture.runAsync(() -> player.toggleMusic())
                        .thenRunAsync(() -> lmsPlayers.afterAll());
                response = player.name + " - play/pause";
                break;
            case ("stop_all"):
                CompletableFuture.runAsync(() -> lmsPlayers.turnOffMusicAll())
                        .thenRunAsync(() -> lmsPlayers.afterAll());
                response = "All players - Stop";
                break;
            case ("next"):
                CompletableFuture.runAsync(() -> player.ctrlNextChannelOrTrack())
                        .thenRunAsync(() -> lmsPlayers.afterAll());
                response = player.name + " - Next";
                break;
            case ("prev"):
                CompletableFuture.runAsync(() -> player.ctrlPrevChannelOrTrack())
                        .thenRunAsync(() -> lmsPlayers.afterAll());
                response = player.name + " - Prev";
                break;
            case ("next_track"): // TODO неиспользует Таскер
                CompletableFuture.runAsync(() -> player.ctrlNextTrack())
                        .thenRunAsync(() -> lmsPlayers.afterAll());
                response = player.name + " - Next track";
                break;
            case ("prev_track"): // TODO неиспользует Таскер
                CompletableFuture.runAsync(() -> player.ctrlPrevTrack())
                        .thenRunAsync(() -> lmsPlayers.afterAll());
                response = player.name + " - Next track";
                break;
            case ("next_channel"): // TODO неиспользует Таскер
                CompletableFuture.runAsync(() -> player.ctrlNextChannel())
                        .thenRunAsync(() -> lmsPlayers.afterAll());
                response = player.name + " - Next channel";
                break;
            case ("prev_channel"): // TODO неиспользует Таскер
                CompletableFuture.runAsync(() -> player.ctrlPrevChannel())
                        .thenRunAsync(() -> lmsPlayers.afterAll());
                response = player.name + " - Prev channel";
                break;
            case ("separate_on"):
                VoiceActions.separateOn(player);
                response = "Separate On";
                break;
            case ("separate_off"):
                VoiceActions.separateOff(player);
                response = "Separate Off";
                break;
            case ("switch_here"):
                CompletableFuture.runAsync(() -> VoiceActions.syncSwitchToHere(player))
                        .thenRunAsync(() -> lmsPlayers.afterAll());
                response = "Switch music to " + player.name;
                break;
            case ("shuffle_on"):
                CompletableFuture.runAsync(() -> player.shuffleOn())
                        .thenRunAsync(() -> lmsPlayers.afterAll());
                response = "SHUFFLE ON";
                break;
            case ("shuffle_off"):
                CompletableFuture.runAsync(() -> player.shuffleOff())
                        .thenRunAsync(() -> lmsPlayers.afterAll());
                response = "SHUFFLE OFF";
                break;
            case ("favorites_add"):
                log.info("CASE FAVORITES ADD");
                CompletableFuture.runAsync(() -> player.favoritesAdd())
                        .thenRunAsync(() -> lmsPlayers.afterAll());
                response = "FAVORITES ADD";
                break;
            case ("get_room_player"): // Таскер по названию виджета вернуть комнату и плеер при активации нового виджета
                response = lmsPlayers.playerNameByWidgetName(value);
                break;
            case ("get_refresh_json"): // Таскер для виджетов иконок плееров
                response = lmsPlayers.forTaskerWidgetsRefreshJson(player, value);
                break;
            default:
                log.info("ACTION NOT FOUND: " + action);
                response = "ACTION NOT FOUND: " + action;
                break;
        }
        if (response != "null") {
            context.bodyResponse = response;
            return context;
        }
        context.bodyResponse = response;
        return context;
    }
}