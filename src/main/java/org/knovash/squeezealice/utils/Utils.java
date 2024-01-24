package org.knovash.squeezealice.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.provider.SmartHome;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.knovash.squeezealice.Requests.headByUriForResponse;
import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class Utils {

    public static Map<String, String> altNames;

    public static void generatePlayersAltNamesToFile() {
        log.info("ALT NAMES");
        File file = new File("alt_names.json");
        Map<String, String> namesGenerated = new HashMap<>();
        Map<String, String> namesFromFile = new HashMap<>();
        if (Utils.altNames == null) Utils.altNames = new HashMap<>();
        // generate
        lmsPlayers.players.forEach(player -> {
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
        playerName = getAltPlayerNameByName(playerName);
        Player player = lmsPlayers.getPlayerByName(playerName);
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
        lmsPlayers.write();
    }

    public static String getAltPlayerNameByName(String name) {
        log.info("NAME: " + name + " ALT NAMES: " + altNames);
        if (altNames.containsKey(name)) {
            name = altNames.get(name);
        } else {
            log.info("NO ALT NAME FOR " + name);
        }
        return name;
    }

    public static String getPlayerName(String name) {
        log.info(name);
        String finalName = name;
        Optional<String> result = altNames.entrySet()
                .stream()
                .filter(entry -> finalName.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst();
        return result.get();
    }

    public static void addPlayerAltName(HashMap<String, String> parameters) {
//        http://localhost:8001/cmd?action=alt_name_add&query_name=ggmm&lms_name=4
        String query_name = parameters.get("player");
        String lms_name = parameters.get("value_name");
        Utils.altNames.put(query_name, lms_name);
        JsonUtils.pojoToJsonFile(altNames, "alt_names.json");
    }

    public static String logLastLines(HashMap<String, String> parameters) {
        int lastCount = 50;
        if (parameters.containsKey("value")) {
            lastCount = Integer.parseInt(parameters.get("value"));
        }
        log.info("LOG LINES " + lastCount);
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

    public static String statePlayers() {
        String json = JsonUtils.pojoToJson(lmsPlayers);
        return json;
    }

    public static String players() {
        String json = JsonUtils.pojoToJson(Main.lmsPlayers.players);
        return json;
    }

    public static String stateDevices() {
        String json = JsonUtils.pojoToJson(SmartHome.devices);
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

    public static String backupServer(HashMap<String, String> parameters) {
        String stamp = LocalDate.now().toString() + "-" + LocalTime.now().toString();
        lmsPlayers.write("server-backup-" + stamp);
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

    public static boolean checkIpIsLms(String ip) {
        log.info("CHECK IF IP IS LMS: " + ip);
        String uri = "http://" + ip + ":9000";
        HttpResponse response = headByUriForResponse(uri);
        if (response == null) {
//            log.info("NOT LMS IP");
            return false;
        }
        Header[] server = response.getHeaders("Server");
        String header = server[0].toString();
        log.info("HEADER: " + header);
        if (header.contains("Logitech Media Server")) log.info("IS LMS IP OK");
        return header.contains("Logitech Media Server");
    }

    public static String getMyIpAddres() {
        String myip = null;
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            try {
                if (!networkInterface.isUp())
                    continue;
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
//                log.info(networkInterface.getDisplayName() + " " + addr.getHostAddress());
                if (addr.getHostAddress().contains("192.")) {
                    myip = addr.getHostAddress();
                    log.info("MY IP: " + myip);
                }
            }
        }
        return myip;
    }

    public static String searchLmsIp() {
        log.info("SEARCH LMS IN NETWORK");
        String lmsIp = null;
        log.info("TRY GET IP FROM PREVIOUS SEARCH RESULT IN lms_ip.json");
        lmsIp = JsonUtils.valueFromJsonFile("lms_ip.json");
//        log.info("IP FROM FILE: " + lmsIp);
        if (lmsIp != null && checkIpIsLms(lmsIp)) {
            log.info("IP FROM FILE: " + lmsIp);
            return lmsIp;
        }
        log.info("NO PREVIOUS FILE. START SEARCH NETWORK...");
        String myip = Utils.getMyIpAddres();
//        log.info("MY IP " + myip);
//        String lmsIp = null;
        Integer start = 1;
        while (lmsIp == null && start < 255) {
            lmsIp = IntStream
                    .range(start, start + 50)
                    .boxed()
//                    .peek(s -> log.info("INDEX: " + s))
                    .map(index -> CompletableFuture.supplyAsync(() -> Utils.checkIpIsReachable(myip, Integer.valueOf(index))))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), cfs -> cfs.stream().map(CompletableFuture::join)))
                    .filter(Objects::nonNull)
                    .findFirst().orElse(null);
//            log.info("TRY: " + lmsIp);
            start = start + 50;
        }
        log.info("LMS IP: " + lmsIp);

        if (lmsIp != null) JsonUtils.valueToJsonFile("lms_ip", lmsIp);
        return lmsIp;
    }

    public static boolean isCyrillic(String text) {
        Pattern cyril = Pattern.compile("[а-ябА-Я\\s]*");
        Matcher matchCyril = cyril.matcher(text);
        return matchCyril.matches();
    }

    public static String ping(Integer index) {
        String lmsip = "192.168.1.52";
        String ip = "192.168.1." + index;
        log.info("PING " + index);
        if (lmsip.equals(ip)) return ip;
        return null;
    }

    public static String checkIpIsReachable(String fullIp, Integer index) {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(fullIp);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        byte[] ip = inetAddress.getAddress();
        ip[3] = Byte.parseByte(String.valueOf(index));
        String ipTry = null;
        try {
            InetAddress address = InetAddress.getByAddress(ip);
            ipTry = address.toString().substring(1);
//            log.info("TRY IP... " + ipTry);
            if (address.isReachable(1000) && checkIpIsLms(ipTry)) {
                log.info("IP IS LMS: " + ipTry);
                return ipTry;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static Map<String, String> stringToMap(String text) {
//        return Arrays.stream(text.split(","))
//                .map(s -> s.replace(" ", ""))
//                .map(s -> s.split(":"))
//                .collect(Collectors.toMap(s -> s[0], s -> s[1]));
//    }

    public static Map<Integer, Integer> stringSplitToIntMap(String text, String split1, String split2) {
        return Arrays.stream(text.split(split1))
                .map(s -> s.split(split2))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]))
                .entrySet().stream()
                .collect(Collectors.toMap(entry -> Integer.valueOf(entry.getKey()), entry -> Integer.valueOf(entry.getValue())));
    }

    public static String mapToString(Map<Integer, Integer> headerMap) {
        return headerMap.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(","));
    }

    public static String readFile(String path) throws IOException {
        Path filePath = Path.of(path);
        String content = Files.readString(filePath);
        return content;
    }

    public static String timeToString(LocalTime time) {
        String timeStr = time.toString();
        return timeStr;
    }

    public static LocalTime stringToTime(String timeStr) {
        LocalTime time = LocalTime.parse(timeStr);
        return time;
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}