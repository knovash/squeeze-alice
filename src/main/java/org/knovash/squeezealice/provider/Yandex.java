package org.knovash.squeezealice.provider;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.knovash.squeezealice.spotify.spotify_pojo.SpotifyCredentials;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.util.Base64;

@Log4j2
@Data
public class Yandex {

    public static String bearerToken;
    public static SpotifyCredentials credentialsYandex = new SpotifyCredentials();

    public static String getBearerToken() {
        String client_id = SmartHome.client_id;
        String client_secret = SmartHome.client_secret;
//        Credentials sc = new Credentials();
        log.info("clientId: " + client_id + " clientSecret: " + client_secret);
        Response response;
        String token;
        String clientIdSecret = client_id + ":" + client_secret;
        String base64 = Base64.getEncoder().encodeToString(clientIdSecret.getBytes());
        log.info("base64: " + base64);
        String json = null;

        String urlParameters =
                "client_id=" + Yandex.credentialsYandex.clientId +
                        "&" +
                        "client_secret=" + Yandex.credentialsYandex.clientSecret +
                        "&" +
                        "grant_type=client_credentials";
        try {
            ContentType contentType = ContentType.parse("application/x-www-form-urlencoded");
            response = Request.Post("https://oauth.yandex.ru/token?grant_type=refresh_token")
                    .bodyString(urlParameters, contentType)
//                    .setHeader("Authorization", "Basic " + base64)
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .execute();
            json = response.returnContent().asString();
            log.info("json: " + json);
        } catch (IOException e) {
            log.info("YANDEX BEARER TOKEN REQUEST ERROR try check credentials in spotify.json");
            return null;
//            throw new RuntimeException(e);
        }
        token = JsonUtils.jsonGetValue(json, "access_token");
        log.info("token: " + token);
        String bearer = "Bearer " + token.replace("\"", "");
        log.info("bearerToken: " + bearer);
        bearerToken = bearer;
        return bearer;
    }


    public static void createCredFile() {
        SpotifyCredentials sc = new SpotifyCredentials();
        credentialsYandex.setClientId("ClientId");
        credentialsYandex.setClientSecret("ClientSecret");
        JsonUtils.pojoToJsonFile(sc, "yandex.json");
    }

    public static void createCredFile(String id, String secret) {
        SpotifyCredentials sc = new SpotifyCredentials();
        credentialsYandex.setClientId(id);
        credentialsYandex.setClientSecret(secret);
        credentialsYandex.setClientSecret(secret);
        JsonUtils.pojoToJsonFile(sc, "yandex.json");
    }
}