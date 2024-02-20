package org.knovash.squeezealice.provider;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

@Log4j2
@Data
public class Yandex {

    //    public static String bearerToken;
    public String clientId = "no-------------------";
    public String clientSecret = "no---------------";
    public String bearer = "no---------------";
    public static Yandex yandex = new Yandex();

    public static void credentialsYandex(HashMap<String, String> parameters) {
        yandex.clientId = parameters.get("client_id");
        yandex.clientSecret = parameters.get("client_secret");
        yandex.bearer = getBearerToken();
        JsonUtils.pojoToJsonFile(yandex, "yandex.json");
    }

    public static String getBearerToken() {
        if (yandex.clientId == null) JsonUtils.jsonFileToPojo("yandex.json", Yandex.class);
        String client_id = yandex.clientId;
        String client_secret = yandex.clientSecret;
        log.info("clientId: " + client_id + " clientSecret: " + client_secret);
        Response response;
        String token;
        String clientIdSecret = client_id + ":" + client_secret;
        String base64 = Base64.getEncoder().encodeToString(clientIdSecret.getBytes());
        log.info("base64: " + base64);
        String json = null;

        String urlParameters =
                "client_id=" + client_id +
                        "&" +
                        "client_secret=" + client_secret +
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
        log.info("bearerToken: " + token);
        yandex.bearer = token;
        return token;
    }
}