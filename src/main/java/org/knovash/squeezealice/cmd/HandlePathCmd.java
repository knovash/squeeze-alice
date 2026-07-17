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
        String response = null;
        log.info("QUERY PARAMS: PLAYER: " + playerName + " ROOM: " + room + " ACTION: " + action + " VALUE: " + value + " VOLUME: " + volume);

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

// выбор действия без плеера
        response = SwitchNoPlayerCommand.run(action);

        if (response != null) {
            context.bodyResponse = response;
            return context;
        }

        Player player = lmsPlayers.playerByPlayerNameOrRoomName(playerName, room);
        log.info("COMMAND TO PLAYER: " + player);

// выбор действия с плеером
        response = SwitchPlayerCommand.run(action, player, value);

        if (response == null) {
            log.info("COMMAND ACTION NOT FOUND: " + action);
            context.bodyResponse = response;
            response = "COMMAND ACTION NOT FOUND: " + action;
        }

        lmsPlayers.write();

        context.bodyResponse = response;
        log.info(finish);
        log.info("");
        return context;
    }

}