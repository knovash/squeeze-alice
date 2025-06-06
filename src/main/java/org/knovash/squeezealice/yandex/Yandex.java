package org.knovash.squeezealice.yandex;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Capability;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.provider.response.State;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.web.PageIndex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.Main.links;

@Log4j2
@Data
public class Yandex {

    public static Yandex yandex = new Yandex();
    public static YandexInfo yandexInfo = new YandexInfo();
    public static Map<String, String> scenariosIds = new HashMap<>();
    public static int devicesMusicCounter;
    public static int yandexMusicDevCounter;
    public static List<String> yandexMusicDevListRooms;


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
        log.info("YANDEX ROOMS ALL: " + Main.rooms + " DEVICES ALL: " + yandexInfo.devices.size());

        List<YandexInfo.Device> yandexMusicDevices = yandexInfo.devices.stream()
                .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                .filter(device -> device.name.equals("музыка"))
                .sorted(Comparator.comparing(device -> device.external_id))
                .peek(device -> log.info("DEVICE ID: " + device.external_id + " ROOM: " + roomNameByRoomId(device.room)))
                .peek(device -> links.addLinkRoom(device.id, device.external_id, device.room, Yandex.roomNameByRoomId(device.room), null))
                .collect(Collectors.toList());

        links.write();

        yandexMusicDevCounter = yandexMusicDevices.size();

//        создать локальные девайсы Музыка из полученных из YandexInfo
        log.info("CREATE LOCAL DEVICES");
        yandexMusicDevices.stream()
                .forEach(device -> SmartHome.create(roomNameByRoomId(device.room), Integer.valueOf(device.external_id)));
        SmartHome.write();

// для отображения на вебинтерфейсе
        yandexMusicDevListRooms = yandexInfo.devices.stream()
                .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                .filter(device -> device.name.equals("музыка"))
                .map(device -> getRoomNameByRoomId(device.room))
                .collect(Collectors.toList());
        PageIndex.msgDevices = "УДЯ подключено " + yandexMusicDevCounter + " устройств Музыка в комнатах "
                + yandexMusicDevListRooms;
    }


    public static String roomNameByRoomId(String id) {
        String roomName = null;
        YandexInfo.Room room = yandexInfo.rooms.stream().filter(r -> r.id.equals(id)).findFirst().orElseGet(null);
        if (room != null) roomName = room.name;
        return roomName;
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

    public static String deviceIdbyRoomName(String roomName) {
//        log.info("ROOM NAME: " + roomName);
        String roomId = yandexInfo.rooms.stream()
                .filter(r -> r.name.equals(roomName))
                .findFirst().get().id;
//        log.info("ROOM ID: " + roomId);
        String deviceId = yandexInfo.devices.stream()
                .filter(d -> d.name.equals("музыка"))
                .filter(d -> d.room.equals(roomId))
                .findFirst().get().external_id;
//        log.info("DEVICE ID: " + deviceId);
        return deviceId;
    }

    public static void sendAllStates(){
        Main.lmsPlayers.players.stream()
                .filter(player -> player != null)
                .filter(player -> player.deviceId != null)
                .forEach(player ->
                        Yandex.sendDeviceState(player.deviceId, "on_off", "on", String.valueOf(player.playing), null)); // startPeriodicUpdate
    }

    public static void sendDeviceState(String deviceId, String type, String instance, String capState, String status) {
//        Уведомление об изменении состояний устройств
//        https://yandex.ru/dev/dialogs/smart-home/doc/ru/reference-alerts/post-skill_id-callback-state
        log.info("ID: " + deviceId + " CAPABILITY: " + type + " STATE: " + capState);
        CompletableFuture.runAsync(() -> {
            HttpResponse response = null;
            try {
                String url = "https://dialogs.yandex.net/api/v1/skills/5e3196e7-dc7c-4de3-b42a-95f56f58a9fe/callback/state";
                Map<String, Object> deviceMap = new HashMap<>();
                deviceMap.put("id", deviceId);
                Map<String, Object> capabilityMap = new HashMap<>();
                capabilityMap.put("type", "devices.capabilities." + type);
                Map<String, Object> stateMap = new HashMap<>();
                stateMap.put("instance", instance);
                stateMap.put("value", capState);
                capabilityMap.put("state", stateMap);
                deviceMap.put("capabilities", Collections.singletonList(capabilityMap));
                Map<String, Object> payload = new HashMap<>();
                payload.put("user_id", config.yandexUid);
                payload.put("devices", Collections.singletonList(deviceMap));
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("ts", System.currentTimeMillis() / 1000.0);
                requestBody.put("payload", payload);
                String jsonBody = JsonUtils.pojoToJson(requestBody);
//                log.info("Request body: " + jsonBody);
                // Отправляем запрос и получаем ответ
                response = null;
                response = Request.Post(url)
                        .setHeader("Authorization", "OAuth " + "y0__xDzxbXDARij9xMgof3dqBNVNLZJ5TUBmwMndUdpOq_rMn5GQw")
                        .setHeader("Content-Type", "application/json")
                        .bodyString(jsonBody, ContentType.APPLICATION_JSON)
                        .execute().returnResponse();
// ответ сервера
//                int statusCode = response.getStatusLine().getStatusCode();
//                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
//                log.info("Response status: " + statusCode + " Response body: " + responseBody);
            } catch (IOException e) {
                log.error("Error updating device state: " + e.getMessage(), e);
            } finally {
                // Закрываем ресурсы ответа
                if (response != null && response.getEntity() != null) {
                    try {
                        EntityUtils.consume(response.getEntity());
                    } catch (IOException e) {
                        log.warn("Error closing response entity", e);
                    }
                }
            }
        });
    }
}