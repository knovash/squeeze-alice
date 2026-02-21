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

@Log4j2
public class ProviderAction {

    private static String xRequestId;

    public static Context providerActionRun(Context context) {
        if (config.lmsIp == null) errorContext("LMS NULL");
        String body = context.body;
// получить из хедеров запроса id запроса который надо вернуть в ответе яндексу
        List<String> requestIdHeaders = context.requestHeaders.get("X-request-id");
        if (requestIdHeaders == null || requestIdHeaders.isEmpty()) {
            log.info("X-request-id header missing");
            return errorContext("X-request-id header missing");
        } else {
            xRequestId = requestIdHeaders.get(0);
        }
//        xRequestId = context.headers.get("X-request-id").get(0);

        if (body.equals("") || body == null) return errorContext("BODY NULL");
// создать ответ из запроса
        ResponseYandex responseYandex = JsonUtils.jsonToPojo(body, ResponseYandex.class);
        if (responseYandex == null) return errorContext("RESPONSE YANDEX NULL");
// положить в ответ id запроса полученый из хедеров запроса
        responseYandex.request_id = xRequestId;

// посмотреть полученные девайсы и их капабилити
        log.info("DEVICES IN PAYLOAD: " + responseYandex.payload.devices.size());
        responseYandex.payload.devices.forEach(d -> d.capabilities
                .forEach(c -> log.info(
                        "DEVICE ID: " + d.id +
                                " CAPABILITI: " + c.state.instance + " = " + c.state.value +
                                " RELATIVE: " + c.state.relative)));

// обновить состояние плееров из LMS
        log.info("UPDATE LMS PLAYERS");
        lmsPlayers.updateLmsPlayers();

        log.info("SET DEVICES CAPABILITIES. SIZE=" + responseYandex.payload.devices.size());
// обновить для всех девайсов все капабилити
        List<Device> jsonDevices = responseYandex.payload.devices.parallelStream()
                .map(d -> setDeviceCapabilities(d)) // если устройство недоступно то статус DEVICE_UNREACHABLE
                .collect(Collectors.toList());
// удалить девайсы которые недоступны
        List<Device> unreachableDevices = new ArrayList<>();
        unreachableDevices = responseYandex.payload.devices.stream()
                .filter(device -> device.action_result != null)
                .filter(device -> "ERROR".equals(device.action_result.status))
                .peek(device -> log.info("REMOVE UNREACHABLE DEVICE ID: " + device.id + " PLAYER: " + device.playerName()))
                .collect(Collectors.toList());
        if (!unreachableDevices.isEmpty()) {
            log.info("BEFORE REMOVE: " + responseYandex.payload.devices.stream().map(d -> "\nID=" + d.id + " " + d.playerName()).collect(Collectors.toList()));
            responseYandex.payload.devices.removeAll(unreachableDevices);
            log.info("AFTER REMOVE: " + responseYandex.payload.devices.stream().map(d -> "\nID=" + d.id + " " + d.playerName()).collect(Collectors.toList()));
        }

        List<Device> devicesForAsync = responseYandex.payload.devices;
        log.info("RUN DEVICES CAPABILITIES. SIZE=" + devicesForAsync.size());
// если устройств нет - выход
        if (!devicesForAsync.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    runMultipleDevicesCapabilities(devicesForAsync);
                } finally {
                    lmsPlayers.afterAll();
                }
            });
        }

// положить в пэйлоад все девайсы с обновленными состояниями капабилитей
        responseYandex.payload.devices = jsonDevices;
        context.bodyResponse = JsonUtils.pojoToJson(responseYandex);
        context.code = 200;
        return context;
    }

    private static Device setDeviceCapabilities(Device device) {
// получаем плеер для выполнения действия с ним
        Player player = lmsPlayers.playerByDeviceId(device.id);
// если плеер не получен или недоступен - вернуть device с кодом ошибки
// https://yandex.ru/dev/dialogs/smart-home/doc/ru/reference/post-action
// если нажать на кнопку увтройства вкл/выкл если ошибка - ответить Устройство неотвечает
        if (player == null || !player.connected) {
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

    public static void runMultipleDevicesCapabilities(List<Device> devices) {
// Фильтруем устройства с каналом
        List<Device> devicesWithChannel = devices.stream()
                .filter(device -> device.capabilities.stream()
                        .anyMatch(capability -> "channel".equals(capability.state.instance) && capability.state.value != null)
                )
                .collect(Collectors.toList());
// Группируем по значению канала
        Map<Object, List<Device>> groupedByChannel = devicesWithChannel.stream()
                .collect(Collectors.groupingBy(
                        device -> device.capabilities.stream()
                                .filter(cap -> "channel".equals(cap.state.instance) && cap.state.value != null)
                                .findFirst()
                                .map(cap -> cap.state.value)
                                .orElse(null)
                ));
// 1. Группы устройств с одинаковым каналом (размер группы > 1)
        List<List<Device>> sameChannelGroups = groupedByChannel.values().stream()
                .filter(group -> group.size() > 1)
                .collect(Collectors.toList());
// 2. Устройства с уникальными каналами (одиночные)
        List<Device> differentChannelDevices = groupedByChannel.values().stream()
                .filter(group -> group.size() == 1)
                .flatMap(List::stream)
                .collect(Collectors.toList());
// 3. Устройства без канала
        List<Device> noChannelDevices = devices.stream()
                .filter(device -> device.capabilities.stream()
                        .filter(cap -> "channel".equals(cap.state.instance)) // выбираем только каналы
                        .allMatch(cap -> cap.state.value == null) // все найденные каналы должны иметь value == null
                ).collect(Collectors.toList());

// 4. Устройства с on=true и channel=null
        List<Device> devicesTurnOnAll = devices.stream()
                .filter(device -> device.capabilities.stream()
                        .anyMatch(cap -> "on".equals(cap.state.instance) && cap.state.value.equals("true")) // все найденные каналы должны иметь value == null
                ).collect(Collectors.toList());

        log.info("devicesTurnOnAll " + devicesTurnOnAll.size());
        devicesTurnOnAll.stream().forEach(device -> log.info(device.id));
        log.info("noChannelDevices " + noChannelDevices.size());
        noChannelDevices.stream().forEach(device -> log.info(device.id));
        log.info("sameChannelGroups " + sameChannelGroups.size());
        sameChannelGroups.stream().forEach(g -> g.stream().forEach(device ->
                log.info("DEVICE: " + device.id)));

//  если включи все только 1 колонка то выполнять действие как для одной попытаться подключить к тграющей
        if (!devicesTurnOnAll.isEmpty() && devicesTurnOnAll.size() < 2) devicesTurnOnAll = new ArrayList<>();

        noChannelDevices.removeAll(devicesTurnOnAll);

        if (!devicesTurnOnAll.isEmpty()) {
            log.info("TURN ON ALL: " + devicesTurnOnAll.size());
            sameChannelGroups.add(devicesTurnOnAll);
        }

        differentChannelDevices.addAll(noChannelDevices);

        log.info("----------------------------------");
        log.info("devicesTurnOnAll " + devicesTurnOnAll.size());
        devicesTurnOnAll.stream().forEach(device -> log.info(device.id));
        log.info("noChannelDevices " + noChannelDevices.size());
        noChannelDevices.stream().forEach(device -> log.info(device.id));
        log.info("sameChannelGroups " + sameChannelGroups.size());
        sameChannelGroups.stream().forEach(g -> g.stream().forEach(device ->
                log.info("DEVICE: " + device.id)));
//        log.info("\nRUN DEVICES SAME CHANNEL. SIZE=" + sameChannelGroups.size());
//        sameChannelGroups.parallelStream().forEach(group -> runDevicesSameChannel(group));
//        log.info("\nRUN DEVICES DIFFERENT OR NO CHANNEL. SIZE=" + differentChannelDevices.size());
//        runDevicesDifferentChannel(differentChannelDevices);

// Создаем список futures для sameChannelGroups
        List<CompletableFuture<Void>> sameGroupFutures = sameChannelGroups.stream()
                .map(group -> CompletableFuture.runAsync(() -> runDevicesSameChannel(group)))
                .collect(Collectors.toList());
// Запускаем differentChannelDevices асинхронно
        CompletableFuture<Void> differentFuture = CompletableFuture.runAsync(
                () -> runDevicesDifferentChannel(differentChannelDevices)
        );
// Объединяем все futures
        List<CompletableFuture<Void>> allFutures = new ArrayList<>(sameGroupFutures);
        allFutures.add(differentFuture);
// Ожидаем завершения ВСЕХ задач
        CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();

    }

    // включить несколько колонок с одинаковым каналом, возможно разная громкость
    public static void runDevicesSameChannel(List<Device> devices) {
        log.info("\nRUN DEVICES SAME CHANNEL");
// достать из каждого девайса громкость, отключить, разбудить, установить громкость ПАРАЛЕЛЬНО
        devices.parallelStream().forEach(device -> {
            String volume = device.capabilities.stream()
                    .filter(cap -> cap.state != null)
                    .filter(cap -> "volume".equals(cap.state.instance))
                    .findFirst()
                    .map(cap -> cap.state.value)
                    .orElse(null);
            Player player = lmsPlayers.playerByDeviceId(device.id);
            if (player != null) player.unsync().ifExpiredAndNotPlayingUnsyncWakeSet(volume);
        });
// синхронизировать плееры
        log.info("\nSYNC PLAYERS. SIZE=" + devices.size());
        String previousPlayerName = null;
        for (Device device : devices) {
            Player player = lmsPlayers.playerByDeviceId(device.id);
            if (player != null) {
                if (previousPlayerName != null) player.syncTo(previousPlayerName);
                previousPlayerName = player.name;
                log.info("SET PREVIOUS PLAYER NAME: " + previousPlayerName);
            }
        }
// включить общий канал
        Integer channel = null;
        try {
            channel = Integer.valueOf(devices.get(0).capabilities.stream()
                    .filter(capability -> capability.state.instance.equals("channel"))
                    .findFirst().orElseGet(null)
                    .state.value);
        } catch (Exception e) {
            log.info("ERROR: " + e);
        }
        log.info("\nPLAY SAME CHANNEL: " + channel);
        Player player = lmsPlayers.playerByDeviceId(devices.get(0).id);
        if (channel != null) player.playChannelRelativeOrAbsolute(channel.toString(),false);
        else player.playLast();
    }

    // выполнить действия листа колонок - power,channel,volume
    public static void runDevicesDifferentChannel(List<Device> devices) {
        log.info("\nRUN DEVICES DIFFERENT OR NO CHANNEL. SIZE = " + devices.size());
// достать из каждого девайса громкость, отключить, разбудить, установить громкость ПАРАЛЕЛЬНО
        devices.parallelStream().forEach(device -> {
            Player player = lmsPlayers.playerByDeviceId(device.id);
            if (player == null) return;
            String power = device.capabilities.stream()
                    .filter(cap -> cap.state != null)
                    .filter(cap -> "on".equals(cap.state.instance)).findFirst()
                    .map(cap -> cap.state.value).orElse(null);
            String volume = device.capabilities.stream()
                    .filter(cap -> cap.state != null)
                    .filter(cap -> "volume".equals(cap.state.instance)).findFirst()
                    .map(cap -> cap.state.value).orElse(null);
            Boolean volumeRelative = device.capabilities.stream()
                    .filter(cap -> cap.state != null)
                    .filter(cap -> "volume".equals(cap.state.instance)).findFirst()
                    .map(cap -> cap.state.relative).orElse(null);
            String channel = device.capabilities.stream()
                    .filter(cap -> cap.state != null)
                    .filter(cap -> "channel".equals(cap.state.instance)).findFirst()
                    .map(cap -> cap.state.value).orElse(null);
            Boolean channelRelative = device.capabilities.stream()
                    .filter(cap -> cap.state != null)
                    .filter(cap -> "channel".equals(cap.state.instance)).findFirst()
                    .map(cap -> cap.state.relative).orElse(null);

            log.info("RUN " + player.name + " POWER: " + power + " CHANNEL: " + channel + " REL: " + channelRelative + " VOLUME: " + volume + " REL: " + volumeRelative);

// выключить плеер
            if ("false".equals(power)) {
                log.info(player.name + " POWER: " + power);
                player
                        .unsync()
                        .pause();
                return;
            }
// изменение громкости
            if (volume != null && power == null && channel == null) {
                log.info(player.name + "VOLUME SET: " + volume + " rel " + volumeRelative);
                player
                        .volumeRelativeOrAbsolute(volume, volumeRelative);
                return;
            }
// изменение канала и громкости - разбудить, установить громкость, включить канал
            if (volume != null && channel != null) {
                log.info(player.name + "CHANNEL SET: " + channel + " rel " + channelRelative);
                log.info(player.name + "VOLUME SET: " + volume + " rel " + volumeRelative);
                player
                        .ifExpiredAndNotPlayingUnsyncWakeSet(player.volumeRelativeOrAbsoluteGetValue(volume, volumeRelative))
                        .playChannelRelativeOrAbsolute(channel, channelRelative);
                return;
            }
// изменение канала - разбудить, установить громкость, включить канал
            if (channel != null) {
                log.info(player.name + "CHANNEL SET: " + channel + " rel " + channelRelative);
                player
                        .ifExpiredAndNotPlayingUnsyncWakeSet(null)
                        .playChannelRelativeOrAbsolute(channel, channelRelative);
                return;
            }
// включить если не играет
            if ("true".equals(power) && !player.playing) {
                log.info(player.name + " NOT PLAY. POWER SET: " + power + " - SYNC TO PLAYING OR PLAY LAST");
                player.unsync().ifExpiredAndNotPlayingUnsyncWakeSet(player.volumeRelativeOrAbsoluteGetValue(volume, volumeRelative));
                Player playingPlayer = lmsPlayers.playingPlayer(player.name, true);
                if (playingPlayer != null && !player.separate && !playingPlayer.separate)
                    player.syncTo(playingPlayer.name);
                else player.playLast();
                return;
            }
// включить если играет и есть играющий не в группе
            if ("true".equals(power) && player.playing) {
                log.info(player.name + "PLAY. POWER SET: " + power + " - SYNC TO PLAYING NOT IN GROUP");
                List<String> playersNames = player.playingPlayersNamesNotInCurrentGroup(true);
                Player playingPlayer = null;
                if (!playersNames.isEmpty()) playingPlayer = lmsPlayers.playerByCorrectName(playersNames.get(0));
                if (playingPlayer != null && !player.separate && !playingPlayer.separate)
                    player.syncTo(playingPlayer.name);
                return;
            }
            log.info("ERROR SKIP ALL ACTIONS");
        });
    }

    public static Context errorContext(String errorMessage) {
        log.info("ERROR: " + errorMessage);
        Context context = new Context();
        context.bodyResponse = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[]}}";
        context.code = 200;
        return context;
    }

}