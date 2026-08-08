package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.yandex.Yandex;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.web.PageIndex.pageOuter;

@Log4j2
public class PagePlayers {

    public static final String delay_expire_save = "delay_expire_save";
    public static final String delay_expire_value = "delay_expire_value";
    public static final String player_save = "player_save";
    public static final String player_remove = "player_remove";
    public static final String player_name_value = "player_name_value";
    public static final String player_room_value = "player_room_value";
    public static final String player_delay_value = "player_delay_value";
    public static final String player_schedule_value = "player_schedule_value";
    public static final String player_volume_max_value = "player_volume_max_value";
    public static final String lms_save = "lms_save";
    public static final String lms_ip_value = "lms_ip_value";
    public static final String lms_port_value = "lms_port_value";
    public static final String statusbar_refresh = "statusbar_refresh";

    public static Context action(Context context) {
        log.info("PAGE PLAYERS");
        context.bodyResponse = page();
        context.code = 200;
        return context;
    }

    public static String page() {
        String pageInner;
        Yandex.devicesGetFromYandexInfo();
        log.info("YANDEX MUSIC ROOMS LIST: " + Yandex.roomsWithDevice);
        if (lmsPlayers.players.size() == 0) {
            pageInner = "<b>Плееры в LMS не найдены</b>";
        } else
            pageInner = lmsPlayers.players.stream().map(p -> playerSettings(p)).collect(Collectors.joining());
        String page = pageOuter(pageInner, "Настройка плееров", "Настройка плееров");
        return page;
    }

    public static String playerSettings(Player p) {
        String inYaState = " Yandex <span style='color: red;'>" + "отключен" + "</span>";
        if (Yandex.roomsWithDevice != null && Yandex.roomsWithDevice.contains(p.room)) {
            inYaState = " Yandex <span style='color: green;'>" + "подключен" + "</span>";
        }
        String inLmsState = "LMS <span style='color: red;'>" + "отключен" + "</span>";

        lmsPlayers.checkUpdated(); // TODO DEBUG
        if (p.connected) inLmsState = "LMS <span style='color: green;'>" + "подключен" + "</span>";
        String roomState = "<span style='color: red;'>" + "комната" + "</span>";
        if (p.room != null) roomState = "<span style='color: green;'>" + "комната" + "</span>";
//        p.requestPlayerStatus(); // TODO
        String form =
                "<br>" +
                        "<form method='POST' action='/form' enctype='application/x-www-form-urlencoded'>" + // Добавить enctype
                        "<fieldset>" +
                        "<legend><b>" + p.name + "</b> " + inLmsState + inYaState + "</legend>" +

                        "<select name='" + player_room_value + "' required>" +
                        "<option value='" + p.room + "' " + p.room + ">" + p.room + "</option>" +
                        Yandex.rooms.stream()
                                .filter(r -> !r.equals(p.room))
                                .map(r -> {
                                    String decodedRoom = java.net.URLDecoder.decode(r, StandardCharsets.UTF_8);
                                    return "<option value=\"" + r + "\">" + decodedRoom + "</option>";
                                })
                                .collect(Collectors.joining()) +
                        "</select> " + roomState + "<br>" +

                        "<input required " +
                        "name='" + player_delay_value + "' " +
                        "type='number'" + "min='0'" + "max='20'" +
                        "placeholder='10'" +
                        "value='" + p.delay + "'> секунд, задержка включения колонки, для установки громкости по пресету перед проигрыванием музыки" + "<br>" +

                        "<input required " +
                        "name='" + player_volume_max_value + "' " +
                        "type='number'" + "min='0'" + "max='100'" +
                        "placeholder='100'" +
                        "value='" + p.volume_high + "'> ограничение максимальной громкости" + "<br>" +

                        "<input required " +
                        "name='" + player_schedule_value + "' " +
                        "placeholder='0:10,9:20,20:15,22:10,7:15'" +
                        "value='" + Utils.mapToString(p.schedule) + "'> время:громкость - пресеты громкости по интервалам времени" + "<br>" +

                        "<input type='hidden' name='" + player_name_value + "' value='" + p.name + "'>" +

                        "<button type='submit' name='action' value='player_save'>Сохранить</button>" +
                        "<button type='submit' name='action' value='player_remove'>Удалить</button>" +

                        "<br>" +
                        "  last play time:" + p.lastPlayTimePlayer +

                        "</fieldset>" +
                        "</form>";
        return form;
    }
}