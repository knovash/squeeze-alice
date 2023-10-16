package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Home;
import org.knovash.squeezealice.provider.pojo.Capability;
import org.knovash.squeezealice.provider.pojo.Device;
import org.knovash.squeezealice.provider.pojo.Range;
import org.knovash.squeezealice.provider.pojo.State;
import org.knovash.squeezealice.utils.JsonUtils;

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
        volume.parameters.range = new Range();
        volume.parameters.range.min = 1;
        volume.parameters.range.max = 100;
        myDevice.capabilities.add(volume);

        Capability channel = new Capability();
        channel.type = "devices.capabilities.range"; // Тип умения. channel     volume
        channel.retrievable = true; // Доступен ли для данного умения устройства запрос состояния
        channel.reportable = false; // Признак включенного оповещения об изменении состояния умения
        channel.parameters.instance = "channel"; // Название функции для данного умения. volume channel ...
        channel.parameters.random_access = true; // Возможность устанавливать произвольные значения функции
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
        pause.parameters.random_access = true;
        myDevice.capabilities.add(pause);

        Capability on_of = new Capability();
        on_of.type = "devices.capabilities.on_off"; // Тип умения. channel     volume
        on_of.retrievable = true; // Доступен ли для данного умения устройства запрос состояния
        on_of.reportable = false; // Признак включенного оповещения об изменении состояния умения
        on_of.parameters.instance = "on"; // Название функции для данного умения. volume channel ...
        on_of.parameters.random_access = true;
        on_of.parameters.split = false;
        myDevice.capabilities.add(on_of);

        Capability mute = new Capability();
        mute.type = "devices.capabilities.toggle"; // Тип умения. channel     volume
        mute.retrievable = true; // Доступен ли для данного умения устройства запрос состояния
        mute.reportable = false; // Признак включенного оповещения об изменении состояния умения
        mute.parameters.instance = "mute"; // Название функции для данного умения. volume channel ...
        mute.parameters.random_access = true;
        myDevice.capabilities.add(mute);

        log.info("MY DEVICE: " + myDevice);
        Integer id = myDevice.addToYandex();
        log.info("ID: " + id);
        log.info("Ya size" + Home.devices.size());
        log.info("Ya size" + Home.devices.get(0));


        log.info("DEVICE POJO TO JSON: " + JsonUtils.pojoToJson(myDevice).replace("\\",""));


    }
}