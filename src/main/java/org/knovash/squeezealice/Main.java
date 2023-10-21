package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.SmartHome;
import org.knovash.squeezealice.provider.pojo.Device;
import org.knovash.squeezealice.utils.ArgsParser;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Log4j2
public class Main {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");
    public static String lmsIP = bundle.getString("lmsIP");
    public static String lmsPort = bundle.getString("lmsPort");
    public static String lmsServer = "http://" + lmsIP + ":" + lmsPort + "/jsonrpc.js/";
    public static String silence = bundle.getString("silence");
    public static int port = Integer.parseInt(bundle.getString("port"));
    public static Server server = new Server();

    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");
        log.info("READ CONFIG FROM PROPERTIES");
        log.info("LMS IP: " + lmsIP);
        log.info("lmsServer: " + lmsServer);
        log.info("port: " + port);

        log.info("READ CONFIG FROM ARGS");
        ArgsParser.parse(args);
        log.info("LMS IP: " + lmsIP);
        log.info("lmsServer: " + lmsServer);
        log.info("port: " + port);

        if (!Utils.isLms(lmsIP)) {
            log.info("CONFIG FROM PROPERTIES AND ARGS NOT VALID LMS SERVER");
            lmsIP = Utils.searchLmsIp();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        log.info("LMS IP: " + lmsIP);
        lmsServer = "http://" + lmsIP + ":" + lmsPort + "/jsonrpc.js/";
        log.info("LMS SERVER: " + lmsServer);

        log.info("READ ALICE SMART HOME DEVICES from file home_devices.json");
        List<Device> devices = JsonUtils.jsonFileToList("home_devices.json", Device.class);
        SmartHome.devices = new LinkedList<>();
        if (devices != null) SmartHome.devices.addAll(devices);
        log.info("HOME DEVICE: " + SmartHome.devices.size());
        log.info("HOME DEVICE: " + SmartHome.devices.stream().map(d -> d.customData.lmsName).collect(Collectors.toList()));
//        NewDevice.create("колонка", "HomePod", "Гостиная");
//        NewDevice.create("колонка", "JBL black", "Спальня");
//        NewDevice.create("колонка", "JBL white", "Веранда");

        log.info("  ---+++===[ START SERVER ]===+++--- ");
        server = new Server();
        server.readServerFile();
        server.updatePlayers();
        ServerController.start();
    }
}