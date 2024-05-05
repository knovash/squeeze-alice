package org.knovash.squeezealice.provider;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;

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
//            throw new RuntimeException(e);
        }
        token = JsonUtils.jsonGetValue(json, "access_token");
        log.info("token: " + token);
        String bearer = "Bearer " + token.replace("\"", "");
        log.info("bearerToken: " + token);
        yandex.bearer = token;
        JsonUtils.pojoToJsonFile(yandex, "yandex.json");
        return token;
    }

    public static void save() {
        JsonUtils.pojoToJsonFile(yandex, "yandex.json");
    }

    public static void read() {
        log.info("");
        log.info("READ CREDENTIALS FROM yandex.json");
        Map<String, String> map = new HashMap<>();
        map = JsonUtils.jsonFileToMap("yandex.json", String.class, String.class);
        if (map == null) return;
        yandex.clientId = map.get("clientId");
        yandex.clientSecret = map.get("clientSecret");
        yandex.bearer = map.get("bearer");
        log.info("BEARER: " + yandex.bearer);
    }

    public static void getInfo() {
        log.info("YANDEX GET INFO START >>>>>>>>>>");
        Response response;
        String json = null;
        String oauthToken = "OAuth " + yandex.bearer;
        try {
            response = Request.Get("https://api.iot.yandex.net/v1.0/user/info")
                    .setHeader("Authorization", oauthToken)
                    .execute();
            json = response.returnContent().asString();
        } catch (IOException e) {
            log.info("YANDEX GET INFO ERROR");
            return;
//            throw new RuntimeException(e);
        }
        YandexInfo yandexInfo = JsonUtils.jsonToPojo(json, YandexInfo.class);
        Map<String, String> roomIdRoomName = yandexInfo.rooms.stream()
                .collect(Collectors.toMap(room -> room.id, room -> room.name));
        Map<String, String> roomIdRoomExtId = yandexInfo.devices.stream()
                .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                .filter(device -> device.name.equals("музыка"))
                .collect(Collectors.toMap(device -> device.room, device -> device.external_id));
        log.info("ROOM ID - ROOM EXTID - ROOM NAME");
        Map<String, String> roomNameRoomExtId = roomIdRoomExtId.entrySet().stream()
                .peek(entry -> log.info(entry.getKey() + " " + entry.getValue() + " " + roomIdRoomName.get(entry.getKey())))
                .collect(Collectors.toMap(entry -> roomIdRoomName.get(entry.getKey()), entry -> entry.getValue()));

        if (roomIdRoomExtId.size() > 0) {
            log.info("CREATE DEVICES FROM YANDEX");
            roomNameRoomExtId.entrySet().stream()
                    .forEach(entry -> {
                        HashMap<String, String> parameters = new HashMap<>();
                        parameters.put("room", entry.getKey());
                        parameters.put("id", entry.getValue());
                        Device device = SmartHome.create(parameters);

                    });
        } else {
            log.info("NO DEVICES IN YANDEX. CREATE NEW DEVICES. THEN YOU NEEED TO ADD THEM IN ALICE APP !!!");
            lmsPlayers.players.stream()
                    .filter(player -> player.roomPlayer != null)
                    .forEach(player -> {
                        HashMap<String, String> parameters = new HashMap<>();
//                        parameters.put("speaker_name_lms", player.name);
                        parameters.put("room", player.roomPlayer);
                        SmartHome.create(parameters);
                        return;
                    });

//            Map<String, String> roomIdRoomName = yandexInfo.rooms.stream()
//                    .collect(Collectors.toMap(room -> room.id, room -> room.name));



            log.info("YANDEX GET INFO FINISH <<<<<<<<<<");
        }
    }
}