package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

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
    public static Map<String, String> rooms = new HashMap<>();

    public static void main(String[] args) {
        log.info("READ CONFIG FROM config.properties");
        log.info("LMS IP: " + lmsIp);
        log.info("LMS PORT: " + lmsPort);
        log.info("LMS URL: " + lmsUrl);
        log.info("THIS PORT: " + port);
        log.info("SILENCE: " + silence);
        Utils.readConfig();
        Utils.writeConfig();
        if (!Utils.checkLmsIp(lmsIp)) {
            log.info("WRONG LMS IP. RUN SEARCH LMS IP");
            if (!Utils.searchLmsIp()) return;
        }
        Utils.readRooms();

        Utils.writeConfig();
        lmsPlayers.read();
        lmsPlayers.update();
        SpotifyAuth.read();
        Yandex.read();
        Yandex.getInfo();
        log.info(SmartHome.devices);
//        JsonUtils.pojoToJsonFile(SmartHome.devices,"devices.json");
        Server.start();
//        Utils.timerRequestPlayersState(lmsPlayers.delayUpdate);
    }
}