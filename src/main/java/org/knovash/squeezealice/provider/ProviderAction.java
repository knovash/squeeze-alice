package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.provider.response.*;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class ProviderAction {

    public static String channelValueByProvider = null;
    public static Boolean channelRelativeByProvider = null;
    public static String onValueByProvider = null;
    public static Boolean volumeRelativeByprovider = null;
    public static String volumeByProvider = null;

    public static Context providerActionRun(Context context) {
        log.info("ACTION START");
        channelValueByProvider = null;
        channelRelativeByProvider = null;
        onValueByProvider = null;
        volumeRelativeByprovider = null;
        volumeByProvider = null;
//        volumelValue = null;

//        log.info("CONTEXT: " + context);
        if (config.lmsIp == null) return null;
//        получить из контекста тело запроса от умного дома
        String body = context.body;
//        получить из хедеров запроса id запроса
        String xRequestId = context.headers.get("X-request-id").get(0);
//        log.info("XREQUESTID: " + xRequestId);
        if (body.equals("") || body.equals(null)) {
            log.info("BODY NULL");
//            json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[]}}";
//            context.bodyResponse = json;
            context.code = 200;
            return context;
        }
//        создать ответ из запроса
        ResponseYandex responseYandex = JsonUtils.jsonToPojo(body, ResponseYandex.class);
//        положить в ответ id запроса полученый из хедеров запроса
        responseYandex.request_id = xRequestId;
        log.info("DEVICES IN PAYLOAD: " + responseYandex.payload.devices.size());
        List<Device> jsonDevices = new ArrayList<>();

        log.info("DEVICES & CAPABILITIES");


        responseYandex.payload.devices.stream()
                .forEach(d -> d.capabilities.stream()
                        .forEach(c -> log.info("ID " + d.id + " CAPABILITI: " +
                                c.state.instance + " = " +
                                c.state.value +
                                " REPORTABLE=" + c.reportable +
                                " RETRIEVABLE=" + c.retrievable
                        )));

//        responseYandex.payload.devices.stream()
//                .forEach(d -> d.capabilities.stream()
//                        .forEach(capability -> capability.reportable = true)
//                );


// выбрать из запроса девайсы которые надо включить
        List<Device> devicesForTurnOn = responseYandex.payload.devices.stream()
                .filter(device -> device.capabilities != null)
                .filter(device -> device.capabilities.stream()
                        .anyMatch(capability ->
                                "on".equals(capability.state.instance) &&
                                        "true".equals(capability.state.value))
                )
                .peek(device -> log.info("FOR TURN ON DEVICE ID: " + device.id))
                .collect(Collectors.toList());
// выбрать из запроса девайсы которые надо вЫключить
        List<Device> devicesForTurnOff = responseYandex.payload.devices.stream()
                .filter(device -> device.capabilities != null)
                .filter(device -> device.capabilities.stream()
                        .anyMatch(capability ->
                                "on".equals(capability.state.instance) &&
                                        "false".equals(capability.state.value))
                )
                .peek(device -> log.info("FOR TURN OFF DEVICE ID: " + device.id))
                .collect(Collectors.toList());

//  если пришел запрос включи музыку на нескольких устройствах
        if (devicesForTurnOn.size() > 1
        ) {
            log.info("TURN ON MULTIPLE DEVICES: " + devicesForTurnOn.size());
// выполнить паралельно пробуждение и запуск всех плееров в умном доме
            CompletableFuture.runAsync(() -> lmsPlayers.turnOnMusicMultiply(devicesForTurnOn));
// создать пэйлоад для ответа в умный дом
            jsonDevices = responseYandex.payload.devices;
// вернуть контекст
            responseYandex.payload.devices = jsonDevices;
            context.bodyResponse = JsonUtils.pojoToJson(responseYandex);
            context.code = 200;
            log.info("FINISH ACTION");
            return context;
        }

//  если пришел запрос вЫключи музыку на нескольких устройствах
        if (devicesForTurnOff.size() > 1
        ) {
            log.info("TURN OFF MULTIPLE DEVICES: " + devicesForTurnOn.size());
// выполнить паралельно пробуждение и запуск всех плееров в умном доме
//            CompletableFuture.runAsync(() -> lmsPlayers.turnOffMusicAll());
            lmsPlayers.turnOffMusicAll();
// создать пэйлоад для ответа в умный дом
            jsonDevices = responseYandex.payload.devices;
// вернуть контекст
            responseYandex.payload.devices = jsonDevices;
            context.bodyResponse = JsonUtils.pojoToJson(responseYandex);
            context.code = 200;
            log.info("FINISH ACTION");
            return context;
        }

        log.info("RUN INSTANCE ON DEVICES: " + responseYandex.payload.devices.size());

//   выполнить действие с каждым устройством и создать лист устройств с измененными свойствами
        jsonDevices = responseYandex.payload.devices.stream()
                .map(d -> runInstance(d)) // TODO сделать паралельно
                .collect(Collectors.toList());
        // вернуть контекст
        responseYandex.payload.devices = jsonDevices;
        log.info("RETURB DEVICES: " + jsonDevices);
        context.bodyResponse = JsonUtils.pojoToJson(responseYandex);
        context.code = 200;
        log.info("FINISH ACTION");
        return context;


//        только после выполнения действия со всеми устройствами запрашивать обновление виджетов
//        удалить обновление виджетов из runInstance
//        CompletableFuture.runAsync(() -> Requests.autoRemoteRefresh()); // только после выполнения действия со всеми устройствами
//        log.info("LIST DEVICES: " + jsonDevices);
//        положить в ответ лист устройств с измененными свойствами
    }

    private static Device runInstance(Device device) {
        log.info("RUN INSTANCE START");

//        device.capabilities.get(0).state.action_result = new ActionResult();
//        device.capabilities.get(0).state.action_result.status = "DONE";
//        device.capabilities.get(0).reportable = true;

//        String instance = device.capabilities.get(0).state.instance;
//        String value = device.capabilities.get(0).state.value;
//        Boolean relative = device.capabilities.get(0).state.relative;
//        log.info("DEVICE ID: " + id + " INSTANCE: " + instance + " VALUE: " + value + " RELATIVE: " + relative);

//        получаем плеер для выполнения действия с ним
        Player player = lmsPlayers.playerByDeviceId(device.id);
        log.info("PLAYER: " + player);

//        если плеер не получен или недоступен - вернуть device с кодом ошибки
        if (player == null || player.modeReal() == null) {
            device.action_result = new ActionResult();
            device.action_result.status = "ERROR";
            device.action_result.error_code = "DEVICE_UNREACHABLE";
            device.action_result.error_message = "Устройство потеряно";

            device.capabilities.stream().forEach(capability -> {
// https://yandex.ru/dev/dialogs/smart-home/doc/ru/reference/post-action
// если нажать на кнопку увтройства вкл/выкл если ошибка - ответить Устройство неотвечает
                capability.state.action_result = new ActionResult();
                capability.state.action_result.status = "ERROR";
                capability.state.action_result.error_code = "DEVICE_UNREACHABLE";
                capability.state.action_result.error_message = "Устройство потеряно";
                log.info("CAP: " + capability);

            });


            log.info("ACTION RESULT ERROR PLAYER NULL");
            log.info("DEVICE " + device);
            return device;
        } else {
            device.action_result = new ActionResult();
            device.action_result.status = "DONE";
            device.action_result.error_code = null;
            device.action_result.error_message = null;
            log.info("ACTION RESULT SET DONE");
        }

//        если несколько капабилитис
        if (device.capabilities.size() > 0) {
            log.info("MULTIPLE CAPABILITIES");
// сначала достать все капабилити девайса но не выполнять их
            device.capabilities.stream()
                    .forEach(c -> {
                                if (c.state == null) {
                                    c.state = new State();
                                }
                                c.state.action_result = new ActionResult();
                                c.state.action_result.error_code = "";
                                c.state.action_result.error_message = "";
                                c.state.action_result.status = "DONE";

                                c.reportable = true;
                                c.retrievable = true;
                                if (c.state.instance.equals("on")) {
                                    log.info("CAPABILITI ON");
                                    onValueByProvider = c.state.value;
                                }
                                if (c.state.instance.equals("channel")) {
                                    log.info("CAPABILITI CHANNEL");
                                    channelValueByProvider = c.state.value;
                                    channelRelativeByProvider = c.state.relative;
                                }
                                if (c.state.instance.equals("volume")) {
                                    log.info("CAPABILITI VOLUME");
                                    volumeByProvider = c.state.value;
                                    volumeRelativeByprovider = c.state.relative;
                                    c.state.value = "1";
                                    c.state.relative = false;
                                }
                                log.info("CAPABILITI: " + c.state.instance + "=" + c.state.value + " relative=" + c.state.relative);
                            }
                    );


// выполнить полученые капаюилити девайса в определеном порядке
// если надо включить канал то не надо запускать задачу turnOn
// надо предать в wake значение громкости
            if (volumeByProvider != null && channelValueByProvider == null && onValueByProvider == null) {
//                device.capabilities.stream().filter(capability -> capability.state.instance.equals())
                CompletableFuture.runAsync(() -> player.volumeRelativeOrAbsolute(volumeByProvider, volumeRelativeByprovider))
                        .thenRunAsync(() -> {
                            channelValueByProvider = null;
                            channelRelativeByProvider = null;
                            onValueByProvider = null;
                            volumeRelativeByprovider = null;
                            volumeByProvider = null;
                        });
            }
            if (channelValueByProvider != null && !"false".equals(onValueByProvider)) {
                CompletableFuture.runAsync(() -> player.playChannelRelativeOrAbsolute(channelValueByProvider, channelRelativeByProvider))
                        .thenRunAsync(() -> {
                            channelValueByProvider = null;
                            channelRelativeByProvider = null;
                            onValueByProvider = null;
                            volumeRelativeByprovider = null;
                            volumeByProvider = null;
                            player.saveLastTimePathAutoremoteRequest();
                        });
            }
            if ("true".equals(onValueByProvider) && channelValueByProvider == null) {
                CompletableFuture.runAsync(player::turnOnMusic)
                        .thenRunAsync(() -> {
                            channelValueByProvider = null;
                            channelRelativeByProvider = null;
                            onValueByProvider = null;
                            volumeRelativeByprovider = null;
                            volumeByProvider = null;
                            player.saveLastTimePathAutoremoteRequest();
                        });
            }
            if ("false".equals(onValueByProvider)) {
                CompletableFuture.runAsync(player::turnOffMusic)
                        .thenRunAsync(() -> {
                            channelValueByProvider = null;
                            channelRelativeByProvider = null;
                            onValueByProvider = null;
                            volumeRelativeByprovider = null;
                            volumeByProvider = null;
                            player.saveLastTimePathAutoremoteRequest();
                        });
            }
//            выход. вернуть девайс
            log.info("DEVICE RETURN: " + device);
            return device;
        }
//        ----------------------------------------

//        если у девайса только одно капабилити (потом это удалить и использовать только вариант с множеством капабилити)

//        log.info("SINGLE CAPABILITI");
//     выбор действия с плеером
//        log.info("SWITCH instance: " + instance + " = " + value);
//        switch (instance) {
//            case ("volume"):
//                log.info("CASE VOLUME");
//                CompletableFuture.runAsync(() -> player.volumeRelativeOrAbsolute(value, relative));
////                        .thenRunAsync(Requests::autoRemoteRefresh);
//                break;
//            case ("channel"):
//                log.info("CASE CHANNEL");
//                CompletableFuture.runAsync(() -> player.playChannelRelativeOrAbsolute(value, relative))
//                        .thenRunAsync(player::saveLastTimePathAutoremoteRequest);
//                break;
//            case ("on"):
//                if (value.equals("true")) {
//                    log.info("CASE TURN ON");
//                    CompletableFuture.runAsync(player::turnOnMusic)
//                            .thenRunAsync(player::saveLastTimePathAutoremoteRequest);
//                    break;
//                }
//                if (value.equals("false")) {
//                    log.info("CASE TURN OFF");
//                    CompletableFuture.runAsync(player::turnOffMusic)
//                            .thenRunAsync(player::saveLastTimePathAutoremoteRequest);
//                    break;
//                }
//                break;
//        }

        return device;
    }
}