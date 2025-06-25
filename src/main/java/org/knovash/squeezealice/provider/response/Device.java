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

    public String type = "devices.types.media_device.receiver";
    public String name = "музыка";
    public String id; // TODO переименовать в external_id
    public String room;
    public List<Capability> capabilities = new ArrayList<>();
    public List<Property> properties = new ArrayList<>();
    public List<String> aliases = new ArrayList<>();
    public String external_id; // TODO удалить
    public String skill_id;
    public String household_id;
    public List<String> groups;
    public ActionResult action_result;
    public String error_code = null;
    public String error_message = null;

    //    public YandexInfo.QuasarInfo quasar_info;

    public String playerName() {
        Player player = lmsPlayers.playerByDeviceId(this.id);
        String playerName = null;
        if (player != null) playerName = player.name;
        return playerName;
    }

    public Player player() {
        Player player = lmsPlayers.playerByDeviceId(this.id);
        return player;
    }

}