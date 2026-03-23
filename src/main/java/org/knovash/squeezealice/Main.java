package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.SchedulerPlayersUpdate;
import org.knovash.squeezealice.utils.SchedulerSpotifyRefreshToken;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.yandex.Yandex;
import org.knovash.squeezealice.yandex.YandexUtils;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;

@Log4j2
public class Main {

    public static LmsPlayers lmsPlayers = new LmsPlayers();
    public static SmartHome smartHome = new SmartHome();
    public static Map<String, String> roomsAndAliceIds = new HashMap<>();
    public static Map<String, String> roomsAndPlayers = new HashMap<>();
    //    public static List<String> rooms = new ArrayList<>();
    public static ZoneId zoneId = ZoneId.of("Europe/Minsk");
    public static Config config = new Config();
    public static Boolean lmsServerOnline;
    public static String yandexToken = "";
    public static Hive hive;

    public static void main(String[] args) {
        log.info("TIME ZONE: " + zoneId + " TIME: " + LocalTime.now(zoneId).truncatedTo(MINUTES));
        System.setProperty("userApp.root", System.getProperty("user.home"));
        log.info("OS: " + System.getProperty("os.name") + ", user.home: " + System.getProperty("user.home") + " userApp.root: " + System.getProperty("userApp.root"));
        config.readConfigProperties();
        config.readConfigJson();
        config.write();
        Utils.getMyIpAddres();
        lmsPlayers.searchForLmsIp();
        Utils.readRoomsAndAliceIds(); // соответствие комнат и id колонок Алиса
        Utils.readRoomsAndPlayers(); // соответствие комнат и плееров // TODO используется пока только для сохранения. использовать при первом запуске сервиса

        lmsPlayers.read();
        lmsPlayers.updatePlayers(); // получить список плееров из LMS и создать плееры в сервисе
        lmsPlayers.write();
        lmsPlayers.logPlayersNames();

//        Volumio.createPlayer();

        log.info("YANDEX DEVICES: " + SmartHome.devices.stream().filter(Objects::nonNull).map(device -> device.room).collect(Collectors.toList()));
        List<YandexUtils.MusicDevice> yandexInfoDevices = Yandex.devicesGetFromYandexInfo(); // получить устройства "Музыка" которые уже есть в Яндексе
        Yandex.createDevicesFromYandexDevices(yandexInfoDevices); // создать устройства "Музыка" в сервисе для Яндекса
        log.info("YANDEX DEVICES: " + SmartHome.devices.stream().filter(Objects::nonNull).map(device -> device.room).collect(Collectors.toList()));


        Server.start();
        hive = new Hive();
        hive.start();
        hive.subscribeByYandex();
        log.info("VERSION 2025.03.05");

        SchedulerSpotifyRefreshToken.startPeriodicRefresh(60, 5); // Spotify периодическое обновление токена

//        SchedulerPlayersUpdate.startPeriodicUpdate2(1); // Yandex периодическая отправка состояния плееров

    }
}
