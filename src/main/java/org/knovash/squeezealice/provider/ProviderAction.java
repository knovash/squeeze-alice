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

    public static Context providerActionRun(Context context) {
        if (config.lmsIp == null) errorContext("LMS NULL", null);
        String body = context.body;
        String xRequestId;
// получить из хедеров запроса id запроса который надо вернуть в ответе яндексу
        List<String> requestIdHeaders = context.requestHeaders.get("X-request-id");
        if (requestIdHeaders == null || requestIdHeaders.isEmpty()) {
            log.info("X-request-id header missing");
            return errorContext("X-request-id header missing", null);
        } else {
            xRequestId = requestIdHeaders.get(0);
        }

        if (body.equals("") || body == null) return errorContext("BODY NULL", xRequestId);
// создать ответ из запроса
        ResponseYandex responseYandex = JsonUtils.jsonToPojo(body, ResponseYandex.class);
        if (responseYandex == null) return errorContext("RESPONSE YANDEX NULL", xRequestId);
// положить в ответ id запроса полученый из хедеров запроса
        responseYandex.request_id = xRequestId;

//      ПРИХОДИТ DEVICE EXTERNAL ID !!!
        log.info("BODY: " + body);

        // к полученым девайсам яндекса по id приделать комнаты от локальных девайсов
        responseYandex.payload.devices.forEach(device -> device.room = smartHome.deviceByExternalId(device.id).room);

// посмотреть полученные девайсы и их капабилити
        log.info("DEVICES: " + responseYandex.payload.devices.stream().map(device1 -> device1.room).collect(Collectors.toList()));
        responseYandex.payload.devices.forEach(d -> d.capabilities
                .forEach(c -> log.info("DEVICE: " + d.room +
                        " CAPABILITI: " + c.state.instance + " = " + c.state.value + " RELATIVE: " + c.state.relative)));

        lmsPlayers.updatePlayers(); // для капабилитей надо знать состояние коннектед если не подключен будет DEVICE_UNREACHABLE

        // обновить для всех девайсов все капабилити
        responseYandex.payload.devices.parallelStream().forEach(d -> setDeviceCapabilities(d)); // если устройство недоступно то статус DEVICE_UNREACHABLE


        List<Device> devicesForAsync = responseYandex.payload.devices.stream()
                .filter(device -> device.action_result == null || !"DEVICE_UNREACHABLE".equals(device.action_result.error_code))
                .collect(Collectors.toList());

        log.info("DEVICES FOR ASYNC: " + devicesForAsync.stream().map(device -> device.room).collect(Collectors.toList()));
// для работы с девайсами отфильтровать недоступные
        devicesForAsync.removeIf(device -> device != null && device.action_result != null && "DEVICE_UNREACHABLE".equals(device.action_result.error_code));
// асинхронное выполнение действий с устройствами. ответ в Алису уже отправлен
        if (!devicesForAsync.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    runMultipleDevicesCapabilities(devicesForAsync);
                } finally {
//                    lmsPlayers.afterAll(); // TODO тут возможно надо обновлять состояния в яндексе но только для измененных девайсов
//                    Yandex.sendAllStates();

                }
            });
        }

// положить в пэйлоад все девайсы с обновленными состояниями капабилитей

        context.bodyResponse = JsonUtils.pojoToJson(responseYandex);
        context.code = 200;
        return context;
    }

    // если плеер не получен или недоступен - вернуть device с кодом ошибки
// если нажать на кнопку увтройства вкл/выкл если ошибка - ответить Устройство неотвечает
    private static Device setDeviceCapabilities(Device device) {
        Player player = lmsPlayers.playerByRoom(device.room);
        String status;
        String errorCode;
        String errorMessage;
        if (player == null || !player.connected) {
            status = "ERROR";
            errorCode = "DEVICE_UNREACHABLE"; // https://yandex.ru/dev/dialogs/smart-home/doc/ru/concepts/response-codes#codes-api
            errorMessage = "Устройство недоступно";
            device.capabilities = new ArrayList<>(); // Важно: очищаем capabilities, чтобы Яндекс не сбивался
            log.info("DEVICE_UNREACHABLE: " + device.room);
        } else {
            status = "DONE";
            errorCode = null;
            errorMessage = null;
        }
        // Устанавливаем результат для самого устройства
        device.action_result = new ActionResult(); // https://yandex.ru/dev/dialogs/smart-home/doc/ru/reference/post-action
        device.action_result.status = status;
        device.action_result.error_code = errorCode;
        device.action_result.error_message = errorMessage;
        return device;
    }

    public static void runMultipleDevicesCapabilities(List<Device> devices) {
//        log.info(devices);
        if (devices == null || devices.isEmpty()) return;
        log.info(">>> DEVICES SIZE: " + devices.size());
        // Итоговые группы
        List<Device> groupForOff = new ArrayList<>();           // устройства с on=false (выключение)
        List<Device> groupForOn = new ArrayList<>();            // устройства с on=true и без channel (включение)
        List<Device> groupForVolume = new ArrayList<>();        // устройства только с volume (без on и channel)
        List<Device> devicesWithChannel = new ArrayList<>();    // все устройства, имеющие channel (кроме тех, что идут в off)
        List<Device> groupForDifferentChannel = new ArrayList<>(); // устройства с уникальными каналами или относительными каналами

        // 1. Первичная классификация устройств
        for (Device device : devices) {
            boolean hasOnFalse = false;
            boolean hasOnTrue = false;
            boolean hasChannel = false;
            boolean hasVolume = false;
            for (Capability cap : device.capabilities) {
                String instance = cap.state.instance;
                log.info("INSTANCE " + instance);
                Object value = cap.state.value;
                log.info("VALUE " + value);
                if ("on".equals(instance)) {
                    if ("false".equals(value)) {
                        hasOnFalse = true;
                    } else if ("true".equals(value)) {
                        hasOnTrue = true;
                    }
                } else if ("channel".equals(instance) && value != null) {
                    hasChannel = true;
                } else if ("volume".equals(instance) && value != null) {
                    hasVolume = true;
                }
            }
            // Приоритет: off > channel > on > volume
            if (hasOnFalse) {
                groupForOff.add(device);
            } else if (hasChannel) {
                devicesWithChannel.add(device); // будет обработано позже с учётом relative
            } else if (hasOnTrue) {
                groupForOn.add(device);
            } else if (hasVolume) {
                groupForVolume.add(device);
            }
            // Устройства без значимых команд игнорируются
        }

        // 2. Разделение устройств с каналом на относительные (relative) и абсолютные
        List<Device> devicesWithChannelRelative = new ArrayList<>();
        List<Device> devicesWithChannelAbsolute = new ArrayList<>();
        for (Device device : devicesWithChannel) {
            Boolean isRelative = device.capabilities.stream()
                    .filter(cap -> "channel".equals(cap.state.instance))
                    .anyMatch(cap -> cap.state.relative != null && cap.state.relative);
            if (isRelative) {
                devicesWithChannelRelative.add(device);
            } else {
                devicesWithChannelAbsolute.add(device);
            }
        }
        // Устройства с relative-каналом сразу уходят в индивидуальную обработку
        groupForDifferentChannel.addAll(devicesWithChannelRelative);

        // 3. Группировка абсолютных устройств по значению канала
        Map<Object, List<Device>> channelToDevices = devicesWithChannelAbsolute.stream()
                .collect(Collectors.groupingBy(device ->
                        device.capabilities.stream()
                                .filter(cap -> cap.state != null && cap.state.value != null && "channel".equals(cap.state.instance))
                                .findFirst()
                                .map(cap -> cap.state.value)
                                .orElse(null)
                ));

        // 4. Разделение на группы с одинаковыми каналами и уникальные
        List<List<Device>> groupsForSameChannels = new ArrayList<>();
        for (Map.Entry<Object, List<Device>> entry : channelToDevices.entrySet()) {
            List<Device> group = entry.getValue();
            if (group.size() > 1) {
                groupsForSameChannels.add(group); // группа устройств с одинаковым каналом (absolute)
            } else {
                groupForDifferentChannel.addAll(group); // уникальный канал -> в индивидуальную обработку
            }
        }

        logDevices("groupForOff", groupForOff);
        logDevices("groupForOn", groupForOn);
        logDevices("groupForVolume", groupForVolume);
        logDevices("groupForDifferentChannel", groupForDifferentChannel);
        groupsForSameChannels.forEach(g -> logDevices("groupForSameChannel", g));

        runGroupForOff(groupForOff);
        runGroupForVolume(groupForVolume);
        runGroupForOn(groupForOn);
        runGroupForDifferentChannel(groupForDifferentChannel);
        runGroupsForSameChannels(groupsForSameChannels);

    }

    // отсоединить, остановить (если не играет ничего неделать)
    public static void runGroupForOff(List<Device> devices) {
        if (devices == null || devices.isEmpty()) return;
        log.info("START runGroupForOff");
        devices.forEach(device -> playerWithCapabilities(device).turnOffMusic());
        log.info("FINISH runGroupForOff");
    }

    // отсоединить, разбудить, установить громкость, соединить в группу подключить к играющему или включить последнее ( TODO если играет ?)
//    выбрать устройства которые  уже играют - соединить их в группу
//    выбрать устройства которые  еще не играют - отсодинить, разбудить, соединить с устройством в группе играющих
//    если группы играющих нет включить последнее
    public static void runGroupForOn(List<Device> devices) {
        if (devices == null || devices.isEmpty()) return;
        log.info("START runGroupForOn");

        List<Player> players = devices.stream()
                .map(ProviderAction::playerWithCapabilities)
                .collect(Collectors.toList());

        // Разделяем устройства на играющие и неиграющие
        List<Player> playing = players.stream()
                .filter(p -> p.playing)
                .collect(Collectors.toList());
        List<Player> notPlaying = players.stream()
                .filter(p -> !p.playing)
                .collect(Collectors.toList());

        log.info("IN GROUP FOR ON - PLAYING: " + playing.size() + " NO PLAYING: " + notPlaying.size());

        if (!playing.isEmpty()) { // если в групее есть играющие, все играющие соединить, разбудить неиграющие и подключить к играющим
            Player master = playing.get(0);
            String masterName = master.name;
            playing.stream().skip(1).forEach(p -> p.syncTo(masterName)); // играющие соединить
            notPlaying.parallelStream().forEach(p -> p.ifExpiredAndNotPlayingUnsyncWakeSetVolume(p.capVolume)); // неиграющие разбудить
            notPlaying.forEach(p -> p.syncTo(masterName)); // неиграющие подключить к играющим
            return;
        }

        // Нет играющих среди переданных устройств
        notPlaying.parallelStream().forEach(p -> p.ifExpiredAndNotPlayingUnsyncWakeSetVolume(p.capVolume));
        Player master = players.get(0);
        master.syncToPlayingOrPlayLast(); // попытка подключиться к уже играющему. неподключать если он отделен и неподключать к отделенным если игращего нет включить последнее игравшее
        notPlaying.forEach(p -> p.syncTo(master.name));
        log.info("FINISH runGroupForOn (play last)");
    }

    // изменить громкость (если не играет ничего неделать)
    public static void runGroupForVolume(List<Device> devices) {
        if (devices == null || devices.isEmpty()) return;
        log.info("START runGroupForVolume");
        devices
                .parallelStream() // TODO
                .map(ProviderAction::playerWithCapabilities)
                .forEach(player -> player.volumeSetLimited(player.capVolume));
        log.info("FINISH runGroupForVolume");
    }

    public static void runGroupForDifferentChannel(List<Device> devices) {
        if (devices == null || devices.isEmpty()) return;
        log.info("START runGroupForDifferentChannel");
        Integer size = devices.size();
        List<Player> players = devices.stream().map(ProviderAction::playerWithCapabilities).collect(Collectors.toList());

        if (size == 1) { // один отсоединять ненадо, разбудить, включить канал, подсоединить к нему другие играющие
            Player p = players.get(0);
            p.ifExpiredAndNotPlayingUnsyncWakeSetVolume(p.capVolume)
                    .playChannel(p.capChannel)
                    .syncOtherPlayingNotInGroupToThis();
        }

        if (size > 1) { // отсоединить каждый, разбудить, включить канал свой на каждом плеере
            players.parallelStream().forEach(player -> player
                    .unsync()
                    .ifExpiredAndNotPlayingUnsyncWakeSetVolume(player.capVolume)
                    .playChannel(player.capChannel));
        }

        log.info("FINISH runGroupForDifferentChannel");
    }

    // отсоединить, разбудить, установить громкость, соединить в группу, включить общий канал
    public static void runGroupForSameChannels(List<Device> devices) {
        if (devices == null || devices.isEmpty()) return;
        log.info("START runGroupForSameChannels");
        List<Player> players = devices.stream().map(ProviderAction::playerWithCapabilities).collect(Collectors.toList());
        Player master = players.get(0);
        players
                .parallelStream() // TODO
                .forEach(player -> player
                        .unsync()
                        .ifExpiredAndNotPlayingUnsyncWakeSetVolume(player.capVolume)
                        .syncTo(master.name)
                );
        master.playChannel(master.capChannel);
        log.info("FINISH runGroupForSameChannels");
    }

    public static void runGroupsForSameChannels(List<List<Device>> groups) {
        if (groups == null || groups.isEmpty()) return;
        groups
                .parallelStream() // TODO
                .forEach(ProviderAction::runGroupForSameChannels);
    }

    public static Player playerWithCapabilities(Device device) {
        String volume = null;
        Boolean volumeRelative = null;
        String channel = null;
        Boolean channelRelative = null;
        String on = null;
        for (Capability cap : device.capabilities) {
            if ("on".equals(cap.state.instance)) on = cap.state.value;
            if ("channel".equals(cap.state.instance)) {
                channel = cap.state.value;
                channelRelative = cap.state.relative;
            }
            if ("volume".equals(cap.state.instance)) {
                volume = cap.state.value;
                volumeRelative = cap.state.relative;
            }
        }
        Player player = lmsPlayers.playerByRoom(device.room); // устройство в умном доме имеет фиксированный id = "HomePod". При смене колонки в комнате этот deviceId присваивается новому плееру (JBL black), но сам идентификатор остаётся прежним.
        log.info("Player class for {}: {}", device.room, player.getClass().getName());

        if (Boolean.TRUE.equals(channelRelative) && channel != null && !channel.contains("-")) channel = "+" + channel;
        if (Boolean.TRUE.equals(volumeRelative) && volume != null && !volume.contains("-")) volume = "+" + volume;
        log.info(player.name + " p:" + on + " c:" + channel + " v:" + volume);
        player.capVolume = volume;
        player.capChannel = channel;
        player.capOn = on;
        return player;
    }

    public static void logDevices(String description, List<Device> devices) {
        List<String> info = devices.parallelStream().map(d -> {
            Capability volumeCap = null;
            Capability channelCap = null;
            Capability onCap = null;
            for (Capability cap : d.capabilities) {
                if (cap.state == null) continue;
                String instance = cap.state.instance;
                if (instance == null) continue;
                if ("volume".equals(instance) && volumeCap == null) {
                    volumeCap = cap;
                } else if ("channel".equals(instance) && channelCap == null) {
                    channelCap = cap;
                } else if ("on".equals(instance) && onCap == null) {
                    onCap = cap;
                }
                if (volumeCap != null && channelCap != null && onCap != null) break;
            }
            String volume = volumeCap != null ? volumeCap.state.value : null;
            Boolean volumeRelative = volumeCap != null ? volumeCap.state.relative : null;
            String channel = channelCap != null ? channelCap.state.value : null;
            Boolean channelRelative = channelCap != null ? channelCap.state.relative : null;
            String on = onCap != null ? onCap.state.value : null;
            return d.room + " p:" + on + " c:" + channel + "|" + channelRelative + " v:" + volume + "|" + volumeRelative;
        }).collect(Collectors.toList());
        log.info(description + " " + info);
    }

    public static Context errorContext(String errorMessage, String xRequestId) {
        log.info("ERROR: " + errorMessage);
        Context context = new Context();
        context.bodyResponse = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[]}}";
        context.code = 200;
        return context;
    }

}