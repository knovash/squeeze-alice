package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ActionUserUnlink {

    public static Context action(Context context) {
        log.info("");
        String xRequestId = context.xRequestId;

        String json = "{\"request_id\":\"" + xRequestId + "\"}";
        log.info("RESPONSE: " + json);

        context.json = json;
        context.code = 200;
        return context;
    }
}