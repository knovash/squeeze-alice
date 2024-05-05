package org.knovash.squeezealice;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.response.Capability;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.provider.response.Range;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Levenstein;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Data
public class SmartHome {

    public static String user_id = "konstantin";
    public static LinkedList<Device> devices = new LinkedList<>();
    public static Map<String, String> rooms = new HashMap<>();

    public static Device getDeviceById(int deviceId) {
        String index = String.valueOf(deviceId);
        return devices.stream()
                .filter(d -> (d.id != null))
                .filter(d -> d.id.equals(index))
                .findFirst().orElse(null);
    }

    public static Device getDeviceByRoom(String room) {
        log.info("ROOM: " + room);
        return  devices.stream()
                .filter(d -> (d.room != null))
                .filter(d -> d.room.equals(room))
                .findFirst().orElse(null);
    }

    public static Device getDeviceByRoomLevenstein(String room) {
        log.info("ROOM " + room);
        if (devices == null || devices.size() == 0) return null;
        List<String> rooms = devices.stream().map(device -> device.room).collect(Collectors.toList());
        room = Levenstein.getNearestElementInList(room, rooms);
        return getDeviceByRoom(room);
    }

    public static String getRoomByPlayerName(String playerName) {
        log.info("PLAYER NAME: " + playerName);
        Device device = devices.stream().filter(d -> d.takePlayerName().toLowerCase().equals(playerName.toLowerCase())).findFirst().orElse(null);
        if (device == null) return null;
        return device.room;
    }

    public static String getIdByPlayerName(String playerName) {
        log.info("PLAYER NAME: " + playerName);

        log.info("PLAYER NAME: " + playerName);

        if (devices == null) return "---";
        if (devices.size() == 0) return "---";
        Device device = devices.stream()
                .filter(d -> d.takePlayerName() != null)
                .filter(d -> d.takePlayerName().equals(playerName))
                .findFirst()
                .orElse(null);
        if (device == null) return "---";
        if (device.id == null) return "---";
        return device.id;
    }

    public static String getRoomByAliceId(String aliceId) {
        log.info("ALICE ID: " + aliceId);
        if (Main.rooms == null) return null;
        Map.Entry<String, String> entry = Main.rooms.entrySet().stream()
                .filter(r -> r.getValue().equals(aliceId)).findFirst().orElse(null);
        if (entry == null) return null;
        return entry.getKey();
    }

    public static Device getDeviceByAliceId(String aliceId) {
        getRoomByAliceId(aliceId);
        log.info("aliceId " + aliceId);
        Map.Entry<String, String> entry = Main.rooms.entrySet().stream()
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
//        Main.rooms = JsonUtils.jsonFileToMap("rooms.json", String.class, String.class);
        log.info("DEVICES: " + SmartHome.devices.stream().map(d -> d.takePlayerName() + ":" + d.room).collect(Collectors.toList()));
    }


    public static void clear() {
        log.info("CLEAR DEVICES");
        devices = new LinkedList<>();
    }

    public static Integer addNewDevice(Device device, int idd) {
        log.info("ADD DEVICE: " + device.takePlayerName() + " ID: " + idd);
        int id = SmartHome.devices.size() + 1;
        if (idd != 0) id = idd;
        device.id = String.valueOf(id);
        SmartHome.devices.add(device);
        log.info("DEVICES: " + SmartHome.devices.stream().map(d -> d.takePlayerName() + " id=" + d.id)
                .collect(Collectors.toList()));
        return id;
    }

    public static Device create(HashMap<String, String> parameters) {
        log.info("");
        log.info("CREATE parameters: " + parameters);
        Device device = new Device();
        device.type = "devices.types.media_device.receiver";
        device.room = parameters.get("room");

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

        Integer id = null;
        Integer idByParameters = null;
        if (parameters.get("id") != null) idByParameters = Integer.parseInt(parameters.get("id"));
        log.info("ID BY PARAMETERS: " + idByParameters);

        if (SmartHome.devices == null) SmartHome.devices = new LinkedList<>();

        if (idByParameters == null) {
            if (SmartHome.devices.size() == 0) {
                id = 1;
            } else {
                Integer idBySmartHome = Integer.valueOf(SmartHome.devices.stream()
                        .map(device1 -> device1.id)
                        .max(Comparator.naturalOrder())
                        .orElse(null));
                log.info("ID BY SMARTHOME: " + idBySmartHome);
                id = idBySmartHome + 1;
            }
        } else id = idByParameters;
        device.id = String.valueOf(id);
        log.info("NEW DEVICE: " + "Room = " + device.room + " Device id = " + device.id);
        SmartHome.devices.add(device);
        log.info("DEVICES: " + SmartHome.devices.stream().map(d -> "Room = " + d.room + " Device id = " + d.id)
                .collect(Collectors.toList()));
        return device;
    }
}