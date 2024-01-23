package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.provider.response.*;
import org.knovash.squeezealice.utils.JsonUtils;

@Log4j2
public class ProviderAction {

    public static Context action(Context context) {
        String body = context.body;
        String xRequestId = context.xRequestId;

        Response response = JsonUtils.jsonToPojo(body, Response.class);
        response.request_id = xRequestId;
        response.payload.devices.stream().forEach(d -> deviseSetResult(d));
        String json = JsonUtils.pojoToJson(response);

        //TODO make after response
        log.info("DEVICES: " + response.payload.devices.size());
        response.payload.devices.forEach(device -> DeviceActions.runInstance(device));

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