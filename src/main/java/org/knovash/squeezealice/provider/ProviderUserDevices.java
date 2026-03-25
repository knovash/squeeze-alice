package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.provider.response.Payload;
import org.knovash.squeezealice.provider.response.ResponseYandex;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.yandex.YandexUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.config;

@Log4j2
public class ProviderUserDevices {
//    https://yandex.ru/dev/dialogs/smart-home/doc/ru/reference/get-devices-jrpc
//    Объект payload
//    user_id   String              Идентификатор пользователя.
//    devices   Array of objects    Массив с устройствами пользователя.
//    Объект devices
//    id        String              Идентификатор устройства. Должен быть уникален среди всех устройств производителя.
//    name      String              Название устройства.
//    room      String              Название помещения, в котором расположено устройство.
//
//    external_id используется только тут:
//    GET https://api.iot.yandex.net/v1.0/user/info             https://yandex.ru/dev/dialogs/smart-home/doc/ru/concepts/platform-user-info
//    GET https://api.iot.yandex.net/v1.0/devices/{device_id}   https://yandex.ru/dev/dialogs/smart-home/doc/ru/concepts/platform-device-info

    public static Context providerUserDevicesRun(Context context) {
        String xRequestId = context.requestHeaders.getFirst("X-request-id");
        log.info("XREQUESTID: " + xRequestId);
        log.info("LOCAL DEVICES " + SmartHome.devices.size());
        log.info("LOCAL ROOMS " + SmartHome.devices.stream().map(device -> device.room).collect(Collectors.toList()));
        log.info("LOCAL IDS " + SmartHome.devices.stream().map(device -> device.id).collect(Collectors.toList()));
        log.info("LOCAL EXTIDS " + SmartHome.devices.stream().map(device -> device.external_id).collect(Collectors.toList()));

        ResponseYandex responseYandex = new ResponseYandex();
        responseYandex.request_id = xRequestId;
        responseYandex.payload = new Payload();
        responseYandex.payload.user_id = config.yandexUid;
        SmartHome.devices.forEach(device -> device.capabilities.forEach(capability -> capability.state = null));

        List<Device> devicesNew = SmartHome.devices.stream() // отфильтровать девайсы которые уже были получены от яндекса и не отправлять их как новые устройства
                .filter(device ->  !YandexUtils.checkContainsByRoom(device.room))
                .collect(Collectors.toList());

        log.info("DEVICES NEW FOR YANDEX" + devicesNew.stream().map(device -> device.room).collect(Collectors.toList()));

        responseYandex.payload.devices = devicesNew;
//        responseYandex.payload.devices = SmartHome.devices;

        SmartHome.devices.removeAll(devicesNew); // удалить временные устроййства, потом получить их снова от яндекс с новыми id


        String json = JsonUtils.pojoToJson(responseYandex);
        log.info("USER: " + config.yandexUid);
        log.info("JSON: " + json);

        json = json.replace("\"true\"", "true");
        json = json.replace("\"false\"", "false");

        context.bodyResponse = json;
        context.code = 200;
        return context;
    }
}