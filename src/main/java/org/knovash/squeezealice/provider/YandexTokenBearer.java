package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

@Log4j2
public class YandexTokenBearer {

    public static Context action(Context context) {
        String bearer = Yandex.getBearerToken();
        String json = "YANDEX TOKEN bearer = " + bearer;
        context.json = json;
        context.code = 200;
        return context;
    }
}

