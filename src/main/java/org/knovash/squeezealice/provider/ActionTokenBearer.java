package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.Headers;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ActionTokenBearer {

    public static Context action(Context context) {
        String body = context.body;
        Headers headers = context.headers;
        String path = context.path;
        String xRequestId = context.xRequestId;

        String bearer = Yandex.getBearerToken();
        String json = "YANDEX TOKEN bearer = " + bearer;

        context.json = json;
        context.code = 200;
        return context;
    }
}

