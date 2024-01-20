package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.SwitchAliceCommand;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.LmsPlayers;
import org.knovash.squeezealice.provider.responseAction.Device;

@Log4j2
public class DeviceActions {

    public static void runInstance(Device device) {
        int id = Integer.parseInt(device.id);
        String value = device.capabilities.get(0).state.value;
        String name = SmartHome.getByDeviceId(id).customData.lmsName;
//        String relative = String.valueOf(device.capabilities.get(0).state.relative);
        Boolean relative = device.capabilities.get(0).state.relative;
        String type = device.capabilities.get(0).type;
        String instance = device.capabilities.get(0).state.instance;
        log.info("ID: " + id);
        log.info("VALUE: " + value);
        log.info("RELATIVE: " + relative); // boolean
        log.info("TYPE: " + type);
        log.info("INSTANCE: " + instance);
        log.info("NAME " + name);


        // обратиться к девайсу и изменить его состояние
        Player player = LmsPlayers.playerByName(name);
        log.info("PLAYER: " + player);
        log.info(relative);
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
                SwitchAliceCommand.channel(player, channel);
                SmartHome.lastChannel = channel;
                break;
            case ("on"):
                log.info("ON/OFF PLAY/PAUSE " + value);
                if (value.equals("true")) SwitchAliceCommand.turnOnMusicSpeaker(player);
                if (value.equals("false")) SwitchAliceCommand.turnOffSpeaker(player);
                break;
        }
        log.info("OK");
    }
}
