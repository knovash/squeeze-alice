package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Hive;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.spotify.SpotifyUserParser;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.yandex.Yandex;
import org.knovash.squeezealice.yandex.YandexJwtUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.yandex.Yandex.devicesSize;

@Log4j2
public class PageIndex {

    public static String msgLmsIp;
    public static String msgLmsPlayers;
    public static String msgMusicDevices;
    public static String msgAliseSpeakers;
    public static String msgDevices;
    public static String msgRoomsYandex;
    public static String msgYandexUser;
    public static String msgSpotifyUser;


    public static Context action(Context context) {
        context.bodyResponse = page();
        context.code = 200;
        return context;
    }

    public static void refresh(HashMap<String, String> parameters) {
        log.info("REFRESH");
        if (!lmsServerOnline) lmsPlayers.searchForLmsIp();
        lmsPlayers.updatePlayers(); // refresh
        Yandex.devicesGetFromYandexInfo();
    }

    public static String page() {
        Yandex.devicesGetFromYandexInfo();
        Utils.readRoomsAndAliceIds();
        String pageInner = statusBar() +
                "<p><a href=\\html\\manual " +
                "target=\"_blank\" rel=\"noopener noreferrer\"" +
                ">Инструкция</a></p>" +
                "<p><a href=\\lms>Настройка LMS</a></p>" +
                "<p><a href=\\players>Настройка плееров</a></p>" +
                "<p><a href=\\auth " +
                "target=\"_blank\" rel=\"noopener noreferrer\"" +
                ">Авторизация в Яндекс</a></p>" +
                "<p><a href=\\auth_spotify " +
                "target=\"_blank\" rel=\"noopener noreferrer\"" +
                ">Авторизация в Spotify</a></p>" +
                "";
        String page = pageOuter(pageInner, "Squeeze-Alice", "Squeeze-Alice");
        return page;
    }

    public static String statusBar() {
        log.info("PAGE STATUSBAR START");
//        LMS
        if (lmsServerOnline) {
            PageIndex.msgLmsIp = "LMS " + config.lmsIp;
            PageIndex.msgLmsPlayers = "LMS плееры " + lmsPlayers.players.stream().map(player -> player.name).collect(Collectors.toList());
        } else {
            PageIndex.msgLmsIp = "LMS не найден";
            PageIndex.msgLmsPlayers = "LMS плееры не найдены";
        }

        // колонки Алиса
        List<String> aliceInRoomsList = roomsAndAliceIds.entrySet().stream().map(rooms -> {
            try {
                return URLDecoder.decode(rooms.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        msgAliseSpeakers = "Колонки Алиса в комнатах " + aliceInRoomsList;
        if (aliceInRoomsList.size() == 0) {
            msgAliseSpeakers = "Колонки Алиса не найдены, скажите навыку Привет или Настрой";
        }

// комнаты
        msgRoomsYandex = "Комнаты Умного дома " + Yandex.rooms;
        if (Yandex.rooms.size() == 0)
            msgRoomsYandex = "Комнаты Умного дома не найдены ";

        List<String> listMusic = SmartHome.devices.stream()
                .filter(d -> d.room != null)
                .map(d -> d.room)
                .collect(Collectors.toList());

        if (listMusic.size() == 0)
            msgMusicDevices = "Устройства Музыка не найдены " + "<a href=\\players>выбрать комнаты</a>";

        if (listMusic.size() > 0)
            msgMusicDevices = "Устройства Музыка " + listMusic;


//        Плееры в умном доме


        msgDevices = "Умный дом подключено: " + devicesSize;
        if (SmartHome.devices.size() > devicesSize)
            PageIndex.msgDevices = "Умный дом подключено " + devicesSize + " Есть <b>новые</b> устройства Музыка! Обновите список устройств в приложении Умный дом с Алисой";


// Yandex user name
        if (config.yandexName == null && (config.yandexToken != null && !config.yandexToken.equals("")))
            config.yandexName = YandexJwtUtils.getValueByTokenAndKey(config.yandexToken, "display_name");
        msgYandexUser = "Пользователь Yandex: " + config.yandexName;
        if (config.yandexName == null)
            msgYandexUser = "Пользователь Yandex: " +
                    "<a href=\\auth " + "target=\"_blank\" rel=\"noopener noreferrer\"" + ">Авторизация в Яндекс</a>";

//        Spotify user name
        SpotifyUserParser.parseUserInfo(Spotify.me());
        if (SpotifyUserParser.spotifyUser != null) config.spotifyName = SpotifyUserParser.spotifyUser.getDisplayName();
        msgSpotifyUser = "Пользователь Spotify: " + config.spotifyName;
        if (config.spotifyName == null)
            msgSpotifyUser = "Пользователь Spotify: " +
                    "<a href=\\auth_spotify " + "target=\"_blank\" rel=\"noopener noreferrer\"" + ">Авторизация в Spotify</a>";

        String hiveStatus = "";
        if (hive.mqttClient.isConnected()) hiveStatus = "Hive mqtt <span style='color: green;'>" + "подключен" + "</span>";
        else hiveStatus ="Hive mqtt <span style='color: red;'>" + "отключен" + "</span>" +" проверьте "+ config.hiveBroker;

        String bar = "<fieldset>" +
                "<legend><b>" + "Информация" + "</b></legend>" +

                msgLmsIp + "<br>" + // LMS 192.168.1.110
                msgLmsPlayers + "<br>" + // LMS плееры [HomePod, Radiotechnika, Homepod1, JBL white, Mi Box, HomePod2]
                msgYandexUser + "<br>" +
                msgRoomsYandex + "<br>" + //Комнаты Умного дома не найдены войти в Яндекс аккаунт

                msgMusicDevices + "<br>" + //Устройства Музыка не найдены
//                msgAliseSpeakers + "<br>" + //Колонки Алиса не найдены, скажите навыку Привет или Настрой

                msgDevices + "<br>" + //Плееры в Умном доме не найдены

                msgSpotifyUser + "<br>" +
//                "Пользователь Spotify: " + config.spotifyName +
                hiveStatus + "<br>" +

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