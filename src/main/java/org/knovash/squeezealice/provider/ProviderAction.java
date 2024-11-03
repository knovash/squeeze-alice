package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Actions;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.provider.response.*;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.concurrent.CompletableFuture;

import static org.knovash.squeezealice.Main.lmsIp;
import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class ProviderAction {

    public static Context action(Context context) {
        if (lmsIp == null) return null;
        String body = context.body;
        String xRequestId = context.xRequestId;
        ResponseYandex responseYandex = JsonUtils.jsonToPojo(body, ResponseYandex.class);
        responseYandex.request_id = xRequestId;
//        responseYandex.payload.devices.forEach(d -> deviseSetResult(d));
        String json = JsonUtils.pojoToJson(responseYandex);
        log.info("RUN INSTANCE FOR DEVICES: " + responseYandex.payload.devices.size());
        responseYandex.payload.devices.forEach(d -> runInstance(d));
        context.json = json;
        context.code = 200;
        return context;
    }

//    public static void deviseSetResult(Device device) {
//        device.capabilities.get(0).state.action_result = new ActionResult();
//        device.capabilities.get(0).state.action_result.status = "DONE";
//    }

    public static void runInstance(Device device) {
        device.capabilities.get(0).state.action_result = new ActionResult();
        device.capabilities.get(0).state.action_result.status = "DONE";
        String id = device.id;
        String instance = device.capabilities.get(0).state.instance;
        String value = device.capabilities.get(0).state.value;
        Boolean relative = device.capabilities.get(0).state.relative;
        log.info("ID: " + id + " INSTANCE: " + instance + " VALUE: " + value + " RELATIVE: " + relative);
        log.error("ID: " + id + " INSTANCE: " + instance + " VALUE: " + value + " RELATIVE: " + relative);
        Player player = lmsPlayers.getPlayerByDeviceId(id);
        if (player == null) return;
        switch (instance) {
            case ("volume"):
                CompletableFuture.runAsync(() -> player.volumeRelativeOrAbsolute(value, relative));
                break;
            case ("channel"):
//                CompletableFuture.runAsync(() -> Actions.providerChannelPlay(player, value, relative));
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