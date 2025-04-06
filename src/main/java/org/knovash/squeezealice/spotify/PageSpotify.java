package org.knovash.squeezealice.spotify;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.spotify.SpotifyAuth;

import static org.knovash.squeezealice.web.PageIndex.pageOuter;

@Log4j2
public class PageSpotify {

    public static Context action(Context context) {
        context.bodyResponse = page();
        context.code = 200;
        return context;
    }

    public static String page() {
        String clientId = "client id";
        if (SpotifyAuth.client_id != null) clientId = SpotifyAuth.client_id;

        String pageInner = "<p>Авторизуйтесь в Spotify</p> \n" +
                "<form method='POST' action='/form'>" +
                "<input name='clientid' placeholder='" + clientId + "' required> client id<br>" +
                "<input name='clientsecret' placeholder='client secret' required> client secret<br>" +

                "<input name='action' type='hidden'  value='spotify_save_creds'>" +
                "<button type='submit'>Save</button>" +
                "</form>" +

                "<br>" +

                "<form method='POST' action='/form'>" +
                "<input name='action' type='hidden'  value='spoti_auth'>" +
                "<button type='submit'>Spotify Authorize</button>" +
                "</form>" +

                "<br>";
        String page = pageOuter(pageInner, "Настройка Spotify", "Настройка Spotify");
        return page;
    }
}

