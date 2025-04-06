package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.MINUTES;

@Log4j2
public class MainTest {

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
//        System.setProperty("userApp.root", System.getProperty("user.home"));
//        log.info("userApp.root: " + System.getProperty("userApp.root"));
//        config.readConfigProperties();
//        config.readConfigJson();
//        lmsPlayers.searchForLmsIp();
//        Utils.readAliceIdInRooms();
//        lmsPlayers.readPlayersSettings();
//        lmsPlayers.updateLmsPlayers();
//        lmsPlayers.write();
//        SmartHome.read();
//        SpotifyAuth.read();
//        SpotifyAuth.callbackRequestRefresh();
//        Yandex.read();
//        Yandex.getRoomsAndDevices();
//        JsonUtils.pojoToJsonFile(SmartHome.devices, "devices.json");
//        Server.start();
//        Hive.start();

        String token = "y0__xDzxbXDARi79i4g1Kq52BLBrH5AiuK_6jAmQvamADVB964geA";
        String jwtToken ="";

            jwtToken= YandexJwtUtils.getJwtByOauth(token);
            log.info("JWT: " + jwtToken);
            YandexJwtUtils.parseYandexJwtForKey(jwtToken,"");




//        Context context = new Context();
//        String contextJson = "dddddddd";
//        contextJson = Hive.publishContextWaitForContext("from_local_request", context, 5, "token");
//        log.info(contextJson);


//        Utils.timerRequestPlayersState(lmsPlayers.delayUpdate);
    }
}
