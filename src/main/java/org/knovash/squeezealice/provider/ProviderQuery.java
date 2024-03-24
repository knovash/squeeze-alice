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


    public static Context action(Context context) {
        String body = context.body;
        String xRequestId = context.xRequestId;

        Payload bodyPojo = JsonUtils.jsonToPojo(body, Payload.class);
        String json;
        if (body == null) {
            json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[]}}";
        } else {
            if (SmartHome.devices.size() == 0) log.info("ERROR - no registered LMS players in Alice home");
            ResponseYandex responseYandex = new ResponseYandex();
            responseYandex.request_id = xRequestId;
            List<Device> jsonDevices = bodyPojo.devices.stream().map(d -> updateDevice(Integer.valueOf(d.id))).collect(Collectors.toList());
            responseYandex.payload = new Payload();
            responseYandex.payload.devices = jsonDevices;
            json = JsonUtils.pojoToJson(responseYandex);
            json = json.replaceAll("(\"value\" :) \"([0-9a-z]+)\"", "$1 $2");
        }

        context.json = json;
        context.code = 200;
        return context;
    }

    public static Device updateDevice(Integer device_id) {
        // обратиться к девайсу и обновить все его значения
        player = lmsPlayers.getPlayerByName(SmartHome.getDeviceById(device_id).customData.lmsName);
        Device device = SmartHome.getDeviceById(device_id);
        device.capabilities.forEach(capability -> changeCapability(capability));
        log.info("DEVICE UPDATED\n" + device);
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
}