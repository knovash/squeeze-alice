package org.knovash.squeezealice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.knovash.squeezealice.provider.YandexToken;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.voice.SwitchVoiceCommand.aliceId;

@Log4j2
public class Hive {


    private static final ResponseManager responseManager = new ResponseManager();

    public static String hiveBroker = config.hiveBroker;
    public static String hiveUsername = config.hiveUsername;
    public static String hivePassword = config.hivePassword;
    public static String hiveUserId = config.hiveUserId;
    public static String topicRecieveDevice = "to_lms_id"; // подписаться
    public static String topicRecieveVoice = "to_lms_voice_id"; // подписаться
    public static String topicRecieveDeviceUserId = Hive.hiveUserId + "to_lms_id"; // подписаться

    public static String topicPublish = "from_lms_id"; // отправить сюда
    public static String topicService = "INFO"; // отправить сюда

    private static MqttClient mqttClient;

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
            mqttClient.subscribe(topicRecieveDevice, (topic, message) -> handleDeviceMqttRequestAndPublishAnswer(topic, message));
            mqttClient.subscribe(topicRecieveVoice, (topic, message) -> handleVoiceMqttRequestAndPublishAnswer(topic, message));
//            mqttClient.subscribe(hiveUserId + topicRecieveDevice, (topic, message) -> handleDeviceMqttRequestAndPublishAnswer(topic, message));
//            mqttClient.subscribe(hiveUserId + topicRecieveVoice, (topic, message) -> handleVoiceMqttRequestAndPublishAnswer(topic, message));
            log.info("MQTT STARTED OK");
            sendToTopicText(topicService, "CONNECTED! V.1.5 OS: " + System.getProperty("os.name"));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void sendToTopicText(String topic, String payload) {
        try {
            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleDeviceMqttRequestAndPublishAnswer(String topicRecieved, MqttMessage message) {
        log.info("RECIEVED MESSAGE FROM TOPIC: " + topicRecieved);
        log.debug("MESSAGE : " + message);
        String payload = new String(message.getPayload());
        Map<String, String> params = parseParams(payload);
        String contextJson = "";
        String correlationId = "";
        String recievedUserId = "";
        if (params.containsKey("correlationId")) {
            correlationId = params.get("correlationId");
            contextJson = params.getOrDefault("context", "");
            recievedUserId = params.getOrDefault("userId", "");
        }
        log.info("correlationId : " + correlationId);
        log.info("hiveUserId : " + hiveUserId);
//      получить объект контекст из json
        Context context = Context.fromJson(contextJson);
//      обработка контекста
        context = HandlerAll.processContext(context);
//      положить в пэйлоад сообщения ид и контекст
        payload = "correlationId=" + correlationId + "&" +
                "hiveUserId=" + hiveUserId + "&" +
                "context=" + context.toJson();
        try {
            MqttMessage responseMessage = new MqttMessage(payload.getBytes());
            mqttClient.publish(topicPublish, responseMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private static void handleVoiceMqttRequestAndPublishAnswer(String topicRecieved, MqttMessage request) {
        log.info("RECIEVED MESSAGE FROM TOPIC: " + topicRecieved);
        log.debug("MESSAGE : " + request);
        String payload = new String(request.getPayload());
        Map<String, String> params = parseParams(payload);
        String contextJson = "";
        String correlationId = "";
        if (params.containsKey("correlationId")) {
            correlationId = params.get("correlationId");
            contextJson = params.getOrDefault("context", "");

            Context context = Context.fromJson(contextJson);
            AliceRequest aliceRequest = AliceRequest.fromJson(context.body);
            String applicationId = aliceRequest.session.application.applicationId;
            String command = aliceRequest.request.command;
            log.debug("applicationId : " + applicationId);
            log.info("command : " + command);
            aliceId = applicationId;
//          выполнить голосовую команду c ID колонки(комнаты) и получить ответ
            String answer = SwitchVoiceCommand.switchVoice(applicationId, command);
            log.info("ANSWER : " + answer);

//         положить в пэйлоад сообщения ид и ответ
            payload = "correlationId=" + correlationId + "&" +
                    "hiveUserId=" + hiveUserId + "&" +
                    "context=" + answer;
            try {
                MqttMessage responseMessage = new MqttMessage(payload.getBytes());
                mqttClient.publish(topicPublish, responseMessage);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public static String publishCommandWaitForAnswer(String topic, String command) {
        log.info("MQTT PUBLISH TO TOPIC: " + topic);
//        сгенерировать ключ сообщения в брокер, и ожидать ответа с этим ключом в топике названном как ключ
        String correlationId = UUID.randomUUID().toString();
//        подписаться на топик с названием такимже как ключ
        try {
            mqttClient.subscribe(topicRecieveVoice, (tocorrelationIdpic, message) -> handleVoiceMqttRequestAndPublishAnswer(topic, message));
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
//
        String responseBody = "";
//        String contextJson = context.toJson();
        try {
//       отправить в брокер Ключ и Команду (запуск аутентификации в Яндекс и возврат User ID)
            String payload =
                    "correlationId=" + correlationId + "&" +
                            "command=" + command;
            mqttClient.publish("command_to_cloud", new MqttMessage(payload.getBytes()));

            // Ожидание ответа
            CompletableFuture<String> future = responseManager.waitForResponse(correlationId);
            try {
                responseBody = future.get(15, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.info("MQTT ERROR NO RESPONSE");
                responseBody = "---";
            }
        } catch (Exception e) {
        }
        log.info("RESPONSE: " + responseBody);
//        String answer = extractBodyResponse(responseBody);
        return "answer";
    }


    public static String publishContextWaitForContext(String topic, Context context) {
        log.info("MQTT PUBLISH TO TOPIC: " + topic);
        String correlationId = UUID.randomUUID().toString();
        String responseBody = "";
        String contextJson = context.toJson();
        try {
            // Отправка запроса в MQTT
            String payload = String.format("correlationId=%s&context=%s",
                    correlationId, contextJson);
            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));
            // Ожидание ответа
            CompletableFuture<String> future = responseManager.waitForResponse(correlationId);
            try {
                log.info("MQTT WAIT FOR RESPONSE...");
                responseBody = future.get(15, TimeUnit.SECONDS);
                log.info("MQTT CONTEXT RECIEVED: " + responseBody);
            } catch (TimeoutException e) {
                log.info("MQTT ERROR NO RESPONSE: " + e);
                responseBody = "---";
            }
        } catch (Exception e) {
        }
        return responseBody;
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


    private static class ResponseManager {

        private final ConcurrentMap<String, CompletableFuture<String>> responses = new ConcurrentHashMap<>();

        public CompletableFuture<String> waitForResponse(String correlationId) {
            log.info("WAIT FOR RESPONSE ID: " + correlationId);
            CompletableFuture<String> future = new CompletableFuture<>();
            responses.put(correlationId, future);
//            log.info("FUTURE: " + future);
            return future;
        }

        public void completeResponse(String correlationId, String contextJson) {
//            log.info("COMPLETE RESPONSE ID: " + correlationId + " RECIEVED CONTEXT JSON");
            CompletableFuture<String> future = responses.remove(correlationId);
//            log.info("FUTURE: " + future.toString());
            if (future != null) {
//                log.info("FUTURE != NULL: " + future);
                future.complete(contextJson);
//                log.info("FUTURE: " + future);
            }
        }
    }

    public static String extractBodyResponse(String rawJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(rawJson);
            // Получаем значение bodyResponse напрямую из корневого узла
            JsonNode bodyResponseNode = rootNode.get("bodyResponse");
            if (bodyResponseNode != null && bodyResponseNode.isTextual()) {
                return bodyResponseNode.asText();
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return null;
        }
    }
}
