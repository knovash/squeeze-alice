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
        log.info("YANDEX ROOMS ALL: " + Main.rooms);
//        SmartHome.devices = new ArrayList<>();

        log.info("YANDEX DEVICES ALL: " + yandexInfo.devices.size());

//        yandexMusicDevCounter =
//                (int) yandexInfo.devices.stream()
//                        .filter(device -> device.type.equals("devices.types.media_device.receiver"))
//                        .filter(device -> device.name.equals("музыка"))
//                        .peek(device -> log.info("DEDICE ID: " + device.external_id + " ROOM: " + roomNameByRoomId(device.room)))
//                        .count();

        List<YandexInfo.Device> yandexMusicDevices = yandexInfo.devices.stream()
                .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                .filter(device -> device.name.equals("музыка"))
                .peek(device -> log.info("DEVICE ID: " + device.external_id + " ROOM: " + roomNameByRoomId(device.room)))
                .collect(Collectors.toList());

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
}