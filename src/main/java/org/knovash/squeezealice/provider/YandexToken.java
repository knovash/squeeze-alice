package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

@Log4j2
public class YandexToken {

    public static Context action(Context context) {
        String json = " {\"access_token\":\"" + "token12345" + "\",\"token_type\":\"bearer\",\"expires_in\":4294967296}";
        log.info("JSON"+json);
        context.bodyResponse = json;
        context.code = 200;
        return context;
    }
}