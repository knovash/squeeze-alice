package org.knovash.squeezealice;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.response.*;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Data
public class SmartHome {

    public static List<Device> devices = new ArrayList<>();
    public static String saveToFileJson = "data/devices.json";

    public static Device getDeviceById(int deviceId) {
        String index = String.valueOf(deviceId);
        return devices.stream()
                .filter(d -> (d.id != null))
                .filter(d -> d.id.equals(index))
                .findFirst().orElse(null);
    }

    public static Device getDeviceByCorrectRoom(String room) {
//        log.info("SEARCH BY ROOM: " + room + " IN DEVICES: " + devices.size());
        Device device = devices.stream()
                .filter(d -> (d.room != null))
                .filter(d -> d.room.equalsIgnoreCase(room))
                .findFirst().orElse(null);
        if (device == null) {
            log.info("NO DEVICE WITH ROOM: " + room);
            return null;
        }
        log.info("BY ROOM " + room+ " GET DEVICE: " + device.room + " ID: " + device.id);
        return device;
    }

    public static void create(String room, Integer index) {
//        log.info("START CREATE ROOM: " + room + " INDEX: " + index);
//        log.info("EXISTS: " + SmartHome.devices.stream().map(d -> d.id + ":" + d.room).collect(Collectors.toList()));
//        SmartHome.devices.stream().forEach(d -> log.info("EXISTS LOCAL DEVICE ID: " + d.id + " ROOM: " + d.room));
//        если локальных девайсов еще нет - создать пустой лист
        if (SmartHome.devices == null) SmartHome.devices = new ArrayList<>();
//        если в метод не пришел индекс - создать индекс = существующий макс индекс + 1
        if (index == null) {
            int maxId = SmartHome.devices.stream()
                    .mapToInt(d -> Integer.parseInt(d.id))
                    .max()
                    .orElse(0);
            index = maxId + 1;
            log.info("NEW ID IN SMART HOME: {}", index);
        }

// проверить если девайс с такой комнатой уже существует - выход
        Integer counter = (int) SmartHome.devices.stream().filter(device1 -> device1.room.equals(room)).count();
//        log.info("DEVICE EXISTS COUNTER: " + counter);
        if (counter > 0) {
            log.info("DEVICE EXISTS. CREATE SKIP");
            return;
        }

//        создать новый девайс
        Device device = new Device();
        device.type = "devices.types.media_device.receiver";
        device.room = room;

//        log.info("NEW DEVICE: " + device);


//        создать для нового девайса капабилити
        Capability volume = new Capability();
        volume.type = "devices.capabilities.range"; // Тип умения. channel     volume
        volume.retrievable = true; // Доступен ли для данного умения устройства запрос состояния
        volume.reportable = true; // Признак включенного оповещения об изменении состояния умения
        volume.parameters.instance = "volume"; // Название функции для данного умения. volume channel
        volume.parameters.random_access = true; // Возможность устанавливать произвольные значения функции
        volume.parameters.range = new Range();
        volume.parameters.range.min = 1;
        volume.parameters.range.max = 100;
        volume.parameters.range.precision = 1;
        volume.state = new State();
        volume.state.instance = "volume";
        volume.state.value = "0";
        volume.state.action_result = new ActionResult();
        volume.state.action_result.error_code = null;
        volume.state.action_result.error_message = null;
        device.capabilities.add(volume);
//        log.info("DEVICE ADD CAPABILITI VOLUME: " + device.capabilities.get(device.capabilities.indexOf(volume)));

        Capability channel = new Capability();
        channel.type = "devices.capabilities.range"; // Тип умения. channel     volume
        channel.retrievable = true; // Доступен ли для данного умения устройства запрос состояния
        channel.reportable = true; // Признак включенного оповещения об изменении состояния умения
        channel.parameters.instance = "channel"; // Название функции для данного умения. volume channel
        channel.parameters.random_access = true; // Возможность устанавливать произвольные значения функции
        channel.parameters.range = new Range();
        channel.parameters.range.min = 1;
        channel.parameters.range.max = 9;
        channel.parameters.range.precision = 1;
        channel.state = new State();
        channel.state.instance = "channel";
        channel.state.value = null;
        channel.state.relative = false;
        channel.state.action_result = new ActionResult();
        channel.state.action_result.error_code = null;
        channel.state.action_result.error_message = null;
        device.capabilities.add(channel);
//        log.info("DEVICE ADD CAPABILITI CHANNEL: " + device.capabilities.get(device.capabilities.indexOf(channel)));

        Capability on_of = new Capability();
        on_of.type = "devices.capabilities.on_off"; // Тип умения. channel     volume
        on_of.retrievable = true; // Доступен ли для данного умения устройства запрос состояния
        on_of.reportable = true; // Признак включенного оповещения об изменении состояния умения
        on_of.parameters.instance = "on"; // Название функции для данного умения. volume channel
//        on_of.state = new State();
//        on_of.state.instance = "on";
//        on_of.state.value = null;
//        on_of.state.relative = false;
//        on_of.state.action_result = new ActionResult();
//        on_of.state.action_result.error_code = null;
//        on_of.state.action_result.error_message = null;
        device.capabilities.add(on_of);
//        log.info("DEVICE ADD CAPABILITI ON_OF: " + device.capabilities.get(device.capabilities.indexOf(on_of)));

// назначить девайсу индекс
        device.id = String.valueOf(index);
        //        добавить девайс в лист локальных девайсов
        SmartHome.devices.add(device);
//        log.info("CREATED DEVICE ID: " + device.id + " ROOM: " + device.room + " " + SmartHome.devices.stream().map(d -> d.id + ":" + d.room).collect(Collectors.toList()));
        log.info("CREATED DEVICE ID: " + device.id + " ROOM: " + device.room);
//        log.info("CREATER DEVICE FULL: " + device);
//        log.info("DEVICES: " + SmartHome.devices.size());
        //        return device;
    }

    public static void read() {
        devices = JsonUtils.jsonFileToList(SmartHome.saveToFileJson, Device.class);
        if (devices == null) devices = new ArrayList<>();
        log.info("DEVICES FROM devices.json: " + devices.size());
        log.debug("DEVICES: " + devices);
    }

    public static void write() {
        log.info("WRITE devices.json");
        JsonUtils.pojoToJsonFile(SmartHome.devices, SmartHome.saveToFileJson);
    }
}