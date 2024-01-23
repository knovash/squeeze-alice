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
// из QUERY на /auth добавить в HEADERS "Location"
        String location = context.queryMap.get("redirect_uri") + "?" + // обязательно (если рандом стр - ошибка)
// redirect_uri=https://social.yandex.net/broker/redirect&
                "state=" + context.queryMap.get("state") +  // обязательно (если рандом стр - ошибка)
// state=https://social.yandex.ru/broker2/authz_in_web/17b50a9fa5af4dd286a0275fb5a57976/callback&
//                        "&client_id=" + context.queryMap.get("client_id") + // необязательно любой стринг
// client_id=0d17cba2ab25----ddcedabc4191
                "&code=" + "scope"; // обязательно! неважночто
// scope=12345&

// location если = "redirect_uri" то при привязке показывает index.html и всё. фэйл
// String location = ""; // если так то в Алисе не откроется Привязать.
// откроется пустая стр с надписью из json REDIRECT
        log.info("REDIRECTURI: " + location);
        Headers lll = new Headers();
        lll.add("Location", location);
//                httpExchange.getResponseHeaders().add("Location", location);
        context.headers = lll;
//        httpExchange.getResponseHeaders().putAll(lll);
        return context;
    }
}