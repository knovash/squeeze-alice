package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

import static org.knovash.squeezealice.provider.Yandex.yandex;

@Log4j2
public class YandexToken {

    public static Context action(Context context) {
        log.info("");
        Yandex.getBearerToken();
        String json = " {\"access_token\":\"" + yandex.bearer + "\",\"token_type\":\"bearer\",\"expires_in\":4294967296}";

        context.json = json;
        context.code = 200;
        return context;
    }
}