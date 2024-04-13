package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

@Log4j2
public class PageDevices {

    public static Context action(Context context) {
        log.info("PAGE SPEAKERS");
        String json = Html.formSpeakers();
        context.json = json;
        context.code = 200;
        return context;
    }
}

