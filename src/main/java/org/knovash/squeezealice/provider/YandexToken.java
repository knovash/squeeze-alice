package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

@Log4j2
public class YandexToken {

    public static Context action(Context context) {
        log.info("");
        String json = " {\"access_token\":\"" + SmartHome.bearerToken + "\",\"token_type\":\"bearer\",\"expires_in\":4294967296}";
        context.json = json;
        context.code = 200;
        return context;
    }
}