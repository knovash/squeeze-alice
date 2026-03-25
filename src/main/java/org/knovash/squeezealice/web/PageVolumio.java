package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.volumio.Volumio;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.web.PageIndex.pageOuter;

@Log4j2
public class PageVolumio {

    public static Context action(Context context) {
        context.bodyResponse = page();
        context.code = 200;
        return context;
    }

    public static String page() {
        log.info("PAGE VOLUMIO START");
        String buttonPower = "Включить";
        String value = "volumio_on";

        if (lmsPlayers.playerByName(Volumio.volumioPlayerName) != null) {
            buttonPower = "Выключить";
            value = "volumio_off";
        }

        String pageInner =
                "<form method='POST' action='/form'>" +
                        "<p><a href='http://" + config.volumioIp + "' target='_blank' rel='noopener noreferrer'>" +
                        config.volumioIp + "</a></p>" +
                        "<input required='required' type='text' name='volumio_ip_value' " +
                        "placeholder='192.168.1.120' value='" + config.volumioIp + "'>" +
                        "<label> IP адрес</label><br><br>" +
                        "<button type='submit' name='action' value='" + value + "'>" + buttonPower + "</button><br>" +
                        "<button type='submit' name='action' value='volumio_save'>Сохранить</button>" +
                        "</form><br>";

        return pageOuter(pageInner, "Настройка Volumio", "Настройка Volumio");
    }
}