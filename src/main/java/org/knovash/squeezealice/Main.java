package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.utils.JsonUtils;
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

    public static Users hiveUsers = new Users();

    public static void main(String[] args) {
        log.info("TIME ZONE: " + zoneId + " TIME: " + LocalTime.now(zoneId).truncatedTo(MINUTES));
        log.info("OS: " + System.getProperty("os.name") + ", user.home: " + System.getProperty("user.home"));
        System.setProperty("userApp.root", System.getProperty("user.home"));
        log.info("userApp.root: " + System.getProperty("userApp.root"));
        config.readConfigProperties();
        config.readConfigJson();
        lmsPlayers.searchForLmsIp();
        Utils.readAliceIdInRooms();
        lmsPlayers.readPlayersSettings();
        lmsPlayers.updateLmsPlayers();
        lmsPlayers.write();
        SmartHome.read();


        SpotifyAuth.read();
        SpotifyAuth.callbackRequestRefresh();

        Yandex.read();
        Yandex.getRoomsAndDevices();


        JsonUtils.pojoToJsonFile(SmartHome.devices, "devices.json");
        Server.start();
        Hive.start();
//        Utils.timerRequestPlayersState(lmsPlayers.delayUpdate);
    }
}
