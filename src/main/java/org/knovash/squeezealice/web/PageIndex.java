package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.spotify.SpotifyUserParser;
import org.knovash.squeezealice.yandex.Yandex;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.yandex.YandexJwtUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.yandex.Yandex.yandexMusicDevCounter;
import static org.knovash.squeezealice.yandex.Yandex.yandexMusicDevListRooms;

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
        if (!lmsServerOnline) lmsPlayers.searchForLmsIp();
        lmsPlayers.updateLmsPlayers();
        Yandex.getRoomsAndDevices();
    }

    public static String page() {

        Spotify.me();
        Yandex.getRoomsAndDevices();

        if (lmsServerOnline) {
            msgLmsIp = "LMS найден " + "<a href=\"http://" + config.lmsIp + ":"
                    + config.lmsPort + "\">" + config.lmsIp + ":" + config.lmsPort + "</a>";

            msgLmsPlayers = "LMS плееры " + lmsPlayers.players.stream().map(player -> player.name).collect(Collectors.toList());
        } else {
            msgLmsIp = "LMS сервер не найден";
            msgLmsPlayers = "LMS плееры не найдены";
        }

        String newDevices = "";
        if (yandexMusicDevCounter < SmartHome.devices.size())
            newDevices = " Есть <b>новые</b> устройства Музыка. Обновите список устройств в приложении УДЯ";

        if (SmartHome.devices.size() == 0)
            msgSqa = "нет устройств Музыка для подключения в УДЯ";
        else
            msgSqa = "Музыка " + SmartHome.devices.size() + " устройств " + SmartHome.devices.stream().map(d ->
                            lmsPlayers.getPlayerNameByDeviceId(d.id) + " в " + d.room)
                    .collect(Collectors.toList());
        if (yandexMusicDevCounter == 0) {
            if (SmartHome.devices.size() == 0)
                PageIndex.msgUdy = "УДЯ нет плееров " + "<a href=\\players>настроить</a>";
            else PageIndex.msgUdy = "Есть <b>новые</b> устройства Музыка. Обновите список устройств в приложении УДЯ";
            log.info(PageIndex.msgUdy);
        } else

            PageIndex.msgUdy = "УДЯ подключено " + yandexMusicDevCounter + " устройств Музыка в комнатах "
                    + yandexMusicDevListRooms + newDevices;


        Utils.readAliceIdInRooms();
        String pageInner = statusBar() +
                "<p><a href=\\html\\manual " +
                "target=\"_blank\" rel=\"noopener noreferrer\"" +
                ">Инструкция</a></p>" +
                "<p><a href=\\lms>Настройка LMS</a></p>" +
                "<p><a href=\\players>Настройка плееров</a></p>" +
//                "<p><a href=\\yandex>Настройка Yandex</a></p>" +

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
                " \"Алиса, переключи канал\""+



//                "<p><a href=\\spotify>Настройка Spotify</a></p>" +
//                "<p><b>" + "Комманды:</b></p>" +
//                "<p>" +
//                "Алиса, включи музыку<br>" +
//                "Алиса, выключи музыку<br>" +
//                "Алиса, музыку громче<br>" +
//                "Алиса, музыку громче на 5<br>" +
//                "Алиса, канал 4<br>" +
//                "Алиса, переключи канал<br>" +
//                "<br>" +


//                "Алиса, скажи <навык>, привет(подключи,настрой)<br>" +
//                "Алиса, скажи <навык>, что играет - сейчас на Homepod1 играет Deep Organic House громкость 12<br>" +
//                "Алиса, скажи <навык>, выбери колонку Радиотехника - выберет для этой комнаты колонку из LMS плееров [HomePod2, Radiotechnika, HomePod, Mi Box, Homepod1]<br>" +
//                "<br>" +
//                "Алиса, скажи <навык>, включи Depeche Mode - включаю Depeche Mode - найдет в Spotify<br>" +
//                "Алиса, скажи <навык>, дальше<br>" +
//                "Алиса, скажи <навык>, добавь в избранное<br>" +
//                "Алиса, скажи <навык>, переключи музыку сюда<br>" +
//                "Алиса, скажи <навык>, включи отдельно<br>" +
//                "Алиса, скажи <навык>, включи только тут<br>" +
//                "Алиса, скажи <навык>, включи вместе<br>" +
//                "Алиса, скажи <навык>, где пульт<br>" +
//                "Алиса, скажи <навык>, включи пульт <br>";
                "";

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
        String aliceInRooms = "в навык подключены колонки Алиса в комнатах " + aliceInRoomsList;
        if (aliceInRoomsList.size() == 0) {

            aliceInRooms = "в навык не подключены колонки Алиса, скажите навыку Привет или Настрой";
        }

        if (config.yandexName == null && (config.yandexToken != null && !config.yandexToken.equals("")))
            config.yandexName = YandexJwtUtils.getValueByTokenAndKey(config.yandexToken, "display_name");

        String roomsMsg = "УДЯ комнаты " + rooms;
        if (rooms.size() == 0) roomsMsg = "УДЯ нет комнат";


        SpotifyUserParser.parseUserInfo(Spotify.me());
        if (SpotifyUserParser.spotifyUser != null)
            config.spotifyName = SpotifyUserParser.spotifyUser.getDisplayName();

        String bar = "<fieldset>" +
                "<legend><b>" + "Информация" + "</b></legend>" +

                PageIndex.msgLmsIp + "<br>" +
                PageIndex.msgLmsPlayers + "<br>" +
                PageIndex.msgSqa + "<br>" + aliceInRooms + "<br>" +

                roomsMsg + "<br>" +
                PageIndex.msgUdy + "<br>" +
                "Пользователь Yandex: " + config.yandexName + "<br>" +
                "Пользователь Spotify: " + config.spotifyName +

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