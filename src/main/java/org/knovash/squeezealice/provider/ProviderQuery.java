package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.*;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class ProviderQuery {

    private static Player player;

    public static Context providerQueryRun(Context context) {
        log.info("START CONTEXT: " + context);
        String body = context.body;
        String xRequestId = context.xRequestId;

//        Payload bodyPojo = JsonUtils.jsonToPojo(body, Payload.class);
        String json;
        if (body.equals(null) || body.equals("")) {
            log.info("BODY NULL");
            json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[]}}";
            context.bodyResponse = json;
            context.code = 200;
            return context;

        } else {
            Payload bodyPojo = JsonUtils.jsonToPojo(body, Payload.class);
            if (SmartHome.devices.size() == 0) log.info("ERROR - no registered LMS players in Alice home");
            ResponseYandex responseYandex = new ResponseYandex();
            responseYandex.request_id = xRequestId;
            List<Device> jsonDevices = bodyPojo.devices.stream()
                    .map(d -> SmartHome.getDeviceById(Integer.parseInt(d.id)))
                    .map(d -> updateDevice(d)).collect(Collectors.toList());
            responseYandex.payload = new Payload();
            responseYandex.payload.devices = jsonDevices;
            json = JsonUtils.pojoToJson(responseYandex);
            json = json.replaceAll("(\"value\" :) \"([0-9a-z]+)\"", "$1 $2");
        }

        context.bodyResponse = json;
        context.code = 200;
        return context;
    }

    private static Device updateDevice(Device device) {
        log.info("DEVICE UPDATE " + device.room + " " + device.id);
        // обратиться к девайсу и обновить все его значения
        player = lmsPlayers.getPlayerByDeviceId(device.id);
        log.info("DEVICE PLAYER " + player);
        if (player != null) device.capabilities.forEach(capability -> changeCapability(capability));
        else device.capabilities.forEach(capability -> changeCapabilityOff(capability));
        log.info("DEVICE UPDATED");
        return device;
    }

    private static void changeCapability(Capability capability) {
        log.info("CAPABILITY " + capability);
        capability.state = new State();
        capability.state.instance = capability.parameters.instance;
        capability.state.action_result.error_code = "";
        capability.state.action_result.error_message = "";
        capability.state.value = "";
        capability.reportable = true;
        Boolean power = false;
        switch (capability.parameters.instance) {
            case ("volume"):
                capability.state.value = player.volumeGet();
                break;
            case ("on"):
                if (player.mode().equals("play")) power = true;
                capability.state.value = String.valueOf(power);
                break;
            case ("channel"):
                capability.state.value = "2";
                break;
            case ("pause"):
                if (player.mode().equals("play")) power = true;
                capability.state.value = String.valueOf(power);
                break;
            default:
                log.info("ACTION NOT FOUND: ");
                break;
        }
    }

//    если плеер для комнаты не выбран. в УДЯ показывать выключенный плеер
    private static void changeCapabilityOff(Capability capability) {
        log.info("CAPABILITY " + capability);
        capability.state = new State();
        capability.state.instance = capability.parameters.instance;
        capability.state.action_result.error_code = "";
        capability.state.action_result.error_message = "";
        capability.state.value = "";
        capability.reportable = true;
        switch (capability.parameters.instance) {
            case ("volume"):
                capability.state.value = "1";
                break;
            case ("on"):
                capability.state.value = String.valueOf(false);
                break;
            case ("channel"):
                capability.state.value = "1";
                break;
            case ("pause"):
                capability.state.value = String.valueOf(true);
                break;
            default:
                log.info("ACTION NOT FOUND: ");
                break;
        }
    }
}