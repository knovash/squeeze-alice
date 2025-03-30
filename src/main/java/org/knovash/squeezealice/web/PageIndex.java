package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Hive;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.provider.Yandex.yandexMusicDevCounter;
import static org.knovash.squeezealice.provider.Yandex.yandexMusicDevListRooms;

@Log4j2
public class PageIndex {

    public static String msgLmsIp;
    public static String msgLmsPlayers;
    public static String msgSqa = "SQA добавьте плееры http://localhost:8010/players";
    public static String msgUdy = "УДЯ подключите акаунт http://localhost:8010/yandex";


    public static Context action(Context context) {
        context.bodyResponse = page();
        context.code = 200;
        return context;
    }

    public static void refresh(HashMap<String, String> parameters) {
        log.info("REFRESH");
//        lmsPlayers.searchForLmsIp();
        lmsPlayers.updateLmsPlayers();

        Yandex.read();
        Yandex.getRoomsAndDevices();
    }

    public static String page() {

        if (lmsServerOnline) {
            msgLmsIp = "LMS найден " + "<a href=\"http://" + config.lmsIp + ":"
                    + config.lmsPort + "\">" + config.lmsIp + ":" + config.lmsPort + "</a>";

            msgLmsPlayers = "LMS плееры " + lmsPlayers.players.stream().map(player -> player.name).collect(Collectors.toList());
        } else {
            msgLmsIp = "LMS сервер не найден";
            msgLmsPlayers = "LMS плееры не найдены";
        }

        if (SmartHome.devices.size() == 0)
            msgSqa = "SA для плееров LMS не выбраны комнаты УДЯ. Перейдите в настройку плееров и выберите комныты для плееров";
        else
            msgSqa = "SA подключено " + SmartHome.devices.size() + " плееров " + SmartHome.devices.stream().map(d ->
                            lmsPlayers.getPlayerNameByDeviceId(d.id) + " в " + d.room)
                    .collect(Collectors.toList());
        if (yandexMusicDevCounter == 0) {
            if (SmartHome.devices.size() == 0)
                PageIndex.msgUdy = "УДЯ нет плееров";
            else PageIndex.msgUdy = "УДЯ нет плееров. Обновите список устройств навыка в приложениии УДЯ";
            log.info(PageIndex.msgUdy);
        } else
            PageIndex.msgUdy = "УДЯ подключено " + yandexMusicDevCounter + " устройств Музыка в комнатах "
                    + yandexMusicDevListRooms;


        Utils.readAliceIdInRooms();
        String pageInner = statusBar() +
                "<p><a href=\\html\\manual " +
                "target=\"_blank\" rel=\"noopener noreferrer\"" +
                ">Инструкция</a></p>" +
                "<p><a href=\\lms>Настройка LMS</a></p>" +
                "<p><a href=\\players>Настройка плееров</a></p>" +
//                "<p><a href=\"/hive\">Настройка MQTT</a></p>" +
                "<p><a href=\\yandex>Настройка Yandex</a></p>" +
                "<p><a href=\\spotify>Настройка Spotify</a></p>" +
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
                "Алиса, включи пульт <br>";


        String page = pageOuter(pageInner, "Squeeze-Alice", "Squeeze-Alice");

        return page;
    }

    public static String statusBar() {
        log.info("PAGE STATUSBAR START");

        if (lmsServerOnline) {
            PageIndex.msgLmsIp = "LMS " + config.lmsIp;
            PageIndex.msgLmsPlayers = "LMS плееры " + lmsPlayers.players.stream().map(player -> player.name).collect(Collectors.toList());
        } else {
            PageIndex.msgLmsIp = "LMS не найден";
            PageIndex.msgLmsPlayers = "LMS плееры не найдены";
        }


        List<String> aliceInRoomsList = idRooms.entrySet().stream().map(rooms -> {
            try {
                return URLDecoder.decode(rooms.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        String aliceInRooms = "SA колонки Алиса подключены в комнатах " + aliceInRoomsList;
        if (aliceInRoomsList.size() == 0) {

            aliceInRooms = "SA Навык еще незнает в какой он комнате. Скажите навыку Это комната Гостиная";
        }


        String bar = "<fieldset>" +
                "<legend><b>" + "Информация" + "</b></legend>" +

                PageIndex.msgLmsIp + "<br>" +
                PageIndex.msgLmsPlayers + "<br>" +
                PageIndex.msgSqa + "<br>" + aliceInRooms + "<br>" +

                "УДЯ все комнаты " + rooms + "<br>" +
                PageIndex.msgUdy + "<br>" +
                "Пользователь: " + Hive.hiveUserId +

                "<form method='POST' action='/form'>" +
                "<input name='action' type='hidden'  value='statusbar_refresh'>" +
                "<button type='submit'>обновить</button>" +
                "</form>" +

                "</fieldset>";
        return bar;
    }


    public static String pageOuter(String pageInner, String title, String header) {
        String page = "<!DOCTYPE html><html lang=\"ru\">" + // Изменили lang на "ru"
                "<head>" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
                "<meta charset=\"UTF-8\">" +
                "<title>" + title + " local</title>" +
                "</head>" +

                "<body>" +
                "<p><a href=\"/\">Home</a></p>" +
                "  <h2>" + header + "</h2>" +
                pageInner +
                "<p><a href=\"/\">Home</a></p>" +
                "</body></html>";
        return page;
    }
}