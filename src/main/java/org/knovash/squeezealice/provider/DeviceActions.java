package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Actions;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Device;

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
        log.info("ID: " + id);
        log.info("NAME " + name);
        log.info("TYPE: " + type);
        log.info("INSTANCE: " + instance);
        log.info("VALUE: " + value);
        log.info("RELATIVE: " + relative);

        // обратиться к девайсу и изменить его состояние
        Player player = lmsPlayers.getPlayerByName(name);
        switch (instance) {
            case ("volume"):
                if (relative != null && relative.equals(true)) {
                    log.info("VOLUME rel: " + value);
                    if (value.contains("-")) {
                        player.volume(value);
                    } else {
                        player.volume("+" + value);
                    }
                }
                if (relative != null && relative.equals(false)) {
                    log.info("VOLUME abs: " + value);
                    player.volume(value);
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
                Actions.channel(player, channel);
                SmartHome.lastChannel = channel;
                break;
            case ("on"):
                log.info("ON/OFF PLAY/PAUSE " + value);
                if (value.equals("true")) Actions.turnOnMusicSpeaker(player);
                if (value.equals("false")) Actions.turnOffSpeaker(player);
                break;
        }
        log.info("OK");
    }
}