package org.knovash.squeezealice;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.response.*;
import org.knovash.squeezealice.yandex.YandexUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.knovash.squeezealice.Main.*;

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

    public Device deviceById(String deviceId) { // приходит от яндекса EXT ID
//        log.info(">>> DEVICE ID " + deviceId);
        return devices.stream()
                .filter(d -> d.id != null && d.id.equals(deviceId))
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

    public void create(String deviceByRoomName, YandexUtils.MusicDevice deviceFromYandex) {
        if (devices == null) devices = new ArrayList<>();

        if (deviceFromYandex != null)

        // Случай 1: синхронизация с Яндексом
        {
            Device deviceExists = SmartHome.devices.stream()
                    .filter(device -> device.id.equals(deviceFromYandex.id))
                    .findFirst().orElse(null);
            if (deviceExists != null) return;
            log.info("CREATE DEVICE FROM YANDEX " + deviceFromYandex.roomName + " id: " + deviceFromYandex.id);
            createNewDeviceMusic(deviceFromYandex.roomName, deviceFromYandex.externalId, deviceFromYandex.name);
//    ВАЖНО !!! external_id от Яндекс сохранять в id устройства Музыка !!!


        } else
//
        // создание 1. глосом 2. веб. используя имя комнаты. если девай еще небыл создан и его нет в локальных девайсах по имени комнаты тогда создавать ненадо
        {
            if (devices.stream().noneMatch(d -> (deviceByRoomName != null && deviceByRoomName.equalsIgnoreCase(d.room)))) {
                log.info("DEVICE NOT EXISTS. CREATE NEW DEVICE FROM PLAYER ROOM NAME: " + deviceByRoomName);
                createNewDeviceMusic(deviceByRoomName, null, "музыка");

            } else {
                log.info("DEVICE EXISTS. SKIP CREATE BY VOICE OR WEB");
            }
        }
    }

    private Device createNewDeviceMusic(String roomName, String deviceId, String deviceName) {
        if (deviceId == null) deviceId = String.valueOf(UUID.randomUUID());
        Device device = new Device();
        device.room = roomName;
        device.id = deviceId;
//        device.external_id = deviceExternalId;
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

        devices.add(device);
        return device;
    }

    public Device createNewDeviceSwitch(String roomName, String deviceName) {
        Device device = new Device();
        device.room = roomName;
        device.id = String.valueOf(UUID.randomUUID());
        device.name = deviceName;
        device.type = "devices.types.switch";
        // Базовая способность вкл/выкл
        Capability onOff = new Capability();
        onOff.type = "devices.capabilities.on_off";
        onOff.retrievable = true;   // возможность запрашивать состояние
        onOff.reportable = true;    // отправлять уведомления об изменении
        // onOff.parameters.instance = "on"; // для on_off это значение по умолчанию
        device.capabilities.add(onOff);
        log.info("SWITCH DEVICE CREATED OK");
        devices.add(device);
        return device;
    }

}