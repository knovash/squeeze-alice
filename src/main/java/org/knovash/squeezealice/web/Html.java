package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.provider.SmartHome;
import org.knovash.squeezealice.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.server;

@Log4j2
public class Html {

    public static String name = "музыка";

    public static String index() {
        String page =
                "<!DOCTYPE html>" +
                        "<html lang=\"en\"><head>" +
                        "<!doctype html>" +
                        "<html lang=\"ru\">" +
                        "<head>" +
                        "<meta charSet=\"utf-8\" />" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, shrink-to-fit=no, viewport-fit=cover'>" +
                        "<meta http-equiv='X-UA-Compatible' content='ie=edge'>" +
                        "<style>" +
                        "   html," +
                        "   body {" +
                        "      background: #eee;" +
                        "   }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<p><strong>Squeeze-Alice</strong></p>" +
                        "<p><a href=\"/speakers\">Подключение колонок в УД с Алисой</a></p>" +
                        "<p><a href=\"/players\">Настройка колонок</a></p>" +
                        "<p><a href=\"/spotify\">Настройка Spotify</a></p>" +
//                        "<p><a href=\"/cmd?action=update\">/cmd?action=update - Update players</a></p>" +
                        "<p><a href=\"/cmd?action=state\">Посмотреть настройки</a></p>" +
                        "<p><a href=\"/cmd?action=backup\">Сохранить настройки</a></p>" +
                        "<p><a href=\"/cmd?action=log\">Посмотреть лог</a></p>" +
                        "</body>" +
                        "</html>";
        return page;
    }


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
                "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"cred\">" +
                "<div>" +
                "<br><button>save</button>" +
                "</div>" +
                "</form>" +
                "<p><a href=\"/\">Home</a></p>" +
                "<script src=\"script.js\"></script>" +
                "</body></html>";

        return page;
    }

//    public static String auth =
//            "<!DOCTYPE html>" +
//                    "<html lang=\"en\"><head>" +
//                    "<!doctype html>" +
//                    "<html lang=\"ru\">" +
////                    "<head>" +
////                    "<meta charSet=\"utf-8\" />" +
////                    "<meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, shrink-to-fit=no, viewport-fit=cover'>" +
////                    "<meta http-equiv='X-UA-Compatible' content='ie=edge'>" +
////                    "<style>" +
////                    "   html," +
////                    "   body {" +
////                    "      background: #eee;" +
////                    "   }" +
////                    "</style>" +
////                    "</head>" +
//
//                    "<head>\n" +
//                    "   <script src=\"https://yastatic.net/s3/passport-sdk/autofill/v1/sdk-suggest-with-polyfills-latest.js\"></script>\n" +
//                    "</head>"+
//
//                    "<body>" +
//                    "<p><strong>Squeeze-Alice</strong></p>" +
//
//                    "YaAuthSuggest.init(\n" +
//                    "      {\n" +
//                    "         client_id: 'c46f0c53093440c39f12eff95a9f2f93',\n" +
//                    "         response_type: 'token',\n" +
//                    "         redirect_uri: 'https://examplesite.com/suggest/token'\n" +
//                    "      },\n" +
//                    "      'https://examplesite.com'\n" +
//                    "   )\n" +
//                    "   .then(({\n" +
//                    "      handler\n" +
//                    "   }) => handler())\n" +
//                    "   .then(data => console.log('Сообщение с токеном', data))\n" +
//                    "   .catch(error => console.log('Обработка ошибки', error));"+
//
//                    "<p><a href=\"/cmd?action=state\">Посмотреть настройки</a></p>" +
//                    "<p><a href=\"/cmd?action=backup\">Сохранить настройки</a></p>" +
//                    "<p><a href=\"/cmd?action=log\">Посмотреть лог</a></p>" +
//                    "</body>" +
//                    "</html>";

    public static String auth = "<!DOCTYPE html>" +
            "<html lang=\"en\"><head>" +
            "<!doctype html>" +
            "<html lang=\"ru\">" +
            "<head>" +
            "<meta charSet=\"utf-8\" />" +
            "<meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, shrink-to-fit=no, viewport-fit=cover'>" +
            "<meta http-equiv='X-UA-Compatible' content='ie=edge'>" +
            "<style>" +
            "   html," +
            "   body {" +
            "      background: #eee;" +
            "   }" +
            "</style>" +
            "<script src=\"https://yastatic.net/s3/passport-sdk/autofill/v1/sdk-suggest-with-polyfills-latest.js\"></script>" +
            "</head>" +
            "<body>" +
            "   <script>" +
            "   window.onload = function() {" +
            "      window.YaAuthSuggest.init({" +
            "                  client_id: '0d17cba2ab254d838ac1ddcedabc4191'," +
            "                  response_type: 'token'," +
            "                  redirect_uri: 'https://social.yandex.net/broker/redirect'" +
            "               }," +
            // TODO
            "               'https://unicorn-neutral-badly.ngrok-free.app', {" +
            "                  view: 'button'," +
            "                  parentId: 'container'," +
            "                  buttonView: 'main'," +
            "                  buttonTheme: 'light'," +
            "                  buttonSize: 'm'," +
            "                  buttonBorderRadius: 0" +
            "               }" +
            "            )" +
            "            .then(function(result) {" +
            "               return result.handler()" +
            "            })" +
            "            .then(function(data) {" +
            "               console.log('Сообщение с токеном: ', data);" +
            "               document.body.innerHTML += `Сообщение с токеном: ${JSON.stringify(data)}`;" +
            "            })" +
            "            .catch(function(error) {" +
            "               console.log('Что-то пошло не так: ', error);" +
            "               document.body.innerHTML += `Что-то пошло не так: ${JSON.stringify(error)}`;" +
            "            });" +
            "      };" +
            "   </script>" +
            "</body>" +
            "</html>";

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
                                                "<input type=\"hidden\" name=\"speaker_name_alice\" id=\"speaker_name_alice\" value=\"" + name + "\">" +
                                                "<br>" +
                                                "<input name=\"speaker_name_lms\" id=\"speaker_name_lms\" value=\"" + d.customData.lmsName + "\" />" +
                                                "<label for=\"speaker_name_lms\">Название колонки в LMS</label>" +
                                                "<br>" +
//                                        "<input name=\"speaker_name_query\" id=\"speaker_name_query\" value=\"" + Utils.getPlayerName(d.customData.lmsName) + "\" />" +
//                                        "<label for=\"speaker_name_query\">Название колонки в query лучше не менять</label>" +
//                                        "<br>" +
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
                "<input type=\"hidden\" name=\"speaker_name_alice\" id=\"speaker_name_alice\" value=\"" + name + "\">" +
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
                join(server.players.stream()
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
        List<String> inLms = server.players.stream().map(p -> p.name).collect(Collectors.toList());
        List<String> inHome = SmartHome.devices.stream().map(d -> d.customData.lmsName).collect(Collectors.toList());
        inLms.removeAll(inHome);
        return inLms.toString();
    }

    public static String getNotInHomeFirst() {
        List<String> inLms = server.players.stream().map(p -> p.name).collect(Collectors.toList());
        List<String> inHome = SmartHome.devices.stream().map(d -> d.customData.lmsName).collect(Collectors.toList());
        inLms.removeAll(inHome);
        if (inLms.size() == 0) return "--";
        else return inLms.get(0).toString();
    }

}