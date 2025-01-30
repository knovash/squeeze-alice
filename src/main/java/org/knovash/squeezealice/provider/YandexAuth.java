package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.Headers;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

@Log4j2
public class YandexAuth {

    public static Context action(Context context) {
        log.info("/AUTH");
        context.json = "REDIRECT";
        context.code = 302;
        log.info("REDIRECT: " + context.queryMap.get("redirect_uri"));
        log.info("STATE: " + context.queryMap.get("state"));
        String location = context.queryMap.get("redirect_uri") + "?" + // обязательно (если рандом стр - ошибка)
                "state=" + context.queryMap.get("state") +  // обязательно (если рандом стр - ошибка)
//              "&client_id=" + context.queryMap.get("client_id") + // необязательно любой стринг
                "&code=" + "scope"; // обязательно! неважночто
        log.info("REDIRECTURI: " + location);
        Headers headers = new Headers();
        headers.add("Location", location);
        context.headers = headers;
        return context;
    }
}