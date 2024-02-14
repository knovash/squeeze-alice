package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class PageSpeakers {

    public static Context action(Context context) {
        log.info("UPDATE LMS PLAYERS FROM LMS");
        lmsPlayers.update();
        log.info("PLAYERS: " + lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList()));
        lmsPlayers.write();
        String json = Html.formSpeakers();
        context.json = json;
        context.code = 200;
        return context;
    }
}

