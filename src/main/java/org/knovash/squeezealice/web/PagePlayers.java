package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

@Log4j2
public class PagePlayers {

    public static Context action(Context context) {
        log.info("PAGE SPEAKERS");
        String json = Html.formPlayers();
        context.json = json;
        context.code = 200;
        return context;
    }
}

