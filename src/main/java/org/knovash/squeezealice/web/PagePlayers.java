package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;

import java.util.List;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.Main.rooms;

@Log4j2
public class PagePlayers {

    public static Context action(Context context) {
        log.info("PAGE SPEAKERS");
        String json = page();
        context.json = json;
        context.code = 200;
        return context;
    }

    public static String page() {
        log.info("START GENER PAGE");
        lmsPlayers.updateServerStatus();
        String page2 = "<!DOCTYPE html><html lang=\"en\">" +
                "<head><meta charset=\"UTF-8\" />" +
                "  <title>Настройка колонок</title></head><body>" +
                "<p><a href=\"/\">Home</a></p>" +

                "LMS: " + Main.lmsIp + ":" + Main.lmsPort + "<br>" +
                "Yandex Devices: " + SmartHome.devices.size() + " " +
                SmartHome.devices.stream().map(device -> device.id + " " + device.room + " "
                                + lmsPlayers.getPlayerNameByDeviceId(device.id))
                        .collect(Collectors.toList()) + "<br>" +
                "LMS Players: " + lmsPlayers.players.size() + " " + lmsPlayers.players.stream().map(player -> player.name)
                .collect(Collectors.toList()) + "</p>" +

                rooms + "</p>" +
                "</p>" +
                "  <h2>Настройка колонок</h2>" +
                join(lmsPlayers.players.stream().map(p -> "<form action=\"/cmd\" method=\"get\">" +
                                "<b>" + p.name + "</b>" +
                                " Player id = " + p.deviceId +

                                "<br>" +
                                "<label for=\"room\">Комната</label>\n" +
                                "<select name=\"room\" id=\"room\">\n" +
                                "<option value=\"\">-- " + p.room + " --</option>\n"
                                + rooms.stream().map(r -> " <option value=" + r + ">" + r + "</option>")
                                .collect(Collectors.joining()) +
                                "</select>\n" +

                                "<br>" +
                                "<input name=\"delay\" id=\"delay\" value=\"" + p.delay + "\" />" +
                                "<label for=\"delay\"> задержка включения</label>" +

                                "<br>" +
                                "<input name=\"schedule\" id=\"schedule\" value=\"" + Utils.mapToString(p.schedule) + "\" />" +
                                "<label for=\"schedule\"> время:громкость</label>" +

                                "<br>" +
                                "<input type=\"hidden\" name=\"name\" id=\"name\" value=\"" + p.name + "\">" +
                                "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"player_save\">" +
                                "<button>save</button></form>" +

                                "<form action=\"/cmd\" method=\"get\">" +
                                "<input type=\"hidden\" name=\"name\" id=\"name\" value=\"" + p.name + "\">" +
                                "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"player_remove\">" +
                                "<button>remove</button></form>"
                ).collect(Collectors.toList())) +
                "<p><a href=\"/\">Home</a></p>" +
                "</body></html>";

        log.info("FINISH GENER PAGE");
        return page2;
    }

    public static String join(List<String> list) {
        final String[] join = {""};
        list.stream()
                .map(l -> "<p>" + l + "</p>")
                .map(l -> join[0] = join[0] + l).collect(Collectors.toList());
        return join[0];
    }
}