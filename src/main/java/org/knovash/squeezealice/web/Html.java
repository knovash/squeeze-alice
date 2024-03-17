package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.provider.Yandex.yandex;

@Log4j2
public class Html {

    public static String index() {
        String page = "<!doctype html><html lang=\"ru\">\n" +
                "<head>\n" +
                "<meta charSet=\"utf-8\" />\n" +
                "<title>Squeeze-Alice</title>" +
                "</head>\n" +
                "<body> \n" +
                "<p><strong>Squeeze-Alice</strong></p> \n" +
                "<p><a href=\\speakers>Подключение колонок LMS в УД с Алисой</a></p> \n" +
                "<p><a href=\\players>Настройка колонок</a></p> \n" +
                "<p><a href=\\spotify>Настройка Spotify</a></p> \n" +
                "<p><a href=\\cmd?action=state_devices>Посмотреть настройки Devices</a></p> \n" +
                "<p><a href=\\cmd?action=state_players>Посмотреть настройки Players</a></p> \n" +
                "<p><a href=\\cmd?action=log>Посмотреть лог</a></p> \n" +
//                "<p>-------</p> \n" +
//                "<p><a href=\\spoti_auth>Spotify Auth</a></p> \n" +
//                "<p><a href=\\spoti_refresh>Spotify Auth Refresh direct</a></p> \n" +
//                "<p><a href=\\cmd?action=spoti_refresh>Spotify Auth Refresh cmd</a></p> \n" +
//                "<p><a href=\\cmd?action=spoti_state>Spotify state</a></p> \n" +
                "<p><a href=\\cmd?action=restart>Restart server service</a></p> \n" +
                "<p><a href=\\cmd?action=reboot>Reboot device</a></p> \n" +
                "</body>\n" +
                "</html>";
        return page;
    }

    public static String formSpotifyLogin() {
        String page = "<!DOCTYPE html><html lang=\"en\">" +
                "<head><meta charset=\"UTF-8\" />" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                "  <link rel=\"stylesheet\" href=\"style.css\" />" +
                "  <title>Настройка Spotify</title>" +
                "</head>" +
                "<body>" +
                "<p><a href=\"/\">Home</a></p>" +
                "  <h1>Настройка Spotify</h1>" +
                "<br>" +
                "<form action=\"/cmd\" method=\"get\">" +
                "<div>" +
                "<input name=\"id\" id=\"id\" value=\"" + Spotify.getClientIdHidden() + "\"/>" +
                "<label for=\"id\"> client id</label>" +
                "</div>" +
                "<div>" +
                "<br>" +
                "<input name=\"secret\" id=\"secret\" value=\"" + Spotify.getClientSecretHidden() + "\"/>" +
                "<label for=\"secret\"> client secret</label>" +
                "</div>" +
                "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"spotify_save_creds\">" +
                "<div>" +
                "<br><button>save</button>" +
                "</div>" +
                "</form>" +
                "<p><a href=\\spoti_auth>Spotify Auth</a></p> \n" +
                "<p><a href=\"/\">Home</a></p>" +
                "</body></html>";
        return page;
    }

    public static String formYandexLogin() {
        String page = "<!DOCTYPE html><html lang=\"en\">" +
                "<head><meta charset=\"UTF-8\" />" +
                "  <title>Yandex credentials</title>" +
                "</head>" +
                "<body>" +
                "<p><a href=\"/\">Home</a></p>" +
                "  <h1>Yandex credentials</h1>" +
                "<br>" +
                "<form action=\"/cmd\" method=\"get\">" +
                "<div>" +
                "<input name=\"client_id\" id=\"client_id\" value=\"" + yandex.clientId + "\"/>" +
                "<label for=\"client_id\"> client id</label>" +
                "</div>" +
                "<div>" +
                "<br>" +
                "<input name=\"client_secret\" id=\"client_secret\" value=\"" + yandex.clientSecret + "\"/>" +
                "<label for=\"client_secret\"> client secret</label>" +
                "</div>" +
                "<p>Yandex bearer token: " + yandex.bearer +
                "</p>" +
                "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"cred_yandex\">" +
                "<div>" +
                "<br><button>save</button>" +
                "</div>" +
                "</form>" +
                "<p><a href=\"/\">Home</a></p>" +
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
        lmsPlayers.update();
        String page = "<!DOCTYPE html><html lang=\"en\">" +
                "<head><meta charset=\"UTF-8\" />" +
                "<title>Подключение колонок LMS в УД с Алисой</title></head><body>" +
                "<p><a href=\"/\">Home</a></p>" +
                "<h2>Подключение колонок LMS в УД с Алисой</h2>" +
                "<p>Колонки найденные в LMS: " +
                getNotInHome() + " " +
                "<a href=\"/cmd?action=players_update\">обновить</a></p>" +
                "<p>Колонки подключенные в УД: " +
                SmartHome.devices.stream().map(d -> d.customData.lmsName).collect(Collectors.toList()) + " " +
                "<a href=\"/cmd?action=speakers_clear\">очистить</a></p>" +
                formAddSpeaker() +
                "<h3>Подключенные колонки</h3>" +
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
                "<p><a href=\"/\">Home</a></p>" +
                "</body></html>";
        return page;
    }

    public static String formAddSpeaker() {
        String page = "";
        if (getNotInHome().size() > 0) {
            page = "<h3>Добавить колонку</h3>" +
                    "<form action=\"/cmd\" method=\"get\">" +
                    "<input type=\"hidden\" name=\"speaker_name_alice\" id=\"speaker_name_alice\" value=\"" + "музыка" + "\">" +
                    getNotInHomeFirst() +
                    "<br>" +
                    "<input type=\"hidden\" name=\"speaker_name_lms\" id=\"speaker_name_lms\" value=\"" + getNotInHomeFirst() + "\" />" +
                    "<br>" +
                    "<input name=\"room\" id=\"room\" value=\"Комната\" />" +
                    "<label for=\"room\">Название комнаты в Умном доме</label>" +
                    "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"speaker_create\">" +
                    "<br><button>add</button></form>";
        }
        return page;
    }

    public static String formPlayers() {
        lmsPlayers.update();
        String page = "<!DOCTYPE html><html lang=\"en\">" +
                "<head><meta charset=\"UTF-8\" />" +
                "  <title>Настройка колонок</title></head><body>" +
                "<p><a href=\"/\">Home</a></p>" +
                "  <h2>Настройка колонок</h2>" +
                join(lmsPlayers.players.stream()
                        .map(p -> "<form action=\"/cmd\" method=\"get\">" +
                                "<b>" + p.name + " - " + "</b>" +
                                checkPlayerOnlineInLms(p.name) + " - " +
                                checkPlayerConnectInSmartHome(p.name) +
                                "<br>" +
                                "<input name=\"step\" id=\"step\" value=\"" + p.volume_step + "\" />" +
                                "<label for=\"step\"> шаг громкости</label>" +
                                "<br>" +
                                "<input name=\"delay\" id=\"delay\" value=\"" + p.wake_delay + "\" />" +
                                "<label for=\"delay\"> задержка включения</label>" +
                                "<br>" +
                                "<input name=\"black\" id=\"black\" value=\"" + p.black + "\" />" +
                                "<label for=\"black\"> в черном списке</label>" +
                                "<br>" +
                                "<input name=\"schedule\" id=\"schedule\" value=\"" + Utils.mapToString(p.timeVolume) + "\" />" +
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
                "<p>последний запрос от Алисы id: " + Player.lastAliceId + "</p>" +
                "<p>чтобы узнать id Алисы, спросите Алиса скажи раз два, что сейчас играет? и обновите страницу</p>" +
                "<p><a href=\"/\">Home</a></p>" +
                "</body></html>";
        return page;
    }

    public static String checkPlayerOnlineInLms(String playerName) {
        if (lmsPlayers.playersOnlineNames.contains(playerName)) return "in LMS online";
        return "in LMS offline";
    }

    public static String checkPlayerConnectInSmartHome(String playerName) {
        if (SmartHome.getRoomByPlayerName(playerName) == null) return "в УД не подключено";
        return "в УД подключено";
    }

    public static List<String> getNotInHome() {
        List<String> inLms = lmsPlayers.playersOnlineNames;
        List<String> inHome = SmartHome.devices.stream().map(d -> d.customData.lmsName).collect(Collectors.toList());
        inLms.removeAll(inHome);
        return inLms;
    }

    public static String getNotInHomeFirst() {
        List<String> inLms = lmsPlayers.playersOnlineNames;
        List<String> inHome = SmartHome.devices.stream().map(d -> d.customData.lmsName).collect(Collectors.toList());
        inLms.removeAll(inHome);
        if (inLms.size() == 0) return "--";
        else return inLms.get(0).toString();
    }

    public static String spoti_callback() {
        String page = "<!doctype html><html lang=\"ru\">\n" +
                "<head>\n" +
                "<meta charSet=\"utf-8\" />\n" +
                "<title>Spotify callback</title>" +
                "</head>\n" +
                "<body> \n" +
                "<p><a href=\"/\">Home</a></p>" +
                "<p><strong>Spotify callback</strong></p> \n" +
                "<p>client_id: " + SpotifyAuth.client_id + "</p> \n" +
                "<p>client_secret: " + SpotifyAuth.client_secret + "</p> \n" +
                "<p>encoded: " + SpotifyAuth.encoded + "</p> \n" +
                "<p>response_type: " + SpotifyAuth.response_type + "</p> \n" +
                "<p>redirect_uri: " + SpotifyAuth.redirect_uri + "</p> \n" +
                "<p>show_dialog: " + SpotifyAuth.show_dialog + "</p> \n" +
                "<p>scope: " + SpotifyAuth.scope + "</p> \n" +
                "<p>code: " + SpotifyAuth.code + "</p> \n" +
                "<p>state: " + SpotifyAuth.state + "</p> \n" +
                "<p>access_token: " + SpotifyAuth.access_token + "</p> \n" +
                "<p>bearer_token: " + SpotifyAuth.bearer_token + "</p> \n" +
                "<p>refresh_token: " + SpotifyAuth.refresh_token + "</p> \n" +
                "</body>\n" +
                "</html>";
        log.info("bearerToken: " + SpotifyAuth.bearer_token);
        return page;
    }
}