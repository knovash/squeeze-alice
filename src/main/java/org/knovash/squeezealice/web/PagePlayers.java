package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.yandex.Yandex;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.Main.rooms;
import static org.knovash.squeezealice.web.PageIndex.pageOuter;

@Log4j2
public class PagePlayers {

    public static final String alt_sync_save = "alt_sync_save";
    public static final String alt_sync_value = "alt_sync_value";

    public static final String autoremote_save = "autoremote_save";
    public static final String autoremote_remove = "autoremote_remove";
    public static final String autoremote_value = "autoremote_value";

    public static final String delay_expire_save = "delay_expire_save";
    public static final String delay_expire_value = "delay_expire_value";

    public static final String last_this_save = "last_this_save";
    public static final String last_this_value = "last_this_value";


    public static final String toggle_wake_save = "toggle_wake_save";
    public static final String toggle_wake_value = "toggle_wake_value";

    public static final String player_save = "player_save";
    public static final String player_remove = "player_remove";
    public static final String player_name_value = "player_name_value";
    public static final String player_room_value = "player_room_value";
    public static final String player_delay_value = "player_delay_value";
    public static final String player_schedule_value = "player_schedule_value";
    public static final String players_all_schedule_value = "players_all_schedule_value";
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
        String autoremoteShow = "";
        if (lmsPlayers.autoRemoteUrls == null) lmsPlayers.autoRemoteUrls = new ArrayList<>();
        if (lmsPlayers.autoRemoteUrls.size() == 0) autoremoteShow = "";
        else autoremoteShow = lmsPlayers.autoRemoteUrls.get(0);

        Yandex.getRoomsAndDevices();
        log.info("YANDEX MUSIC ROOMS LIST: " + Yandex.yandexMusicDevListRooms);

        String autoRemoteUrls = "";
        if (lmsPlayers.autoRemoteUrls != null) autoRemoteUrls = lmsPlayers.autoRemoteUrls.stream()
                .map(url -> "<label>" + url + "</label><br>" +
                        "<form method='POST' action='/form'>" +
                        "<input name='autoremote_value' type='hidden' value='" + url + "'>" +
                        "<input name='action'           type='hidden' value='" + autoremote_remove + "'>" +
                        "<button type='submit'>Удалить</button>" +
                        "</form>" +
                        "<br>")
                .collect(Collectors.joining(""));
        log.info("TASKER URLS: " + autoRemoteUrls);

        if (lmsPlayers.players.size() == 0) {
            pageInner = "<b>Плееры в LMS не найдены</b>";
        } else
            pageInner =
                    lmsPlayers.players.stream().map(p -> playerSettings(p)).collect(Collectors.joining()) +

                            "<br>" +
                            "<form method='POST' action='/form'>" +
                            "<label>Минут до сброса громкости на значаение по пресету когда не играет</label><br>" +
                            "<input " +
                            "required='required'" +
                            "type='number'" + "min='1'" + "max='30'" +
                            "name='" + delay_expire_value + "'" +
                            "value='" + lmsPlayers.delayExpire + "'>" +
                            "<input name='action' type='hidden'  value='" + delay_expire_save + "'>" +
                            "<button type='submit'>Сохранить</button>" +
                            "</form>" +
                            "<br>" +

//                            "<form method='POST' action='/form'>" +
//                            "<label>Синхронизация альтернативная (" + lmsPlayers.syncAlt + ") если di.fm работает нормально, должно быть false</label><br>" +
//                            "<select name='" + alt_sync_value + "' required>" +
//                            "<option value='true' " + (lmsPlayers.syncAlt ? "selected" : "") + ">вкл</option>" +
//                            "<option value='false' " + (!lmsPlayers.syncAlt ? "selected" : "") + ">выкл</option>" +
//                            "</select>" +
//                            "<input type='hidden' name='action' value='" + alt_sync_save + "'>" +
//                            "<button type='submit'>Сохранить</button>" +
//                            "</form>" +
//                            "<br>" +

                            "<form method='POST' action='/form'>" +
                            "<label>Включать последнее игравшее на этой колонке (" + lmsPlayers.lastThis + ") иначе с последней игравшей колонки</label><br>" +
                            "<select name='" + last_this_value + "' required>" +
                            "<option value='true' " + (lmsPlayers.lastThis ? "selected" : "") + ">вкл</option>" +
                            "<option value='false' " + (!lmsPlayers.lastThis ? "selected" : "") + ">выкл</option>" +
                            "</select>" +
                            "<input type='hidden' name='action' value='last_this_save'>" +
                            "<button type='submit'>Сохранить</button>" +
                            "</form>" +


                            "<form method='POST' action='/form'>" +
                            "<label>Задержка пред включением</label><br>" +
                            "<select name='" + toggle_wake_value.toString() + "' required>" +
                            "<option value='true' " + (lmsPlayers.toggleWake ? "selected" : "") + ">вкл</option>" +
                            "<option value='false' " + (!lmsPlayers.toggleWake ? "selected" : "") + ">выкл</option>" +
                            "</select>" +
                            "<input type='hidden' name='action' value='" + toggle_wake_save + "'>" +
                            "<button type='submit'>Сохранить</button>" +
                            "</form>" +

//                            обновление виджетов Tasker на планшете
                            "<br>" +
                            "<fieldset>" +
                            "<legend>Tasker refresh</legend>" +
                            autoRemoteUrls +
                            "<form method='POST' action='/form'>" +
                            "<label>Tasker AutoRemote URL для обновления виджета на Android устройстве</label><br>" +
                            "<input " +
                            "required='required'" +
                            "type='url'" +
                            "name='" + autoremote_value + "'" +
                            "placeholder='https://autoremotejoaomgcd.appspot.com/sendmessage...'" +
                            "value='" + "autoremotejoaomgcd" + "'>" +
                            "<input name='action' type='hidden'  value='" + autoremote_save + "'>" +
                            "<button type='submit'>Сохранить</button>" +
                            "</form>" +
                            "Проверить настройки Autoremote. Батарея: использование в фоне без ограничений. Передача данных: в фоне не ограничена."+
                            "</fieldset>" +


//    время громкость для всех
                            "<br>" +
                            "<fieldset>" +
                            "<legend>Время громкость установить для всех</legend>" +

                            "<form method='POST' action='/form'>" +
                            "<label>Время громкость</label><br>" +
                            "<input " +
                            "required='required'" +
                            "type='text'" +
                            "name='" + players_all_schedule_value + "' " +
                            "placeholder='0:10,9:20,20:15,22:10,7:15'" +
                            "value='" + Utils.mapToString(lmsPlayers.scheduleAll) + "'> время:громкость - пресеты громкости по интервалам времени" + "<br>" +
                            "<input type='hidden' name='" + player_name_value + "' value='" + "ffff" + "'>" +
                            "<button type='submit'>Сохранить для всех</button>" +
                            "</form>" +
                            "<br>" +
                            "</fieldset>" +


                            "";

        String page = pageOuter(pageInner, "Настройка плееров", "Настройка плееров");
        return page;
    }

    public static String playerSettings(Player p) {
        String inYaState = " Yandex <span style='color: red;'>" + "отключен" + "</span>";
        if (Yandex.yandexMusicDevListRooms != null && Yandex.yandexMusicDevListRooms.contains(p.room)) {
            inYaState = " Yandex <span style='color: green;'>" + "подключен" + "</span>";
        }
        String inLmsState = "LMS <span style='color: red;'>" + "отключен" + "</span>";
        if (p.connected) inLmsState = "LMS <span style='color: green;'>" + "подключен" + "</span>";
        String roomState = "<span style='color: red;'>" + "комната" + "</span>";
        if (p.room != null) roomState = "<span style='color: green;'>" + "комната" + "</span>";
        p.requestPlayerStatus();
        String form =
                "<br>" +
                        "<form method='POST' action='/form' enctype='application/x-www-form-urlencoded'>" + // Добавить enctype
                        "<fieldset>" +
                        "<legend><b>" + p.name + "</b> " + inLmsState + inYaState + "</legend>" +

                        "<select name='" + player_room_value + "' required>" +
                        "<option value='" + p.room + "' " + p.room + ">" + p.room + "</option>" +
                        rooms.stream()
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
//                        " <b>title:</b>" + p.requestTitle() + // PagePlayers
                        "  <b>lastTime:</b>" + p.lastPlayTimePlayer +
                        "   <b>lastChannel:</b>" + p.lastChannelPlayer +
                        "   <b>lastPath:</b>" + p.lastPathPlayer +
//                        "   <b>currentTrackInPlaylist:</b>" + p.playerStatus.result.remoteMeta.title +

                        "</fieldset>" +
                        "</form>";
        return form;
    }
}