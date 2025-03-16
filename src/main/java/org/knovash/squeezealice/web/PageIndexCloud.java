package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.utils.Utils;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class PageIndexCloud {

    public static String msgLms = "LMS сервер не найден https://lyrion.org";
    public static String msgSqa = "SQA добавьте плееры http://localhost:8010/players";
    public static String msgUdy = "УДЯ подключите акаунт http://localhost:8010/yandex";


    public static Context action(Context context) {
        log.info("ACTION");
        String response = page();
        context.bodyResponse = response;
        context.code = 200;
        return context;
    }

    public static void refresh() {
        log.info("REFRESH");
        log.info("LMS IP: " + config.lmsIp);
        if (config.lmsIp == null) Utils.searchLmsIp();

        log.info("REFRESH --- LMS");
        lmsPlayers.updateServerStatus();

        log.info("REFRESH --- UDY");
        Yandex.getRoomsAndDevices();
    }

    public static String page() {
        log.info("PAGE INDEX");
        Utils.readIdRooms();
        String page = "<!doctype html><html lang=\"ru\">\n" +
                "<head>\n" +
                "<meta charSet=\"utf-8\" />\n" +
                "<title>Squeeze-Alice</title>" +
                "</head>\n" +
                "<body> \n" +
                "<p><strong>Squeeze-Alice</strong></p> \n" +


                "<p><a href=\\refresh>222222222222222222222</a></p> \n" +
                "<p><a href=\\players>Настрffffffffffffffойка колонок</a></p> \n" +
                "<p><a href=\\spotify>Настрfffffffffffffffffойка New</a></p> \n" +
                "<p><a href=\\spotify>Настffffffffffffffffffройка Spotify</a></p> \n" +
                "<p><a href=\\yandex>Настройка Yandex</a></p> \n" +
                "<p><a href=\\cmd?action=state_devices>Посмотреть настройки Devices</a></p> \n" +
                "<p><a href=\\cmd?action=state_players>Посмотреть настройки Players</a></p> \n" +


                "<p><b>" + "Комманды:</b></p>" +
                "<p>" +
                "Алиса, что играет<br>" +
                "Алиса, включи музыку<br>" +
                "Алиса, выключи музыку<br>" +
                "Алиса, громче<br>" +
                "Алиса, музыку громче на 5<br>" +
                "Алиса, канал 4<br>" +
                "Алиса, переключи канал<br>" +
                "Алиса, скажи раз-два, включи Кровосток<br>" +
                "Алиса, дальше<br>" +
                "Алиса, скажи раз-два, добавь в избранное<br>" +
                "Алиса, переключи музыку сюда<br>" +
                "Алиса, включи отдельно<br>" +
                "Алиса, включи только тут<br>" +
                "Алиса, включи вместе<br>" +
                "Алиса, скажи раз-два, выбери колонку Радиотехника<br>" +
                "Алиса, где пульт<br>" +
                "Алиса, включи пульт <br>" +
                "<p></p>" +
                "</body>\n" +
                "</html>";
        log.info("OK");
        return page;
    }
}