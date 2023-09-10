package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static org.knovash.squeezealice.Main.server;

@Log4j2
public class Utils {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");

    public static void addPlayerAlterName(){
        Map<String ,String > alternames = new HashMap<>();
        alternames = new HashMap<>(Map.of(
                "homepod","HomePod",
                "bathroom","Bathroom",
                "ggmm","GGMM_E2_2650",
                "mibox","Mi Box"));

    }

    public static void changePlayerValue(HashMap<String, String> parameters) {
        String playerName = parameters.get("player");
        String valueName = parameters.get("value_name");
        Integer newValue = Integer.valueOf(parameters.get("value"));
        Field field = null;
        playerName = alterName(playerName);
        Player player = Server.playerByName(playerName);
        log.info("PLAYER: " + playerName + " VALUE NAME: " + valueName + " NEW VALUE: " + newValue);
        try {
            field = Player.class.getField(valueName);
            field.set(player, newValue);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            log.info("VALUE SET: " + valueName + " = " + field.get(player));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        server.writeFile();
    }

    public static String alterName(String name) {
        if (bundle.containsKey(name)) {
            name = bundle.getString(name);
        } else {
            log.info("NO ALTER NAME FOR " + name);
        }
        return name;
    }
}