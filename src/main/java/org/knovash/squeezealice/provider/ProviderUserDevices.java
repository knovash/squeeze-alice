package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Payload;
import org.knovash.squeezealice.provider.response.ResponseYandex;
import org.knovash.squeezealice.utils.JsonUtils;

@Log4j2
public class ProviderUserDevices {

    public static Context action(Context context) {
        log.info("");
        String xRequestId = context.xRequestId;
        log.info("XREQUESTID: " + xRequestId);
        ResponseYandex responseYandex = new ResponseYandex();
        responseYandex.request_id = xRequestId;
        responseYandex.payload = new Payload();
//        responseYandex.payload.user_id = SmartHome.user_id;
        responseYandex.payload.user_id = Yandex.yandex.user;
        responseYandex.payload.devices = SmartHome.devices;
        String json = JsonUtils.pojoToJson(responseYandex);

        json = json.replace("\"true\"","true");
        json = json.replace("\"false\"","false");

        context.json = json;
        context.code = 200;
        return context;
    }
}


