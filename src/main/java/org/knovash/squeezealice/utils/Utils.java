package org.knovash.squeezealice.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.Requests.headToUriForHttpResponse;

@Log4j2
public class Utils {

    public static Map<String, String> altNames;

//    public static String getPlayerByNameInQuery(String name) {
//        log.info("NAME: " + name + " ALT NAMES: " + altNames);
//        if (altNames.containsKey(name)) {
//            name = altNames.get(name);
//        } else {
//            log.info("NO ALT NAME FOR " + name);
//        }
//        return name;
//    }


//    public static String timeVolumeGet(Player player) {
//        return player.schedule.entrySet().toString();
//    }
//
//    public static String timeVolumeSet(Player player, HashMap<String, String> parameters) {
//        Integer time = Integer.valueOf(parameters.get("time"));
//        Integer volume = Integer.valueOf(parameters.get("volume"));
//        player.schedule.put(time, volume);
//        return "SET " + time + " : " + volume;
//    }
//
//    public static String timeVolumeDel(Player player, HashMap<String, String> parameters) {
//        Integer time = Integer.valueOf(parameters.get("time"));
//        player.schedule.remove(time);
//        return "REMOVED time:" + time;
//    }

    public static boolean checkIpIsLms(String ip) {
        String uri = "http://" + ip + ":" + config.lmsPort;
        HttpResponse response = headToUriForHttpResponse(uri);
        if (response == null) return false;
        Header[] server = response.getHeaders("Server");
        if (server == null) return false;
        String header = server[0].toString();
        log.info("IP: " + ip + " HEADER: " + header);
        if (header.contains("Logitech Media Server") || header.contains("Lyrion Music Server")) return true;
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


    public static boolean isCyrillic(String text) {
        Pattern cyril = Pattern.compile("[а-ябА-Я\\s]*");
        Matcher matchCyril = cyril.matcher(text);
        Boolean result = matchCyril.matches();
//        log.info(text + " " + result);
        return result;
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
            if (address.isReachable(1000) && checkIpIsLms(ipTry)) {
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

    public static Map<Integer, Integer> stringSplitToIntMap2(String text, String split1, String split2) {
        if (text == null || text.isEmpty()) {
            return new HashMap<>();
        }
        return Arrays.stream(text.split(split1))
                .map(s -> s.split(split2))
                .filter(arr -> arr.length == 2) // Игнорировать некорректные элементы
                .collect(Collectors.toMap(
                        arr -> Integer.parseInt(arr[0]),
                        arr -> Integer.parseInt(arr[1]),
                        (oldVal, newVal) -> newVal // Обработка дубликатов: берем последнее значение
                ));
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
//        log.info("START " + message);
        String result = message.replace("дж", "j")
                .replace("у", "oo");
        char[] abcCyr = {' ', 'а', 'б', 'в', 'г', 'д', 'ѓ', 'е', 'ж', 'з', 'ѕ', 'и', 'ј', 'к', 'л', 'љ', 'м', 'н', 'њ', 'о', 'п', 'р', 'с', 'т', 'ќ', 'у', 'ф', 'х', 'ц', 'ч', 'џ', 'ш', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Ѓ', 'Е', 'Ж', 'З', 'Ѕ', 'И', 'Ј', 'К', 'Л', 'Љ', 'М', 'Н', 'Њ', 'О', 'П', 'Р', 'С', 'Т', 'Ќ', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Џ', 'Ш', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '/', '-'};
        String[] abcLat = {" ", "a", "b", "v", "g", "d", "]", "e", "zh", "z", "y", "i", "j", "k", "l", "q", "m", "n", "w", "o", "p", "r", "s", "t", "'", "u", "f", "h", "c", "ch", "x", "{", "e", "u", "y", "A", "B", "V", "G", "D", "}", "E", "Zh", "Z", "Y", "I", "J", "K", "L", "Q", "M", "N", "W", "O", "P", "R", "S", "T", "KJ", "U", "F", "H", "C", ":", "X", "{", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "/", "-"};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < result.length(); i++) {
            for (int x = 0; x < abcCyr.length; x++) {
                if (result.charAt(i) == abcCyr[x]) {
                    builder.append(abcLat[x]);
                }
            }
        }
        result = builder.toString();
//        log.info(message + " -> " + result);
        return result;
    }

//    public static void restart() {
//        try {
//            Runtime.getRuntime().exec("systemctl restart squeeze-alice.service");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static void reboot() {
//        try {
//            Runtime.getRuntime().exec("reboot");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static void timerRequestPlayersState(int period) {
//      обновить состояние плееров в ЛМС. опросить все плееры сохранить время если играет
        log.info("TIMER REQUEST PLAYERS STATE UPDATE");
        Runnable drawRunnable = new Runnable() {
            public void run() {
                lmsPlayers.updateLmsPlayers();
            }
        };
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleAtFixedRate(drawRunnable, 5, period, TimeUnit.MINUTES);
    }

    public static void readAliceIdInRooms() {
        log.debug("READ ALICE IN ROOMS FROM rooms.json");
        idRooms = JsonUtils.jsonFileToMap(SwitchVoiceCommand.saveToFileJson, String.class, String.class);
        if (idRooms == null) {
            idRooms = new HashMap<>();
            log.info("READ NO ROOMS");
            return;
        }
        log.info("ALICE IN ROOMS FROM rooms.json: " + Main.idRooms);
    }

//    public static String widget() {
//        log.info("WIDGET");
//        Yandex.getRoomsAndDevices();
//        YandexInfo.Device ddd = Yandex.yandexInfo.devices.stream()
//                .filter(device -> device.name.equals("душ"))
//                .filter(device -> device.type.equals("devices.types.sensor"))
//                .peek(device -> log.info(device.name + " " + device.properties.get(0).state.value))
//                .findFirst()
//                .orElse(null);
//        log.info(ddd);
//
//        return ddd.properties.get(0).state.value.toString();
//    }

    public static String getCorrectRoomName(String approxRoomName) {
        log.info("GET CORRECT ROOM NAME BY: " + approxRoomName);
//        String correctRoom = Levenstein.getNearestElementInList(target, rooms);
        String correctRoom = Levenstein.search(approxRoomName, rooms);
        if (correctRoom == null) {
            log.info("ERROR ROOM NOT EXISTS IN YANDEX SMART HOME " + approxRoomName);
            return null;
        }
        log.info("CORRECT ROOM: " + approxRoomName + " -> " + correctRoom);
        return correctRoom;
    }

    public static String getCorrectPlayerName(String player) {
        log.info("START: " + player);
        List<String> players = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        player = Utils.convertCyrilic(player);
//        String correctPlayer = Levenstein.getNearestElementInList(player, players);
        String correctPlayer = Levenstein.getNearestElementInListWord(player, players);
//        String correctPlayer = Levenstein.search(player, players);
        if (correctPlayer == null) log.info("ERROR PLAYER NOT EXISTS IN LMS ");
        log.info("CORRECT PLAYER: " + player + " -> " + correctPlayer);
        return correctPlayer;
    }

    public static List<String> linesFromList(List<String> list, int index, int lines) {
//   показывать из плейлиста часть сторок до и после играющего трека
        int left = lines;
        int start = Math.max(0, index - left);
        int end = Math.min(list.size(), start + lines * 2 + 1);
        int delta = lines * 2 + 1 - (end - start);
        int start2 = Math.max(0, start - delta);
        log.info("INDEX: " + index + " LEFT: " + left + " START: " + start + " END: " + end + " DELTA: " + delta);
        return new ArrayList<>(list.subList(start2, end));
    }
}