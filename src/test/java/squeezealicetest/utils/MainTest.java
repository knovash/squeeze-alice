package squeezealicetest.utils;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Config;
import org.knovash.squeezealice.Hive;
import org.knovash.squeezealice.LmsPlayers;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.utils.Utils;
import squeezealicetest.yandex.YandexTest;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class MainTest {

    public static LmsPlayersTest lmsPlayersTest = new LmsPlayersTest();
    public static SmartHome smartHomeTest = new SmartHome();
    public static List<String> roomsTest = new ArrayList<>();
    public static Config configTest = new Config();
    public static HiveTest hiveTest;

    public static void init() {

        configTest.readConfigProperties();
        configTest.readConfigJson();

        lmsPlayersTest.read();
        log.info("PLAYERS ------------------ " +lmsPlayersTest.players);

        Utils.readAliceIdInRooms();
        YandexTest.getRoomsAndDevices();
        hiveTest = new HiveTest();
        hiveTest.start("ssl://811c56b338f24aeea3215cd680851784.s1.eu.hivemq.cloud:8883",
                "novashki",
                "Darthvader0");
//        hiveTest.subscribeByYandex();
        hiveTest.subscribe("test");
    }
}
