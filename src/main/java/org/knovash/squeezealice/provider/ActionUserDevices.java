package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.pojoUserDevices.Payload;
import org.knovash.squeezealice.provider.pojoUserDevices.Response;
import org.knovash.squeezealice.utils.JsonUtils;

@Log4j2
public class ActionUserDevices {

    public static Context action(Context context) {
        log.info("");
        String xRequestId = context.xRequestId;

        Response response = new Response();
        response.request_id = xRequestId;
        response.payload = new Payload();
        response.payload.user_id = SmartHome.user_id;
        response.payload.devices = SmartHome.devices;
        String json = JsonUtils.pojoToJson(response);

        context.json = json;
        context.code = 200;
        return context;
    }
}