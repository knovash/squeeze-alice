package org.knovash.squeezealice;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.response.Capability;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.provider.response.Range;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Log4j2
@Data
public class SmartHome {

    public static List<Device> devices = new ArrayList<>();

    public static Device getDeviceById(int deviceId) {
        String index = String.valueOf(deviceId);
        return devices.stream()
                .filter(d -> (d.id != null))
                .filter(d -> d.id.equals(index))
                .findFirst().orElse(null);
    }

    public static Device getDeviceByCorrectRoom(String room) {
        log.info("SEARCH BY ROOM: " + room + " IN DEVICES: " + devices.size());
        Device device = devices.stream()
                .filter(d -> (d.room != null))
                .filter(d -> d.room.equalsIgnoreCase(room))
                .findFirst().orElse(null);
        if (device == null) {
            log.info("NO DEVICE WITH ROOM: " + room);
            return null;
        }
        log.info("DEVICE: " + device.room + " ID: " + device.id);
        return device;
    }

    public static Device create(String room, Integer index) {
        log.info("CREATE DEVICE ROOM: " + room + " ID: " + index);
        Device device = new Device();
        device.type = "devices.types.media_device.receiver";
        device.room = room;

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
        if (index != null) {
            idByParameters = index;
            log.info("ID BY PARAMETERS: " + idByParameters);
        }
        if (SmartHome.devices == null) SmartHome.devices = new ArrayList<>();
        if (idByParameters == null) {
            if (SmartHome.devices.size() == 0) {
                id = 1;
            } else {
                Integer idBySmartHome = Integer.valueOf(SmartHome.devices.stream()
                        .map(d -> d.id)
                        .max(Comparator.naturalOrder())
                        .orElse(null));
                id = idBySmartHome + 1;
                log.info("NEW ID IN SMART HOME: " + id);
            }
        } else id = idByParameters;
        device.id = String.valueOf(id);
        log.info("NEW DEVICE: " + "room=" + device.room + " id=" + device.id + " type=" + device.type);
        SmartHome.devices.add(device);
        return device;
    }

    public static void write() {
        log.info("WRITE devices.json");
        JsonUtils.pojoToJsonFile(SmartHome.devices, "devices.json");
    }
}