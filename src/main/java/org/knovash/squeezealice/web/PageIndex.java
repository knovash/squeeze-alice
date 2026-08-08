package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.yandex.Yandex;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.LmsSearchForIp.isLmsServer;
import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.yandex.Yandex.otherDevices;

@Log4j2
public class PageIndex {

    public static Context action(Context context) {
        context.bodyResponse = page();
        context.code = 200;
        return context;
    }

    public static void refresh(HashMap<String, String> parameters) {
        log.info("REFRESH");

        if (!isLmsServer(config.lmsIp, Integer.parseInt(config.lmsPort))) {
            log.info("SEARCH FOR LMS IP...");
            lmsPlayers.searchForLmsIp();
        } else
            log.info("LMS IP " + config.lmsIp);


        if (lmsServerOnline) {
            log.info("LMS ONLINE - UPDATE PLAYERS");
            lmsPlayers.updatePlayers();
        } else
            log.info("LMS OFFLINE - SKIP UPDATE PLAYERS");


        if (config.yandexName != null) {
            yandexInfoDevices = Yandex.devicesGetFromYandexInfo(); // получить устройства "Музыка" которые уже есть в Яндексе
            log.info("YANDEX get yandexInfoDevices: " + yandexInfoDevices);
            //Yandex.createDevicesFromYandexDevices(yandexInfoDevices); // создать устройства "Музыка" в сервисе для Яндекса
            //Yandex.createOtherDevicesFromYandexDevices(otherDevices); // создать устройства  в сервисе для Яндекса // TODO
        } else
            log.info("YANDEX NOT LOGGED");

        //Yandex.devicesGetFromYandexInfo();
    }

    public static String page() {
        //Yandex.devicesGetFromYandexInfo();
        //Utils.readRoomsAndAliceIds();
        String pageInner = statusBar() +
                "<p><a href=\\html\\manual " +
                "target=\"_blank\" rel=\"noopener noreferrer\"" +
                ">Инструкция</a></p>" +
                "<p><a href=\\lms>Настройка LMS</a></p>" +
                "<p><a href=\\players>Настройка плееров</a></p>" +
                "<p><a href=\\settings>Настройки дополнительные</a></p>" +
                "<p><a href=\\settings_buttons>Настройки создания устройств действий</a></p>" +
                "<p><a href=\\actions>Тесты действий</a></p>" +

                "<p><a href=\\auth " + "target=\"_blank\" rel=\"noopener noreferrer\"" + ">Авторизация в Яндекс</a></p>" +

                "<p><a href=\\auth_spotify " + "target=\"_blank\" rel=\"noopener noreferrer\"" + ">Авторизация в Spotify</a></p>" +


                "";
        String page = pageOuter(pageInner, "Squeeze-Alice", "Squeeze-Alice");
        return page;
    }

    public static String statusBar() {
        log.info("PAGE STATUSBAR START");
        String bar = "<fieldset>" +
                "<legend><b>" + "Информация" + "</b></legend>" +
                "LMS: " + infoLmsIpStatus() + "<br>" +
                "LMS плееры: " + infoLmsPlayers() + "<br>" +
                "Yandex пользователь: " + infoUserYandex() + "<br>" +
                "Yandex комнаты: " + indexYandexRooms() + "<br>" +
                "Yandex устройства Музыка: " + indexMusicDevicesYanex() + "<br>" +
                "Устройства Музыка тут: " + indexMusicDevicesLocal() + "<br>" +
                "Spotify: " + infoUserSpoty() + "<br>" +
                "Hive mqtt: " + infoHiveStatus() + "<br>" +
                "<form method='POST' action='/form'>" +
                "<input name='action' type='hidden'  value='statusbar_refresh'>" +
                "<button type='submit'>обновить</button>" +
                "</form>" +
                "</fieldset>";
        return bar;
    }

    private static String infoUserSpoty() {
        long now = System.currentTimeMillis();
        long expiresAt = Main.config.spotifyTokenExpiresAt;
        long timeLeft = expiresAt - now;

        Duration d = Duration.ofMillis(timeLeft);
        long hours = d.toHours();
        long minutes = d.toMinutesPart();   // Java 9+
        long seconds = d.toSecondsPart();
        //return hours + "ч " + minutes + "м " + seconds + "с";

        if (config.spotifyToken != null) return "<span style='color: green;'>" + " подключен" + "</span>" + (" " +hours + "ч " + minutes + "м ");
        else return
                "<a href=\\auth_spotify " + "target=\"_blank\" rel=\"noopener noreferrer\"" + ">Авторизация в Spotify</a>";
    }

    private static String infoUserYandex() {
        if (config.yandexName != null) return config.yandexName;
        else return
                "<a href=\\auth " + "target=\"_blank\" rel=\"noopener noreferrer\"" + ">Авторизация в Яндекс</a>";
    }


    private static String infoLmsIpStatus() {
        if (isLmsServer(config.lmsIp, Integer.parseInt(config.lmsPort)))
            return config.lmsIp + "<span style='color: green;'>" + " подключен" + "</span>";
        else
            return "<span style='color: red;'>" + " отключен" + "</span>";
    }

    private static String infoHiveStatus() {
        if (hive.mqttClient.isConnected())
            return "<span style='color: green;'>" + "подключен" + "</span>";
        else
            return "<span style='color: red;'>" + "отключен" + "</span>" + " проверьте " + config.hiveBroker;
    }

    private static String indexYandexRooms() {
        if (Yandex.rooms == null || Yandex.rooms.isEmpty()) return "нет";
        else return Yandex.rooms.toString();
    }

    private static String indexMusicDevicesYanex() {
        if (yandexInfoDevices == null || yandexInfoDevices.isEmpty()) return "нет";
        List<String> listMusic = yandexInfoDevices.stream()
                .filter(d -> "музыка".equals(d.name.toLowerCase()))
                .map(d -> d.roomName)
                .collect(Collectors.toList());
        return listMusic.toString();
    }

    private static String indexMusicDevicesLocal() {
        if (smartHome.devices == null || smartHome.devices.isEmpty()) return "нет";
        List<String> listMusic = smartHome.devices.stream()
                //.filter(d -> d.room != null)
                .filter(d -> "музыка".equals(d.name.toLowerCase()))
                .map(d -> d.room)
                .collect(Collectors.toList());
        return listMusic.toString();
    }

    private static String infoLmsPlayers() {
        if (!lmsServerOnline) return "нет";
        return lmsPlayers.players.stream().map(player -> player.name).collect(Collectors.toList()).toString();
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