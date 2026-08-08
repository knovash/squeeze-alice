package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.server.Server;
import org.knovash.squeezealice.utils.*;
import org.knovash.squeezealice.web.ActionsRunner;
import org.knovash.squeezealice.web.SettingsButtons;
import org.knovash.squeezealice.web.Settings;
import org.knovash.squeezealice.yandex.Yandex;
import org.knovash.squeezealice.yandex.YandexUtils;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.knovash.squeezealice.utils.InfoClient.setInfo;
import static org.knovash.squeezealice.yandex.Yandex.otherDevices;

@Log4j2
public class Main {

    public static LmsPlayers lmsPlayers = new LmsPlayers();
    public static SmartHome smartHome = new SmartHome();
    public static Map<String, String> roomsAndAliceIds = new HashMap<>();
    public static Map<String, String> roomsAndPlayers = new HashMap<>();
    public static ZoneId zoneId = ZoneId.of("Europe/Minsk");
    public static Config config = new Config();
    public static Boolean lmsServerOnline;
    public static Hive hive;
    public static List<YandexUtils.MusicDevice> yandexInfoDevices;
    public static String start = "- - - - STARTED - - - - -";
    public static String finish = "- - - - FINISHED - - - - -";
    public static String line = "-----------------------------------------------------------------------";
    public static String sayText = "привет";
    public static String music = "музыка";
    public static String myIp = "";
    public static Settings settingsNew = new Settings();
    public static SettingsButtons settingsButtons = new SettingsButtons();
    public static ActionsRunner actionsRunner = new ActionsRunner();

    public static void main(String[] args) {
        log.info("VERSION 2026.08.08");
        log.info("TIME ZONE: " + zoneId + " TIME: " + LocalTime.now(zoneId).truncatedTo(MINUTES));
        System.setProperty("userApp.root", System.getProperty("user.home"));
        log.info("OS: " + System.getProperty("os.name") + ", user.home: " + System.getProperty("user.home") + " userApp.root: " + System.getProperty("userApp.root"));
        config.load();
        config.write();
        settingsNew.loadFromFile();
        setInfo(config.domain); // откуда получить информацию о ключах
        Utils.getMyIpAddress();
        lmsPlayers.searchForLmsIp();
        Utils.readRoomsAndAliceIds(); // соответствие комнат и id колонок Алиса
        Utils.readRoomsAndPlayers(); // соответствие комнат и плееров // TODO используется пока только для сохранения. использовать при первом запуске сервиса
        lmsPlayers.read(); // прочитать ранее сохраненные плееры LMS и их настройки
        lmsPlayers.updatePlayers(); // получить список плееров из LMS и создать плееры в сервисе
        lmsPlayers.logPlayersNames();
        yandexInfoDevices = Yandex.devicesGetFromYandexInfo(); // получить устройства "Музыка" которые уже есть в Яндексе
        log.info("YANDEX get yandexInfoDevices: " + yandexInfoDevices);
        Yandex.createDevicesFromYandexDevices(yandexInfoDevices); // создать устройства "Музыка" в сервисе для Яндекса
        Yandex.createOtherDevicesFromYandexDevices(otherDevices); // создать устройства  в сервисе для Яндекса // TODO

        log.info("YANDEX DEVICES saved local: " + smartHome.devices.stream().filter(Objects::nonNull).map(device -> device.room).collect(Collectors.toList()));
        CopySoundsToLmsFolder.copySoundsToLmsFolder(); // звуки для уведомлений
        Server.start();
        hive = new Hive();
        hive.start();
        hive.subscribeByYandex();

//        ШЕДУЛЛЕРЫ
        SchedulerSpotifyRefreshToken.startPeriodicRefresh(2, 5); // Spotify периодическое обновление токена 30min
        SchedulerPlayersUpdate.startPeriodicUpdate2(2); // Yandex периодическая отправка состояния плееров 5

        //smartHome.createDeviceToggle("Гостиная", "Переключи");

        smartHome.write();

        log.info(Main.line);
    }


}
