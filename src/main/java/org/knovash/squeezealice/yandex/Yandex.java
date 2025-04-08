package org.knovash.squeezealice.yandex;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.web.PageIndex;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
@Data
public class Yandex {

//    public String clientId;
//    public String clientSecret;
//    public String bearer;
//    public String user;
    public static Yandex yandex = new Yandex();
    public static YandexInfo yandexInfo = new YandexInfo();
    public static Map<String, String> scenariosIds = new HashMap<>();
    public static int devicesMusicCounter;
    public static int yandexMusicDevCounter;
    public static List<String> yandexMusicDevListRooms;


//    public static void saveClientId(HashMap<String, String> parameters) {
//        log.info("SAVE CLIENT ID " + parameters.get("client_id"));
//        yandex.clientId = parameters.get("client_id");
//        JsonUtils.pojoToJsonFile(yandex, "yandex.json");
//    }

//    public static void saveToken(HashMap<String, String> parameters) {
//        log.info("SAVE TOKEN " + parameters.get("token"));
//        yandex.bearer = parameters.get("token");
//        JsonUtils.pojoToJsonFile(yandex, "yandex.json");
//    }

//    public static String getBearerToken() {
//        if (yandex.clientId == null) JsonUtils.jsonFileToPojo("yandex.json", Yandex.class);
//        String client_id = yandex.clientId;
//        String client_secret = yandex.clientSecret;
//        client_id = "9aa97fffe29849bb945db5b82b3ee015";
//        client_secret = "37cf34e9fdbd48d389e293fc96d5e794";
//        log.info("clientId: " + client_id + " clientSecret: " + client_secret);
//        Response response;
//        String token;
//        String clientIdSecret = client_id + ":" + client_secret;
//        String base64 = Base64.getEncoder().encodeToString(clientIdSecret.getBytes());
//        log.info("base64: " + base64);
//        String json = null;
////      String uri = "https://oauth.yandex.ru/token?grant_type=refresh_token";
////        String uri = "https://oauth.yandex.ru/token?grant_type=authorization_code&code=scope";
//        String uri = "https://oauth.yandex.ru/token?grant_type=authorization_code&code=scope";
//        String urlParameters =
//                "client_id=" + client_id +
//                        "&" +
//                        "client_secret=" + client_secret +
//                        "&" +
//                        "grant_type=client_credentials";
//        try {
//            ContentType contentType = ContentType.parse("application/x-www-form-urlencoded");
//            response = Request.Post(uri)
//                    .bodyString(urlParameters, contentType)
////                    .setHeader("Authorization", "Basic " + base64)
//                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
//                    .execute();
//            json = response.returnContent().asString();
//            log.info("json: " + json);
//        } catch (IOException e) {
//            log.info("YANDEX BEARER TOKEN REQUEST ERROR try check credentials in spotify.json");
//            return null;
//        }
//        token = JsonUtils.jsonGetValue(json, "access_token");
//        log.info("token: " + token);
//        log.info("bearerToken: " + token);
//        yandex.bearer = token;
//        JsonUtils.pojoToJsonFile(yandex, "yandex.json");
//        return token;
//    }

//    public static void save() {
//        JsonUtils.pojoToJsonFile(yandex, "yandex.json");
//    }

//    public static void read() {
//        log.debug("READ CREDENTIALS FROM yandex.json");
//        Map<String, String> map = new HashMap<>();
//        map = JsonUtils.jsonFileToMap("yandex.json", String.class, String.class);
//        if (map == null) return;
//        yandex.clientId = map.get("clientId");
//        yandex.clientSecret = map.get("clientSecret");
//        yandex.bearer = map.get("bearer");
//        yandex.user = map.get("user");
//        log.info("READ CREDENTIALS FROM yandex.json OK");
//        log.debug(yandex);
////        log.info("TOKEN: " + yandex.bearer);
//    }

    public static void getRoomsAndDevices() {
        if (config.yandexToken == null || config.yandexToken.equals("")) {
            log.info("NO YANDEX TOKEN");
            return;
        }
        log.debug("GET ROOMS FROM YANDEX SMART HOME");
        String json;
        String bearer = config.yandexToken;
        try {
            Response response = Request.Get("https://api.iot.yandex.net/v1.0/user/info")
//                    .setHeader("Authorization", "OAuth " + yandex.bearer)
                    .setHeader("Authorization", "OAuth " + bearer)
                    .execute();
            json = response.returnContent().asString();
        } catch (IOException e) {
            log.info("YANDEX GET INFO ERROR");
            return;
        }
        yandexInfo = JsonUtils.jsonToPojo(json, YandexInfo.class);
//        log.info("YANDEX:" + json);
        Main.rooms = yandexInfo.rooms.stream().map(r -> r.name).collect(Collectors.toList());
        log.info("YANDEX ROOMS ALL: " + Main.rooms);
//        SmartHome.devices = new ArrayList<>();

        log.debug("YANDEX DEVICES ALL: " + yandexInfo.devices.size());

        yandexMusicDevCounter =
                (int) yandexInfo.devices.stream()
                        .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                        .filter(device -> device.name.equals("музыка")).count();

        log.info("YANDEX DEVICES MUSIC COUNT: " + yandexMusicDevCounter);
        if (SmartHome.devices != null) log.info("SA DEVICES COUNT: " + SmartHome.devices.size());
        else log.info("SA DEVICES COUNT: 0 -----------");

//        log.info("---------  LMS PLAYERS COUNT: " + lmsPlayers.players.size());
//        int count = lmsPlayers.players.size();
//        for (int i = 0; i < count; i++) {
//            log.info("CREATE RANDOM ROOM DEVICE");
//            SmartHome.create("комната",i);
//        }

        if (yandexMusicDevCounter == 0) {
            log.info("YANDEX MUSIC DEVICES COUNT: " + yandexMusicDevCounter);
            return;
        }

//        getRoomNameByRoomId(device.room)

        List<String> yandexMusicDevList = yandexInfo.devices.stream()
                .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                .filter(device -> device.name.equals("музыка"))
                .map(device -> "id=" + device.external_id + " " + getRoomNameByRoomId(device.room))
                .collect(Collectors.toList());

        yandexMusicDevListRooms = yandexInfo.devices.stream()
                .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                .filter(device -> device.name.equals("музыка"))
                .map(device -> getRoomNameByRoomId(device.room))
                .collect(Collectors.toList());

        PageIndex.msgUdy = "УДЯ подключено " + yandexMusicDevCounter + " устройств Музыка в комнатах "
                + yandexMusicDevListRooms;

        yandexInfo.devices.stream()
                .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                .filter(device -> device.name.equals("музыка"))
                .forEach(device -> SmartHome.create(getRoomNameByRoomId(device.room), Integer.valueOf(device.external_id)));
        log.info("SA DEVICES COUNT: " + SmartHome.devices.size());




        SmartHome.write();
    }

    public static String getScenarioIdByName(String scenarioName) {
        log.info("GET SCENARIO ID BY NAME: " + scenarioName);
        String scenarioId = null;
        scenarioId = scenariosIds.get(scenarioName);
        if (scenarioId != null) return scenarioId;
        log.info("GO SEARCH IN YANDEX INFO....");
        String json;
        try {
            Response response = Request.Get("https://api.iot.yandex.net/v1.0/user/info")
                    .setHeader("Authorization", "OAuth " + config.yandexToken)
                    .execute();
            json = response.returnContent().asString();
        } catch (IOException e) {
            log.info("YANDEX GET INFO ERROR");
            return "";
        }
        yandexInfo = JsonUtils.jsonToPojo(json, YandexInfo.class);
        List<String> scenarios = yandexInfo.scenarios.stream().map(r -> r.name).collect(Collectors.toList());
//        log.info("SCENARIOS: " + scenarios);
        YandexInfo.Scenario scenario = yandexInfo.scenarios.stream()
                .filter(s -> s.name.equals(scenarioName))
                .findFirst()
                .orElseGet(null);
        if (scenario != null) scenarioId = scenario.id;
        scenariosIds.put(scenarioName, scenarioId);
        log.info("ID " + scenarioId);
        log.info("IDS " + scenariosIds);
        log.info("GET ID " + scenariosIds.get(scenarioName));
        scenariosIds.get(scenarioName);
        return scenarioId;
    }

    public static void runScenario(String scenarioName) {
        log.info("RUN SCENARIO NAME: " + scenarioName);
        String scenarioId = getScenarioIdByName(scenarioName);
        log.info("RUN SCENARIO ID: " + scenarioId);
        if (scenarioId == null) return;
        CompletableFuture.runAsync(() -> {
            try {
                Request.Post("https://api.iot.yandex.net/v1.0/scenarios/" + scenarioId + "/actions")
                        .setHeader("Authorization", "OAuth " + config.yandexToken)
                        .execute();
            } catch (IOException e) {
                log.info("ERROR " + e);
            }
        });
    }

    public static String getRoomNameByRoomId(String roomId) {
        return yandexInfo.rooms.stream()
                .filter(r -> r.id.equals(roomId))
                .findFirst().get().name;
    }
}