package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.PlayersUpdateScheduler;
import org.knovash.squeezealice.yandex.Yandex;
import org.knovash.squeezealice.utils.Utils;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.knovash.squeezealice.yandex.Yandex.sendDeviceState;

@Log4j2
public class Main {

    public static LmsPlayers lmsPlayers = new LmsPlayers();
    public static Map<String, String> idRooms = new HashMap<>();
    public static List<String> rooms = new ArrayList<>();
    public static ZoneId zoneId = ZoneId.of("Europe/Minsk");
    public static Config config = new Config();
    public static Boolean lmsServerOnline;
    public static String yandexToken = "";
    public static Links links = new Links();
    public static Hive hive;

    public static void main(String[] args) {
        log.info("TIME ZONE: " + zoneId + " TIME: " + LocalTime.now(zoneId).truncatedTo(MINUTES));
        System.setProperty("userApp.root", System.getProperty("user.home"));
        log.info("OS: " + System.getProperty("os.name") + ", user.home: " + System.getProperty("user.home") + " userApp.root: " + System.getProperty("userApp.root"));
        config.readConfigProperties();
        config.readConfigJson();
        config.write();
        links.read();
        Utils.getMyIpAddres();
        lmsPlayers.searchForLmsIp();
        Utils.readAliceIdInRooms();
        lmsPlayers.read();
        lmsPlayers.updateLmsPlayers(); // Main
        Yandex.getRoomsAndDevices();
        lmsPlayers.checkRooms();
        lmsPlayers.write();
        Server.start();
        hive = new Hive();
        hive.start();
        hive.subscribeByYandex();
        log.info("VERSION 1.2");

//        PlayersUpdateScheduler.startPeriodicUpdate(1);
//        sendDeviceState();
//        Spotify.ifExpiredRunRefersh();
//        hive.periodicCheckStart();


//        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//        scheduler.scheduleAtFixedRate(() -> {
//            if (hive != null && !hive.isConnected()) {
//                log.warn("MQTT connection lost! Attempting to reconnect...");
//                hive.stop();
//                hive.start();
//                hive.subscribeByYandex();
//            }
//        }, 1, 1, TimeUnit.MINUTES); // Проверка каждую минуту

    }
}
