package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.knovash.squeezealice.Main.config;

@Log4j2
public class Hive {

    private static MqttClient mqttClient;
    private static final String hiveBroker = config.hiveBroker;
    private static final String hiveUsername = config.hiveUsername;
    private static final String hivePassword = config.hivePassword;
    private static final ResponseManager responseManager = new ResponseManager();

    public static String topicRecieveDevice = "to_lms_idnovashki@yandex.ru"; // подписаться
    public static String topicPublish = "from_lms_id"; // отправить сюда


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
            log.info("SUBSCRIBE topicRecieveDevice: <" + topicRecieveDevice + ">");
//            log.info("SUBSCRIBE topicRecieveVoice: <" + topicRecieveVoice + ">");
            mqttClient.subscribe(topicRecieveDevice, (topic, message) -> handleDeviceAndPublish(topic, message));
//            mqttClient.subscribe(topicRecieveVoice, (topic, message) -> handleVoiceAndPublish(topic, message));
            log.info("MQTT STARTED OK");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private static void handleDeviceAndPublish(String topicRecieved, MqttMessage message) {
        log.info("RECIEVED MESSAGE FROM TOPIC: " + topicRecieved);
        log.debug("MESSAGE : " + message);
        String payload = new String(message.getPayload());


//        Map<String, String> params = parseParams(payload);
        Map<String, String> params = Parser.run(payload);

        String contextJson = "";
        String correlationId = "";
        if (!params.containsKey("correlationId")) return;
        correlationId = params.get("correlationId");


//      получить токен
        if (params.containsKey("token")) {
            log.info("RECIEVED TOKEN");
            String token = params.getOrDefault("token", null);
            if (token == null) return;

            Main.yandexToken = token;
            config.hiveYandexToken = token;
            config.hiveYandexEmail = YandexJwtUtils.getValueByTokenAndKey(token, "email");
            config.writeConfig();

            log.info("TOKEN: " + token);
            responseManager.completeResponse(correlationId, "OK");
            return;
        }


//        полученный контекст
        contextJson = params.getOrDefault("context", "");
        Context context = Context.fromJson(contextJson);

//      обработка контекста
        context = HandlerAll.processContext(context);

//      положить в пэйлоад ответа: ид, топик,  контекст
        payload = "correlationId=" + correlationId + "&" +
                "userTopicId=" + topicRecieveDevice + "&" +
                "context=" + context.toJson();
        try {
            MqttMessage responseMessage = new MqttMessage(payload.getBytes());
            mqttClient.publish(topicPublish, responseMessage);
        } catch (MqttException e) {
            log.info("ERROR: " + e);
        }
    }

    private static Map<String, String> parseParams(String message) {
        Map<String, String> result = new HashMap<>();
        if (message == null || message.isEmpty()) return result;
        // Ищем параметры по ключам с учетом их позиции
        int ctxStart = message.indexOf("context=");
        if (ctxStart == -1) return result;
        // Выделяем correlationId и requestId до начала context
        String prefix = message.substring(0, ctxStart);
        String[] parts = prefix.split("&");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) continue;
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            if (key.equals("correlationId") || key.equals("requestId")) {
                result.put(key, value);
            }
        }
        // Извлекаем context как всю оставшуюся часть строки
        String contextValue = message.substring(ctxStart + "context=".length());
        result.put("context", URLDecoder.decode(contextValue, StandardCharsets.UTF_8));
        return result;
    }

    public static void saveEmail(Map<String, String> userData) {
        log.info("SAVE USER YANDEX EMAIL");
        if (!userData.containsKey("email")) {
            log.info("ERROR USER NO NAME");
            return;
        }
        String email = userData.get("email");
        log.info("USER email: " + email);
        config.hiveYandexEmail = email;
        config.writeConfig();
        topicRecieveDevice = "to_lms_id" + email;
        Hive.start();
        log.info("HIVE: <" + topicRecieveDevice + ">");
        log.info("FINISH EMAIL");
    }

//    ----------------- это для паблиша

    //    ЭТО РАБОЧИЙ СЕЙЧАС МЕТОД ДЛЯ УДЯ КОМАНД
    public static String publishContextWaitForContext(String topic, Context context, Integer timeout, String action, String correlationId) {
        log.info("MQTT PUBLISH TO TOPIC: " + topic);
        if (correlationId == null) correlationId = UUID.randomUUID().toString();
        String responseBody = "";
        String contextJson = context.toJson();
        try {
            //             Отправка запроса в MQTT
//            String payload = String.format("correlationId=%s&context=%s", correlationId, contextJson);
            String payload = "correlationId=" + correlationId + "&" +
                    "callbackTopic=" + topicRecieveDevice + "&" +
                    "action=" + action + "&" +
                    "context=" + contextJson;

            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));
//             Ожидание ответа
            CompletableFuture<String> future = responseManager.waitForResponse(correlationId);
            //            Получение ответа
            try {
                log.info("MQTT WAIT FOR RESPONSE...");
//                если таймаут больше 4 то навык ответит раньше что Навык не отвечает
//                4 - недождалась ответа, но иногда может быть Навык неотвечает
//                для УДЯ было 10
                responseBody = future.get(timeout, TimeUnit.SECONDS);
                log.info("MQTT RESPONSE RECIEVED OK");
            } catch (TimeoutException e) {
                log.info("MQTT ERROR NO RESPONSE: " + e);
                responseBody = "---";
            }
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
