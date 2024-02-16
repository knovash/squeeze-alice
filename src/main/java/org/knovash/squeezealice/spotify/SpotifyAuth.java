package org.knovash.squeezealice.spotify;

import com.sun.net.httpserver.Headers;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.message.BasicHeader;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.web.Html;

import java.io.IOException;
import java.util.Base64;

import static org.knovash.squeezealice.spotify.Request.requestForJson;

@Log4j2
@Data
public class SpotifyAuth {

    public static String client_id = "f45a18e2bcfe456dbd9e7b73e74514af";
    public static String client_secret = "5c3321b4ae7e43ab93a2ce4ec1b4cf48";
    public static String response_type = "code";
    public static String redirect_uri = "https://unicorn-neutral-badly.ngrok-free.app/spoti_callback";
    public static String show_dialog = "-"; // Optional
    public static String scope = "user-read-private user-read-email user-read-playback-state"; // Optional
    public static String code = "-"; // вернется в калбэке
    public static String state = "1234567890123456"; // вернется в калбэке
    public static String access_token = "-";
    public static String bearerToken = "-";
    public static String token_type = "-";
    public static String expires_in = "-";

    public static Context action(Context context) {
        log.info("/AUTH");
        context.json = "REDIRECT";
        context.code = 302;
        String location = "https://accounts.spotify.com/authorize?" +
                "client_id=" + client_id + "&" +            // Required
                "response_type=" + response_type + "&" +    // Required
                "redirect_uri=" + redirect_uri + "&" +      // Required
                "state=" + state + "&" +                    // Optional
                "scope=" + scope;                           // Optional
        log.info("REDIRECTURI: " + location);
        Headers headers = new Headers();
        headers.add("Location", location);
        context.headers = headers;
        return context;
    }

    public static Context callback(Context context) {
        log.info("/CALLBACK QUERY " + context.query);
        code = context.queryMap.get("code");
        state = context.queryMap.get("state");
        requestAccessToken();
        String json = Html.spoti_callback();
        context.json = json;
        context.code = 200;
        return context;
    }

    public static void requestAccessToken() {
        log.info("request Access Token");
        String uri = "https://accounts.spotify.com/api/token?" +
                "grant_type=" + "authorization_code" + "&" +
                "code=" + code + "&" +
                "redirect_uri=" + redirect_uri;
        String encoded = Base64.getEncoder().encodeToString((client_id + ':' + client_secret).getBytes());
//        Response response;
        String json;
        Header[] headers = {
                new BasicHeader("Authorization", "Basic " + encoded),
                new BasicHeader("Content-Type", "application/x-www-form-urlencoded")
        };
        json = requestForJson(uri, headers);
//        try {
//            response = Request.Post(uri)
//                    .setHeaders(headers)
////                    .setHeader("Authorization", "Basic " + encoded)
////                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
//                    .execute();
//            json = response.returnContent().asString();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        access_token = JsonUtils.jsonGetValue(json, "access_token");
        token_type = JsonUtils.jsonGetValue(json, "token_type");
        expires_in = JsonUtils.jsonGetValue(json, "expires_in");
        scope = JsonUtils.jsonGetValue(json, "scope");
        log.info("access_token: " + access_token);
        bearerToken = "Bearer " + access_token.replace("\"", "");
        log.info("bearerToken: " + bearerToken);

    }

}


// https://developer.spotify.com/dashboard/f45a18e2bcfe456dbd9e7b73e74514af/settings
// Client ID & Client secret
