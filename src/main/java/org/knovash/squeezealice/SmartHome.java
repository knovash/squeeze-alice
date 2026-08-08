package org.knovash.squeezealice;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.response.*;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.web.SettingsButtons;
import org.knovash.squeezealice.yandex.Yandex;
import org.knovash.squeezealice.yandex.YandexUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.Main.smartHome;
import static org.knovash.squeezealice.yandex.Yandex.yandexInfo;

@Log4j2
@Data
public class SmartHome {

    public List<Device> devices = new ArrayList<>();
    public static String saveToFileJson = "data/devices.json";
    public List<String> devicesButtons = List.of(
            "Переключи сюда",
            "Дальше",
            "Назад",
            "Отдельно",
            "Вместе",
            "Что играет",
            "Где пульт",
            "Подключи пульт"
    );

    public Device deviceById(String deviceId) { // приходит от яндекса EXT ID
//        log.info(">>> DEVICE ID " + deviceId);
        return devices.stream()
                .filter(d -> d.id != null && d.id.equals(deviceId))
                .findFirst()
                .orElse(null);
    }

    public Device deviceByName(String name, String room) { // приходит от яндекса EXT ID
//        log.info(">>> DEVICE ID " + deviceId);
        return devices.stream()
                .filter(d -> room.equals(d.room) && name.equals(d.name))
                .findFirst()
                .orElse(null);
    }

    public static Device deviceByRoom(String room) {
        if (room == null) return null;
        return smartHome.devices.stream()
                .filter(d -> room.equalsIgnoreCase(d.room))
                .findFirst()
                .orElse(null);
    }

    public void create(String deviceByRoomName, YandexUtils.MusicDevice deviceFromYandex) {
        if (devices == null) devices = new ArrayList<>();

        if (deviceFromYandex != null)
        // Случай 1: синхронизация с Яндексом
        {
            Device deviceExists = smartHome.devices.stream()
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

    public Device createNewDeviceOther(String roomName, String deviceName) {
        Device device = new Device();
        device.room = roomName;
        device.id = String.valueOf(UUID.randomUUID());
        device.name = deviceName;
        device.type = "devices.types.other"; // ← основной тип

        //Базовая способность вкл/выкл (рекомендуется для большинства устройств)
        Capability onOff = new Capability();
        onOff.type = "devices.capabilities.on_off";
        onOff.retrievable = true;
        onOff.reportable = true;
        device.capabilities.add(onOff);

        Capability toggle = new Capability();
        toggle.type = "devices.capabilities.toggle";
        toggle.retrievable = false;
        toggle.reportable = false;
        toggle.parameters = new Parameters();
        toggle.parameters.instance = "pause"; // любое уникальное имя
        toggle.parameters.random_access = false;
        device.capabilities.add(toggle);

        Capability range = new Capability();
        range.type = "devices.capabilities.range";
        range.retrievable = true;   // разрешить запрос состояния
        range.reportable = true;    // разрешить уведомления об изменении
        range.parameters = new Parameters();
        range.parameters.random_access = true;
        range.parameters.range = new Range();
        range.parameters.range.min = 0;        // минимальное значение
        range.parameters.range.max = 100;      // максимальное значение
        range.parameters.range.precision = 1;  // шаг изменения
        // state можно добавить при необходимости, но для описания устройства он необязателен
        range.parameters.instance = "volume"; // допустимое значение из списка функций
        device.capabilities.add(range);

        Capability channel = new Capability();
        channel.type = "devices.capabilities.range";
        channel.retrievable = true;   // разрешить запрос состояния
        channel.reportable = true;    // разрешить уведомления об изменении
        channel.parameters = new Parameters();
        channel.parameters.random_access = true;
        channel.parameters.range = new Range();
        channel.parameters.range.min = 0;        // минимальное значение
        channel.parameters.range.max = 8;      // максимальное значение
        channel.parameters.range.precision = 1;  // шаг изменения
        // state можно добавить при необходимости, но для описания устройства он необязателен
        channel.parameters.instance = "channel"; // допустимое значение из списка функций
        device.capabilities.add(channel);

        Capability mode = new Capability();
        mode.type = "devices.capabilities.mode";
        mode.retrievable = false;   // чтобы можно было запросить текущий режим
        mode.reportable = false;
        mode.parameters = new Parameters();
        mode.parameters.instance = "program"; // уникальное имя для этой функции
        List<Mode> modes = new ArrayList<>();
        modes.add(new Mode("auto"));
        modes.add(new Mode("eco"));
        mode.parameters.modes = modes;
        mode.state = new State();
        mode.state.instance = "program";
        mode.state.value = "auto"; // или null, если ничего не выбрано
        device.capabilities.add(mode);

        //Capability button = new Capability();
        //button.type = "devices.capabilities.custom.button";
        //button.retrievable = false;
        //button.reportable = false;
        //button.parameters = new Parameters();
        //button.parameters.instance = "power";       // уникальный ID (например, "power")
        //button.state = new State();
        //button.state.instance = "power";
        //button.state.value = "true"; // будет отправлено при нажатии
        //device.capabilities.add(button);

        log.info("OTHER DEVICE CREATED OK");
        devices.add(device);
        return device;
    }

    public void createButtonsInAllRooms() {
        createButtonsInAllRooms(null);
    }

    public void createButtonsInAllRooms(List<String> roomsWithSpeaker) {
        log.info("ROOMS: " + Yandex.rooms);
        if (roomsWithSpeaker == null || roomsWithSpeaker.isEmpty()) {
            roomsWithSpeaker = Yandex.getRoomsWithAliceOrMusic(yandexInfo);
        }
        log.info("ROOMS WITH SPEAKER: " + roomsWithSpeaker);

        // создавать кнопки только разрешенные в настройках класс SettingsButtons
        List<String> enabledButtonDescriptions = Arrays.stream(SettingsButtons.ToggleButton.values())
                .filter(button -> button.getValue())
                .map(button -> button.getDescription())
                .collect(Collectors.toList());
        roomsWithSpeaker.stream().forEach(room -> enabledButtonDescriptions.forEach(button -> createDeviceToggle(room, button)));

        //roomsWithSpeaker.stream().forEach(room -> devicesButtons.forEach(button -> createDeviceToggle(room, button)));
    }

    public Device createDeviceToggle(String roomName, String deviceName) {
        // Проверяем, существует ли уже устройство с таким именем в данной комнате
        Device existing = devices.stream()
                .filter(d -> deviceName.equals(d.name) && roomName.equals(d.room))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            log.info("TOGGLE DEVICE ALREADY EXISTS " + deviceName);
            return existing;
        }
        log.info("TOGGLE DEVICE NOT EXISTS - CREATE " + deviceName);
        // Если нет – создаём новое с новым UUID
        Device device = new Device();
        device.room = roomName;
        device.id = String.valueOf(UUID.randomUUID());
        device.name = deviceName;
        device.type = "devices.types.other";
        // Capability on_off
        Capability onOff = new Capability();
        onOff.type = "devices.capabilities.on_off";
        onOff.retrievable = true;
        onOff.reportable = true;
        Parameters params = new Parameters();
        params.instance = "on";
        onOff.parameters = params;
        device.capabilities.add(onOff);
        log.info("NEW TOGGLE DEVICE CREATED: {} in room {} with id {}", deviceName, roomName, device.id);
        devices.add(device);
        return device;
    }


    public Device createDeviceToggle(YandexUtils.MusicDevice ddd) {
        // Проверяем, существует ли уже устройство с таким externalId
        Device existing = devices.stream()
                .filter(d -> ddd.externalId.equals(d.id)) // или d.external_id, смотря где храните
                .findFirst()
                .orElse(null);
        if (existing != null) {
            log.info("TOGGLE DEVICE ALREADY EXISTS: {} in room {} with id {}", ddd.name, ddd.roomName, ddd.externalId);
            return existing;
        }
        // Если нет – создаём новое
        String room = ddd.roomName;
        Device device = new Device();
        device.room = room;
        device.id = ddd.externalId;
        device.name = ddd.name;
        device.type = "devices.types.other";
        Capability onOff = new Capability();
        onOff.type = "devices.capabilities.on_off";
        onOff.retrievable = true;
        onOff.reportable = true;
        Parameters params = new Parameters();
        params.instance = "on";
        onOff.state = new State();
        onOff.state.value = "false";
        onOff.state.relative = false;
        onOff.parameters = params;
        device.capabilities.add(onOff);
        log.info("NEW TOGGLE DEVICE CREATED: {} in room {} with id {}", ddd.name, room, ddd.externalId);
        devices.add(device);
        return device;
    }

    public void write() {
        log.info("WRITE: " + config.fileDevices);
        JsonUtils.pojoToJsonFile(this, config.fileDevices);
    }

}