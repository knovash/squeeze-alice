package org.knovash.squeezealice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.pojo.TimeVolume;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
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

    public static void generateAltNamesFile() {
        log.info("GET ALT NAMES");
        File file = new File("alt_names.json");
        Map<String, String> namesGenerated = new HashMap<>();
        Map<String, String> namesFromFile = new HashMap<>();
        if (Utils.altNames == null) Utils.altNames = new HashMap<>();
        // generate
        server.players.forEach(player -> {
            String altName = player.name
                    .replace(" ", "")
                    .replace("_", "")
                    .toLowerCase();
            namesGenerated.put(altName, player.name);
        });
        // get from file
        if (file.exists()) namesFromFile = JsonUtils.jsonFileToMap("alt_names.json", String.class, String.class);

        Utils.altNames.putAll(namesFromFile);
        Utils.altNames.putAll(namesGenerated);
        JsonUtils.mapToJsonFile(Utils.altNames, "alt_names.json");
        log.info("WRITE alt_names.json " + Utils.altNames);
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
        log.info("NAME: " + name + " ALT NAMES: " + altNames);
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

    public static String timeVolumeGet(Player player) {
        return player.timeVolume.entrySet().toString();
    }

    public static String timeVolumeSet(Player player, HashMap<String, String> parameters) {
        Integer time = Integer.valueOf(parameters.get("time"));
        Integer volume = Integer.valueOf(parameters.get("volume"));
        player.timeVolume.put(time, volume);
        return "SET " + time + " : " + volume;
    }

    public static String timeVolumeDel(Player player, HashMap<String, String> parameters) {
        Integer time = Integer.valueOf(parameters.get("time"));
        player.timeVolume.remove(time);
        return "REMOVED time:" + time;
    }

    public static String credentials(HashMap<String, String> parameters) {
        String id = parameters.get("id");
        String secret = parameters.get("secret");
        if (id == null || secret == null) return "CRED ERROR";
        Spotify.createCredFile(id, secret);
        return "CRED SET";
    }

    public static String backupServer(HashMap<String, String> parameters) {
        String stamp = LocalDate.now().toString() + "-" + LocalTime.now().toString();
        server.writeServerFile("server-backup-" + stamp);
        return "BACKUP SERVER";
    }

    public static void favoritePrev(Player player, HashMap<String, String> parameters) {
        Integer time = Integer.valueOf(parameters.get("time"));
        player.play(1);
        player.timeVolume.remove(time);
    }

    public static void favoriteNext(Player player, HashMap<String, String> parameters) {
        Integer time = Integer.valueOf(parameters.get("time"));
        player.timeVolume.remove(time);
    }

    public static String readBodyJsonCommand(HttpExchange httpExchange) throws IOException {
        String command = null;
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        int b;
        StringBuilder buf = new StringBuilder(512);
        while ((b = br.read()) != -1) {
            buf.append((char) b);
        }
        br.close();
        isr.close();
        String json = buf.toString();
        command = JsonUtils.jsonGetValue(json, "command");
        log.info("COMMAND " + command);
        return command;
    }


    public static HashMap<String, String> getQueryParameters(String query) {
        HashMap<String, String> parameters = new HashMap<>();
        Optional.ofNullable(Arrays.asList(query.split("&"))).orElseGet(Collections::emptyList)
                .stream()
                .filter(s -> s.contains("="))
                .map(s -> s.split("="))
                .filter(Objects::nonNull)
                .forEach(s -> parameters.put(s[0], s[1]));
        return parameters;
    }

}