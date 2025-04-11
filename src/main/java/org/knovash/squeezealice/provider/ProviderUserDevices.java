package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Payload;
import org.knovash.squeezealice.provider.response.ResponseYandex;
import org.knovash.squeezealice.utils.JsonUtils;
import static org.knovash.squeezealice.Main.config;

@Log4j2
public class ProviderUserDevices {

    public static Context providerUserDevicesRun(Context context) {
        log.info("USER DEVICES " + SmartHome.devices.size());



        String xRequestId = context.headers.getFirst("X-request-id");
        log.info("XREQUESTID: " + xRequestId);
//        lmsPlayers.updateServerStatus();
//        log.info("USER DEVICES " + SmartHome.devices);
//        String xRequestId = context.xRequestId;
        log.info("XREQUESTID: " + xRequestId);
        ResponseYandex responseYandex = new ResponseYandex();
        responseYandex.request_id = xRequestId;
        responseYandex.payload = new Payload();
        responseYandex.payload.user_id = config.yandexUid;
        SmartHome.devices.forEach(device -> device.capabilities.forEach(capability -> capability.state = null));
//        если State не null то при обновление устройств в УДЯ будет ошибка
        responseYandex.payload.devices = SmartHome.devices;
        String json = JsonUtils.pojoToJson(responseYandex);
        log.info("USER: " + config.yandexUid);
        log.info("DEVICES: " + SmartHome.devices.size());
//        log.info("JSON: " + json);

        json = json.replace("\"true\"", "true");
        json = json.replace("\"false\"", "false");

        context.bodyResponse = json;
        context.code = 200;
        return context;
    }
}


