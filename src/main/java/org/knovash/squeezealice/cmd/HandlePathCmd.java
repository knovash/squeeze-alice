package org.knovash.squeezealice.cmd;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Player;

import java.util.HashMap;
import java.util.Set;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class HandlePathCmd {

// Комманды /cmd приходят из Таскер или с пульта
    public static Context action(Context context) {
        log.info(start);
        HashMap<String, String> queryParams = context.queryMap;
        context.bodyResponse = "BAD REQUEST NO ACTION IN QUERY";
        if (!queryParams.containsKey("action")) return context;
        context.code = 200;
        String action = queryParams.get("action");
        String playerName = queryParams.get("player");
        String room = queryParams.get("room");
        String value = queryParams.get("value");
        String volume = queryParams.get("volume");
        String response;
        log.info("QUERY PARAMS: PLAYER: " + playerName + " ROOM: " + room + " ACTION: " + action + " VALUE: " + value + " VOLUME: " + volume);
        if (context.body != null) log.info("BODY: " + context.body);

        if (value != null) {
            value = value.toLowerCase();
            value = value.replace("+", " ");
        }

        Set<String> UpdateActions = Set.of(
                "turn_on_music",
                "play",
                "play_pause",
                "toggle_music",
                "switch_here",
                "separate_on",
                "separate_off",
                "next",
                "prev",
                "whats_playing",
                "title",
                "favorites_add"
        );
        if (UpdateActions.contains(action))
            lmsPlayers.updatePlayers(); // обновить состояние плееров

        // действия где плеер не требуется
        response = CommandsWithoutPlayer.run(action, value, context.body);

        if (response != null) {
            context.bodyResponse = response;
            return context;
        }

        // определение плеера по имени плеера или имени комнаты
        Player player = lmsPlayers.playerByPlayerNameOrRoomName(playerName, room);
        log.info("COMMAND TO PLAYER: " + player);

        // действия где плеер требуется
        response = CommandsWithPlayer.run(action, player, value);

        if (response == null) response = "ERROR";
        lmsPlayers.write();
        context.bodyResponse = response;
        log.info(finish);
        log.info("");
        return context;
    }
}