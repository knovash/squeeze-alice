package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.SmartHome;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.provider.responseUserDevices.Device;
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
    public static LmsPlayers lmsPlayers = new LmsPlayers();

    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");
        log.info("READ CONFIG FROM PROPERTIES");
        log.info("LMS IP: " + lmsIP);
        log.info("lmsServer: " + lmsServer);
        log.info("port: " + port);

        Yandex.credentialsYandex.clientId = "0d17cba2ab254d838ac1ddcedabc4191";
        Yandex.credentialsYandex.clientSecret = "b0966cd53b9647b9989bd20a3c9140d8";
//        "client_id=0d17cba2ab254d838ac1ddcedabc4191&" +
//                "client_secret=b0966cd53b9647b9989bd20a3c9140d8&" +
//                "grant_type=client_credentials";

        log.info("READ CONFIG FROM ARGS");
        ArgsParser.parse(args);
        log.info("LMS IP: " + lmsIP);
        log.info("lmsServer: " + lmsServer);
        log.info("port: " + port);

        if (!Utils.checkIpIsLms(lmsIP)) {
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


        SmartHome.applicationIdAndPlayerName.put("76F751A76299DE71E1E9784E207AFC2BA1AB01361D8F8B9483A857FA87C087FA", "HomePod");
        SmartHome.applicationIdAndPlayerName.put("CEE4701A73C8D5113DB40E35CDA9ECBDB6FC2CDCFF8CFD73A1ADEB2607C67AD7", "JBL black");
        SmartHome.applicationIdAndPlayerName.put("B9AC4386E4621FE3F21AC35537D5F52CA9028F5406F599788E0F328329E2E02F","HomePod2");

        log.info(SmartHome.applicationIdAndPlayerName.get("CEE4701A73C8D5113DB40E35CDA9ECBDB6FC2CDCFF8CFD73A1ADEB2607C67AD7"));

        log.info("  ---+++===[ START SERVER ]===+++--- ");
        lmsPlayers = new LmsPlayers();
        lmsPlayers.readServerFile();
        lmsPlayers.updatePlayers();
        Server.start();
    }
}