package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Hive;
import org.knovash.squeezealice.spotify.Spotify;

@Log4j2
public class PageHive {

    public static Context action(Context context) {
        String json = page();
        context.bodyResponse = json;
        context.code = 200;
        return context;
    }

    public static String page() {
        String page = "<!DOCTYPE html><html lang=\"en\">" +
                "<head><meta charset=\"UTF-8\" />" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />" +
                "  <link rel=\"stylesheet\" href=\"style.css\" />" +
                "  <title>Настройка Hive</title>" +
                "</head>" +
                "<body>" +
                "<p><a href=\"/\">Home</a></p>" +
                "  <h1>Настройка Hive</h1>" +

                "<form action=\"/cmd\" method=\"get\">" +

                "<label for=\"hivebroker\"> client id: </label>" +
                "<input name=\"hivebroker\" id=\"hivebroker\" value=\"" + Hive.hiveBroker + "\"/>" +

                "<br>" +

                "<label for=\"hiveuser\"> client secret: </label>" +
                "<input name=\"hiveuser\" id=\"secrhiveuseret\" value=\"" + Hive.hiveUsername + "\"/>" +

                "<br>" +

                "<label for=\"hivepassword\"> client secret: </label>" +
                "<input name=\"hivepassword\" id=\"hivepassword\" value=\"" + Hive.hivePassword + "\"/>" +

                "<br>" +

                "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"hive_save_creds\">" +

                "<br>" +
                "<button>save</button>" +

                "</form>" +

                "<p><a href=\"/\">Home</a></p>" +
                "</body></html>";
        return page;
    }
}

