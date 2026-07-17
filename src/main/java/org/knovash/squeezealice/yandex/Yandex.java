package org.knovash.squeezealice.yandex;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.http.HttpClientWrapper;
import org.knovash.squeezealice.http.HttpResponseResult;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.web.PageIndex;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;

@Log4j2
@Data
public class Yandex {

    private static final HttpClientWrapper httpClient = new HttpClientWrapper();

    public static Yandex yandex = new Yandex();
    public static YandexInfo yandexInfo = new YandexInfo();
    public static Map<String, String> scenariosIds = new HashMap<>();
    public static int devicesMusicCounter;
    public static int devicesSize;
    public static List<String> roomsWithDevice;

    public static List<String> rooms = new ArrayList<>();
    public static Map<String, String> idsAndRooms = new HashMap<>();

    public static List<YandexUtils.MusicDevice> devicesGetFromYandexInfo() {
        log.info(start);
        if (config.yandexToken == null || config.yandexToken.equals("")) {
            log.info("CANCELED TO GET DEVICES FROM YANDEX. USER NOT LOGGED IN YANDEX");
            return null;
        }
        log.debug("GET ROOMS FROM YANDEX");
        String json;
        String bearer = config.yandexToken;
        try {
            log.info("https://api.iot.yandex.net/v1.0/user/info");
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "OAuth " + bearer);
            HttpResponseResult result = httpClient.doGet("https://api.iot.yandex.net/v1.0/user/info", headers);
            if (!result.isSuccess()) {
                log.error("Yandex API error: " + result.getStatusCode());
                return null;
            }
            json = result.getBody();
        } catch (Exception e) {
            log.info("YANDEX GET INFO ERROR", e);
            return null;
        }
        yandexInfo = JsonUtils.jsonToPojo(json, YandexInfo.class);
        rooms = yandexInfo.rooms.stream().map(r -> r.name).collect(Collectors.toList());
        log.info("YANDEX ROOMS: " + rooms);

        List<YandexUtils.MusicDevice> musicDevices = YandexUtils.extractMusicDevices(yandexInfo);

        devicesSize = musicDevices.size();
        roomsWithDevice = yandexInfo.devices.stream()
                .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                .filter(device -> device.name.equals(music))
                .map(device -> roomNameByRoomId(device.room))
                .collect(Collectors.toList());
        PageIndex.msgDevices = "УДЯ подключено " + devicesSize + " устройств Музыка в комнатах " + roomsWithDevice;
        log.info(finish);
        return musicDevices;
    }

    public static void createDevicesFromYandexDevices(List<YandexUtils.MusicDevice> yandexMusicDevices) {
        log.info(start);
        if (config.yandexToken == null || config.yandexToken.equals("")) {
            log.info("CANCELED TO CREATE DEVICES FROM YANDEX. USER NOT LOGGED IN YANDEX");
            return;
        }
        if (yandexMusicDevices == null || yandexMusicDevices.isEmpty()) {
            log.info("FAILED TO CREATE DEVICES. NO DEVICES FROM YANDEX");
            return;
        }
        log.info("CREATE DEVICES FROM YANDEX");
        yandexMusicDevices.forEach(device -> smartHome.create("", device));
        log.info(finish);
    }

    public static String roomNameByRoomId(String id) {
        return Yandex.idsAndRooms.get(id);
    }

    public static String deviceIdbyRoomName(String roomName) {
        String roomId = yandexInfo.rooms.stream()
                .filter(r -> r.name.equals(roomName))
                .findFirst().get().id;
        String deviceId = yandexInfo.devices.stream()
                .filter(d -> d.name.equals("музыка"))
                .filter(d -> d.room.equals(roomId))
                .map(d -> d.external_id)
                .findFirst()
                .orElse(null);
        return deviceId;
    }

    public static void sendAllStates() {
        log.info("\nSEND ALL DEVICES STATES ON/OFF TO YANDEX");
        Main.lmsPlayers.players.stream()
                .filter(player -> player != null && player.room != null)
                .forEach(player ->
                        Yandex.sendDeviceState(player.room, "on_off", "on", String.valueOf(player.playing), null));
    }

    public static void sendDeviceState(String room, String type, String instance, String capState, String status) {
        log.debug(String.format(
                "ID:%-15s PLAYER:%-15s INSTANCE:%-7s TYPE:%-7s STATE: ",
                lmsPlayers.playerByRoom(room).name,
                instance,
                type,
                capState
        ));

        CompletableFuture.runAsync(() -> {
            try {
                String url = "https://dialogs.yandex.net/api/v1/skills/" + config.skillId + "/callback/state";
                Map<String, Object> deviceMap = new HashMap<>();
                deviceMap.put("id", room);

                Map<String, Object> capabilityMap = new HashMap<>();
                capabilityMap.put("type", "devices.capabilities." + type);

                Map<String, Object> stateMap = new HashMap<>();
                stateMap.put("instance", instance);

                if (instance.equals("volume")) {
                    int valueInt = Integer.parseInt(capState);
                    stateMap.put("value", valueInt);
                } else {
                    stateMap.put("value", capState);
                }
                capabilityMap.put("state", stateMap);
                deviceMap.put("capabilities", Collections.singletonList(capabilityMap));
                Map<String, Object> payload = new HashMap<>();
                payload.put("user_id", config.yandexUid);
                payload.put("devices", Collections.singletonList(deviceMap));
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("ts", System.currentTimeMillis() / 1000.0);
                requestBody.put("payload", payload);
                String jsonBody = JsonUtils.pojoToJson(requestBody);
                log.info("POST " + url);
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "OAuth " + config.yandextSkillTokenDeveloper);
                headers.put("Content-Type", "application/json");
                HttpResponseResult result = httpClient.doPost(url, jsonBody, headers);
                if (!result.isSuccess()) {
                    log.info("ERROR: Response status: " + result.getStatusCode() + " Response body: " + result.getBody());
                }
            } catch (Exception e) {
                log.error("Error updating device state: " + e.getMessage(), e);
            }
        });
    }

    public static void sayMyText(String text) {
        if (true) return; // TODO заглушка
        log.info("RUN SCENARIO");
        Main.sayText = text;
        String scenarioId = config.scenarioId;
        String iotToken = config.yandexToken;
        String url = "https://api.iot.yandex.net/v1.0/scenarios/" + scenarioId + "/actions";
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + iotToken);
            headers.put("Content-Type", "application/json");
            HttpResponseResult result = httpClient.doPost(url, "{}", headers);
            if (!result.isSuccess()) {
                log.error("Scenario action failed: " + result.getStatusCode());
            } else {
                log.info("Scenario action success");
            }
        } catch (Exception e) {
            log.error("Scenario action error", e);
        }
    }
}