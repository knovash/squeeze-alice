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
    private static Map<String, String> altNames;

    public static void addPlayerAlternativeName() {
        Map<String, String> altNames = new HashMap<>();
        altNames = new HashMap<>(Map.of(
                "homepod", "HomePod",
                "bathroom", "Bathroom",
                "ggmm", "GGMM_E2_2650",
                "mibox", "Mi Box"));
        JsonUtils.pojoToJsonFile(altNames, "alter.json");
    }

    public static void readAltNames() {
        altNames = JsonUtils.jsonFileToMap("alt_names.json", String.class, String.class);
        if (altNames == null) {
            log.info("!!! ERROR !!! FILE NOT FOUND 'alt_names.json'");
        } else {
            log.info("READ ALT NAMES: " + altNames);
        }
    }


    public static void changePlayerValue(HashMap<String, String> parameters) {
        String playerName = parameters.get("player");
        String valueName = parameters.get("value_name");
        Integer newValue = Integer.valueOf(parameters.get("value"));
        Field field = null;
        playerName = altPlayerName(playerName);
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

    public static String altPlayerName(String name) {
        altNames.get("dd");
        if (bundle.containsKey(name)) {
            name = bundle.getString(name);
        } else {
            log.info("NO QUERY NAME FOR " + name);
        }
        return name;
    }

    public static void altNameAdd(HashMap<String, String> parameters)  {
//        http://localhost:8001/cmd?action=alt_name_add&query_name=ggmm&lms_name=4
        String query_name = parameters.get("player");
        String lms_name = parameters.get("value_name");
        Utils.altNames.put(query_name,lms_name);
        JsonUtils.pojoToJsonFile(altNames,"alt_names.json");
    }

    public static String altPlayerNameMap(String name) {

        if (!altNames.isEmpty() && altNames.containsKey(name)) {
            altNames.get(name);
        } else {

            log.info("ALT NAME ISNT SET FOR " + name);
            log.info("please add to alt_names.json {\"player_name\" : \"Player Name\"}");
        }


        if (bundle.containsKey(name)) {
            name = bundle.getString(name);
        } else {
            log.info("NO QUERY NAME FOR " + name);
        }
        return name;
    }
}