package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.spotify.Spotify;

@Log4j2
public class PageSpotify {

    public static Context action(Context context) {
        String json = page();
        context.json = json;
        context.code = 200;
        return context;
    }

    public static String page() {
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
}

