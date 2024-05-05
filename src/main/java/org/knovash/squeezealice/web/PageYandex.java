package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

import static org.knovash.squeezealice.provider.Yandex.yandex;

@Log4j2
public class PageYandex {

    public static Context action(Context context) {
        String json = page();
        context.json = json;
        context.code = 200;
        return context;
    }

    public static String page() {
        String page = "<!DOCTYPE html><html lang=\"en\">" +
                "<head><meta charset=\"UTF-8\" />" +
                "  <title>Yandex credentials</title>" +
                "</head>" +
                "<body>" +
                "<p><a href=\"/\">Home</a></p>" +
                "  <h1>Yandex credentials</h1>" +
                "<br>" +

                "<p><a href=https://oauth.yandex.ru/authorize?response_type=token&client_id=9aa97fffe29849bb945db5b82b3ee015>login</a></p>" +

                "<form action=\"/cmd\" method=\"get\">" + "<div>" +
                "<input name=\"client_id\" id=\"client_id\" value=\"" + yandex.clientId + "\"/>" +
                "<label for=\"client_id\"> client id</label>" +
                "<br><button>get token</button>" +
                "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"yandex_save_client_id\">" + "<div>" +
                "</div>" + "</form>" +
                "<p></p>" +

                "<form action=\"/cmd\" method=\"get\">" + "<div>" +
                "<input name=\"token\" id=\"bearer\" value=\"" + yandex.bearer + "\"/>" +
                "<label for=\"bearer\"> token</label>" +
                "<br><button>save token</button>" +
                "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"yandex_save_token\">" + "<div>" +
                "</div>" + "</form>" +
                "<p></p>" +


//                "<form action=\"/cmd\" method=\"get\">" + "<div>" +
//                "<input name=\"client_id\" id=\"client_id\" value=\"" + yandex.clientId + "\"/>" +
//                "<label for=\"client_id\"> client id</label>" + "</div>" + "<div>" + "<br>" +
//                "<input name=\"client_secret\" id=\"client_secret\" value=\"" + yandex.clientSecret + "\"/>" +
//                "<label for=\"client_secret\"> client secret</label>" + "</div>" +
//                "<p>Yandex bearer token: " + yandex.bearer + "</p>" +
//                "<input type=\"hidden\" name=\"action\" id=\"action\" value=\"cred_yandex\">" + "<div>" +
//                "<br><button>save</button>" +
//                "</div>" + "</form>" +


                "<p><a href=\"/\">Home</a></p>" +
                "</body></html>";
        return page;
    }
}

