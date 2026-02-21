package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
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
import static org.knovash.squeezealice.yandex.Yandex.yandexMusicDevCounter;

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
        lmsPlayers.updateLmsPlayers(); // refresh
        Yandex.getRoomsAndDevices();
    }

    public static String page() {


        Yandex.getRoomsAndDevices();
        Utils.readAliceIdInRooms();

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

                "Функции:<br>" +
                "<b>Включение устройства \"музыка\"</b> в приложении \"Умный дом с Алисой\" или голосом \"Алиса, включи музыку\"<br>" +
                " 1. Задержка 10 секунд для ожидания выхода из сна плеера LMS (если он только что играл задержки нет, изменяется в настройках)<br>" +
                " 2. Громкость плеера устанавливается по пресету для данного времени (изменяется в настройках)<br>" +
                " 3. Включиться последнее игравшее или если плейлист пуст то включиться первое из избранного LMS<br>" +
                "<b>Выключение устройства \"музыка\"</b> в приложении \"Умный дом с Алисой\" или голосом \"Алиса, выключи музыку\"<br>" +
                " 1. Если плеер LMS играл в группе, отключится от группы, остальные продолжат играть.<br>" +
                " 2. Этот плеер LMS остатанавливает воспроизведение.<br>" +
                "<b>Изменение громкости плеера LMS</b> возможно в приложении или голосом<br>" +
                " \"Алиса, музыку горомче(тише)<br>" +
                " \"Алиса, музыку горомче на 3<br>" +
                " \"Алиса, музыка громкость 12<br>" +
                "<b>Переключение каналов</b> соответствует переключению закладок в избранном LMS<br>" +
                " \"Алиса, включи канал 5\"<br>" +
                " \"Алиса, переключи канал\"<br>" +
                " <br>" +

//                "Алиса, скажи <навык>, привет(подключи,настрой)<br>" +
                "<b>Привязка комнаты и выбор колонки</b> соответствует переключению закладок в избранном LMS<br>" +
                "Алиса, скажи <навык>, это комната <название комнаты в УДЯ><br>" +
                "Алиса, скажи <навык>, выбери/включи колонку <название колонки в LMS><br>" +
                "Алиса, скажи <навык>, что играет<br>" +
                "Алиса, скажи <навык>, какая громкость<br>" +
                "Алиса, скажи <навык>, включи <исполнитель> - запустит из Spotify<br>" +
                "Алиса, скажи <навык>, включи канал <название закладки в избранном LMS> - запустит закладку из LMS<br>" +
                "Алиса, скажи <навык>, дальше<br>" +
                "Алиса, скажи <навык>, добавь в избранное - добавит в закладку в LMS избранное<br>" +
                "Алиса, скажи <навык>, переключи музыку сюда<br>" +
                "Алиса, скажи <навык>, включи отдельно<br>" +
//                "Алиса, скажи <навык>, включи только тут<br>" +
                "Алиса, скажи <навык>, включи вместе<br>" +
                "Алиса, скажи <навык>, включи/выключи рандом/шафл<br>" +
                "Алиса, скажи <навык>, включи/выключи повтор<br>" +
//                "Алиса, скажи <навык>, где пульт<br>" +
//                "Алиса, скажи <навык>, включи пульт <br>";
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
        List<String> aliceInRoomsList = idRooms.entrySet().stream().map(rooms -> {
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
        msgRoomsYandex = "Комнаты Умного дома " + rooms;
        if (rooms.size() == 0)
            msgRoomsYandex = "Комнаты Умного дома не найдены ";

//        Устройства Музыка
        if (SmartHome.devices.size() == 0 && rooms.size() == 0)
            msgMusicDevices = "Устройства Музыка не найдены ";

        List<String> listMusic = SmartHome.devices.stream()
                .filter(d -> lmsPlayers.playerNameByDeviceId(d.id) != null)
                .map(d -> lmsPlayers.playerNameByDeviceId(d.id) + " в " + d.room)
                .collect(Collectors.toList());

        if (listMusic.size() == 0 && rooms.size() > 0)
            msgMusicDevices = "Устройства Музыка не найдены " + "<a href=\\players>выбрать комнаты</a>";

        if (listMusic.size() > 0)
            msgMusicDevices = "Устройства Музыка " + listMusic;


//        Плееры в умном доме


//        String newDevices = "";
//        if (yandexMusicDevCounter < SmartHome.devices.size())
//            newDevices = " Есть <b>новые</b> устройства Музыка. Обновите список устройств в приложении УДЯ";

        msgDevices = "Умный дом подключено: " + yandexMusicDevCounter;
        if (SmartHome.devices.size() > yandexMusicDevCounter)
            PageIndex.msgDevices = "Умный дом подключено " + yandexMusicDevCounter + " Есть <b>новые</b> устройства Музыка! Обновите список устройств в приложении Умный дом с Алисой";


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