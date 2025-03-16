package org.knovash.squeezealice.provider;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.web.PageIndex;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.web.PageIndex.msgSqa;

@Log4j2
@Data
public class Yandex {

    public String clientId = "";
    public String clientSecret = "";
    public String bearer = "";
    public String user = "";
    public static Yandex yandex = new Yandex();
    public static YandexInfo yandexInfo = new YandexInfo();
    public static Map<String, String> scenariosIds = new HashMap<>();
    public static int devicesMusicCounter;

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
        log.info("ROOMS ALL: " + Main.rooms);
//        SmartHome.devices = new ArrayList<>();

        log.info("DEVICES ALL SIZE: " + yandexInfo.devices.size());

        int yandexMusicDevCounter =
                (int) yandexInfo.devices.stream()
                        .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                        .filter(device -> device.name.equals("музыка")).count();

        log.info("DEVICES MUSIC SIZE: " + yandexMusicDevCounter);
        if (SmartHome.devices != null) log.info("SmartHome.devices.size(): " + SmartHome.devices.size());

        if (SmartHome.devices.size() == 0)
            msgSqa = "SQA нет плееров, добавте плееры в /players";
        else
            msgSqa = "SQA подключено " + SmartHome.devices.size() + " плееров " + SmartHome.devices.stream().map(d ->
                            " id=" + d.id + " " + d.room + "-" + lmsPlayers.getPlayerNameByDeviceId(d.id))
                    .collect(Collectors.toList());
        log.info(msgSqa);

        if (yandexMusicDevCounter == 0) {
            if (SmartHome.devices.size() == 0) PageIndex.msgUdy = "УДЯ нет плееров. Сначала добавьте плееры в SQA /playeers";
            else PageIndex.msgUdy = "УДЯ нет плееров. Обновите список устройств навыка в приложениии УДЯ";
            log.info(PageIndex.msgUdy);
            return;
        }

//        getRoomNameByRoomId(device.room)

        List<String> yandexMusicDevList = yandexInfo.devices.stream()
                .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                .filter(device -> device.name.equals("музыка"))
                .map(device -> "id=" + device.external_id + " " + getRoomNameByRoomId(device.room))
                .collect(Collectors.toList());
if (SmartHome.devices.size() > yandexMusicDevCounter)
        PageIndex.msgUdy = "УДЯ подключено " + yandexMusicDevCounter + " плееров " + yandexMusicDevList+ " Обновите устройства в УДЯ";
else
    PageIndex.msgUdy = "УДЯ подключено " + yandexMusicDevCounter + " плееров " + yandexMusicDevList;

        yandexInfo.devices.stream()
                .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                .filter(device -> device.name.equals("музыка"))
                .forEach(device -> SmartHome.create(getRoomNameByRoomId(device.room), Integer.valueOf(device.external_id)));
        log.info("SMARTHOME DEVICES: " + SmartHome.devices.size());
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
                    .setHeader("Authorization", "OAuth " + yandex.bearer)
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
                        .setHeader("Authorization", "OAuth " + yandex.bearer)
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

    public static void sayOn() {
        try {
            Request.Post("https://api.iot.yandex.net/v1.0/scenarios/d04255d2-607e-4805-981e-5ce676dc8482/actions")
                    .setHeader("Authorization", "OAuth " + yandex.bearer)
                    .execute();
        } catch (IOException e) {
            log.info("SAY ERROR");
        }
    }

    public static void sayOff() {
        try {
            Request.Post("https://api.iot.yandex.net/v1.0/scenarios/266db447-2238-4d84-beff-f635b0693953/actions")
                    .setHeader("Authorization", "OAuth " + yandex.bearer)
                    .execute();
        } catch (IOException e) {
            log.info("SAY ERROR");
        }
    }

    public static void sayBeep() {
        CompletableFuture.runAsync(() -> {
            try {
                Request.Post("https://api.iot.yandex.net/v1.0/scenarios/f2ddb649-62e7-4fe2-be01-23d477dd2974/actions")
                        .setHeader("Authorization", "OAuth " + yandex.bearer)
                        .execute();
            } catch (IOException e) {
                log.info("SAY ERROR");
            }
        });
    }

    public static void sayServerStart() {
        try {
            Request.Post("https://api.iot.yandex.net/v1.0/scenarios/11ad405a-1b80-4031-ae7c-9bada79e276d/actions")
                    .setHeader("Authorization", "OAuth " + yandex.bearer)
                    .execute();
        } catch (IOException e) {
            log.info("SAY ERROR");
        }
    }

    public static void sayWait() {
//        log.info("SAY WAIT");
//        try {
//            Request.Post("https://api.iot.yandex.net/v1.0/scenarios/0e2c7cf4-3db9-416e-b17a-f6e2d501f257/actions")
//                    .setHeader("Authorization", "OAuth " + yandex.bearer)
//                    .execute();
//        } catch (IOException e) {
//            log.info("SAY ERROR");
//        }
    }
}