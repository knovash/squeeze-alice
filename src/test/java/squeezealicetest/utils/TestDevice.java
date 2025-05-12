package squeezealicetest.utils;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
//import org.knovash.squeezealice.Hive;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.provider.response.*;
import org.knovash.squeezealice.utils.JsonUtils;
//import org.knovash.squeezealice.yandex.Yandex;
import squeezealicetest.yandex.YandexTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class TestDevice {


    public static Payload payload = new Payload();
    public static boolean flag = false;
    public static CountDownLatch testCompletionLatch;

    public static String capPowerValue = "";
    public static String capVolumeValue = "";
    public static String capChannelValue = "";

    private static List<List<Boolean>> allTestsResults = new ArrayList<>();

    public static void addDevice(String room, String power, String volume, String channel) {
//        log.info("ADD NEW DEVICE FOR TEST: " + room);

        if (volume.equals("")) volume = null;
        if (channel.equals("")) channel = null;


//        Boolean powerBool;
//        if (power.equals("on")) powerBool = true;
//        else powerBool = false;
//        if (power.equals("")) power = null;

        String powerStr;
        if (power.equals("on")) powerStr = "true";
        else powerStr = "false";
        if (power.equals("")) powerStr = null;

        payload.devices.add(createTestdevice(room, powerStr, volume, channel));
    }

    public static Device createTestdevice(String room, String power, String volume, String channel) {

        String deviceId = YandexTest.deviceIdbyRoomName(room);
//        log.info("DEVICE ID: " + deviceId);

        Boolean volumeRelative = false;
        Boolean channelRelative = false;

        Capability capabilityPower = null;
        Capability capabilityVolume = null;
        Capability capabilityChannel = null;

        Device device = new Device();
        device.id = deviceId;
        device.capabilities = new ArrayList<>();

        if (power != null) {
            capabilityPower = new Capability();
            capabilityPower.state = new State();
            capabilityPower.type = "devices.capabilities.on_off";
            capabilityPower.state.instance = "on";
            capabilityPower.state.value = power;
            capabilityPower.state.relative = false;
            device.capabilities.add(capabilityPower);
        }
        if (volume != null) {
            capabilityVolume = new Capability();
            capabilityVolume.state = new State();
            capabilityVolume.type = "devices.capabilities.range";
            capabilityVolume.state.instance = "volume";
            capabilityVolume.state.value = volume;
            capabilityVolume.state.relative = volumeRelative;
            device.capabilities.add(capabilityVolume);
        }
        if (channel != null) {
            capabilityChannel = new Capability();
            capabilityChannel.state = new State();
            capabilityChannel.type = "devices.capabilities.range";
            capabilityChannel.state.instance = "channel";
            capabilityChannel.state.value = channel;
            capabilityChannel.state.relative = channelRelative;
            device.capabilities.add(capabilityChannel);
        }
        return device;
    }


    public static void runTest(String testName) {
        log.info("");
        log.info("------------------" + testName + " START ----------------------------------");
        testCompletionLatch = new CountDownLatch(1); // Инициализация перед запуском теста

        PayloadRoot payloadRoot = new PayloadRoot();
        payloadRoot.payload = payload;
        String payloadRootJson = JsonUtils.pojoToJson(payloadRoot);
        String path = "/v1.0/user/devices/action";
        String headersJson = "{\"Accept-encoding\":[\"gzip\"],\"X-real-ip\":[\"37.9.122.162\"],\"Accept\":[\"application/json\"],\"X-forwarded-port\":[\"443\"],\"X-zeabur-container-port\":[\"8080\"],\"Host\":[\"alice-lms.zeabur.app\"],\"User-agent\":[\"Yandex LLC\"],\"X-zeabur-request-id\":[\"fra1::2f8090cc-1a79-43a1-9b03-554e1ce0e8a1\"],\"Authorization\":[\"Bearer y0__xDzxbXDARi79i4g1Kq52BLBrH5AiuK_6jAmQvamADVB964geA\"],\"X-forwarded-host\":[\"alice-lms.zeabur.app\"],\"Content-type\":[\"application/json\"],\"X-zeabur-ip-country\":[\"RU\"],\"X-request-id\":[\"9f8d856f-d6e8-4ebe-a112-828e4fc17da2\"],\"X-forwarded-for\":[\"37.9.122.162, 37.9.122.162\"],\"X-forwarded-proto\":[\"https\"],\"Content-length\":[\"131\"]}";
        String query = null;
        Context context = Context.contextCreate(path, headersJson, payloadRootJson, query);
        HiveTest.publishContextWaitForContext("to_lms_id409821939", context, 15, null, "d2ece5fd-ba8e-4795-ab28-deda51d51900");

// когда получено сообщение в топик test запуститься проверка состояний плееров squeezealicetest.utils.TestDevice.checkdevicesState();

// сброс пэйлоад для следущего теста
//      payload = new Payload();
        try {
            log.info("WAIT FOR CHECK...");
            testCompletionLatch.await(); // Ожидание завершения проверки
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Добавить паузу 5 секунд после синхронизации
        try {
            log.info("WAIT 10 FOR LISTEN MUSIC...");
            Thread.sleep(5000); // Пауза 5 секунд
        } catch (InterruptedException e) {
            log.error("Ошибка при паузе: ", e);
            Thread.currentThread().interrupt();
        }

        log.info("------------------" + testName + " FINISH ----------------------------------");
    }


    public static boolean checkDevice(Device device) {
        log.info("START CHECK DEVICE: " + device);
        capPowerValue = "";
        capVolumeValue = "";
        capChannelValue = "";

        boolean isPowerValid = false;
        boolean isVolumeValid = false;
        boolean isChannelValid = false;

        String id = device.id;
        PlayerTest player = MainTest.lmsPlayersTest.players.stream()
                .filter(p -> p.deviceId != null)
                .filter(p -> p.deviceId.equals(id))
                .findFirst().orElse(null);
        log.info("PLAYER: " + player);
        if (player == null) return false;
        log.info("CHECK DEVICE ID: " + id + " PLAYER: " + player.name);
        String playerName = player.name;
        String mode = player.mode();
        String volume = player.volumeGet();
        Integer channel = player.currentChannelIndexInFavorites();

        List<String> capList = device.capabilities.stream()
                .filter(capability -> capability.state != null)
                .filter(capability -> capability.state.instance != null)
                .peek(capability -> {
                    if (capability.state.instance.equals("volume")) capVolumeValue = capability.state.value;
                    if (capability.state.instance.equals("channel")) capChannelValue = capability.state.value;
                    if (capability.state.instance.equals("on")) {
                        if (capability.state.value.equals("true")) capPowerValue = "play";
                        else capPowerValue = "stop";
                    }
                }).map(capability -> capability.state.instance)
                .collect(Collectors.toList());

        log.info("CAP LIST: " + capList);

        String result = "CHECK " + playerName +
                " MODE: '" + mode + "'='" + capPowerValue + "'" +
                " VOLUME: '" + volume + "'='" + capVolumeValue + "'" +
                " CHANNEL: '" + channel + "'='" + capChannelValue + "'";

        Boolean deviceResult = false;
        if (capPowerValue.equals("") ||
                (capPowerValue.equals("play") && mode.equals("play")) ||
                (capPowerValue.equals("pause") && mode.equals("pause")) ||
                (capPowerValue.equals("stop") && mode.equals("stop")) ||
                (capPowerValue.equals("pause") && mode.equals("stop")) ||
                (capPowerValue.equals("stop") && mode.equals("pause"))
        ) {
            deviceResult = true;
            isPowerValid = true;
            log.info("------------ POWER OK ----------");
        } else log.info("------------ POWER FAIL ---------- REAL:" + mode + " EXPECTED:" + capPowerValue);

        if (!(capPowerValue.equals("stop") || capPowerValue.equals("pause"))) {

            if (capVolumeValue.equals("") || capVolumeValue.equals(volume)) {
                deviceResult = true;
                isVolumeValid = true;
                log.info("------------ VOLUME OK ----------");
            } else log.info("------------ VOLUME FAIL ---------- REAL:" + volume + " EXPECTED:" + capVolumeValue);

            if (capChannelValue.equals("") || capChannelValue.toString().equals(channel.toString())) {
                deviceResult = true;
                isChannelValid = true;
                log.info("------------ CHANNEL OK ----------");
            } else log.info("------------ CHANNEL FAIL ---------- REAL:" + channel + " EXPECTED:" + capChannelValue);

        }

        if (deviceResult)
            log.info("+++++++ DEVICE TEST OK +++++++");
        else
            log.info("+++++++ DEVICE TEST FAILED +++++++");

        log.info("Результат проверки: {}",
                (deviceResult ? "УСПЕШНО" : "НЕУДАЧА") + " - " + result);

//        return result;
        return deviceResult;  // Возвращаем общий результат проверки
    }


    public static void checkdevicesState() {
        log.info("RUN CHECK DEVICES STATE: " + payload.devices.size());
//        List<String> testCheckResults =
        List<Boolean> testCheckResults = payload.devices.stream()
                .map(TestDevice::checkDevice)
                .collect(Collectors.toList());
        log.info("RESULTS");
//        testCheckResults.stream().forEach(cc -> log.info(cc));


//        AllTestsResults.add(testCheckResults);
        // В методе checkdevicesState замените:
        // AllTestsResults.add(testCheckResults); 
        // на:
        TestDevice.addTestResults(testCheckResults);


//  после завершениявывода результатов теста. разрешить выполнение следущего теста

        // сброс пэйлоад для следущего теста
        log.info("CLEAR PAYLOAD DEVICES --------------------");
        payload = new Payload();

        // Уведомляем об окончании проверки
        if (testCompletionLatch != null) {
//            log.info("CHECK DEVICES FINISHED. TEST FINISHED. RESET LATCH FOR NEXT TEST ----------------------------------");
            testCompletionLatch.countDown();
            testCompletionLatch = null; // Предотвращаем повторное использование
        }

    }

    public static void addTestResults(List<Boolean> results) {
        log.info("ADD RESULTS: " + results);
        allTestsResults.add(results);
        log.info("AFTER ADD RESULTS: " + allTestsResults);
    }

    public static List<List<Boolean>> getAllTestsResults() {
        log.info("ALL RESULTS: " + allTestsResults);
        return allTestsResults;
    }

    public static void resetTestState() {
        payload = new Payload();
        allTestsResults.clear();
        testCompletionLatch = null;
    }


}
