package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.utils.Utils;

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
                "<title>Настройка колонок</title></head><body>" +
                "<p><a href=\"/\">Home</a></p>" +

                "LMS IP: " + Main.lmsIp + ":" + Main.lmsPort + "<br>" +
                "Yandex Rooms: " + rooms + "<br>" +
                "Yandex Devices: " + SmartHome.devices.size() + " " +
                SmartHome.devices.stream().map(d ->
                                d.id + ":" + d.room + ":" + lmsPlayers.getPlayerNameByDeviceId(d.id))
                        .collect(Collectors.toList()) + "<br>" +
                "LMS Players: " + lmsPlayers.players.size() + " " + lmsPlayers.players.stream().map(player -> player.name)
                .collect(Collectors.toList()) + "</p>" +


                "  <h2>Настройка колонок</h2>" +

                "<form action=\"/cmd\" method=\"get\">" +
                "<label for=\"delay_expire_value\">Если не играет Х минут - установить громкость по времени</label>" + "<br>" +
                "<input                 name=\"delay_expire_value\" value= \"" + lmsPlayers.delayExpire + "\" />" + "<br>" +
                "<input type=\"hidden\" name=\"action\"             value= \"delay_expire_save\">" +
                "<button>save</button></form>" + "<br>" +

                "<form action=\"/cmd\" method=\"get\">" +
                "<label for=\"autoremote_value\">AutoRemote URL</label>" + "<br>" +
                "<label for=\"autoremote_value\">https://autoremotejoaomgcd.appspot.com/sendmessage?key=...&message=re</label>" + "<br>" +
                "<input                 name=\"autoremote_value\" value= \"" + lmsPlayers.autoRemoteRefresh + "\" />" + "<br>" +
                "<input type=\"hidden\" name=\"action\"             value= \"autoremote_save\">" +
                "<button>save</button></form>" + "<br>" +

                "<form action=\"/cmd\" method=\"get\">" +
                "<label for=\"alt_sync_value\">Синхронизация альтернативная. Если di.fm работает нормально должно быть false</label><br>" +
                "<select name=\"alt_sync_value\">" +
                "<option value=" + lmsPlayers.syncAlt + ">" + lmsPlayers.syncAlt + "</option>" +
                " <option value=" + !lmsPlayers.syncAlt + ">" + !lmsPlayers.syncAlt + "</option>" +
                "</select>" +
                "<input type=\"hidden\" name=\"action\" value=\"alt_sync_save\">" +
                "<button>save</button></form><br>" +

                "<form action=\"/cmd\" method=\"get\">" +
                "<label for=\"last_this\">Включать последнее игравшее на этой колонке, иначе с последней игравшей колонки</label><br>" +
                "<select name=\"last_this_value\">" +
                "<option value=" + lmsPlayers.lastThis + ">" + lmsPlayers.lastThis + "</option>" +
                " <option value=" + !lmsPlayers.lastThis + ">" + !lmsPlayers.lastThis + "</option>" +
                "</select>" +
                "<input type=\"hidden\" name=\"action\" value=\"last_this_save\">" +
                "<button>save</button></form><br>" +

                join(lmsPlayers.players.stream().map(p ->
                        "<form action=\"/cmd\" method=\"get\">" +
                                "<b>" + p.name + "</b>" + " Player id = " + p.deviceId + "<br>" +
                                "<label for=\"room\">Комната</label>\n" +
                                "<select name=\"room\" id=\"room\">\n" +
                                "<option value=" + p.room + ">" + p.room + "</option>"
                                + rooms.stream().map(r -> " <option value=" + r + ">" + r + "</option>")
                                .collect(Collectors.joining()) +
                                "</select><br>" +

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