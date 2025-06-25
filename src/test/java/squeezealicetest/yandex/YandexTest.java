package squeezealicetest.yandex;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.web.PageIndex;
import squeezealicetest.utils.MainTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static squeezealicetest.utils.MainTest.configTest;
import static squeezealicetest.utils.MainTest.smartHomeTest;

@Log4j2
@Data
public class YandexTest {

    public static YandexTest yandexTest = new YandexTest();
    public static YandexInfoTest yandexInfo = new YandexInfoTest();
    public static Map<String, String> scenariosIds = new HashMap<>();
    public static int devicesMusicCounter;
    public static int yandexMusicDevCounter;
    public static List<String> yandexMusicDevListRooms;

    public static void getRoomsAndDevices() {
        if (configTest.yandexToken == null || configTest.yandexToken.equals("")) {
            log.info("NO YANDEX TOKEN");
            return;
        }
        log.debug("GET ROOMS FROM YANDEX SMART HOME");
        String json;
        String bearer = configTest.yandexToken;
        log.info(configTest);
        try {
            Response response = Request.Get("https://api.iot.yandex.net/v1.0/user/info")
                    .setHeader("Authorization", "OAuth " + bearer)
                    .execute();
            json = response.returnContent().asString();
        } catch (IOException e) {
            log.info("YANDEX GET INFO ERROR");
            return;
        }
        yandexInfo = JsonUtils.jsonToPojo(json, YandexInfoTest.class);
//        log.info("YANDEX:" + json);
        MainTest.roomsTest = yandexInfo.rooms.stream().map(r -> r.name).collect(Collectors.toList());
        log.info("YANDEX ROOMS ALL: " + MainTest.roomsTest);
//        SmartHome.devices = new ArrayList<>();

        log.info("YANDEX DEVICES ALL: " + yandexInfo.devices.size());

        List<YandexInfoTest.Device> yandexMusicDevices = yandexInfo.devices.stream()
                .filter(device -> device.type.equals("devices.types.media_device.receiver"))
                .filter(device -> device.name.equals("музыка"))
                .peek(device -> log.info("DEVICE ID: " + device.external_id + " ROOM: " + roomNameByRoomId(device.room)))
                .collect(Collectors.toList());
        yandexMusicDevCounter = yandexMusicDevices.size();
//        создать локальные девайсы Музыка из полученных из YandexInfo
        log.info("CREATE LOCAL DEVICES");
        yandexMusicDevices.stream()
                .forEach(device -> smartHomeTest.create(roomNameByRoomId(device.room), device.external_id));
        smartHomeTest.write();
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
        YandexInfoTest.Room room = yandexInfo.rooms.stream().filter(r -> r.id.equals(id)).findFirst().orElseGet(null);
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
}