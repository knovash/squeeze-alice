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
//        log.info("START QUERY");
        String body = context.body;
        List<String> xRequestIdList = context.headers.get("X-request-id");
        String xRequestId = xRequestIdList.get(0);
        log.info("XREQUESTID: " + xRequestId);
        log.info("BODY: " + body);

        Payload bodyPojo = JsonUtils.jsonToPojo(body, Payload.class);
        String json;
//   если чтото пошло не так вернуть пустой список устройств
//            json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[]}}";
        if (SmartHome.devices.size() == 0) log.info("ERROR - no registered LMS players in Alice home");
        ResponseYandex responseYandex = new ResponseYandex();
        responseYandex.request_id = xRequestId;

        lmsPlayers.updateLmsPlayers(); // providerQueryRun TODO тут апдейт нужен только для обновления списка подключенных плееров


// лист девайсов для обновления их свойств
        List<Device> jsonDevices = bodyPojo.devices.stream()
                .map(d -> SmartHome.getDeviceById(Integer.parseInt(d.id)))
// обратиться к каждому девайсу и обновить его свойства
                .map(d -> updateDeviceCapabilities(d)).collect(Collectors.toList());
        responseYandex.payload = new Payload();
        responseYandex.payload.devices = jsonDevices;
// объект ответа преобразовать в json ответа
        json = JsonUtils.pojoToJson(responseYandex);
        json = json.replaceAll("(\"value\" :) \"([0-9a-z]+)\"", "$1 $2");
        context.bodyResponse = json;
        context.code = 200;
        log.info("RETURN CONTEXT");
        return context;
    }

    private static Device updateDeviceCapabilities(Device device) {
        log.info("DEVICE UPDATE " + device.room + " " + device.id);
// взять плеер по id устройства в умном доме
        player = lmsPlayers.playerByDeviceId(device.id);

// https://yandex.ru/dev/dialogs/smart-home/doc/ru/concepts/response-codes#codes-api
// требование яндекс при модерации навыка, показывать устройство недоступно

// если плеер не существует - вернуть устройство с ошибкой
        if (player == null) {
            log.info("DEVICE_UNREACHABLE player = null");
            device.error_code = "DEVICE_UNREACHABLE";
            device.error_message = "Устройство потеряно";
            log.info("DEVICE UPDATED");
            return device;
        }

        if (!player.connected) { // updateDevice
            log.info("DEVICE_UNREACHABLE mode real = null");
            device.error_code = "DEVICE_UNREACHABLE";
            device.error_message = "Устройство потеряно";
            log.info("DEVICE UPDATED");
            return device;
        }

// если плеер существует и отвечает - обратиться к плееру и обновить все его значения
        device.error_code = null;
        device.error_message = null;
        device.capabilities.forEach(capability -> changeCapability(capability));
        return device;
    }

    private static void changeCapability(Capability capability) {
//        log.info("CAPABILITY UPDATE");
//        providerUserDevicesRun при Поиске новых устройств state = null
        if (capability.state == null) {
            capability.state = new State();
            capability.state.instance = capability.parameters.instance;
            capability.state.value = null;
            capability.state.relative = false;
            capability.state.action_result = new ActionResult();
            capability.state.action_result.error_code = "";
            capability.state.action_result.error_message = "";
            capability.state.action_result.status = "DONE";
        }

//        log.info("UPDATE CAPABILITY : " + capability.parameters.instance);
//        Boolean power = false;
        switch (capability.parameters.instance) {
            case ("volume"):
//                capability.state.value = player.volumeGet();
                capability.state.value = player.volume;
                log.info("UPDATE CAPABILITY : " + capability.parameters.instance + " VALUE: " + capability.state.value + " PLAYER: " + player.name);
                break;
            case ("on"):
//                if (player.mode().equals("play")) power = true;
//                capability.state.value = String.valueOf(power);
                capability.state.value = String.valueOf(player.playing);
                log.info("UPDATE CAPABILITY : " + capability.parameters.instance + " VALUE: " + capability.state.value + " PLAYER: " + player.name);
                break;
            case ("channel"):
                capability.state.value = "1";
                log.info("UPDATE CAPABILITY : " + capability.parameters.instance + " VALUE: " + capability.state.value + " PLAYER: " + player.name);
                break;
//            case ("pause"):
//                if (player.mode().equals("play")) power = true;
//                capability.state.value = String.valueOf(power);
//                break;
            default:
                log.info("ERROR CAPABILITY NOT FOUND");
                break;
        }
    }
}


// пример ответа
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
