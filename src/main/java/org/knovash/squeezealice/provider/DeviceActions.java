package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Actions;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Device;

import java.util.concurrent.CompletableFuture;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class DeviceActions {

    public static void runInstance(Device device) {
        int id = Integer.parseInt(device.id);
        String name = SmartHome.getDeviceById(id).customData.lmsName;
        String type = device.capabilities.get(0).type;
        String instance = device.capabilities.get(0).state.instance;
        String value = device.capabilities.get(0).state.value;
        Boolean relative = device.capabilities.get(0).state.relative;
        log.info("ID: " + id + " NAME " + name);
        log.info("TYPE: " + type);
        log.info("INSTANCE: " + instance + " VALUE: " + value + " RELATIVE: " + relative);

        // обратиться к девайсу и изменить его состояние
        Player player = lmsPlayers.getPlayerByName(name);
        switch (instance) {
            case ("volume"):
                if (relative != null && relative.equals(true)) {
                    log.info("VOLUME rel: " + value);
                    if (value.contains("-")) {
                        player.volumeSet(value);
                    } else {
                        player.volumeSet("+" + value);
                    }
                }
                if (relative != null && relative.equals(false)) {
                    log.info("VOLUME abs: " + value);
                    player.volumeSet(value);
                }
                break;
            case ("channel"):
                log.info("CHANNEL: " + value + " LAST CHANNEL: " + SmartHome.lastChannel);
                int channel;
                if (relative != null && relative.equals(true)) {
                    channel = SmartHome.lastChannel + 1;
                } else {
                    channel = Integer.parseInt(value);
                }
                Actions.playChannel(player, channel);
                SmartHome.lastChannel = channel;
                break;
            case ("on"):
                log.info("ON/OFF PLAY/PAUSE " + value);
                if (value.equals("true")) {
//                    Actions.turnOnMusic(player);
                    CompletableFuture.supplyAsync(() -> {
                        Actions.turnOnMusic(player);
                        return "";
                    });
                }
                ;
                if (value.equals("false")) Actions.turnOffMusic(player);
                break;
        }
        log.info("OK");
    }
}