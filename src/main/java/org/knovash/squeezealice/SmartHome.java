package org.knovash.squeezealice;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.response.*;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.yandex.YandexUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.Main.smartHome;

@Log4j2
@Data
public class SmartHome {

    public static List<Device> devices = new ArrayList<>();
    public static String saveToFileJson = "data/devices.json";

    public Device deviceByExternalId(String deviceId) { // приходит от яндекса EXT ID
//        log.info(">>> DEVICE ID " + deviceId);
        return devices.stream()
                .filter(d -> d.id != null && d.external_id.equals(deviceId))
                .findFirst()
                .orElse(null);
    }

    public static Device deviceByRoom(String room) {
        if (room == null) return null;
        return devices.stream()
                .filter(d -> room.equalsIgnoreCase(d.room))
                .findFirst()
                .orElse(null);
    }

    /**
     * Создаёт или обновляет устройство.
     *
     * @param deviceRoomName название комнаты (для голосовой команды)
     * @param yandexDevice   данные устройства из Яндекса (при синхронизации)
     */
    public void create(String deviceRoomName, YandexUtils.MusicDevice yandexDevice) {
        if (devices == null) devices = new ArrayList<>();
//        log.info(">>> ROOM NAME: " + deviceRoomName);
//        log.info(">>> DEVICE YANDEX: " + yandexDevice.roomName + " " + yandexDevice.id);

        // Случай 1: синхронизация с Яндексом
        if (yandexDevice != null) {
            String deviceId = yandexDevice.id;
            String deviceExternalId = yandexDevice.externalId;
            String roomId = yandexDevice.roomId;
            String room = yandexDevice.roomName;
            String name = yandexDevice.name;

            // 1. проверить что в локальных девайсах devices нет девайса полученого от яндекса. если есть то создавать не надо а только обновить
            if (devices.stream().noneMatch(d ->
                    (deviceId != null && deviceId.equals(d.id))
                            || (deviceExternalId != null && deviceExternalId.equals(d.external_id))
                            || (room != null && room.equals(d.room))
            )) {
//          нет в локальных. надо создать новый девайс из полученого от яндекса
                Device device = createNewDevice(room, deviceId, deviceExternalId, name);
                devices.add(device);
                log.info("CREATED NEW DEVICE FROM YANDEX DEVICE. ROOM: {} ID: {} EXT_ID: {}", device.room, device.id, device.external_id);
            } else {
                log.info("DEVICE EXISTS. SKIP CREATE FROM YANDEX DEVICE");
            }
        } else {
            // создание 1. глосом 2. веб. используя имя комнаты. если девай еще небыл создан и его нет в локальных девайсах по имени комнаты тогда создавать ненадо
            if (devices.stream().noneMatch(d -> (deviceRoomName != null && deviceRoomName.equalsIgnoreCase(d.room)))) {
                log.info("DEVICE NOT EXISTS. CREATE NEW DEVICE FROM PLAYER ROOM NAME: " + deviceRoomName);
                //    Объект devices
//    id        String              Идентификатор устройства. Должен быть уникален среди всех устройств производителя.
//    name      String              Название устройства.
//    room      String              Название помещения, в котором расположено устройство.
//    external_id
                String id = String.valueOf(UUID.randomUUID());
                String extid = String.valueOf(UUID.randomUUID());
                Device device = createNewDevice(deviceRoomName, id, extid, "музыка");
                devices.add(device);
                log.info("CREATED NEW DEVICE BY VOICE OR WEB. ROOM: {} ID: {} EXT_ID: {}", device.room, device.id, device.external_id);
            } else {
                log.info("DEVICE EXISTS. SKIP CREATE BY VOICE OR WEB");
            }
        }
//        smartHome.write();
    }

    private Device createNewDevice(String roomName, String deviceId, String deviceExternalId, String deviceName) {
        Device device = new Device();
        device.room = roomName;
        device.id = deviceId;
        device.external_id = deviceExternalId;
        device.name = deviceName;
        // Capability volume
        Capability volume = new Capability();
        volume.type = "devices.capabilities.range";
        volume.retrievable = true;
        volume.reportable = true;
        volume.parameters.instance = "volume";
        volume.parameters.random_access = true;
        volume.parameters.range = new Range();
        volume.parameters.range.min = 1;
        volume.parameters.range.max = 100;
        volume.parameters.range.precision = 1;
        volume.state = new State();
        volume.state.instance = "volume";
        volume.state.value = "0";
        volume.state.action_result = new ActionResult();
        volume.state.action_result.status = "DONE";
        device.capabilities.add(volume);
        // Capability channel
        Capability channel = new Capability();
        channel.type = "devices.capabilities.range";
        channel.retrievable = true;
        channel.reportable = true;
        channel.parameters.instance = "channel";
        channel.parameters.random_access = true;
        channel.parameters.range = new Range();
        channel.parameters.range.min = 1;
        channel.parameters.range.max = 200;
        channel.parameters.range.precision = 1;
        channel.state = new State();
        channel.state.instance = "channel";
        channel.state.value = null;
        channel.state.relative = false;
        channel.state.action_result = new ActionResult();
        channel.state.action_result.status = "DONE";
        device.capabilities.add(channel);
        // Capability on_off
        Capability onOff = new Capability();
        onOff.type = "devices.capabilities.on_off";
        onOff.retrievable = true;
        onOff.reportable = true;
        onOff.parameters.instance = "on";
        // onOff.state = null; // необязательно
        device.capabilities.add(onOff);
        return device;
    }

    public void write() {
//        log.info("WRITE: {}", config.fileDevices);
//        JsonUtils.pojoToJsonFile(devices, config.fileDevices);
    }

    public void read() {
//        devices = JsonUtils.jsonFileToList(config.fileDevices, Device.class);
//        if (devices == null) devices = new ArrayList<>();
//        log.info("DEVICES FROM devices.json: {}", devices.size());
    }
}