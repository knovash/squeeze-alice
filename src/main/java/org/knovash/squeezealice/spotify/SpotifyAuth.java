package org.knovash.squeezealice.spotify;

import com.sun.net.httpserver.Headers;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.web.Html;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.knovash.squeezealice.spotify.SpotifyRequests.requestForJson;


@Log4j2
@Data
public class SpotifyAuth {

    public static String client_id;
    public static String client_secret;
    public static String encoded;
    public static String response_type = "code";
    public static String redirect_uri = "https://unicorn-neutral-badly.ngrok-free.app/spoti_callback";
    public static String show_dialog; // Optional
    public static String scope = "user-read-private user-read-email user-read-playback-state"; // Optional
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
        log.info("requestUserAuthorization");
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

    public static void requestAccessToken() {
//  https://developer.spotify.com/documentation/web-api/tutorials/code-flow
        log.info("request Access Token");
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
        json = requestForJson(uri, headers);
        access_token = JsonUtils.jsonGetValue(json, "access_token");
        token_type = JsonUtils.jsonGetValue(json, "token_type");
        expires_in = JsonUtils.jsonGetValue(json, "expires_in");
        refresh_token = JsonUtils.jsonGetValue(json, "refresh_token");
        scope = JsonUtils.jsonGetValue(json, "scope");
        bearer_token = "Bearer " + access_token.replace("\"", "");
        log.info("access_token: " + access_token);
        log.info("bearerToken: " + bearer_token);
        write();
    }

    public static Context callback(Context context) {
//  https://developer.spotify.com/documentation/web-api/tutorials/code-flow
        log.info("/CALLBACK QUERY " + context.query);
        code = context.queryMap.get("code");
        state = context.queryMap.get("state");
        requestAccessToken();
        String json = Html.spoti_callback();
        context.json = json;
        context.code = 200;
        return context;
    }

    public static Context requestRefresh(Context context) {
//  https://developer.spotify.com/documentation/web-api/tutorials/refreshing-tokens
        log.info("REFRESH");
        context.json = "REFRESH";
        context.code = 302;
        String location = "https://accounts.spotify.com/api/token";
        String json = "{" +
                "\"grant_type\": \"refresh_token\"," +
                "\"refresh_token\": \"" + "AQCInBGE4cutlNYbShDtT4Z_3G0pCTAKvNwWzyT3IK2XBnLvN0xhKGSj_2S1tP0HzjiSK-X_LHZi0Ay2IFRNThrwXqhbRzocfDoqkXbGoCHeCC_r8I72qV4eBreLo67DpOo" + "\"" +
                "}";
//        Header[] headers = {
//                new BasicHeader("Authorization", "Basic ZjQ1YTE4ZTJiY2ZlNDU2ZGJkOWU3YjczZTc0NTE0YWY6NWMzMzIxYjRhZTdlNDNhYjkzYTJjZTRlYzFiNGNmNDg="),
//                new BasicHeader("Content-Type", "application/x-www-form-urlencoded")
//        };
        log.info("REDIRECTURI: " + location);
        Headers headers = new Headers();
        headers.add("Location", location);
        headers.add("Authorization", "Basic ZjQ1YTE4ZTJiY2ZlNDU2ZGJkOWU3YjczZTc0NTE0YWY6NWMzMzIxYjRhZTdlNDNhYjkzYTJjZTRlYzFiNGNmNDg=");
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        context.json = json;
        context.headers = headers;
        return context;
    }

    public static void runRequestRefresh() {
        log.info("RUN REQUEST REFRESH");
    }

//    public static void refresh() {
//        log.info("REFRESH");
//        String uri = "https://accounts.spotify.com/api/token";
//        String json = "{" +
//                "\"grant_type\": \"refresh_token\"," +
//                "\"refresh_token\": \"" + "AQCInBGE4cutlNYbShDtT4Z_3G0pCTAKvNwWzyT3IK2XBnLvN0xhKGSj_2S1tP0HzjiSK-X_LHZi0Ay2IFRNThrwXqhbRzocfDoqkXbGoCHeCC_r8I72qV4eBreLo67DpOo" + "\"" +
//                "}";
//        Header[] headers = {
//                new BasicHeader("Authorization", "Basic ZjQ1YTE4ZTJiY2ZlNDU2ZGJkOWU3YjczZTc0NTE0YWY6NWMzMzIxYjRhZTdlNDNhYjkzYTJjZTRlYzFiNGNmNDg="),
//                new BasicHeader("Content-Type", "application/x-www-form-urlencoded")
//        };
//        HttpResponse response = Requests.postByHeadersJsonForResponse(uri, headers, json);
//        log.info("REFRESH CONTENT " + response.getEntity().toString());
//    }

    public static String save(HashMap<String, String> parameters) {
        if (parameters.get("id") == null || parameters.get("secret") == null) return "CREDS ERROR";
        SpotifyAuth.client_id = parameters.get("id");
        SpotifyAuth.client_secret = parameters.get("secret");
        return "CREDS SAVE";
    }

    public static void write() {
        Map<String, String> map = new HashMap<>();
        map.put("client_id", client_id);
        map.put("client_secret", client_secret);
        map.put("bearerToken", bearer_token);
        JsonUtils.mapToJsonFile(map, "spotify.json");
    }

    public static void read() {
        Map<String, String> map = new HashMap<>();
        map = JsonUtils.jsonFileToMap("spotify.json", String.class, String.class);
        if (map == null) return;
        client_id = map.get("client_id");
        client_secret = map.get("client_secret");
        bearer_token = map.get("bearerToken");
    }
}