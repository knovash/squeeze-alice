package org.knovash.squeezealice;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import java.io.IOException;
import java.util.Base64;

@Log4j2
@Data
public class Spotify {

    public static String bearerToken;

    public static String getBearerToken(String clientId, String clientSecret) {
        log.info("clientId: " + clientId + " clientSecret: " + clientSecret);
        Response response;
        String token;
        String clientIdSecret = clientId + ":" + clientSecret;
        String base64 = Base64.getEncoder().encodeToString(clientIdSecret.getBytes());
        log.info("base64: " + base64);
        String json = null;
        try {
            response = Request.Post("https://accounts.spotify.com/api/token?grant_type=client_credentials")
                    .setHeader("Authorization", "Basic " + base64)
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .execute();
            json = response.returnContent().asString();
            log.info("json: " + json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        token = JsonUtils.jsonGetValue(json, "access_token");
        log.info("token: " + token);
        bearerToken = "Bearer " + token.replace("\"", "");
        log.info("bearerToken: " + bearerToken);
        return bearerToken;
    }

    public static void action(String uri) {
        log.info("uri: " + uri);
        Response response = null;
        try {
            response = Request.Get(uri)
                    .setHeader("Authorization", bearerToken)
                    .execute();
            log.info("response: " + response.returnContent().asString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}