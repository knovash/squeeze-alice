package org.knovash.squeezealice.yandex;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.http.HttpClientWrapper;
import org.knovash.squeezealice.http.HttpResponseResult;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.web.Settings.KeyToggle.*;

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
    public static List<YandexUtils.DeviceFromYandex> otherDevices;

    public static List<String> rooms = new ArrayList<>();
    public static Map<String, String> idsAndRooms = new HashMap<>();

    public static YandexInfo getYandexInfo() {
        if (config.yandexToken == null || config.yandexToken.equals("")) {
            log.info("CANCELED TO GET DEVICES FROM YANDEX. USER NOT LOGGED IN YANDEX");
            return null;
        }
        String json;
        String bearer = config.yandexToken;
        try {
            log.info("GET YANDEX INFO https://api.iot.yandex.net/v1.0/user/info");
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
        return yandexInfo;
    }

    public static String getScenarioIdByName(String scenarioName) {
        if (scenarioName == null) return null;
        YandexInfo info = getYandexInfo();
        if (info == null || info.scenarios == null) return null;
        for (YandexInfo.Scenario s : info.scenarios)
            if (s.name.equals(scenarioName)) return s.id;
        return null;
    }

    public static boolean runScenarioByName(String scenarioName) {
        return runScenarioById(getScenarioIdByName(scenarioName));
    }

    public static boolean runScenarioById(String scenarioId) {
        if (scenarioId == null) return false;
        String iotToken = config.yandexToken;
        String url = "https://api.iot.yandex.net/v1.0/scenarios/" + scenarioId + "/actions";
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + iotToken);
            headers.put("Content-Type", "application/json");
            HttpResponseResult result = httpClient.doPost(url, "{}", headers);
            if (!result.isSuccess()) {
                log.error("Scenario action failed: " + result.getStatusCode());
                return false;
            } else {
                log.info("Scenario action success");
                return true;
            }
        } catch (Exception e) {
            log.error("Scenario action error", e);
            return false;
        }
    }

    public static List<YandexUtils.DeviceFromYandex> devicesGetFromYandexInfo() {
        log.info(start);
        if (config.yandexToken == null || config.yandexToken.equals("")) {
            log.info("CANCELED TO GET DEVICES FROM YANDEX. USER NOT LOGGED IN YANDEX");
            return null;
        }
        yandexInfo = getYandexInfo();
        rooms = yandexInfo.rooms.stream().map(r -> r.name).collect(Collectors.toList());
        log.info("YANDEX ROOMS: " + rooms);
        List<YandexUtils.DeviceFromYandex> musicDevices = YandexUtils.extractMusicDevices(yandexInfo);
        // Получить другие устройства
        otherDevices = YandexUtils.extractOtherDevices(yandexInfo);
        //log.info("OTHER: " + otherDevices);
        devicesSize = musicDevices.size();
        roomsWithDevice = yandexInfo.devices.stream()
                .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                .filter(device -> device.name.equals(music))
                .map(device -> roomNameByRoomId(device.room))
                .collect(Collectors.toList());
        //PageIndex.msgDevices = "УДЯ подключено " + devicesSize + " устройств Музыка в комнатах " + roomsWithDevice;
        log.info(finish);
        return musicDevices;
    }

    public static void createMusicDevicesFromYandex(List<YandexUtils.DeviceFromYandex> yandexMusicDevices) {
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
        lmsPlayers.write();
        //smartHome.write();
        log.info(finish);
    }

    public static void createOtherDevicesFromYandexDevices(List<YandexUtils.DeviceFromYandex> devicesFromYandex) {
        log.info(start);
        if (config.yandexToken == null || config.yandexToken.equals("")) {
            log.info("CANCELED TO CREATE DEVICES FROM YANDEX. USER NOT LOGGED IN YANDEX");
            return;
        }
        if (devicesFromYandex == null || devicesFromYandex.isEmpty()) {
            log.info("FAILED TO CREATE DEVICES. NO DEVICES FROM YANDEX");
            return;
        }
        log.info("CREATE OTHER DEVICES FROM YANDEX");
        devicesFromYandex.forEach(device -> smartHome.createDeviceToggle(device));
        lmsPlayers.write();
        //smartHome.write();
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

    public static void sendDevicesStatesAsync() {
        if (!TOGGLE_YANDEX_REFRESH.value) {
            log.info("TOGGLE YANDEX REFRESH STATES DISABLED !!!");
            return;
        }
        log.info("\nSEND ALL DEVICES STATES ON/OFF TO YANDEX");
        CompletableFuture.runAsync(() -> {
            lmsPlayers.updatePlayers();
            Main.lmsPlayers.players.stream()
                    .filter(player -> player != null && player.room != null)
                    .forEach(player ->
                            {
                                Yandex.sendDeviceState(SmartHome.deviceByRoom(player.room), "on_off", "on", String.valueOf(player.playing));
                                //Yandex.sendDeviceState(SmartHome.deviceByRoom(player.room).id, "range", "volume", String.valueOf(player.volume));
                            }
                    );
        });
    }

    public static void sendDeviceState(Device device, String type, String instance, String capState) {

        log.info("DEVICE ROOM: " + device.room + " NAME " + device.name + " ID: " + device.id);

        try {
            String url = "https://dialogs.yandex.net/api/v1/skills/" + config.skillId + "/callback/state";
            // Формируем устройство
            Map<String, Object> deviceMap = new HashMap<>();
            deviceMap.put("id", device.id);

            // Формируем capability
            Map<String, Object> capabilityMap = new HashMap<>();
            capabilityMap.put("type", "devices.capabilities." + type);

            // Формируем состояние
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


            // Собираем payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("user_id", config.yandexUid);
            payload.put("devices", Collections.singletonList(deviceMap));

            // Собираем тело запроса
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ts", System.currentTimeMillis() / 1000.0);
            requestBody.put("payload", payload);
            String jsonBody = JsonUtils.pojoToJson(requestBody);
            log.info("POST " + url);


            // Заголовки
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "OAuth " + config.yandextSkillTokenDeveloper);
            headers.put("Content-Type", "application/json");


            // Выполняем запрос (синхронно)
            HttpResponseResult result = httpClient.doPost(url, jsonBody, headers);
            if (!result.isSuccess()) {
                log.info("ERROR: Response status: " + result.getStatusCode() + " Response body: " + result.getBody());
            }
        } catch (Exception e) {
            log.error("Error updating device state: " + e.getMessage(), e);
        }

    }

    public static boolean sendDeviceOtherState(Device device, String type, String instance, String capState) {

        log.info("OTHER DEVICE ROOM: " + device.room + " NAME " + device.name + " ID: " + device.id);

        Utils.sleep(200);
        try {
            String url = "https://dialogs.yandex.net/api/v1/skills/" + config.skillId + "/callback/state";

            // Формируем устройство
            Map<String, Object> deviceMap = new HashMap<>();
            deviceMap.put("id", device.id);

            // Формируем capability
            Map<String, Object> capabilityMap = new HashMap<>();
            capabilityMap.put("type", "devices.capabilities." + type);

            // Формируем состояние
            Map<String, Object> stateMap = new HashMap<>();
            stateMap.put("instance", instance);

            Object value;
            if ("volume".equals(instance)) {
                value = Integer.parseInt(capState);
            } else if ("on".equals(instance)) {
                // Для on_off instance всегда "on", значение boolean
                value = "on".equalsIgnoreCase(capState) || "true".equalsIgnoreCase(capState);
            } else {
                // Для остальных (температура, влажность и т.п.) – число или строка
                try {
                    value = Double.parseDouble(capState);
                } catch (NumberFormatException e) {
                    value = capState;
                }
            }
            stateMap.put("value", value);
            capabilityMap.put("state", stateMap);

            deviceMap.put("capabilities", Collections.singletonList(capabilityMap));

            // Собираем payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("user_id", config.yandexUid);
            payload.put("devices", Collections.singletonList(deviceMap));

            // Собираем тело запроса
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ts", System.currentTimeMillis() / 1000.0);
            requestBody.put("payload", payload);

            String jsonBody = JsonUtils.pojoToJson(requestBody);
            log.info("POST " + url);
            log.info("BODY " + jsonBody);

            // Заголовки
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "OAuth " + config.yandextSkillTokenDeveloper);
            headers.put("Content-Type", "application/json");

            // Выполняем запрос (синхронно)
            HttpResponseResult result = httpClient.doPost(url, jsonBody, headers);
            if (result.isSuccess()) {
                log.info("State update successful: {}", result.getBody());
                return true;
            } else {
                log.error("Failed to update state. Status: {}, Body: {}", result.getStatusCode(), result.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("Error updating device state: " + e.getMessage(), e);
            return false;
        }
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

    public static List<String> getRoomsWithAliceOrMusic() {
        if (yandexInfo == null || yandexInfo.devices == null || yandexInfo.rooms == null) {
            return Collections.emptyList();
        }
        Map<String, YandexInfo.Device> deviceMap = yandexInfo.devices.stream()
                .collect(Collectors.toMap(d -> d.id, d -> d));
        List<String> result = new ArrayList<>();
        for (YandexInfo.Room room : yandexInfo.rooms) {
            boolean hasTargetDevice = false;
            for (String deviceId : room.devices) {
                YandexInfo.Device device = deviceMap.get(deviceId);
                if (device == null) continue;
                String type = device.type;
                String name = device.name;
                boolean isAliceSpeaker = type != null && type.startsWith("devices.types.smart_speaker.yandex.station");
                boolean isMusicDevice = (type != null && type.contains("media_device"))
                        || (name != null && name.equalsIgnoreCase("музыка"));
                if (isAliceSpeaker || isMusicDevice) {
                    hasTargetDevice = true;
                    break;
                }
            }
            if (hasTargetDevice) {
                result.add(room.name);
            }
        }
        return result;
    }
}