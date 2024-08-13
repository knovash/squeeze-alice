package org.knovash.squeezealice.provider;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Data
public class Yandex {

    public String clientId = "";
    public String clientSecret = "";
    public String bearer = "";
    public String user = "";
    public static Yandex yandex = new Yandex();
    public static YandexInfo yandexInfo = new YandexInfo();

    public static void writeCredentialsYandex(HashMap<String, String> parameters) {
        yandex.clientId = parameters.get("client_id");
        yandex.clientSecret = parameters.get("client_secret");
        yandex.bearer = getBearerToken();
        JsonUtils.pojoToJsonFile(yandex, "yandex.json");
    }

    public static void saveClientId(HashMap<String, String> parameters) {
        log.info("SAVE CLIENT ID " + parameters.get("client_id"));
        yandex.clientId = parameters.get("client_id");
        JsonUtils.pojoToJsonFile(yandex, "yandex.json");
    }

    public static void saveToken(HashMap<String, String> parameters) {
        log.info("SAVE TOKEN " + parameters.get("token"));
        yandex.bearer = parameters.get("token");
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
//      String uri = "https://oauth.yandex.ru/token?grant_type=refresh_token";
        String uri = "https://oauth.yandex.ru/token?grant_type=authorization_code&code=scope";
        String urlParameters =
                "client_id=" + client_id +
                        "&" +
                        "client_secret=" + client_secret +
                        "&" +
                        "grant_type=client_credentials";
        try {
            ContentType contentType = ContentType.parse("application/x-www-form-urlencoded");
            response = Request.Post(uri)
                    .bodyString(urlParameters, contentType)
//                    .setHeader("Authorization", "Basic " + base64)
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .execute();
            json = response.returnContent().asString();
            log.info("json: " + json);
        } catch (IOException e) {
            log.info("YANDEX BEARER TOKEN REQUEST ERROR try check credentials in spotify.json");
            return null;
        }
        token = JsonUtils.jsonGetValue(json, "access_token");
        log.info("token: " + token);
        log.info("bearerToken: " + token);
        yandex.bearer = token;
        JsonUtils.pojoToJsonFile(yandex, "yandex.json");
        return token;
    }

    public static void save() {
        JsonUtils.pojoToJsonFile(yandex, "yandex.json");
    }

    public static void read() {
        log.info("READ CREDENTIALS FROM yandex.json");
        Map<String, String> map = new HashMap<>();
        map = JsonUtils.jsonFileToMap("yandex.json", String.class, String.class);
        if (map == null) return;
        yandex.clientId = map.get("clientId");
        yandex.clientSecret = map.get("clientSecret");
        yandex.bearer = map.get("bearer");
        yandex.user = map.get("user");
        log.info(yandex);
//        log.info("TOKEN: " + yandex.bearer);
    }

    public static void getRoomsAndDevices() {
        log.info("GET ROOMS FROM YANDEX SMART HOME");
        String json;
        try {
            Response response = Request.Get("https://api.iot.yandex.net/v1.0/user/info")
                    .setHeader("Authorization", "OAuth " + yandex.bearer)
                    .execute();
            json = response.returnContent().asString();
        } catch (IOException e) {
            log.info("YANDEX GET INFO ERROR");
            return;
        }

        yandexInfo = JsonUtils.jsonToPojo(json, YandexInfo.class);
//        log.info("YANDEX:" + json);

        Main.rooms = yandexInfo.rooms.stream().map(r -> r.name).collect(Collectors.toList());
        log.info("FOUND ROOMS: " + Main.rooms);

        yandexInfo.devices.stream()
                .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                .filter(device -> device.name.equals("музыка"))
                .forEach(device -> SmartHome.create(getRoomNameByRoomId(device.room), Integer.valueOf(device.external_id)));
        SmartHome.write();
    }

    public static String getRoomNameByRoomId(String roomId) {
        return yandexInfo.rooms.stream()
                .filter(r -> r.id.equals(roomId))
                .findFirst().get().name;
    }


}