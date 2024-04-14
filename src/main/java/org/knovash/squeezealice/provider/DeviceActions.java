package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Actions;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.spotify.Spotify;

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

        if (name.equals("Spotify")) {
            log.info("SPOTY");
//            Spotify.transfer()
            return;
        }

        // обратиться к девайсу и изменить его состояние
        Player player = lmsPlayers.getPlayerByName(name);
        if (player == null) return;
        log.info("PLAYER: " + player.name);
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
                log.info("CHANNEL: " + value + " LAST CHANNEL: " + lmsPlayers.lastChannel);
                int channel;
                if (relative != null && relative.equals(true)) {
                    if (player.lastChannel != 0) channel = player.lastChannel + 1;
                    else channel = lmsPlayers.lastChannel + 1;
                } else {
                    channel = Integer.parseInt(value);
                }
//                Actions.playChannel(player, channel);
                CompletableFuture.supplyAsync(() -> {
                    Actions.playChannel(player, channel);
                    return "";
                });
                lmsPlayers.lastChannel = channel;
                break;
            case ("on"):
                log.info("ON/OFF PLAY/PAUSE " + value);
                if (value.equals("true")) {
                    log.info("TURN OFF");
//                    Actions.turnOnMusic(player);
                    CompletableFuture.supplyAsync(() -> {
                        Actions.turnOnMusic(player);
                        return "";
                    });
                }
                if (value.equals("false")) {
                    log.info("TURN OFF");
//                    Actions.turnOffMusic(player);
                    CompletableFuture.supplyAsync(() -> {
                        Actions.turnOffMusic(player);
                        return "";
                    });
                }
                break;
        }
        log.info("OK");
    }
}