package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;
import org.knovash.squeezealice.voice.VoiceActions;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class SwitchQueryCommand {

    private static Player player;

    public static Context action(Context context) {
        HashMap<String, String> queryParams = context.queryMap;
        log.info("QUERY: " + queryParams);

        context.bodyResponse = "BAD REQUEST NO ACTION IN QUERY";
        if (!queryParams.containsKey("action")) return context;
        context.code = 200;
        String action = queryParams.get("action");
        String playerInQuery = queryParams.get("player");
        String roomInQuery = queryParams.get("room");
        log.info("PLAYER: " + playerInQuery + " ROOM: " + roomInQuery);
        player = null;
        if (playerInQuery != null) {
            if (playerInQuery.equals("btremote")) {
                log.info("BT PLAYER: " + lmsPlayers.btPlayerName);
                playerInQuery = lmsPlayers.btPlayerName;
            }
// проверка что пришло, имя плеера или комнаты, если пришла комната - взять плеер по комнате
// найти плеер по похожему имени
            player = lmsPlayers.playerByNearestName(playerInQuery);
// если плеер не найден попробовать взять плеер по похожему названию комнаты
            if (player == null) player = lmsPlayers.playerByNearestRoom(playerInQuery);
        }

        String value = queryParams.get("value");
        String playerName;
        String roomName;
        String response = "null";

// if (player == null) return null;

        switch (action) {

// такиеже методы в Провайдере
// player.volumeRelativeOrAbsolute(value, relative)
// player.playChannelRelativeOrAbsolute(value, relative)
// player.turnOnMusic()
// player.turnOffMusic()

// управление с пульта или виджетов таскер
// респонс для отображения действия на телевизоре или планшете
            case ("volume_dn"):
                response = player.volumeRelativeOrAbsolute("-3", true);
// player.saveLastTimePathAutoremoteRequest();
                break;
            case ("volume_up"):
                response = player.volumeRelativeOrAbsolute("3", true);
// player.saveLastTimePathAutoremoteRequest();
                break;
            case ("channel"):
// response = player.playChannelRelativeOrAbsolute(value, false);
                CompletableFuture.runAsync(() -> player.playChannelRelativeOrAbsolute(value, false, null))
                        .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());
                response = player.name + " - play channel " + value;
// player.saveLastTimePathAutoremoteRequest();
                break;
            case ("play"):
                CompletableFuture.runAsync(() -> player.turnOnMusic(null))
                        .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());
                response = player.name + " - play";
                break;
            case ("toggle_music"):
                CompletableFuture.runAsync(() -> player.toggleMusic())
                        .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());
                response = player.name + " - play/pause";
                break;
            case ("stop_all"):
                lmsPlayers.turnOffMusicAll();
                response = "All players - Stop";
                break;
            case ("next"):
                CompletableFuture.runAsync(() -> player.ctrlNextChannelOrTrack())
                        .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());
                response = player.name + " - Next";
                break;
            case ("prev"):
                CompletableFuture.runAsync(() -> player.ctrlPrevChannelOrTrack())
                        .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());
                response = player.name + " - Prev";
                break;
            case ("next_track"):
                CompletableFuture.runAsync(() -> player.ctrlNextTrack())
                        .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());
                response = player.name + " - Next track";
                break;
            case ("prev_track"):
                CompletableFuture.runAsync(() -> player.ctrlPrevTrack())
                        .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());
                response = player.name + " - Next track";
                break;
            case ("next_channel"):
                CompletableFuture.runAsync(() -> player.ctrlNextChannel())
                        .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());
                response = player.name + " - Next channel - " + player.title;
                break;
            case ("prev_channel"):
                CompletableFuture.runAsync(() -> player.ctrlPrevChannel())
                        .thenRunAsync(() -> player.saveLastTimePathAutoremoteRequest());
                response = player.name + " - Prev channel - " + player.title;
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
                VoiceActions.syncSwitchToHere(player);
                response = "Switch music to " + player.name;
                break;
            case ("select"):
                roomName = Utils.getCorrectRoomName(roomInQuery);
                playerName = Utils.getCorrectPlayerName(playerInQuery);
                if (SwitchVoiceCommand.selectPlayerInRoom(playerName, roomName, true) != null) response = "SELECT OK";
                else response = "SELECT ERROR";
                break;
            case ("shuffle_on"):
                player.shuffleOn();
                response = "SHUFFLE ON";
                break;
            case ("shuffle_off"):
                player.shuffleOff();
                response = "SHUFFLE OFF";
                break;
            case ("favorites_add"):
                log.info("CASE FAVORITES ADD");
                player.favoritesAdd();
                response = "FAVORITES ADD";
                break;
            case ("get_room_player"):
// Таскер по названию виджета вернуть комнату и плеер
                response = lmsPlayers.playerNameByWidgetName(value);
                break;
            case ("get_refresh_json"):
// Таскер для виджетов иконок плееров
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