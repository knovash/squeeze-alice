package org.knovash.squeezealice.spotify;

import com.sun.net.httpserver.Headers;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.web.PageSpotiCallback;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static org.knovash.squeezealice.spotify.SpotifyRequests.requestPost;


@Log4j2
@Data
public class SpotifyAuth {

    public static String client_id;
    public static String client_secret;
    public static String encoded;
    public static String response_type = "code";
    public static String redirect_uri = Main.tunnel+"/spoti_callback";
    public static String show_dialog; // Optional
    public static String scope =
            "app-remote-control " +
                    "user-read-private " +
                    "user-read-email " +
                    "user-read-playback-state " +
                    "user-read-currently-playing " +
                    "user-read-private " +
                    "user-modify-playback-state";
    public static String code; // вернется в калбэке
    public static String state = "1234567890123456"; // вернется в калбэке
    public static String access_token;
    public static String bearer_token;
    public static String token_type;
    public static String expires_in;
    public static String refresh_token;

    public static Context requestUserAuthorization(Context context) {
//  https://developer.spotify.com/documentation/web-api/tutorials/code-flow
//  https://developer.spotify.com/dashboard/f45a18e2bcfe456dbd9e7b73e74514af/settings
        log.info("");
        log.info("REQUEST AUTH REDIRECT");
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
        log.info("FINISH\n");
        return context;
    }

    public static void requestAccessToken() {
//  https://developer.spotify.com/documentation/web-api/tutorials/code-flow
        log.info("");
        log.info("CALLBACK REQUEST ACCESS TOKEN");
        String uri = "https://accounts.spotify.com/api/token?" +
                "grant_type=" + "authorization_code" + "&" +
                "code=" + code + "&" +
                "redirect_uri=" + redirect_uri;
        encoded = Base64.getEncoder().encodeToString((client_id + ':' + client_secret).getBytes());
        String json;
        Header[] headers = {
                new BasicHeader("Authorization", "Basic " + encoded),
                new BasicHeader("Content-Type", "application/x-www-form-urlencoded")
        };
        json = requestPost(uri, headers);
        access_token = JsonUtils.jsonGetValue(json, "access_token");
        token_type = JsonUtils.jsonGetValue(json, "token_type");
        expires_in = JsonUtils.jsonGetValue(json, "expires_in");
        refresh_token = JsonUtils.jsonGetValue(json, "refresh_token");
        scope = JsonUtils.jsonGetValue(json, "scope");
        bearer_token = "Bearer " + access_token.replace("\"", "");
        log.info("access_token: " + access_token);
        log.info("bearerToken: " + bearer_token);
        log.info("expires_in: " + expires_in);
        log.info("refresh_token: " + refresh_token);
        write();
        log.info("FINISH\n");
    }

    public static Context callback(Context context) {
//  https://developer.spotify.com/documentation/web-api/tutorials/code-flow
        log.info("");
        log.info("CALLBACK QUERY: " + context.query);
        code = context.queryMap.get("code");
        state = context.queryMap.get("state");
        requestAccessToken();
        String json = PageSpotiCallback.page();
        context.json = json;
        context.code = 200;
        log.info("FINISH\n");
        return context;
    }

    public static void callbackRequestRefresh() {
//  https://developer.spotify.com/documentation/web-api/tutorials/refreshing-tokens
        String url = "https://accounts.spotify.com/api/token";
        String refToken = refresh_token;
        log.info("SPOTIFY CALLBACK REFRESH TOKEN " + url);
        encoded = Base64.getEncoder().encodeToString((client_id + ':' + client_secret).getBytes());
        String json;
        final Collection<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "refresh_token"));
        params.add(new BasicNameValuePair("refresh_token", refToken));
        final Content postResultForm;
        try {
            postResultForm = Request.Post(url)
                    .bodyForm(params, Charset.defaultCharset())
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Authorization", "Basic " + encoded)
                    .execute().returnContent();
        } catch (IOException e) {
            log.info("ERROR: " + e);
            return;
//            throw new RuntimeException(e);
        }
        String jsonResponse = postResultForm.asString();
        access_token = JsonUtils.jsonGetValue(jsonResponse, "access_token");
        bearer_token = "Bearer " + access_token;
//        log.info("SPOTIFY TOKEN: " + bearer_token);
    }


    public static String save(HashMap<String, String> parameters) {
        if (parameters.get("id") == null || parameters.get("secret") == null) return "CREDS ERROR";
        SpotifyAuth.client_id = parameters.get("id");
        SpotifyAuth.client_secret = parameters.get("secret");
        log.info("FINISH\n");
        return "CREDS SAVE";
    }

    public static void write() {
        log.info("WRITE CREDENTIALS TO spotify.json");
        Map<String, String> map = new HashMap<>();
        map.put("client_id", client_id);
        map.put("client_secret", client_secret);
        map.put("access_token", access_token);
        map.put("bearer_token", bearer_token);
        map.put("refresh_token", refresh_token);
        map.put("expires_in", expires_in);
        map.put("redirect_uri", redirect_uri);
        log.info(map);
        JsonUtils.mapToJsonFile(map, "spotify.json");
        log.info("FINISH\n");
    }

    public static void read() {
        log.info("READ CREDENTIALS FROM spotify.json");
        Map<String, String> map = JsonUtils.jsonFileToMap("spotify.json", String.class, String.class);
        if (map == null) return;
        client_id = map.get("client_id");
        client_secret = map.get("client_secret");
        access_token = map.get("access_token");
        bearer_token = map.get("bearer_token");
        refresh_token = map.get("refresh_token");
        expires_in = map.get("expires_in");
        redirect_uri = map.get("redirect_uri");
//        log.info(map);
    }
}