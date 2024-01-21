package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.response.Capability;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.provider.response.Range;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.HashMap;
import java.util.stream.Collectors;

@Log4j2
public class DeviceUtils {

    public static Integer addToHome(Device device) {
        int id = 0;
        if (SmartHome.devices.size() != 0) id = Integer.parseInt(SmartHome.devices.getLast().id) + 1;
        log.info(SmartHome.devices.stream().map(d -> d.room).collect(Collectors.toList()));
        device.id = String.valueOf(id);
        SmartHome.devices.add(device);
        return id;
    }

    public static String create(HashMap<String, String> parameters) {
        String speaker_name_alice = parameters.get("speaker_name_alice");
        String speaker_name_lms = parameters.get("speaker_name_lms");
        String room = parameters.get("room");
        Integer id = create(speaker_name_alice, speaker_name_lms, room);
//        JsonUtils.listToJsonFile(Home.devices, "alice_devices.json"); // сделано в create
        return "CREATED " + id + " HOME: " + SmartHome.devices.size();
    }

    public static String edit(HashMap<String, String> parameters) {
        log.info("PARAMETERS: " + parameters);
        int index_old = Integer.parseInt(parameters.get("id_old"));
        Device deviceNew = SmartHome.devices.get(index_old);
        deviceNew.name = parameters.get("speaker_name_alice");
        deviceNew.id = parameters.get("id");
        deviceNew.room = parameters.get("room");
        deviceNew.customData.lmsName = parameters.get("speaker_name_lms");
        SmartHome.devices.remove(index_old);
        SmartHome.devices.add(deviceNew);
        SmartHome.devices.sort((d1, d2) -> d1.id.compareTo(d2.id));
        JsonUtils.listToJsonFile(SmartHome.devices, "alice_devices.json");
        return "EDITED";
    }

    public static String remove(HashMap<String, String> parameters) {
        String idd = parameters.get("id");
        Device device = SmartHome.devices.stream().filter(d -> d.id.equals(idd)).findFirst().get();
        SmartHome.devices.remove(device);
        JsonUtils.listToJsonFile(SmartHome.devices, "alice_devices.json");
        return "REMOVED " + idd + " HOME: " + SmartHome.devices.size();
    }

    public static Integer create(String name, String lmsName, String room) {
        Device myDevice = new Device(name);
        myDevice.description = lmsName;
        myDevice.type = "devices.types.media_device.receiver";
        myDevice.room = room;
        myDevice.customData.lmsName = lmsName;

        myDevice.aliases.add("радио");
//        myDevice.aliases.add("музыка");

        Capability volume = new Capability();
        volume.type = "devices.capabilities.range"; // Тип умения. channel     volume
        volume.retrievable = true; // Доступен ли для данного умения устройства запрос состояния
        volume.reportable = false; // Признак включенного оповещения об изменении состояния умения
        volume.parameters.instance = "volume"; // Название функции для данного умения. volume channel ...
//        volume.parameters.random_access = true; // Возможность устанавливать произвольные значения функции
        volume.parameters.range = new Range();
        volume.parameters.range.min = 1;
        volume.parameters.range.max = 100;
        volume.parameters.range.precision = 1;
//        volume.parameters.split = false;
        myDevice.capabilities.add(volume);

        Capability channel = new Capability();
        channel.type = "devices.capabilities.range"; // Тип умения. channel     volume
        channel.retrievable = true; // Доступен ли для данного умения устройства запрос состояния
        channel.reportable = false; // Признак включенного оповещения об изменении состояния умения
        channel.parameters.instance = "channel"; // Название функции для данного умения. volume channel ...
//        channel.parameters.random_access = true; // Возможность устанавливать произвольные значения функции
        channel.parameters.range = new Range();
        channel.parameters.range.min = 1;
        channel.parameters.range.max = 9;
        channel.parameters.range.precision = 1;
        myDevice.capabilities.add(channel);

        Capability pause = new Capability();
        pause.type = "devices.capabilities.toggle"; // Тип умения. channel     volume
        pause.retrievable = true; // Доступен ли для данного умения устройства запрос состояния
        pause.reportable = false; // Признак включенного оповещения об изменении состояния умения
        pause.parameters.instance = "pause"; // Название функции для данного умения. volume channel ...
//        pause.parameters.random_access = true;
//        myDevice.capabilities.add(pause);

        Capability on_of = new Capability();
        on_of.type = "devices.capabilities.on_off"; // Тип умения. channel     volume
        on_of.retrievable = true; // Доступен ли для данного умения устройства запрос состояния
        on_of.reportable = false; // Признак включенного оповещения об изменении состояния умения
        on_of.parameters.instance = "on"; // Название функции для данного умения. volume channel ...
//        on_of.parameters.random_access = true;
//        on_of.parameters.split = false;
        myDevice.capabilities.add(on_of);

//        Capability mute = new Capability();
//        mute.type = "devices.capabilities.toggle"; // Тип умения. channel     volume
//        mute.retrievable = true; // Доступен ли для данного умения устройства запрос состояния
//        mute.reportable = false; // Признак включенного оповещения об изменении состояния умения
//        mute.parameters.instance = "mute"; // Название функции для данного умения. volume channel ...
//        mute.parameters.random_access = true;
//        myDevice.capabilities.add(mute);

        log.info("NEW DEVICE: " + myDevice);
        log.info("HOME SIZE BEFORE ADD: " + SmartHome.devices.size());
        Integer id =DeviceUtils.addToHome(myDevice);
//        Integer id = myDevice.addToHome(myDevice);
        log.info("NEW DEVICE ID: " + id);
        log.info("HOME SIZE AFTER ADD: " + SmartHome.devices.size());
        log.info("HOME DEVICES: " + SmartHome.devices.stream().map(d -> d.customData.lmsName + " id=" + d.id).collect(Collectors.toList()));
        JsonUtils.listToJsonFile(SmartHome.devices, "alice_devices.json");
        return id;
    }
    
    
    
    
    
    
}