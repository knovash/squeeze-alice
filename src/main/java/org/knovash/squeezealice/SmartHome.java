package org.knovash.squeezealice;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.response.Capability;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.provider.response.Range;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Levenstein;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Data
public class SmartHome {

    public static String user_id = "konstantin";
    public static LinkedList<Device> devices = new LinkedList<>();
    public static Map<String, String> rooms = new HashMap<>();

    public static Device getDeviceById(int deviceId) {
        String index = String.valueOf(deviceId);
        return devices.stream().filter(d -> d.id.equals(index)).findFirst().orElse(null);
    }

    public static Device getDeviceByLmsName(String lmsName) {
        return devices.stream().filter(d -> d.customData.lmsName.equals(lmsName)).findFirst().orElse(null);
    }

    public static Device getDeviceByRoom(String room) {
        log.info("ROOM: " + room);
        return devices.stream()
                .filter(d -> d.room.toLowerCase().equals(room.toLowerCase()))
                .findFirst().orElse(null);
    }

    public static Device getDeviceByRoomLevenstein(String room) {
        log.info("room " + room);
        List<String> rooms = devices.stream().map(device -> device.room).collect(Collectors.toList());
        room = Levenstein.getNearestElementInList(room, rooms);
        return getDeviceByRoom(room);
    }

    public static String getRoomByPlayerName(String playerName) {
        log.info("PLAYER NAME: " + playerName);
        Device device = devices.stream().filter(d -> d.customData.lmsName.toLowerCase().equals(playerName.toLowerCase())).findFirst().orElse(null);
        if (device == null) return null;
        return device.room;
    }

    public static String getRoomByAliceId(String aliceId) {
        log.info("ALICE ID: " + aliceId);
        Map.Entry<String, String> entry = SmartHome.rooms.entrySet().stream()
                .filter(r -> r.getValue().equals(aliceId)).findFirst().orElse(null);
        if (entry == null) return null;
        return entry.getKey();
    }

    public static Device getDeviceByAliceId(String aliceId) {
        getRoomByAliceId(aliceId);
        log.info("aliceId " + aliceId);
        Map.Entry<String, String> entry = SmartHome.rooms.entrySet().stream()
                .filter(r -> r.getValue().equals(aliceId)).findFirst().orElse(null);
        if (entry == null) return null;
        return SmartHome.getDeviceByRoom(entry.getKey());
    }

    public static void read() {
        log.info("");
        log.info("READ ALICE DEVICES FROM alice_devices.json");
        SmartHome.devices = new LinkedList<>();
        List<Device> devices = JsonUtils.jsonFileToList("alice_devices.json", Device.class);
        if (devices != null) SmartHome.devices.addAll(devices);
        SmartHome.rooms = JsonUtils.jsonFileToMap("rooms.json", String.class, String.class);
        log.info("DEVICES: " + SmartHome.devices.stream().map(d -> d.customData.lmsName + ":" + d.room).collect(Collectors.toList()));
    }

    public static void write() {
        JsonUtils.listToJsonFile(SmartHome.devices, "alice_devices.json");
        JsonUtils.mapToJsonFile(SmartHome.rooms, "rooms.json");
    }

    public static void logListStringDevices() {
        log.info("LOG DEVICES");
        SmartHome.devices.stream()
                .forEach(device -> log.info(device.id + " " + device.room + " " + device.customData.lmsName));
    }

    public static void clear() {
        log.info("CLEAR DEVICES");
        devices = new LinkedList<>();
        SmartHome.write();
    }

    public static Integer addNewDevice(Device device, int idd) {
        log.info("ADD DEVICE: " + device.customData.lmsName);
        int id = SmartHome.devices.size() + 1;
        if (idd != 0) id = idd;
        device.id = String.valueOf(id);
        SmartHome.devices.add(device);
        return id;
    }

    public static String save(HashMap<String, String> parameters) {
        log.info("PARAMETERS: " + parameters);
        int index_old = Integer.parseInt(parameters.get("id_old"));
        Device device = SmartHome.devices.get(index_old);
        device.name = parameters.get("speaker_name_alice");
        device.id = parameters.get("id");
        device.room = parameters.get("room");
        device.customData.lmsName = parameters.get("speaker_name_lms");
        SmartHome.devices.remove(index_old);
        SmartHome.devices.add(device);
        SmartHome.devices.sort((d1, d2) -> d1.id.compareTo(d2.id));
        SmartHome.write();
        return "SAVED";
    }

    public static String remove(HashMap<String, String> parameters) {
        log.info("REMOVE DEVICE");
        String deviceId = parameters.get("id");
        Device device = SmartHome.devices.stream().filter(d -> d.id.equals(deviceId)).findFirst().get();
        SmartHome.devices.remove(device);
        SmartHome.write();
        return "REMOVED " + deviceId + " DEVICES SIZE: " + SmartHome.devices.size();
    }

    public static void create(HashMap<String, String> parameters) {
        log.info("CREATE " + parameters);
        Device device = new Device(parameters.get("speaker_name_alice"));
        device.type = "devices.types.media_device.receiver";
        device.room = parameters.get("room");
        if (parameters.get("name") != null) device.name = parameters.get("name");
        device.customData.lmsName = parameters.get("speaker_name_lms");

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
        device.capabilities.add(volume);

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
        device.capabilities.add(channel);

        Capability on_of = new Capability();
        on_of.type = "devices.capabilities.on_off"; // Тип умения. channel     volume
        on_of.retrievable = true; // Доступен ли для данного умения устройства запрос состояния
        on_of.reportable = true; // Признак включенного оповещения об изменении состояния умения
        on_of.parameters.instance = "on"; // Название функции для данного умения. volume channel
        device.capabilities.add(on_of);

        log.info("NEW DEVICE: " + device);

        int id = 0;
        if (parameters.get("id") != null) id = Integer.parseInt(parameters.get("id"));

        SmartHome.addNewDevice(device, id);

        log.info("DEVICES: " + SmartHome.devices.stream().map(d -> d.customData.lmsName + " id=" + d.id)
                .collect(Collectors.toList()));
        SmartHome.write();
    }
}