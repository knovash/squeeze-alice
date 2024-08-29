package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.SmartHome;

import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class PageIndex {

    public static Context action(Context context) {
        String json = page();
        context.json = json;
        context.code = 200;
        return context;
    }

    public static String page() {
        String page = "<!doctype html><html lang=\"ru\">\n" +
                "<head>\n" +
                "<meta charSet=\"utf-8\" />\n" +
                "<title>Squeeze-Alice</title>" +
                "</head>\n" +
                "<body> \n" +
                "<p><strong>Squeeze-Alice</strong></p> \n" +

                "LMS: " + Main.lmsIp + ":" + Main.lmsPort + "<br>" +
                "Yandex Devices: " + SmartHome.devices.size() + " " +
                SmartHome.devices.stream().map(device -> device.id + " " + device.room + " "
                                + lmsPlayers.getPlayerNameByDeviceId(device.id))
                        .collect(Collectors.toList()) + "<br>" +
                "LMS Players: " + lmsPlayers.players.size() + " " + lmsPlayers.players.stream().map(player -> player.name)
                .collect(Collectors.toList()) + "</p>" +

                "<p><a href=\\players>Настройка колонок</a></p> \n" +
                "<p><a href=\\spotify>Настройка Spotify</a></p> \n" +
                "<p><a href=\\yandex>Настройка Yandex</a></p> \n" +
                "<p><a href=\\cmd?action=state_devices>Посмотреть настройки Devices</a></p> \n" +
                "<p><a href=\\cmd?action=state_players>Посмотреть настройки Players</a></p> \n" +

//                "<p><a href=\\cmd?action=log>Посмотреть лог</a></p> \n" +
//                "<p><a href=\\cmd?action=reset_players>Reset players</a></p> \n" +
//                "<p><a href=\\cmd?action=restart>Restart server service</a></p> \n" +
//                "<p><a href=\\cmd?action=reboot>Reboot device</a></p> \n" +
                "<p><b>" + "Комманды через навык:</b></p>" +
                "<p>" +
                "Алиса, скажи раз-два, что играет<br>" +
                "Алиса, скажи раз-два, включи {исполнитель} - включит исполнителя из Spotify<br>" +
                "Алиса, скажи раз-два, Spotify - Spotify transfer на колонку в комнате<br>" +
                "Алиса, скажи раз-два, канал {название} - включит закладку в избранном LMS<br>" +
                "Алиса, скажи раз-два, дальше - следущий трек плейлиста или закладка в избранном<br>" +
                "Алиса, скажи раз-два, отдельно - отключит колонку от группы<br>" +
                "Алиса, скажи раз-два, только тут - отключит все остальные колонки<br>" +
                "Алиса, скажи раз-два, вместе - подключить колонку в группу<br>" +
                "Алиса, скажи раз-два, добавь в избранное - добавит закладку в избранное LMS</p>" +

                "<br>" +
                "Алиса, скажи раз-два, это комната {название комнаты} - связать Алису и комнату<br>" +
                "Алиса, скажи раз-два, включи колонку {название в LMS} - связать колонку и комнату<br>" +
                "Алиса, скажи раз-два, где пульт<br>" +
                "Алиса, скажи раз-два, включи пульт - связать пульт и колонку в комнате<br>" +

                "<p></p>" +
                "</body>\n" +
                "</html>";
        return page;
    }
}