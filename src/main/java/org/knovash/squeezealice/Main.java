package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.utils.ArgsParser;
import org.knovash.squeezealice.utils.Utils;

import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Log4j2
public class Main {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("config");
    public static String lmsIP = bundle.getString("lmsIP");
    public static String lmsPort = bundle.getString("lmsPort");
    public static String lmsUrl = "http://" + lmsIP + ":" + lmsPort + "/jsonrpc.js/";
    public static String silence = bundle.getString("silence");
    public static int port = Integer.parseInt(bundle.getString("port"));
    public static LmsPlayers lmsPlayers = new LmsPlayers();

    public static void main(String[] args) {
        log.info("--- TRY TO START SERVER ---");
        log.info("READ CONFIG FROM PROPERTIES");
        log.info("LMS URL: " + lmsUrl);
        log.info("THIS PORT: " + port);

        log.info("READ CONFIG FROM ARGS");
        ArgsParser.parse(args);
        lmsUrl = "http://" + lmsIP + ":" + lmsPort + "/jsonrpc.js/";
        log.info("LMS URL: " + lmsUrl);
        log.info("THIS PORT: " + port);

        if (!Utils.checkIpIsLms(lmsIP)) {
            log.info("CONFIG FROM PROPERTIES AND ARGS NOT VALID LMS SERVER");
            lmsIP = Utils.searchLmsIp();
            Utils.sleep(2000);
            lmsUrl = "http://" + lmsIP + ":" + lmsPort + "/jsonrpc.js/";
            log.info("LMS URL: " + lmsUrl);
            log.info("THIS PORT: " + port);
        }

        log.info("READ ALICE DEVICES FROM FILE alice_devices.json");
        SmartHome.read();
        log.info("DEVICES: " + SmartHome.devices.stream().map(d -> d.customData.lmsName).collect(Collectors.toList()));

        log.info("READ LMS PLAYERS FROM FILE lms_players.json");
        lmsPlayers.players = new ArrayList<>();
        lmsPlayers.read();
        log.info("PLAYERS: " + lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList()));
        log.info("UPDATE LMS PLAYERS FROM LMS");
        lmsPlayers.update();
        lmsPlayers.write();
        log.info("PLAYERS: " + lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList()));

        SpotifyAuth.read();
        log.info("BEARER: " + SpotifyAuth.bearer_token);

        log.info("--- SERVER START ---");
        Server.start();
    }
}