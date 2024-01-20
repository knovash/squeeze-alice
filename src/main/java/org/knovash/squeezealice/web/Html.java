package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.provider.SmartHome;
import org.knovash.squeezealice.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class Html {

    public static String formSpotifyLogin() {
        String page = "<!DOCTYPE html><html lang=\"en\">" +
                "<head><meta charset=\"UTF-8\" />" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                "  <link rel=\"stylesheet\" href=\"style.css\" />" +
                "  <title>Browser</title>" +
                "</head>" +
                "<body>" +
                "<p><a href=\"/\">Home</a></p>" +
                "  <h1>Spotify credentials</h1>" +
                "<br>" +
                "<form action=\"/cmd\" method=\"get\">" +
                "<div>" +
                "<input name=\"id\" id=\"id\" value=\"" + Spotify.client_id.substring(0, 4) + "*****" + "\"/>" +
                "<label for=\"id\"> client id</label>" +
                "</div>" +
                "<div>" +
                "<br>" +
                "<input name=\"secret\" id=\"secret\" value=\"" + Spotify.client_secret.substring(0, 4) + "*****" + "\"/>" +
                "<label for=\"secret\"> client secret</label>" +
                "</div>" +
                "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"cred_spotify\">" +
                "<div>" +
                "<br><button>save</button>" +
                "</div>" +
                "</form>" +
                "<p><a href=\"/\">Home</a></p>" +
                "<script src=\"script.js\"></script>" +
                "</body></html>";
        return page;
    }

    public static String formYandexLogin() {
        String page = "<!DOCTYPE html><html lang=\"en\">" +
                "<head><meta charset=\"UTF-8\" />" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                "  <link rel=\"stylesheet\" href=\"style.css\" />" +
                "  <title>Yandex credentials</title>" +
                "</head>" +
                "<body>" +
                "<p><a href=\"/\">Home</a></p>" +
                "  <h1>Yandex credentials</h1>" +
                "<br>" +
                "<form action=\"/cmd\" method=\"get\">" +
                "<div>" +
                "<input name=\"id\" id=\"id\" value=\"" + SmartHome.client_id.substring(0, 4) + "*****" + "\"/>" +
                "<label for=\"id\"> client id</label>" +
                "</div>" +
                "<div>" +
                "<br>" +
                "<input name=\"secret\" id=\"secret\" value=\"" + SmartHome.client_secret.substring(0, 4) + "*****" + "\"/>" +
                "<label for=\"secret\"> client secret</label>" +
                "</div>" +
                "<p>Yandex bearer token: " + Yandex.bearerToken+
                "</p>" +
                "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"cred_yandex\">" +
                "<div>" +
                "<br><button>save</button>" +
                "</div>" +
                "</form>" +
                "<p><a href=\"/\">Home</a></p>" +
                "<script src=\"script.js\"></script>" +
                "</body></html>";
        return page;
    }

    public static String join(List<String> list) {
        final String[] join = {""};
        list.stream()
                .map(l -> "<p>" + l + "</p>")
                .map(l -> join[0] = join[0] + l).collect(Collectors.toList());
        log.info(join[0]);
        return join[0];
    }

    public static String formSpeakers() {
        String page = "<!DOCTYPE html><html lang=\"en\">" +
                "<head><meta charset=\"UTF-8\" />" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                "  <link rel=\"stylesheet\" href=\"style.css\" />" +
                "  <title>Browser</title></head><body>" +
                "<p><a href=\"/\">Home</a></p>" +
                "  <h2>Подключение колонок LMS в Умный дом с Алисой</h2>" +
                "<p>всего колонок подключено: " +
                SmartHome.devices.size() +
                "</p>" +
                "<p>" +
                SmartHome.devices.stream().map(d -> d.customData.lmsName).collect(Collectors.toList()) +
                "</p>" +

                join(SmartHome.devices.stream()
                        .map(d ->
                                        "<form action=\"/cmd\" method=\"get\">" +
                                                "<input type=\"hidden\" name=\"speaker_name_alice\" id=\"speaker_name_alice\" value=\"" + "музыка" + "\">" +
                                                "<br>" +
                                                "<input name=\"speaker_name_lms\" id=\"speaker_name_lms\" value=\"" + d.customData.lmsName + "\" />" +
                                                "<label for=\"speaker_name_lms\">Название колонки в LMS</label>" +
                                                "<br>" +
                                                "<input name=\"room\" id=\"room\" value=\"" + d.room + "\" />" +
                                                "<label for=\"room\">Комната в УД Яндекс</label>" +
                                                "<br>" +
                                                "<input name=\"id\" id=\"id\" value=\"" + d.id + "\" />" +
                                                "<label for=\"id\">ID колонки в УД Яндекс</label>" +
                                                "<input type=\"hidden\" name=\"id_old\" id=\"id_old\" value=\"" + SmartHome.devices.indexOf(d) + "\" />" +
                                                "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"speaker_edit\">" +
                                                "<br>" +
                                                "<button>save</button></form>" +
                                                "<form action=\"/cmd\" method=\"get\">" +
                                                "<input type=\"hidden\" name=\"id\" id=\"id\" value=\"" + d.id + "\">" +
                                                "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"speaker_remove\">" +
                                                "<button>remove</button></form>"
                        ).collect(Collectors.toList())) +
                "<h2>Добавить колонку</h2>" +
                "<p>Колонки найденные в LMS: " +
                getNotInHome() +
                "<form action=\"/cmd\" method=\"get\">" +
                "<input type=\"hidden\" name=\"speaker_name_alice\" id=\"speaker_name_alice\" value=\"" + "музыка" + "\">" +
                "<br><input name=\"speaker_name_lms\" id=\"speaker_name_lms\" value=\"" +
                getNotInHomeFirst() +
                "\" />" +
                "<label for=\"speaker_name_lms\">Название колонки в Logitech Media Server</label>" +
                "<br><input name=\"room\" id=\"room\" value=\"Комната\" />" +
                "<label for=\"room\">Название комнаты в Умном доме</label>" +
                "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"speaker_create\">" +
                "<br><button>add</button></form>" +
                "<p><a href=\"/\">Home</a></p>" +
                "<script src=\"script.js\"></script>" +
                "</body></html>";
        return page;
    }

    public static String formPlayers() {
        String page = "<!DOCTYPE html><html lang=\"en\">" +
                "<head><meta charset=\"UTF-8\" />" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                "  <link rel=\"stylesheet\" href=\"style.css\" />" +
                "  <title>Browser</title></head><body>" +
                "<p><a href=\"/\">Home</a></p>" +
                "  <h2>Настройка колонок</h2>" +
                join(lmsPlayers.players.stream()
                        .map(p ->
                                "<form action=\"/cmd\" method=\"get\">" +
                                        p.name +
                                        "<br>" +
                                        "<input name=\"step\" id=\"step\" value=\"" + p.volume_step + "\" />" +
                                        "<label for=\"step\">шаг громкости</label>" +
                                        "<br>" +
                                        "<input name=\"delay\" id=\"delay\" value=\"" + p.wake_delay + "\" />" +
                                        "<label for=\"delay\">задержка включения</label>" +
                                        "<br>" +
                                        "<input name=\"black\" id=\"black\" value=\"" + p.black + "\" />" +
                                        "<label for=\"black\">в черном списке</label>" +
                                        "<br>" +
                                        "<input name=\"schedule\" id=\"schedule\" value=\"" + Utils.mapToString(p.timeVolume) + "\" />" +
                                        "<label for=\"schedule\">время:громкость</label>" +
                                        "<br>" +
                                        "<input type=\"hidden\" name=\"name\" id=\"name\" value=\"" + p.name + "\">" +
                                        "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"player_edit\">" +
                                        "<button>save</button></form>" +
                                        "<form action=\"/cmd\" method=\"get\">"
                        ).collect(Collectors.toList())) +
                "<p><a href=\"/\">Home</a></p>" +
                "<script src=\"script.js\"></script>" +
                "</body></html>";
        return page;
    }

    public static String getNotInHome() {
        List<String> inLms = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        List<String> inHome = SmartHome.devices.stream().map(d -> d.customData.lmsName).collect(Collectors.toList());
        inLms.removeAll(inHome);
        return inLms.toString();
    }

    public static String getNotInHomeFirst() {
        List<String> inLms = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        List<String> inHome = SmartHome.devices.stream().map(d -> d.customData.lmsName).collect(Collectors.toList());
        inLms.removeAll(inHome);
        if (inLms.size() == 0) return "--";
        else return inLms.get(0).toString();
    }
}