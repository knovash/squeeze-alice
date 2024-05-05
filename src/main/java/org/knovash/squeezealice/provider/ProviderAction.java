package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.provider.response.*;
import org.knovash.squeezealice.utils.JsonUtils;

@Log4j2
public class ProviderAction {

    public static Context action(Context context) {
        log.info("START " + context);
        String body = context.body;
        String xRequestId = context.xRequestId;
        ResponseYandex responseYandex = JsonUtils.jsonToPojo(body, ResponseYandex.class);
        responseYandex.request_id = xRequestId;
        responseYandex.payload.devices.stream().forEach(d -> deviseSetResult(d));
        String json = JsonUtils.pojoToJson(responseYandex);
        log.info("DEVICES COUNT IN PAYLOAD: " + responseYandex.payload.devices.size());
        responseYandex.payload.devices.forEach(device -> DeviceActions.runInstance(device));
        context.json = json;
        context.code = 200;
        return context;
    }

    public static Device deviseSetResult(Device device) {
        device.capabilities.get(0).state.action_result = new ActionResult();
        device.capabilities.get(0).state.action_result.status = "DONE";
        return device;
    }
}