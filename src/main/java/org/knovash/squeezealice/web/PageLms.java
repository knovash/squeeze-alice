package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.web.PageIndex.pageOuter;

@Log4j2
public class PageLms {

    public static Context action(Context context) {
        context.bodyResponse = page();
        context.code = 200;
        return context;
    }

    public static String page() {
        log.info("PAGE LMS START");
        String pageInner =

//                PageIndex.statusBar() +

                        "<form method='POST' action='/form'>" +

                        "<p>" +
                        "<a href='http://" + config.lmsIp + ":" + config.lmsPort + "'" +
                        " target='_blank' rel='noopener noreferrer'" +
                        ">"
                        + config.lmsIp + ":" + config.lmsPort +
                        "</a></p>" +

                        "<input " +
                        "required='required'" +
                        "type='text'" +
                        "name='lms_ip_value'" +
                        "placeholder='192.168.1.111'" +
                        "value='" + config.lmsIp + "'>" +
                        "<label> ip адрес</label>" +

                        "<br>" +

                        "<input " +
                        "required='required'" +
                        "type='text'" +
                        "name='lms_port_value'" +
                        "placeholder='9000'" +
                        "value='" + config.lmsPort + "'>" +
                        "<label> порт</label>" +

                        "<br>" +

                        "<input name='action' type='hidden'  value='lms_save'>" +
                        "<button type='submit'>Сохранить</button>" +
                        "</form>" +
                        "<br>" +

                        "<p>" + "Скачать LMS "+
                        "<a href='https://lyrion.org'" +
                        " target='_blank' rel='noopener noreferrer'" +
                        ">"
                        + "https://lyrion.org" +
                        "</a></p>" +

                        "<br>";
        String page = pageOuter(pageInner, "Настройка LMS", "Настройка LMS");
        return page;
    }
}

