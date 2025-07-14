package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.ConfigLoader;
import org.knovash.squeezealice.utils.PlayersUpdateScheduler;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.yandex.Yandex;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.MINUTES;

@Log4j2
public class Main {

    public static LmsPlayers lmsPlayers = new LmsPlayers();
    public static SmartHome smartHome = new SmartHome();
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
//        links.read();
        Utils.getMyIpAddres();
        lmsPlayers.searchForLmsIp();
        Utils.readAliceIdInRooms();

        log.info("AUTOREMOTEURLS: "+lmsPlayers.autoRemoteUrls);
        lmsPlayers.read();
        log.info("AUTOREMOTEURLS: "+lmsPlayers.autoRemoteUrls);

        lmsPlayers.updateLmsPlayers(); // Main
        Yandex.getRoomsAndDevices();
        lmsPlayers.checkRooms();
        lmsPlayers.write();
        Server.start();
        hive = new Hive();
        hive.start();
        hive.subscribeByYandex();
        log.info("VERSION 1.2");

        PlayersUpdateScheduler.startPeriodicUpdate(1);
        Spotify.ifExpiredRunRefersh();
        hive.periodicCheckStart();

    }
}
