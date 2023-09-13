package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.server;

@Log4j2
public class Utils {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");
    public static Map<String, String> altNames;

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
            log.info("FILE NOT FOUND 'alt_names.json'");
        } else {
            log.info("READ ALT NAMES: " + altNames);
        }
    }

    public static void generateAltNamesFile() {
        Map<String, String> newAltNames = new HashMap<>();
        if (Utils.altNames == null) {Utils.altNames = new HashMap<>();}
        server.players.forEach(player -> {
            String altName = player.name
                    .replace(" ", "")
                    .replace("_", "")
                    .toLowerCase();
            newAltNames.put(altName, player.name);
        });
        Map<String, String> mapRead = new HashMap<>();
        File file = new File("alt_names.json");
        if (file.exists()) {mapRead = JsonUtils.jsonFileToMap("alt_names.json", String.class, String.class);}
        Utils.altNames.putAll(mapRead);
        Utils.altNames.putAll(newAltNames);
        JsonUtils.mapToJsonFile(Utils.altNames, "alt_names.json");
        log.info(Utils.altNames);
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
        server.writeServerFile();
    }

    public static String altPlayerName(String name) {
        log.info("NAME: " + name);
        log.info("ALT NAMES: " + altNames);
        if (altNames.containsKey(name)) {
            name = altNames.get(name);
        } else {
            log.info("NO ALT NAME FOR " + name);
        }
        return name;
    }

    public static void altNameAdd(HashMap<String, String> parameters) {
//        http://localhost:8001/cmd?action=alt_name_add&query_name=ggmm&lms_name=4
        String query_name = parameters.get("player");
        String lms_name = parameters.get("value_name");
        Utils.altNames.put(query_name, lms_name);
        JsonUtils.pojoToJsonFile(altNames, "alt_names.json");
    }


    public static String logLastLines(HashMap<String, String> parameters) {
        int lastCount = 5;
        if (parameters.containsKey("value")) {
            lastCount = Integer.parseInt(parameters.get("value"));
        }
        log.info("lastCount " + lastCount);
        String filePath = "log/log.txt";
        File file = new File(filePath);
        List<String> readfromFile = null;
        List<String> lastLines = new ArrayList<>();
        try {
            readfromFile = Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int start = readfromFile.size() - lastCount;
        for (int i = start; i < readfromFile.size(); i++) {
            lastLines.add(readfromFile.get(i));
        }
        String result = lastLines.stream().collect(Collectors.joining("\n"));
        return result;
    }

    public static String state() {
        String json = JsonUtils.pojoToJson(server);
        return json;
    }


}