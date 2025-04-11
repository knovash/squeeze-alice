package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.*;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class ProviderAction {

    public static Context providerActionRun(Context context) {
        log.info("ACTION START");
//        log.info("CONTEXT: " + context);
        if (config.lmsIp == null) return null;
        String body = context.body;
        String xRequestId = context.headers.get("X-request-id").get(0);
        log.info("XREQUESTID: " + xRequestId);

        if (body.equals("") || body.equals(null)) {
            log.info("BODY NULL");
//            json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[]}}";
//            context.bodyResponse = json;
            context.code = 200;
            return context;
        }

        ResponseYandex responseYandex = JsonUtils.jsonToPojo(body, ResponseYandex.class);
        responseYandex.request_id = xRequestId;
//        responseYandex.payload.devices.forEach(d -> deviseSetResult(d));
        String json = JsonUtils.pojoToJson(responseYandex);
        log.info("DEVICES IN PAYLOAD: " + responseYandex.payload.devices.size());
        List<Device> jsonDevices = responseYandex.payload.devices.stream()
                .map(d -> runInstance(d))
                .collect(Collectors.toList());

        log.info("LIST DEVICES: " + jsonDevices);

        responseYandex.payload.devices = jsonDevices;
        json = JsonUtils.pojoToJson(responseYandex);


        context.bodyResponse = json;
        context.code = 200;
        log.info("FINISH ACTION");
        return context;
    }

    private static Device runInstance(Device device) {
        log.info("RUN INSTANCE START");
        device.capabilities.get(0).state.action_result = new ActionResult();
        device.capabilities.get(0).state.action_result.status = "DONE";
        String id = device.id;
        String instance = device.capabilities.get(0).state.instance;
        String value = device.capabilities.get(0).state.value;
        Boolean relative = device.capabilities.get(0).state.relative;
//        device.capabilities.get(0).reportable = true;
        log.info("DEVICE ID: " + id + " INSTANCE: " + instance + " VALUE: " + value + " RELATIVE: " + relative);
        Player player = lmsPlayers.getPlayerByDeviceId(id);
        if (player != null && player.modeReal() != null) {
            device.actionResult = new ActionResult();
            device.actionResult.status = "ERROR";
            device.actionResult.error_code = "DEVICE_UNREACHABLE";
            device.actionResult.error_message = "Устройство потеряно";
            log.info("ACTION RESULT DONE");
        } else {
            device.actionResult = new ActionResult();
            device.capabilities = null;
            device.properties = null;
            device.actionResult.status = "DONE";
            device.actionResult.error_code = null;
            device.actionResult.error_message = null;
            log.info("ACTION RESULT ERROR");
            return device;
        }
        switch (instance) {
            case ("volume"):
                CompletableFuture.runAsync(() -> player.volumeRelativeOrAbsolute(value, relative));
                break;
            case ("channel"):

                CompletableFuture.runAsync(() -> player.playChannelRelativeOrAbsolute(value, relative));
                break;
            case ("on"):
                if (value.equals("true"))
                    CompletableFuture.runAsync(() -> player.turnOnMusic().syncAllOtherPlayingToThis());
                if (value.equals("false")) CompletableFuture.runAsync(player::turnOffMusic);
//                if (value.equals("false")) CompletableFuture.runAsync(Spotify::pause);
                break;
        }
        return device;
    }
}