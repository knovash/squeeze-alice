package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.web.PageIndex.pageOuter;

@Log4j2
public class PageYandex {

    public static Context action(Context context) {
        context.bodyResponse = page();
        context.code = 200;
        return context;
    }

    public static String page() {
        String loginMessage = "Для подключения введите почту вашего Яндекс аккаунта user@yandex.ru";
        String placeholder = "user@yandex.ru";
        if (config.yandexUid != null && !config.yandexUid.equals("")) {
            loginMessage = "Вы вошли с аккаунтом: " + config.yandexUid;
            placeholder = config.yandexUid;
        }
        String pageInner = "<br>" +
                loginMessage +
                "<form method='POST' action='/form'>" +
                "<input name='email' placeholder='" + placeholder + "' required><br>" +
                "<input name='action' type='hidden'  value='hive_save_email'>" +
                "<button type='submit'>Сохранить</button>" +
                "</form>" +
                "<br>" +
                "";
        String page = pageOuter(pageInner, "Настройка Yandex", "Настройка Yandex");
        return page;
    }
}

