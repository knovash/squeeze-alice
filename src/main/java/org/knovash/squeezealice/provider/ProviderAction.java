package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Actions;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.provider.response.*;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.concurrent.CompletableFuture;

import static org.knovash.squeezealice.Main.lmsIp;
import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class ProviderAction {

    public static Context action(Context context) {
        log.info("START");
        if (lmsIp == null) return null;
        String body = context.body;
        String xRequestId = context.xRequestId;
        ResponseYandex responseYandex = JsonUtils.jsonToPojo(body, ResponseYandex.class);
        responseYandex.request_id = xRequestId;
        responseYandex.payload.devices.stream().forEach(d -> deviseSetResult(d));
        String json = JsonUtils.pojoToJson(responseYandex);
        log.info("RUN INSTANCE FOR DEVICES IN PAYLOAD: " + responseYandex.payload.devices.size());
        responseYandex.payload.devices.forEach(device -> runInstance(device));
//        если
        context.json = json;
        context.code = 200;
        return context;
    }

    public static Device deviseSetResult(Device device) {
        device.capabilities.get(0).state.action_result = new ActionResult();
        device.capabilities.get(0).state.action_result.status = "DONE";
        return device;
    }

    public static void runInstance(Device device) {
        log.info("START");
        String id = device.id;
        String type = device.capabilities.get(0).type;
        String instance = device.capabilities.get(0).state.instance;
        String value = device.capabilities.get(0).state.value;
        Boolean relative = device.capabilities.get(0).state.relative;
        log.info("ID: " + id + " INSTANCE: " + instance + " VALUE: " + value + " RELATIVE: " + relative);

        // обратиться к девайсу и изменить его состояние
//        Player player = device.lmsGetPlayerByDeviceId();
        Player player = lmsPlayers.getPlayerByDeviceId(id);
        if (player == null) return;
        switch (instance) {
            case ("volume"):
                if (relative != null && relative.equals(true)) {
                    log.info("VOLUME relative: " + value);
                    if (value.contains("-")) {
                        player.volumeSet(value);
                    } else {
                        player.volumeSet("+" + value);
                    }
                }
                if (relative != null && relative.equals(false)) {
                    log.info("VOLUME absolute: " + value);
                    player.volumeSet(value);
                }

//                SPOTIFY
                if (!player.ifPlaying()) {

                    log.info("PLAYER NOT PLAYING");
                    if (Spotify.ifPlaying()) {
                        log.info("SPOTIFY IF PLAYING");
                        if (relative != null && relative.equals(true)) {
                            log.info("VOLUME rel: " + value);
                            if (value.contains("-")) {
                                Spotify.volumeRelOrAbs(value);
                            } else {
                                Spotify.volumeRelOrAbs("+" + value);
                            }
                        }
                        if (relative != null && relative.equals(false)) {
                            log.info("VOLUME abs: " + value);
                            Spotify.volumeRelOrAbs(value);
                        } else {
                            log.info("PLAYER PLAYING. SPOTY VOLUME SKIP");
                        }
                    }
                }
                break;
            case ("channel"):
//                log.info("CHANNEL: " + value + " RELATIVE: " + relative + " LAST CHANNEL: " + lmsPlayers.lastChannel);
                int channel;
                if (relative != null && relative.equals(true)) {
                    if (player.lastChannel != 0) channel = player.lastChannel + 1;
                    else channel = lmsPlayers.lastChannel + 1;
                } else {
                    channel = Integer.parseInt(value);
                }
                CompletableFuture.supplyAsync(() -> {
                    player.playChannel(channel);
                    return "";
                });
                lmsPlayers.lastChannel = channel;
                break;
            case ("on"):
                log.info("ON/OFF PLAY/PAUSE " + value);
                if (value.equals("true")) {
                    log.info("TURN OFF");
                    CompletableFuture.supplyAsync(() -> {
                        Actions.turnOnMusic(player);
                        return "";
                    });
                }
                if (value.equals("false")) {
                    log.info("TURN OFF");
                    CompletableFuture.supplyAsync(() -> {
                        Actions.turnOffMusic(player);
                        return "";
                    });
                }

//                SPOTIFY
                if (value.equals("false")) {
                    log.info("SPOTIFY TURN OFF");
                    CompletableFuture.supplyAsync(() -> {
                        Spotify.pause();
                        return "";
                    });
                }
                break;
        }
    }
}