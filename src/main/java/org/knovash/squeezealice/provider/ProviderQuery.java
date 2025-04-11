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
//        String xRequestId = context.headers.getFirst("X-request-id");

        List<String> xRequestIdList = context.headers.get("X-request-id");
        String xRequestId = xRequestIdList.get(0);

        log.info("XREQUESTID: " + xRequestId);

        Payload bodyPojo = JsonUtils.jsonToPojo(body, Payload.class);
        String json;
//        if (body.equals(null) || body.equals("")) {
//            log.info("BODY NULL");
//            json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[]}}";
//            context.bodyResponse = json;
//            context.code = 200;
//            return context;
//        }

//        Payload bodyPojo = JsonUtils.jsonToPojo(body, Payload.class);
        log.info("111");
        if (SmartHome.devices.size() == 0) log.info("ERROR - no registered LMS players in Alice home");
        log.info("222");
        ResponseYandex responseYandex = new ResponseYandex();
        log.info("333");
        responseYandex.request_id = xRequestId;
        log.info("4444");

        List<Device> jsonDevices = bodyPojo.devices.stream()
                .map(d -> SmartHome.getDeviceById(Integer.parseInt(d.id)))
                .map(d -> updateDevice(d)).collect(Collectors.toList());
        responseYandex.payload = new Payload();
        responseYandex.payload.devices = jsonDevices;
        json = JsonUtils.pojoToJson(responseYandex);
        json = json.replaceAll("(\"value\" :) \"([0-9a-z]+)\"", "$1 $2");
//        log.info("RESPONSE JSON: " + json);


        log.info("555");
        String id = "3";
        id = bodyPojo.devices.get(0).id;
        log.info("ID: " + id);


//        json = "{\n" +
//                "  \"request_id\": \"" + xRequestId + "\",\n" +
//                "  \"payload\": {\n" +
//                "    \"devices\": [\n" +
//                "      {\n" +
//                "        \"id\": \"" + id + "\",\n" +
//                "        \"name\": \"музыка\",\n" +
//                "        \"room\": \"Душ\",\n" +
//                "        \"type\": \"devices.types.media_device.receiver\",\n" +
//                "        \"capabilities\": [],\n" +
//                "        \"properties\": [],\n" +
//                "        \"error_code\": \"DEVICE_UNREACHABLE\",\n" +
//                "        \"error_message\": \"Устройство потеряно\"\n" +
//                "      }\n" +
//                "    ]\n" +
//                "  }\n" +
//                "}";


        log.info("JSON: " + json);

        context.bodyResponse = json;
        context.code = 200;
        return context;
    }

    private static Device updateDevice(Device device) {
        log.info("DEVICE UPDATE " + device.room + " " + device.id);
        // обратиться к девайсу и обновить все его значения
        player = lmsPlayers.getPlayerByDeviceId(device.id);

//        узнать реальное состояние устройства, если lms не ответит на запрос то null
        String modeReal = player.modeReal();
        log.info("PLAYER REAL MODE: " + modeReal);
        log.info("DEVICE PLAYER: " + player);

        if (player == null || modeReal == null) {
            log.info("DEVICE_UNREACHABLE");
//            https://yandex.ru/dev/dialogs/smart-home/doc/ru/concepts/response-codes#codes-api
//  требование яндекс при модерации навыка, показывать устройство недоступно
            device.error_code = "DEVICE_UNREACHABLE";
            device.error_message = "Устройство потеряно";
//            device.capabilities.forEach(capability -> changeCapabilityOff(capability));
        } else {

            device.error_code = null;
            device.error_message = null;
            device.capabilities.forEach(capability -> changeCapability(capability));

        }
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
//    но по требованиям яндекс надо показывать не в сети
//    error_code = DEVICE_UNREACHABLE
//error_message = Устройство не отвечает. Проверьте, вдруг оно выключено или пропал интернет.

//    МЕТОД НЕАКТУАЛЕН заменено ответом DEVICE_UNREACHABLE

    private static void changeCapabilityOff(Capability capability) {
        log.info("CAPABILITY OFF -------------------------- " + capability);
        capability.state = new State();
        capability.state.instance = capability.parameters.instance;
//        capability.state.action_result.error_code = "DEVICE_UNREACHABLE";
//        capability.state.action_result.error_message = "Устройство не отвечает. Проверьте, вдруг оно выключено или пропал интернет.";
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