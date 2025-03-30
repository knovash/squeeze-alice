package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Hive;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.knovash.squeezealice.provider.Yandex.yandex;
import static org.knovash.squeezealice.web.PageIndex.pageOuter;

@Log4j2
public class PageYandex {

    public static Context action(Context context) {
        context.bodyResponse = page();
        context.code = 200;
        return context;
    }

    public static String page() {
        String clientId = "9aa97fffe29849bb945db5b82b3ee015";
        String redirectUri = "http://alice-lms.zeabur.app/callback";
        String authUrl = "https://oauth.yandex.ru/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&force_confirm=true" +
//                "&scope=yandex:passport:access" + // Добавьте необходимый scope
//                "&scope=yandex:passport:access+yandex:cloud:access" // <-- Добавлен scope
                "";


        String pageInner = "<br>" +
                "<a href=\"" + authUrl + "\" target=\"_blank\" rel=\"noopener noreferrer\">" +
                "Войти в Yandex и получить User ID</a>" +
                "<br>" +

                "<p>Введите User ID</p> \n" +

                "<form method='POST' action='/form'>" +
                "<input name='name' placeholder='" + Hive.hiveUserId + "' required> Имя<br>" +
                "<input name='action' type='hidden'  value='hive_save_user'>" +
                "<button type='submit'>Сохранить</button>" +
                "</form>" +

                "";

        String page = pageOuter(pageInner, "Настройка Yandex", "Настройка Yandex");
        return page;
    }

}

