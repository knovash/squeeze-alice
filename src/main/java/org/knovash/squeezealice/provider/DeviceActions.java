package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Action;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Server;
import org.knovash.squeezealice.pojo.pojoActions.Device;

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
        Player player = Server.playerByName(name);
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
                Action.channel(player, channel);
                SmartHome.lastChannel = channel;
                break;
            case ("on"):
                log.info("ON/OFF PLAY/PAUSE " + value);
                if (value.equals("true")) Action.turnOnMusicSpeaker(player);
                if (value.equals("false")) Action.turnOffSpeaker(player);
                break;
        }
        log.info("OK");
    }
}
