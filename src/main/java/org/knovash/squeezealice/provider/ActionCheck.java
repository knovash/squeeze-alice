package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ActionCheck {

    public static Context action(Context context) {
        log.info("");
        // ответить 200 OK
        context.json = "OK";
        context.code = 200;
        return context;
    }
}