package org.knovash.squeezealice.provider.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.provider.YandexInfo;

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
    public String external_id;
    public String skill_id;
    public String household_id;
    public List<String> groups;
    public YandexInfo.QuasarInfo quasar_info;

    // TODO REMOVE BY  lmsPlayers.getPlayerNameByDeviceid(device.id);
    public String takePlayerNameById() {
        Player player = lmsPlayers.getPlayerByDeviceId(id);
        if (player == null) return null;
        log.info("GET BY ID PLAYER NAME: " + player.name);
        return player.name;
    }
}