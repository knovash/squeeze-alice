package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.provider.pojo.Capability;
import org.knovash.squeezealice.provider.pojo.Device;

@Log4j2
public class NewDevice {

    public static void create(String name, String lmsName, String room) {
        Device myDevice = new Device(name);
        myDevice.description = lmsName;
        myDevice.type = "devices.types.media_device.receiver";
        myDevice.room = room;
        myDevice.customData.lmsName = lmsName;

        Capability volume = new Capability();
        volume.type = "devices.capabilities.range"; // Тип умения. channel     volume
        volume.retrievable = true; // Доступен ли для данного умения устройства запрос состояния
        volume.reportable = false; // Признак включенного оповещения об изменении состояния умения
        volume.parameters.instance = "volume"; // Название функции для данного умения. volume channel ...
        volume.parameters.random_access = true; // Возможность устанавливать произвольные значения функции
        volume.state.instance = "volume";
        volume.state.relative = false;
        myDevice.capabilities.add(volume);

//        Capability channel = new Capability();
//        channel.type = "devices.capabilities.range"; // Тип умения. channel     volume
//        channel.retrievable = false; // Доступен ли для данного умения устройства запрос состояния
//        channel.reportable = false; // Признак включенного оповещения об изменении состояния умения
//        channel.parameters.instance = "channel"; // Название функции для данного умения. volume channel ...
//        channel.parameters.random_access = true; // Возможность устанавливать произвольные значения функции
//        channel.state.instance = "channel";
//        channel.state.relative = false;
//        channel.parameters.range.min = 1;
//        channel.parameters.range.max = 9;
//        myDevice.capabilities.add(channel);

//        Capability pause = new Capability();
//        pause.type = "devices.capabilities.on_off"; // Тип умения. channel     volume
//        pause.retrievable = false; // Доступен ли для данного умения устройства запрос состояния
//        pause.reportable = false; // Признак включенного оповещения об изменении состояния умения
//        pause.parameters = null; // Название функции для данного умения. volume channel ...
//        myDevice.capabilities.add(pause);

        log.info("MY DEVICE: " + myDevice);
        Integer id = myDevice.addToYandex();
        log.info("ID: " + id);
        log.info("Ya size" + Yandex.devices.size());
        log.info("Ya size" + Yandex.devices.get(0));


    }
}