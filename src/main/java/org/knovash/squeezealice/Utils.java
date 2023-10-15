package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.knovash.squeezealice.Fluent.uriGetHeader;
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

    public static String httpExchafffngeGetBody(HttpExchange httpExchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
        if (isr == null) return null;
        BufferedReader br = new BufferedReader(isr);
        int b;
        StringBuilder buf = new StringBuilder(512);
        while ((b = br.read()) != -1) {
            buf.append((char) b);
        }
        br.close();
        isr.close();
        String body = buf.toString();
        return body;
    }

    public static boolean isLms(String ip) {
        log.info("CHECK IF IP IS LMS: " + ip);
        String uri = "http://" + ip + ":9000";
        HttpResponse response = uriGetHeader(uri);
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

    public static String myIp() {
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
        if (lmsIp != null && isLms(lmsIp)) {
            log.info("IP FROM FILE: " + lmsIp);
            return lmsIp;
        }
        log.info("NO PREVIOUS FILE. START SEARCH NETWORK...");
        String myip = Utils.myIp();
//        log.info("MY IP " + myip);
//        String lmsIp = null;
        Integer start = 1;
        while (lmsIp == null && start < 255) {
//            log.info("START FROM: " + start);
            lmsIp = IntStream
                    .range(start, start + 50)
                    .boxed()
//                    .peek(s -> log.info("INDEX: " + s))
                    .map(index -> CompletableFuture.supplyAsync(() -> Utils.ipIsReachable(myip, Integer.valueOf(index))))
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

    public static String appIdPlayer(String appId) {
        String playerName = "HomePod";
        switch (appId) {
            case ("CEE4701A73C8D5113DB40E35CDA9ECBDB6FC2CDCFF8CFD73A1ADEB2607C67AD7"):
                playerName = "JBL black";
                break;
            case ("76F751A76299DE71E1E9784E207AFC2BA1AB01361D8F8B9483A857FA87C087FA"):
                playerName = "HomePod";
                break;
        }
        return playerName;
    }

    public static String ping(Integer index) {
        String lmsip = "192.168.1.52";
        String ip = "192.168.1." + index;
        log.info("PING " + index);
        if (lmsip.equals(ip)) return ip;
        return null;
    }

    public static String ipIsReachable(String fullIp, Integer index) {
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
            if (address.isReachable(1000) && isLms(ipTry)) {
                log.info("IP IS LMS: " + ipTry);
                return ipTry;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}