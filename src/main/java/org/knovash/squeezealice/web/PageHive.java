package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Hive;

import static org.knovash.squeezealice.web.PageIndex.pageOuter;

@Log4j2
public class PageHive {

    public static Context action(Context context) {
        String json = page();
        context.bodyResponse = json;
        context.code = 200;
        return context;
    }

    public static String page() {
        String pageInner =

                "<p>Введите уникальное имя для подключения к MQTT брокеру</p> \n" +

                "<form method='POST' action='/form'>" +
                "<input name='name' placeholder='"+ Hive.hiveUserId+ "' required> Имя<br>" +
                "<input name='action' type='hidden'  value='hive_save_user'>" +
                "<button type='submit'>Сохранить</button>" +
                "</form>" +

                "<br>";
        String page = pageOuter(pageInner, "Настройка MQTT брокера", "Настройка MQTT брокера");
        return page;
    }
}

