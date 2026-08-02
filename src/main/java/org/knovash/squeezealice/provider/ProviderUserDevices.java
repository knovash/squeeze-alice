package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.provider.response.Payload;
import org.knovash.squeezealice.provider.response.ResponseYandex;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class ProviderUserDevices {
//    не передавать external_id !!! если его передать яндекс сделает свои id !!!
//    https://yandex.ru/dev/dialogs/smart-home/doc/ru/reference/get-devices-jrpc
//    Объект payload
//    user_id   String              Идентификатор пользователя.
//    devices   Array of objects    Массив с устройствами пользователя.
//    Объект devices
//    id        String              Идентификатор устройства. Должен быть уникален среди всех устройств производителя.
//    name      String              Название устройства.
//    room      String              Название помещения, в котором расположено устройство.
//    external_id используется только тут:
//    GET https://api.iot.yandex.net/v1.0/user/info             https://yandex.ru/dev/dialogs/smart-home/doc/ru/concepts/platform-user-info
//    GET https://api.iot.yandex.net/v1.0/devices/{device_id}   https://yandex.ru/dev/dialogs/smart-home/doc/ru/concepts/platform-device-info

    public static Context providerUserDevicesRun(Context context) {
        log.info(Main.line);
        String xRequestId = context.requestHeaders.getFirst("X-request-id");
        log.info("XREQUESTID: " + xRequestId);
        log.info("LOCAL DEVICES " + smartHome.devices.size());
        List<String> rooms = smartHome.devices.stream().map(device -> device.room).collect(Collectors.toList());
        log.info("LOCAL ROOMS " + rooms);
        log.info("LOCAL IDS " + smartHome.devices.stream().map(device -> device.id).collect(Collectors.toList()));

        ResponseYandex responseYandex = new ResponseYandex();
        responseYandex.request_id = xRequestId;
        responseYandex.payload = new Payload();
        responseYandex.payload.user_id = config.yandexUid;


        // если девайсы локальные плееров не созданы - создать для каждого плеера
        if (smartHome.devices == null || smartHome.devices.isEmpty()) {
            log.info("CREATE ALL PLAYERS DEVICES FOR YANDEX");
            lmsPlayers.players.stream()
                    .filter(p -> p.room != null)
                    .forEach(p -> smartHome.create(p.room, null));
        }

        // создать девайсы кнопок команд
        log.info("CREATE TOGGLS IN ALL ROOMS " + rooms);
        smartHome.createButtonsInAllRooms(rooms);

        smartHome.devices.forEach(device -> device.capabilities.forEach(capability -> capability.state = null));
        responseYandex.payload.devices = smartHome.devices;
        String json = JsonUtils.pojoToJson(responseYandex);
        log.info("USER: " + config.yandexUid);
        log.info("JSON: " + json);

        json = json.replace("\"true\"", "true");
        json = json.replace("\"false\"", "false");

        context.bodyResponse = json;
        context.code = 200;
        log.info(Main.finish);
        log.info(Main.line);
        return context;
    }
}