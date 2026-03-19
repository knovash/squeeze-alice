package org.knovash.squeezealice.yandex;

import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
public class YandexUtils {

    public static List<MusicDevice> yandexDevices;

    public static class MusicDevice {

        public final String roomId;
        public final String roomName;
        public final String id;
        public final String externalId;
        public final String name;

        public MusicDevice(String roomId, String roomName, String id, String externalId, String name) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.id = id;
            this.externalId = externalId;
            this.name = name;
        }

        @Override
        public String toString() {
            return "MusicDevice{" +
                    "roomId='" + roomId + '\'' +
                    ", roomName='" + roomName + '\'' +
                    ", id='" + id + '\'' +
                    ", externalId='" + externalId + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    //    из информации полученной запросом https://api.iot.yandex.net/v1.0/user/info
//    создать лист устройств с названием комнаты, потому что у яндекса оно связано с девайсом по id
    public static List<MusicDevice> extractMusicDevices(YandexInfo yandexInfo) {
        if (yandexInfo == null || yandexInfo.rooms == null || yandexInfo.devices == null) return List.of();

        Map<String, String> roomNameById = yandexInfo.rooms.stream() // комната и id комнаты
                .collect(Collectors.toMap(
                        room -> room.id,
                        room -> room.name,
                        (existing, replacement) -> existing
                ));

        Yandex.idsAndRooms = roomNameById;

        List<MusicDevice> devices = yandexInfo.devices.stream()
                .filter(device -> device.type != null && (
                        device.type.contains("devices.types.media_device.receiver") &&
                                device.name.contains("музыка")
                ))
                .map(device -> new MusicDevice(
                        device.room,                   // ID комнаты  "room": "787eaa19-0e6b-40bc-9949-41f68fcf0286"
                        roomNameById.get(device.room), // название комнаты "rooms": ["name": "Спальня"]
                        device.id,                     // "id": "7e4a5516-c751-4932-812c-6acc238ffa9d"
                        device.external_id,            // "external_id": "ca99e4d5-c775-4d9a-b4fe-25f2cf05fdac"
                        device.name                    // "name": "музыка"
                ))
                .peek(ff -> log.info("DEVICE ROOM: " + ff.roomName))
                .collect(Collectors.toList());

        yandexDevices = devices;
        return devices;
    }

    public static boolean checkContainsByRoom(String room) {
        return yandexDevices != null && room != null &&
                yandexDevices.stream().anyMatch(device -> Objects.equals(device.roomName, room));
    }

}