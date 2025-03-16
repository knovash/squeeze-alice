package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.provider.response.*;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.concurrent.CompletableFuture;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class ProviderAction {

    public static Context providerActionRun(Context context) {
        log.debug("ACTION START CONTEXT: " + context);
        if (config.lmsIp == null) return null;
        String body = context.body;

        if (body.equals("")|| body.equals(null)){
            log.info("BODY NULL");
//            json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[]}}";
//            context.bodyResponse = json;
            context.code = 200;
            return context;
        }

        String xRequestId = context.xRequestId;
        ResponseYandex responseYandex = JsonUtils.jsonToPojo(body, ResponseYandex.class);
        responseYandex.request_id = xRequestId;
//        responseYandex.payload.devices.forEach(d -> deviseSetResult(d));
        String json = JsonUtils.pojoToJson(responseYandex);
        log.info("DEVICES IN PAYLOAD: " + responseYandex.payload.devices.size());
        responseYandex.payload.devices.forEach(d -> runInstance(d));
        context.bodyResponse = json;
        context.code = 200;
        return context;
    }

    private static void runInstance(Device device) {
        device.capabilities.get(0).state.action_result = new ActionResult();
        device.capabilities.get(0).state.action_result.status = "DONE";
        String id = device.id;
        String instance = device.capabilities.get(0).state.instance;
        String value = device.capabilities.get(0).state.value;
        Boolean relative = device.capabilities.get(0).state.relative;
        log.info("DEVICE ID: " + id + " INSTANCE: " + instance + " VALUE: " + value + " RELATIVE: " + relative);
        Player player = lmsPlayers.getPlayerByDeviceId(id);
        if (player == null) return;
        switch (instance) {
            case ("volume"):
                CompletableFuture.runAsync(() -> player.volumeRelativeOrAbsolute(value, relative));
                break;
            case ("channel"):
                CompletableFuture.runAsync(() -> player.playChannelRelativeOrAbsolute(value, relative));
                break;
            case ("on"):
                if (value.equals("true")) CompletableFuture.runAsync(() -> player.turnOnMusic().syncAllOtherPlayingToThis());
                if (value.equals("false")) CompletableFuture.runAsync(player::turnOffMusic);
//                if (value.equals("false")) CompletableFuture.runAsync(Spotify::pause);
                break;
        }
    }
}