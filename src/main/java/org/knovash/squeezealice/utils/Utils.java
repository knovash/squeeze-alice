package org.knovash.squeezealice.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.SmartHome;

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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.Requests.headToUriForHttpResponse;

@Log4j2
public class Utils {

    public static Map<String, String> altNames;

    public static void changePlayerValue(HashMap<String, String> parameters) {
        String playerName = parameters.get("player");
        String valueName = parameters.get("value_name");
        Integer newValue = Integer.valueOf(parameters.get("value"));
        Field field = null;
        playerName = getPlayerByNameInQuery(playerName);
        Player player = lmsPlayers.getPlayerByName(playerName);
        log.info("PLAYER: " + playerName + " VALUE NAME: " + valueName + " NEW VALUE: " + newValue);
        try {
            field = Player.class.getField(valueName);
            field.set(player, newValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            log.info("VALUE SET: " + valueName + " = " + field.get(player));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        lmsPlayers.write();
    }

    public static String getPlayerByNameInQuery(String name) {
        log.info("NAME: " + name + " ALT NAMES: " + altNames);
        if (altNames.containsKey(name)) {
            name = altNames.get(name);
        } else {
            log.info("NO ALT NAME FOR " + name);
        }
        return name;
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

    public static String timeVolumeGet(Player player) {
        return player.schedule.entrySet().toString();
    }

    public static String timeVolumeSet(Player player, HashMap<String, String> parameters) {
        Integer time = Integer.valueOf(parameters.get("time"));
        Integer volume = Integer.valueOf(parameters.get("volume"));
        player.schedule.put(time, volume);
        return "SET " + time + " : " + volume;
    }

    public static String timeVolumeDel(Player player, HashMap<String, String> parameters) {
        Integer time = Integer.valueOf(parameters.get("time"));
        player.schedule.remove(time);
        return "REMOVED time:" + time;
    }

    public static boolean checkLmsIp(String ip) {
        log.info("CHECK LMS IP: " + ip);
        String uri = "http://" + ip + ":" + lmsPort;
        HttpResponse response = headToUriForHttpResponse(uri);
        if (response == null) return false;
        Header[] server = response.getHeaders("Server");
        if (server == null) return false;
        String header = server[0].toString();
        log.info("CHECK LMS HEADER: " + header);
        if (header.contains("Logitech Media Server") || header.contains("Lyrion Music Server")) {
            log.info("CHECK LMS IP OK " + ip);
            return true;
        }
        log.info("IP NOT LMS");
        return false;
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

    public static boolean searchLmsIp() {
        log.info("");
        log.info("SEARCH LMS IP");
        String lmsIp = null;
        String myip = Utils.getMyIpAddres();
        if (Utils.checkLmsIp(myip)) {
            lmsIp = myip;
        } else {

            Integer start = 1;
            while (lmsIp == null && start < 150) {
                lmsIp = IntStream
                        .range(start, start + 50)
                        .boxed()
                        .map(index -> CompletableFuture.supplyAsync(() -> Utils.checkIp(myip, Integer.valueOf(index))))
                        .collect(Collectors.collectingAndThen(Collectors.toList(), cfs -> cfs.stream().map(CompletableFuture::join)))
                        .filter(Objects::nonNull)
                        .findFirst().orElse(null);
                start = start + 50;
            }
        }
        log.info("LMS IP: " + lmsIp);
        if (lmsIp != null) {
            JsonUtils.valueToJsonFile("lms_ip", lmsIp);
            Main.lmsIp = lmsIp;
            Main.lmsUrl = "http://" + Main.lmsIp + ":" + Main.lmsPort + "/jsonrpc.js/";
            return true;
        }
        log.info("LMS NOT FOUND. please check \"config.json\"");
        return false;
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

    public static String checkIp(String fullIp, Integer index) {
        if (index > 124) return null;
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(fullIp);
        } catch (UnknownHostException ignored) {
        }
        byte[] ip = inetAddress.getAddress();
        ip[3] = Byte.parseByte(String.valueOf(index));
        String ipTry = null;
        try {
            InetAddress address = InetAddress.getByAddress(ip);
            ipTry = address.toString().substring(1);
            if (address.isReachable(1000) && checkLmsIp(ipTry)) {
                log.info("LMS IP OK: " + ipTry);
                return ipTry;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Map<Integer, Integer> stringSplitToIntMap(String text, String split1, String split2) {
        return Arrays.stream(text.split(split1))
                .map(s -> s.split(split2))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]))
                .entrySet().stream()
                .collect(Collectors.toMap(entry -> Integer.valueOf(entry.getKey()), entry -> Integer.valueOf(entry.getValue())));
    }

    public static String mapToString(Map<Integer, Integer> headerMap) {
        if (headerMap == null) return "---";
        return headerMap.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(","));
    }

    public static String readFile(String path) throws IOException {
        Path filePath = Path.of(path);
        String content = Files.readString(filePath);
        return content;
    }

    public static void sleep(int seconds) {
        try {
            Thread.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //    https://stackoverflow.com/questions/10893313/how-to-convert-cyrillic-letters-to-english-latin-in-java-string
    public static String convertCyrilic(String message) {
        message = message.replace("дж", "j");
        char[] abcCyr = {' ', 'а', 'б', 'в', 'г', 'д', 'ѓ', 'е', 'ж', 'з', 'ѕ', 'и', 'ј', 'к', 'л', 'љ', 'м', 'н', 'њ', 'о', 'п', 'р', 'с', 'т', 'ќ', 'у', 'ф', 'х', 'ц', 'ч', 'џ', 'ш', 'А', 'Б', 'В', 'Г', 'Д', 'Ѓ', 'Е', 'Ж', 'З', 'Ѕ', 'И', 'Ј', 'К', 'Л', 'Љ', 'М', 'Н', 'Њ', 'О', 'П', 'Р', 'С', 'Т', 'Ќ', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Џ', 'Ш', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '/', '-'};
        String[] abcLat = {" ", "a", "b", "v", "g", "d", "]", "e", "zh", "z", "y", "i", "j", "k", "l", "q", "m", "n", "w", "o", "p", "r", "s", "t", "'", "u", "f", "h", "c", "ch", "x", "{", "A", "B", "V", "G", "D", "}", "E", "Zh", "Z", "Y", "I", "J", "K", "L", "Q", "M", "N", "W", "O", "P", "R", "S", "T", "KJ", "U", "F", "H", "C", ":", "X", "{", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "/", "-"};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            for (int x = 0; x < abcCyr.length; x++) {
                if (message.charAt(i) == abcCyr[x]) {
                    builder.append(abcLat[x]);
                }
            }
        }
        return builder.toString();
    }

    public static void restart() {
        try {
            Runtime.getRuntime().exec("systemctl restart squeeze-alice.service");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reboot() {
        try {
            Runtime.getRuntime().exec("reboot");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void timerRequestPlayersState(int priod) {
        log.info("TIMER REQUEST PLAYERS STATE UPDATE");
        Runnable drawRunnable = new Runnable() {
            public void run() {
                lmsPlayers.updateServerStatus();
            }
        };
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleAtFixedRate(drawRunnable, 5, priod, TimeUnit.MINUTES);
    }


    public static void readConfig() {
        log.info("READ CONFIG FROM config.json");
        config = JsonUtils.jsonFileToMap("config.json", String.class, String.class);
        if (config == null) return;
        Main.lmsIp = config.get("lmsIp");
        Main.lmsPort = config.get("lmsPort");
        Main.port = Integer.parseInt(config.get("port"));
        Main.silence = config.get("silence");
        Main.lmsUrl = "http://" + Main.lmsIp + ":" + Main.lmsPort + "/jsonrpc.js/";
//        log.info("LMS IP: " + Main.lmsIp);
//        log.info("LMS PORT: " + Main.lmsPort);
        log.info("LMS URL: " + Main.lmsUrl);
        log.info("THIS PORT: " + Main.port);
        log.info("SILENCE: " + Main.silence);
    }

    public static void writeConfig() {
        log.info("WRITE CONFIG TO config.json");
        config = new HashMap<>();
        config.put("lmsIp", lmsIp);
        config.put("lmsPort", lmsPort);
        config.put("port", String.valueOf(port));
        config.put("silence", silence);
        log.info(config);
        JsonUtils.mapToJsonFile(config, "config.json");
    }


    public static void readRooms() {
        log.info("READ ROOMS FROM rooms.json");
        rooms = JsonUtils.jsonFileToMap("rooms.json", String.class, String.class);
        if (rooms == null) {
            rooms = new HashMap<>();
            log.info("READ NO ROOMS");
            return;
        }
        log.info("ROOMS: " + Main.rooms);
    }

    public static void writeRooms() {
        log.info("WRITE ROOMS TO rooms.json");
        log.info("ROOMS: " + Main.rooms);
        JsonUtils.mapToJsonFile(rooms, "rooms.json");
    }

    public static String listDevicesRooms() {
        return SmartHome.devices.stream().map(device -> device.room).collect(Collectors.joining(","));
    }
}