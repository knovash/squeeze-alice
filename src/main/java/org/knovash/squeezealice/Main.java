package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.PlayersUpdateScheduler;
import org.knovash.squeezealice.yandex.Yandex;
import org.knovash.squeezealice.utils.Utils;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static java.time.temporal.ChronoUnit.MINUTES;

@Log4j2
public class Main {

    public static LmsPlayers lmsPlayers = new LmsPlayers();
    public static Map<String, String> idRooms = new HashMap<>();
    public static List<String> rooms = new ArrayList<>();
    public static ZoneId zoneId = ZoneId.of("Europe/Minsk");
    public static Config config = new Config();
    public static Boolean lmsServerOnline;
    public static String yandexToken = "";

    public static void main(String[] args) {
        log.info("TIME ZONE: " + zoneId + " TIME: " + LocalTime.now(zoneId).truncatedTo(MINUTES));
        log.info("OS: " + System.getProperty("os.name") + ", user.home: " + System.getProperty("user.home"));
        System.setProperty("userApp.root", System.getProperty("user.home"));
        log.info("userApp.root: " + System.getProperty("userApp.root"));
        config.readConfigProperties();
        config.readConfigJson();
        config.write();

        lmsPlayers.searchForLmsIp();
        Utils.readAliceIdInRooms();
        lmsPlayers.read();
        lmsPlayers.updateLmsPlayers();
        lmsPlayers.write();
//        SmartHome.read();
        Yandex.getRoomsAndDevices();

        log.info("DEVICES: "+SmartHome.devices);
        lmsPlayers.checkRooms();
        lmsPlayers.write();

        Server.start();
        Hive.start();
//        PlayersUpdateScheduler.startPeriodicUpdate(5);
        log.info("VERSION 1.2");
//        Utils.timerRequestPlayersState(lmsPlayers.delayUpdate);
//        Spotify.ifExpiredRunRefersh();

    }
}
