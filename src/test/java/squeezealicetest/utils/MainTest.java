package squeezealicetest.utils;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Hive;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.yandex.Yandex;
import squeezealicetest.steps.ConfigTest;
import squeezealicetest.yandex.YandexTest;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.MINUTES;
import static squeezealicetest.utils.TestDevice.payload;

@Log4j2
public class MainTest {

    public static LmsPlayersTest lmsPlayersTest = new LmsPlayersTest();
    public static Map<String, String> idRooms = new HashMap<>();
    public static List<String> rooms = new ArrayList<>();
    public static ZoneId zoneId = ZoneId.of("Europe/Minsk");
    public static ConfigTest configTest = new ConfigTest();
    public static Boolean lmsServerOnline = true;
    public static String yandexToken = "";

    public static void main() {
        log.info("TIME ZONE: " + zoneId + " TIME: " + LocalTime.now(zoneId).truncatedTo(MINUTES));
        log.info("OS: " + System.getProperty("os.name") + ", user.home: " + System.getProperty("user.home"));
        System.setProperty("userApp.root", System.getProperty("user.home"));
        log.info("userApp.root: " + System.getProperty("userApp.root"));
        lmsPlayersTest.read();
//        lmsPlayersTest.updateLmsPlayers();
//        Yandex.getRoomsAndDevices();
        YandexTest.getRoomsAndDevices();
        HiveTest.start();
//        hive = new Hive();
//        hive.start();
        HiveTest.subscribe("test");
        payload.devices = new ArrayList<>();
        log.info("VERSION 1.2");


    }
}
