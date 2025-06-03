package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.provider.response.*;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.yandex.Yandex.sendDeviceState;

@Log4j2
public class ProviderAction {

    private static Boolean hasDifferentChannels = false;
    private static String xRequestId;
//    private static String volume = null;

    public static Context providerActionRun(Context context) {
        log.info("START >>>");
        if (config.lmsIp == null) errorContext("LMS NULL");
//        получить из контекста тело запроса от умного дома с девайсами и капабилити которые надо выполнить
        String body = context.body;
//        получить из хедеров запроса id запроса который надо вернуть в ответе яндексу
        xRequestId = context.headers.get("X-request-id").get(0);
//        log.info("XREQUESTID: " + xRequestId);
        if (body.equals("") || body == null) return errorContext("BODY NULL");
//        создать ответ из запроса
        ResponseYandex responseYandex = JsonUtils.jsonToPojo(body, ResponseYandex.class);
        if (responseYandex == null) return errorContext("RESPONSE YANDEX NULL");
//        положить в ответ id запроса полученый из хедеров запроса
        responseYandex.request_id = xRequestId;

//        посмотреть полученные девайсы и их капабилити
        log.info("DEVICES IN PAYLOAD: " + responseYandex.payload.devices.size());
        responseYandex.payload.devices.forEach(d -> d.capabilities
                .forEach(c -> log.info(
                        "DEVICE ID: " + d.id +
                                " CAPABILITI: " + c.state.instance + " = " + c.state.value +
                                " RELATIVE: " + c.state.relative)));

        lmsPlayers.updateLmsPlayers();

        log.info("--------------------------------------------");
        log.info("SET DEVICES CAPABILITIES IN PARALLEL STREAMS");
// обновить для всех девайсов все капабилити в соответствии с ожидаемым результатом
        List<Device> jsonDevices = responseYandex.payload.devices.parallelStream()
                .map(d -> setDeviceCapabilities(d)) // если устройство недоступно то статус DEVICE_UNREACHABLE
                .collect(Collectors.toList());

// если устройство недоступно не выполнять действия
// удалить девайсы которые недоступны
        List<Device> unreachableDevices = new ArrayList<>();
        unreachableDevices = responseYandex.payload.devices.stream()
                .filter(device -> device.action_result != null)
                .filter(device -> device.action_result.status != null)
                .filter(device -> device.action_result.status.equals("ERROR"))
                .peek(device -> log.info("REMOVE UNREACHABLE DEVICE ID: " + device.id))
                .collect(Collectors.toList());

        if (unreachableDevices.size() > 0) {
            log.info("BEFORE REMOVE: " + responseYandex.payload.devices.size());
            responseYandex.payload.devices.removeAll(unreachableDevices);
            log.info("AFTER REMOVE: " + responseYandex.payload.devices.size());
        }

        log.info("-----------------------------------");
        log.info("RUN DEVICES CAPABILITIES IN FUTURES");
// выполнить в потоке действия с устройствами
        processDevicesCapabilities(responseYandex)
                .exceptionally(ex -> {
                    log.error("DEVICE ERROR: ", ex);
                    return null;
                })
                .thenRun(() -> {
                    log.info("FINISH OK");
                    lmsPlayers.write();
                    lmsPlayers.autoremoteRequest();
                });

// положить в пэйлоад все девайсы с обновленными состояниями капабилитей
        responseYandex.payload.devices = jsonDevices;
        context.bodyResponse = JsonUtils.pojoToJson(responseYandex);
        context.code = 200;
        return context;
    }

    public static CompletableFuture<Void> processDevicesCapabilities(ResponseYandex responseYandex) {

        if (responseYandex.payload.devices.size() > 1) {
            log.info("MULTIPLE DEVICES");
            return runDevices(responseYandex);
        } else {
            log.info("ONE DEVICE");
            return CompletableFuture.allOf(
                    responseYandex.payload.devices.stream()
//                            TODO вернуть если ошибки. девайсы недоступные должны быть ранее удалены
//                            .filter(d -> !(d.action_result.error_code != null))
                            .map(d -> runCapabilitiesDevices(d)) // Собираем все CompletableFuture
                            .toArray(CompletableFuture[]::new)
            );
        }
    }

    private static CompletableFuture<Void> runCapabilitiesDevices(Device device) {
        return CompletableFuture.runAsync(() -> {
//            log.info("RUN CAPABILITIES DEVICE ID: " + device.id);
// получаем плеер для выполнения действия с ним


          
            
            Player player = lmsPlayers.playerByDeviceId(device.id);
            if (player == null) {
                log.info("ERROR. PLAYER NULL BY DEVICE ID: " + device.id);
                return;
            }
            log.info("RUN CAPABILITIES DEVICE ID: " + device.id + " PLAYER: " + player.name);
// сначала достать все капабилити девайса
            Capability capabilityVolume = device.capabilities.stream().filter(capability -> capability.state.instance.equals("volume")).findFirst().orElse(null);
            Capability capabilityChannel = device.capabilities.stream().filter(capability -> capability.state.instance.equals("channel")).findFirst().orElse(null);
            Capability capabilityPower = device.capabilities.stream().filter(capability -> capability.state.instance.equals("on")).findFirst().orElse(null);


            String power = "true";
            if (capabilityPower != null) power = capabilityPower.state.value;

            List<CompletableFuture<Void>> futures = new ArrayList<>();

// если есть канал и повер НЕ выключить
            if (capabilityChannel != null && !power.equals("false")) {
                log.info("RUN CAPABILITY CHANNEL " + player.name);
                futures.add(CompletableFuture.runAsync(() -> {
                            String volume = null;
                            if (capabilityVolume != null) volume = capabilityVolume.state.value;
                            if (hasDifferentChannels) player.unsync(); // если несколько плееров с разными каналами
                            player.playChannelRelativeOrAbsolute(capabilityChannel.state.value, capabilityChannel.state.relative, volume);
                        }
                ));
            }
// если повер ВКЛ
            if (capabilityChannel == null && capabilityPower != null && capabilityPower.state.value.equals("true")) {
                log.info("RUN CAPABILITY TURN ON " + player.name);
                futures.add(CompletableFuture.runAsync(() -> {
                    String volume = null;
                    if (capabilityVolume != null) volume = capabilityVolume.state.value;
                    player.turnOnMusic(volume);
                }));

            }
// если повер ВЫКЛ
            if (capabilityPower != null && capabilityPower.state.value.equals("false")) {
                log.info("RUN CAPABILITY TURN OFF " + player.name);
                futures.add(CompletableFuture.runAsync(() -> player.turnOffMusic()));
            }
// если громкость
            if (capabilityChannel == null && capabilityPower == null && capabilityVolume != null) {
                log.info("RUN CAPABILITY VOLUME " + player.name);
                futures.add(CompletableFuture.runAsync(() -> player.volumeRelativeOrAbsolute(capabilityVolume.state.value, capabilityVolume.state.relative)));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        });
    }

    private static Device setDeviceCapabilities(Device device) {
//        log.info("SET CAPABILITIES START DEVICE ID: " + device.id);
// получаем плеер для выполнения действия с ним
        Player player = lmsPlayers.playerByDeviceId(device.id);
//        log.info("PLAYER: " + player);
// если плеер не получен или недоступен - вернуть device с кодом ошибки
// https://yandex.ru/dev/dialogs/smart-home/doc/ru/reference/post-action
// если нажать на кнопку увтройства вкл/выкл если ошибка - ответить Устройство неотвечает
        if (player == null || !player.connected) {
//            if (player == null || player.checkPlayerConnected() == null) {
//            log.info("ERROR. PLAYER NULL BY DEVICE ID: " + device.id + " SET ACTION RESULT: DEVICE_UNREACHABLE");
            device.action_result = new ActionResult();
            device.action_result.status = "ERROR";
            device.action_result.error_code = "DEVICE_UNREACHABLE";
            device.action_result.error_message = "Устройство потеряно";
            device.capabilities.stream().forEach(capability -> {
                capability.state.action_result = new ActionResult();
                capability.state.action_result.status = "ERROR";
                capability.state.action_result.error_code = "DEVICE_UNREACHABLE";
                capability.state.action_result.error_message = "Устройство потеряно";
            });
            log.info("SET CAPABILITIES DEVICE ID: " + device.id + " ACTION RESULT: DEVICE_UNREACHABLE");
            return device;
        } else {
            device.action_result = new ActionResult();
            device.action_result.status = "DONE";
            device.action_result.error_code = null;
            device.action_result.error_message = null;
            device.capabilities.stream().forEach(capability -> {
                capability.state.action_result = new ActionResult();
                capability.state.action_result.status = "DONE";
                capability.state.action_result.error_code = null;
                capability.state.action_result.error_message = null;
            });
            log.info("SET CAPABILITIES DEVICE ID: " + device.id + " PLAYER: " + player.name + " ACTION RESULT: DONE");
            return device;
        }
    }

    public static CompletableFuture<Void> runDevices(ResponseYandex responseYandex) {

        return CompletableFuture.supplyAsync(() -> {

            log.info("MULTIPLY DEVICES -----------");
// определить если несколько девайсов с одинаковым каналом CheckForPlayersWithIdenticalChannel
            List<String> playersChannels = responseYandex.payload.devices.stream()
//                    .filter(d -> !(d.action_result.error_code != null)) // TODO девайсы недоступные должны быть ранее удалены
                    .map(d -> d.capabilities.stream()
                            .filter(capability -> capability.state.instance.equals("channel"))
                            .map(capability -> capability.state.value)
                            .findFirst()
                            .orElse(null)
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            log.info("CHECK PLAYERS IDENTICAL CHANNELS: " + playersChannels);
            Set<String> uniqueChannels = new HashSet<>(playersChannels);
// определить общий уникальный канал
            String unicChannel;
            if (playersChannels.isEmpty()) {
                // Все каналы null, считаем их одинаковыми
                hasDifferentChannels = false;
                unicChannel = null;
            } else {
                // Проверяем, есть ли разные каналы среди не-null
                hasDifferentChannels = uniqueChannels.size() > 1;
                unicChannel = uniqueChannels.size() == 1 ? uniqueChannels.iterator().next() : null;
            }
            log.info("PLAYERS HAS IDENTICAL CHANNELS : " + !hasDifferentChannels + " UNIQUE CHANNEL: " + unicChannel);
// определить девайсы которые надо включить или включить канал
            List<Device> devicesForTurnOnOrSetChannel = responseYandex.payload.devices.stream()
                    .filter(device -> device.capabilities.stream()
                            .anyMatch(capability -> {
                                State state = capability.state;
                                boolean isChannel = "channel".equals(state.instance) && state.value != null;
                                boolean isOn = "on".equals(state.instance) && state.value.equals("true");
                                return isChannel || isOn;
                            }))
                    .collect(Collectors.toList());
            if (devicesForTurnOnOrSetChannel != null)
                log.info("DEVICES FOR TURN ON OR SET CHANNEL COUNT: " + devicesForTurnOnOrSetChannel.size());

// если каналы одинаковые или нет каналов но есть команды включить
// сначала разбудить эти плееры, включить один, подключить поочереди остальные
// если каналы разные - просто выполнить отдельно действия с каждым девайсом
            if (!hasDifferentChannels && devicesForTurnOnOrSetChannel.size() > 1) {
                return lmsPlayers.turnOnMusicMultiply(devicesForTurnOnOrSetChannel, unicChannel);
            } else {
                return CompletableFuture.allOf(
                        responseYandex.payload.devices.stream()
                                .map(d -> runCapabilitiesDevices(d))
                                .toArray(CompletableFuture[]::new)
                );
            }
        }).thenCompose(f -> f);
    }

    public static Context errorContext(String errorMessage) {
        log.info("ERROR: " + errorMessage);
        Context context = new Context();
        context.bodyResponse = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[]}}";
        context.code = 200;
        return context;
    }

}