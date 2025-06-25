package squeezealicetest.utils;

import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.knovash.squeezealice.Context;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static squeezealicetest.utils.MainTest.configTest;

@Log4j2
public class HiveTest {

    private static MqttClient mqttClient;
    private static final String hiveBroker = configTest.hiveBroker;
    private static final String hiveUsername = configTest.hiveUsername;
    private static final String hivePassword = configTest.hivePassword;
    private static final ResponseManager responseManager = new ResponseManager();
//    public static String topicRecieveDevice = "to_lms_id";// подписаться
//    public static String topicPublish = "from_lms_id";// отправить сюда
//    public static long spotifyExpiresAt;
//    public static String correlationId = "";


    public void start(String hiveBroker, String hiveUsername, String hivePassword) {
        try {
            mqttClient = new MqttClient(hiveBroker, MqttClient.generateClientId(), new MemoryPersistence());
//            mqttClient.setCallback(this);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true); // Включаем встроенное авто-переподключение
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30); // Частота проверки соединения
            options.setMaxReconnectDelay(30000); // Максимальная задержка между попытками (30 сек)
            options.setUserName(hiveUsername);
            options.setPassword(hivePassword.toCharArray());
            mqttClient.connect(options);
            log.info("MQTT START");
//            isConnected = true;
        } catch (MqttException e) {
            log.error("MQTT INITIAL CONNECTION ERROR: {}", e.getMessage());
//            scheduleReconnection();
        }
    }

    public static void start() {
        log.info("MQTT STARTING...");
        try {
            mqttClient = new MqttClient(hiveBroker, MqttClient.generateClientId(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setUserName(hiveUsername);
            options.setPassword(hivePassword.toCharArray());
            mqttClient.connect(options);
// Подписка на топик ответа
//            subscribeByYandexEmail();
            log.info("MQTT STARTED OK");
        } catch (MqttException e) {
            log.info("MQTT ERROR: " + e);
        }
    }

    public static void subscribe(String subscribeToTopic) {
        log.info("SUBSCRIBE TO TOPIC: " + subscribeToTopic);
        try {
            mqttClient.subscribe(subscribeToTopic, (topic, message) -> handleTest(topic, message));
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleTest(String topicRecieved, MqttMessage message) {
        log.info("PLAYERS ACTIONS COMPLETED");
        TestDevice.checkdevicesState();
    }

    public static void publish(String topic, String message) {
        log.info("PUBLISH TO TOPIC: " + topic);
        try {
            mqttClient.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            log.info("ERROR: " + e);
        }
    }

    //  это для паблиша
    public static String publishContextWaitForContext(String topic, Context context, Integer timeout, String action, String correlationId) {
//        log.info("WITHOUT TEXT text null");
        return publishContextWaitForContext(topic, context, timeout, action, correlationId, null);
    }

    // ЭТО РАБОЧИЙ СЕЙЧАС МЕТОД ДЛЯ УДЯ КОМАНД
    public static String publishContextWaitForContext(String topic, Context context, Integer timeout, String action, String correlationId, String text) {
        log.info("MQTT PUBLISH TO TOPIC: " + topic);
//        log.info("TEXT: " + text);
        if (context == null) context = new Context();
        if (correlationId == null) correlationId = UUID.randomUUID().toString();
        String responseBody = "";
        String contextJson = context.toJson();

//        генерация топика для колбэка с токеном
//        String callbackTopic = "callback" + correlationId;
        //     подписаться на сгенерированый топик
//        subscribe(callbackTopic);

//        подготовка пэйлоад
        try {
            String payload = "correlationId=" + correlationId + "&" +
                    "callbackTopic=" + "callbackTopic" + "&" +
                    "action=" + action + "&" +
                    "text=" + text + "&" +
                    "context=" + contextJson;
//            log.info("PAYLOAD: " + payload);
// Отправка запроса в MQTT
            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));
        } catch (Exception e) {
        }
        return responseBody;
    }

    private static class ResponseManager {

        private final ConcurrentMap<String, CompletableFuture<String>> responses = new ConcurrentHashMap<>();

        public CompletableFuture<String> waitForResponse(String correlationId) {
            CompletableFuture<String> future = new CompletableFuture<>();
            responses.put(correlationId, future);
            return future;
        }

        public void completeResponse(String correlationId, String contextJson) {
            CompletableFuture<String> future = responses.remove(correlationId);
            if (future != null) {
                future.complete(contextJson);
            }
        }
    }
}
