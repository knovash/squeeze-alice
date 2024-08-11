package org.knovash.squeezealice.provider.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;

import java.util.ArrayList;
import java.util.List;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    public String id;
    public String name = "музыка";
    public String room;
    public String type;
    public List<Capability> capabilities = new ArrayList<>();
    public List<Property> properties = new ArrayList<>();
    public List<String> aliases = new ArrayList<>();

//    public Player lmsGetPlayerByDeviceId() {
//        log.info("THIS DEVICE ID: " + this.id + " ROOM: " + this.room);
//       if (this.id == null) return null;
//        Player player = lmsPlayers.players.stream()
////                .peek(p -> log.info("PLAYER deviceId: " + p.deviceId + " ROOM: " + p.roomPlayer))
//                .filter(p -> p.roomPlayer != null)
//                .filter(p -> p.deviceId != null)
//                .filter(p -> p.deviceId.equals(this.id))
//                .findFirst().orElse(null);
//        log.info("PLAYER: " + player);
//        return player;
//    }


    // TODO REMOVE BY  lmsPlayers.getPlayerNameByDeviceid(device.id);
    public String takePlayerNameById() {
        log.info("TAKE PLAYER NAME BY DEVICE ID");
        Player player = lmsPlayers.getPlayerByDeviceId(id);
        log.info("PLAYER: " + player);
        if (player == null) return null;
        log.info("PLAYER NAME: " + player.name);
        return player.name;
    }
}