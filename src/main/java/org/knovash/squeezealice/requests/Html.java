package org.knovash.squeezealice.requests;

public class Html {

    public static String form = "<!DOCTYPE html><html lang=\"en\">" +
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
            "   <input type=\"hidden\" name=\"action\" id=\"action\" value=\"cred\">"+
            "   <div>" +
            "     <br><button>submit</button>" +
            "   </div>" +
            " </form>" +
            "<p><a href=\"/\">Home</a></p>" +
            "<script src=\"script.js\"></script>" +
            "</body></html>";

    public static String index = "<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "" +
            "<head>" +
            "  <meta charset=\"UTF-8\" />" +
            "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
            "  <link rel=\"stylesheet\" href=\"style.css\" />" +
            "  <title>Browser</title>" +
            "</head>" +
            "<body>" +
            "<p><strong>Hellow! its LMS control</strong></p>" +
            "<p><a href=\"https://oauth.yandex.ru/authorize?response_type=code&amp;client_id=04046b5a2b0e41ef82feff72f1dacb27&amp;state=etcCpwmsrknC348Y81kU1w==\">login by Yandex</a></p>" +
            "<p><a href=\"/spotify\">Spotify credentials</a></p>" +
            "<p><a href=\"/cmd?action=log\">/cmd?action=log - Show log</a></p>" +
            "<p><a href=\"/cmd?action=update\">/cmd?action=update - Update players</a></p>" +
            "<p><a href=\"/cmd?action=backup\">/cmd?action=backup - Backup server state</a></p>" +
            "<p><a href=\"/cmd?action=state\">/cmd?action=state - Show state</a></p>" +
            "<p>/cmd?action=channel&amp;player=homepod&amp;value=1 &nbsp;- &nbsp;Play favorites 1 from LMS on player HomePod<br />" +
            "</body>" +
            "</html>";

    public static String web(String text) {

        text = text.replace("\n", "<br>");

        String page = "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "  <meta charset=\"UTF-8\" />" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                "  <link rel=\"stylesheet\" href=\"style.css\" />" +
                "  <title>Browser</title>" +
                "</head>" +
                "<body>" +
                "<p><a href=\"/\">home</a></p>" +
                "<p>" + text + "</p>" +
                "<p><a href=\"/\">home</a></p>" +
                "</body>" +
                "</html>";
        return page;
    }
}
