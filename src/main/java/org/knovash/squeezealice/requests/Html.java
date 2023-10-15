package org.knovash.squeezealice.requests;

public class Html {



    public static String sss = "{\n" +
            "  \"request_id\": \"EE109B31-FF6C-48BD-80DB-4D07A9AFEBB3\",\n" +
            "  \"payload\": {\n" +
            "      \"devices\": [{\n" +
            "          \"id\": \"abc-123\",\n" +
            "          \"capabilities\": [{\n" +
            "              \"type\": \"devices.capabilities.color_setting\",\n" +
            "              \"state\": {\n" +
            "                  \"instance\": \"hsv\",\n" +
            "                  \"action_result\": {\n" +
            "                      \"status\": \"ERROR\",\n" +
            "                      \"error_code\": \"INVALID_ACTION\",\n" +
            "                      \"error_message\": \"the human readable error message\"\n" +
            "                  }\n" +
            "              }\n" +
            "          },\n" +
            "          {\n" +
            "              \"type\": \"devices.capabilities.on_off\",\n" +
            "              \"state\": {\n" +
            "                  \"instance\": \"on\",\n" +
            "                  \"action_result\": {\n" +
            "                      \"status\": \"DONE\"\n" +
            "                  }\n" +
            "              }\n" +
            "          }]\n" +
            "     },\n" +
            "     {\n" +
            "        \"id\": \"sock-56GF-3\",\n" +
            "        \"action_result\": {\n" +
            "          \"status\": \"ERROR\",\n" +
            "          \"error_code\": \"DEVICE_UNREACHABLE\"\n" +
            "        }\n" +
            "     }]\n" +
            "  }\n" +
            "}";


    public static String formSpotifyLogin = "<!DOCTYPE html><html lang=\"en\">" +
            "<head><meta charset=\"UTF-8\" />" +
            "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
            "  <link rel=\"stylesheet\" href=\"style.css\" />" +
            "  <title>Browser</title>" +
            "</head>" +
            "<body>" +
            "  <h1>Spotify credentials</h1>" +
            "  <form action=\"/cmd\" method=\"get\">" +
            "   <div>" +
            "     <br><label for=\"id\">spot_client_id</label>" +
            "     <br><input name=\"id\" id=\"id\" value=\"f45a******3e74514af\" />" +
            "   </div>" +
            "   <div>" +
            "     <br><label for=\"secret\">spot_client_secret</label>" +
            "     <br><input name=\"secret\" id=\"secret\" value=\"5c332*******c1b4cf48\" />" +
            "   </div>" +
            "   <input type=\"hidden\" name=\"action\" id=\"action\" value=\"cred\">" +
            "   <div>" +
            "     <br><button>submit</button>" +
            "   </div>" +
            " </form>" +
            "<p><a href=\"/\">Home</a></p>" +
            "<script src=\"script.js\"></script>" +
            "</body></html>";

    public static String index = "<!DOCTYPE html>" +
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
            "<p><strong>Hellow! its LMS control</strong></p>" + "<p><a href=\"/spotify\">Spotify credentials</a></p>" +
            "<p><a href=\"/cmd?action=log\">/cmd?action=log - Show log</a></p>" +
            "<p><a href=\"/cmd?action=update\">/cmd?action=update - Update players</a></p>" +
            "<p><a href=\"/cmd?action=backup\">/cmd?action=backup - Backup server state</a></p>" +
            "<p><a href=\"/cmd?action=state\">/cmd?action=state - Show state</a></p>" +
            "</body>" +
            "</html>";

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
            "               'https://sqtest.loca.lt', {" +
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

    public static String web(String text) {
        text = text.replace("\n", "<br>");
        String page = "<!DOCTYPE html>" +
                "<html lang=\"en\"><head>" +
                "  <meta charset=\"UTF-8\" />" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                "  <link rel=\"stylesheet\" href=\"style.css\" />" +
                "  <title>Browser</title>" +
                "</head><body>" +
                "<p><a href=\"/\">home</a></p>" +
                "<p>" + text + "</p>" +
                "<p><a href=\"/\">home</a></p>" +
                "</body></html>";
        return page;
    }
}