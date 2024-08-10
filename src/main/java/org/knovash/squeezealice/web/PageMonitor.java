package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

import java.util.List;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class PageMonitor {

    public static Context action(Context context) {
        log.info("PAGE SPEAKERS");
        String json = monitor();
        context.json = json;
        context.code = 200;
        return context;
    }

    public static String monitor() {
        log.info("MONITOR");
        lmsPlayers.updateServerStatus();


        List<String> list = lmsPlayers.players.stream()
                .map(p -> "<p>NAME: " + p.name + " " +

                        "  PLAYING: <b>" + p.playing + "</b> " +
                        "  SEPARATE: <b>" + p.separate + "</b> " +
                        "  DELAY: " + p.delay + " " +
                        "  TITLE: " + p.title + "</p>" +
                        "<p>CONNECTED: " + p.connected + " " +
                        "  LAST CHANNEL: " + p.lastChannel + " " +
                        "  LAST PATH: " + p.lastPath + "</p>" +
//                        "  LAST PATH: " + p.status.result.current_title + "</p>" +
                        "<p></p>"
                ).collect(Collectors.toList());

        String page = "<!DOCTYPE html><html lang=\"en\">" +
                "<head><meta charset=\"UTF-8\" />" +
                "<title>Monitor</title></head><body>" +
                "<p><a href=\"/\">Home</a></p>" +
                "<h2>Monitor</h2>" +
                list +
                "<p><a href=\"/\">Home</a></p>" +
                "</body></html>";
        return page;
    }
}

