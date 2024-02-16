package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.Headers;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;



@Log4j2
public class YandexAuth {
// state=https://social.yandex.ru/broker2/authz_in_web/17b50a9fa5af4dd286a0275fb5a57976/callback&

    public static Context action(Context context) {

        log.info("/AUTH");
        context.json = "REDIRECT";
        context.code = 302;
// из QUERY на /auth добавить в HEADERS "Location"
        String location = context.queryMap.get("redirect_uri") + "?" + // обязательно (если рандом стр - ошибка)
// redirect_uri=https://social.yandex.net/broker/redirect&
                "state=" + context.queryMap.get("state") +  // обязательно (если рандом стр - ошибка)
//                        "&client_id=" + context.queryMap.get("client_id") + // необязательно любой стринг
// client_id=0d17cba2ab25----ddcedabc4191
                "&code=" + "scope"; // обязательно! неважночто

        log.info("REDIRECTURI: " + location);
        Headers headers = new Headers();
        headers.add("Location", location);
        context.headers = headers;
        return context;
    }
}