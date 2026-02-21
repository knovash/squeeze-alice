package org.knovash.squeezealice.yandex;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.knovash.squeezealice.Config;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.web.PageIndex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;

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
//                .forEach(device -> SmartHome.create(roomNameByRoomId(device.room), Integer.valueOf(device.external_id))); // getRoomsAndDevices
                .forEach(device -> smartHome.create(roomNameByRoomId(device.room), device.external_id)); // getRoomsAndDevices
        smartHome.write();

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

    public static void sendAllStates() {
        log.info("\nSEND ALL DEVICES STATES ON/OFF TO YANDEX");
        Main.lmsPlayers.players.stream()
                .filter(player -> player != null && player.deviceId != null)
                .forEach(player ->
                        Yandex.sendDeviceState(player.deviceId, "on_off", "on", String.valueOf(player.playing), null));
    }

    public static void sendDeviceState(String deviceId, String type, String instance, String capState, String status) {
//        Уведомление об изменении состояний устройств
//        https://yandex.ru/dev/dialogs/smart-home/doc/ru/reference-alerts/post-skill_id-callback-state
        log.info(String.format(
                "ID:%-15s" +
                        "PLAYER:%-15s " +
                        "INSTANCE:%-7s " +
                        "TYPE:%-7s " +
                        "STATE:%-6s ",
                deviceId,
                lmsPlayers.playerByDeviceId(deviceId).name,
                instance,
                type,
                capState
        ));

        CompletableFuture.runAsync(() -> {
            HttpResponse response = null;
            try {
                String url = "https://dialogs.yandex.net/api/v1/skills/" + config.skillId + "/callback/state";
                Map<String, Object> deviceMap = new HashMap<>();
                deviceMap.put("id", deviceId);

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
//                log.info("Request body: " + jsonBody);
                // Отправляем запрос и получаем ответ
                response = Request.Post(url)
//                        .setHeader("Authorization", "OAuth " + "y0__xDzxbXDARij9xMgof3dqBNVNLZJ5TUBmwMndUdpOq_rMn5GQw")
                        .setHeader("Authorization", "OAuth " + config.yandextSkillTokenDeveloper)
                        .setHeader("Content-Type", "application/json")
                        .bodyString(jsonBody, ContentType.APPLICATION_JSON)
                        .execute()
                        .returnResponse();
//Тип токена: OAuth-токен разработчика навыка.
//Как получить: Через консоль разработчика Яндекс.Диалогов: Навык → Настройки → Авторизация для HTTP-запросов → Скопировать OAuth-токен.
//Не требует программирования – токен статичен для навыка.
//Назначение: Управление состоянием навыка Алисы (отправка событий, состояние сессии).
//Срок жизни: Бессрочный (но можно перегенерировать вручную).

// ответ сервера
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (statusCode != 202) {
                    log.info("ERROR: Response status: " + statusCode + " Response body: " + responseBody);
                }
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