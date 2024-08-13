package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;

import java.util.*;

@Log4j2
public class Main {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("config");
    public static String lmsIp = bundle.getString("lmsIp");
    public static String lmsPort = bundle.getString("lmsPort");
    public static String lmsUrl = "http://" + lmsIp + ":" + lmsPort + "/jsonrpc.js/";
    public static String silence = bundle.getString("silence");
    public static int port = Integer.parseInt(bundle.getString("port"));
    public static LmsPlayers lmsPlayers = new LmsPlayers();
    public static Map<String, String> config = new HashMap<>();
    public static Map<String, String> idRooms = new HashMap<>();
    public static List<String> rooms = new ArrayList<>();
    public static String tunnel;

    public static void main(String[] args) {
        log.info("CONFIG FROM config.properties");
        log.info("LMS URL: " + lmsUrl);
        log.info("THIS PORT: " + port);
        log.info("SILENCE: " + silence);
        Utils.readConfig();
        if (!Utils.checkLmsIp(lmsIp)) {
            log.info("WRONG LMS IP. RUN SEARCH LMS IP");
            Utils.searchLmsIp();
        }
        Utils.readIdRooms();
        Utils.writeConfig();
        lmsPlayers.readPlayers();
        lmsPlayers.updateServerStatus();
        lmsPlayers.writePlayers();
        SpotifyAuth.read();
        SpotifyAuth.callbackRequestRefresh();
        Yandex.read();
        Yandex.getRoomsAndDevices();
        JsonUtils.pojoToJsonFile(SmartHome.devices, "devices.json");
        Server.start();
//        Utils.timerRequestPlayersState(lmsPlayers.delayUpdate);
    }
}