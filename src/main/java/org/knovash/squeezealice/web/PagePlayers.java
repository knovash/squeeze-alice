package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.Main.rooms;
import static org.knovash.squeezealice.web.PageIndex.pageOuter;

@Log4j2
public class PagePlayers {

    public static Context action(Context context) {
        log.info("PAGE PLAYERS");
        context.bodyResponse = page();
        context.code = 200;
        return context;
    }

    public static String page() {

        String pageInner;

        if (lmsPlayers.players.size() == 0) {
            pageInner = "<b>Плееры в LMS не найдены</b>";
        } else
            pageInner =
                    "<form method='POST' action='/form'>" +
                            "<label>Время минут до сброса громкости на значаение по пресету когда не играет</label><br>" +
                            "<input " +
                            "required='required'" +
                            "type='number'" + "min='1'" + "max='30'" +
                            "name='delay_expire_value'" +
                            "value='" + lmsPlayers.delayExpire + "'>" +
                            "<input name='action' type='hidden'  value='delay_expire_save'>" +
                            "<button type='submit'>Сохранить</button>" +
                            "</form>" +
                            "<br>" +

                            "<form method='POST' action='/form'>" +
                            "<label>Tasker AutoRemote URL для обновления виджета</label><br>" +
                            "<input " +
                            "required='required'" +
                            "type='url'" +
                            "name='autoremote_value'" +
                            "value='" + lmsPlayers.autoRemoteRefresh + "'>" +
                            "<input name='action' type='hidden'  value='autoremote_save'>" +
                            "<button type='submit'>Сохранить</button>" +
                            "</form>" +
                            "<br>" +

                            "<form method='POST' action='/form'>" +
                            "<label>Синхронизация альтернативная. Если di.fm работает нормально, должно быть false</label><br>" +
                            "<select name='alt_sync' required>" +
                            "<option value='true' " + lmsPlayers.syncAlt + ">true</option>" +
                            "<option value='false' " + !lmsPlayers.syncAlt + ">false</option>" +
                            "</select>" +
                            "<input type='hidden' name='action' value='alt_sync_save'>" +
                            "<button type='submit'>Сохранить</button>" +
                            "</form>" +
                            "<br>" +

                            "<form method='POST' action='/form'>" +
                            "<label>Включать последнее игравшее на этой колонке, иначе с последней игравшей колонки</label><br>" +
                            "<select name='last_this_value' required>" +
                            "<option value='true' " + lmsPlayers.lastThis + ">true</option>" +
                            "<option value='false' " + !lmsPlayers.lastThis + ">false</option>" +
                            "</select>" +
                            "<input type='hidden' name='action' value='last_this_save'>" +
                            "<button type='submit'>Сохранить</button>" +
                            "</form>" +
                            lmsPlayers.players.stream().map(p -> playerSettings(p)).collect(Collectors.joining());

        String page = pageOuter(pageInner, "Настройка плееров", "Настройка плееров");
        return page;
    }

    public static String playerSettings(Player p) {
        String form = "<br>" +
                "<form method='POST' action='/form' enctype='application/x-www-form-urlencoded'>" + // Добавить enctype
                "<fieldset>" +
                "<legend><b>" + p.name + "</b></legend>" +

                "<select name='room' required>" +
                "<option value='" + p.room + "' " + p.room + ">" + p.room + "</option>" +
                rooms.stream()
                        .filter(r -> !r.equals(p.room))

//                        .map(r -> "<option value=" + r + ">" + r + "</option>")
                        .map(r -> {
                            String decodedRoom = java.net.URLDecoder.decode(r, StandardCharsets.UTF_8);
                            return "<option value=\"" + r + "\">" + decodedRoom + "</option>";
                        })


                        .collect(Collectors.joining()) +
                "</select> комната<br>" +


                "<input required " +
                "name='delay' " +
                "type='number'" + "min='0'" + "max='15'" +
                "placeholder='10'" +
                "value='" + p.delay + "'> секунды, задержка включения колонки, для установки громкости по пресету перед проигрыванием музыки" + "<br>" +

                "<input required " +
                "name='schedule' " +
                "placeholder='0:10,9:20,20:15,22:10,7:15'" +
                "value='" + Utils.mapToString(p.schedule) + "'> время:громкость - пресеты громкости по интервалам времени" + "<br>" +

                "<input type='hidden' name='player_name' value='"+p.name+"'>" +

                "<input type='hidden' name='action' value='player_save'>" +
                "<button type='submit'>Сохранить</button>" +

                "</fieldset>" +
                "</form>";
        return form;
    }
}