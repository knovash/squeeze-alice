package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.spotify.SpotifyAuth;

@Log4j2
public class PageSpotiCallback {

    public static Context action(Context context) {
        String json = page();
        context.bodyResponse = json;
        context.code = 200;
        return context;
    }

    public static String page() {
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