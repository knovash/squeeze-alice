package org.knovash.squeezealice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;

import java.io.IOException;
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

    public static  String hiveBroker = "ssl://811c56b338f24aeea3215cd680851784.s1.eu.hivemq.cloud:8883";
    public static  String hiveUsername = "novashki";
    public static  String hivePassword = "Darthvader0";
    private static MqttClient mqttClient;
    private static final ResponseManager responseManager = new ResponseManager();

    public static void start() {
        log.info("MQTT STARTING...");
        try {
            mqttClient = new MqttClient(hiveBroker, MqttClient.generateClientId(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(hiveUsername);
            options.setPassword(hivePassword.toCharArray());
            mqttClient.connect(options);
                mqttClient.subscribe("to_lms_id", (topic, message) -> handleMqttFromUdyMessage(topic, message));
                mqttClient.subscribe("to_lms_voice_id", (topic, message) -> handleVoiceRequestAndSendAnswer(topic, message));
            log.info("MQTT STARTED OK");
            sendToTopicText("INFO", "CONNECTED! V.1.5 OS: " + System.getProperty("os.name"));
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

    public static String sendToTopicContextWaitForContext(String topic, Context context) {
        log.info("MQTT PUBLISH TO TOPIC: " + topic);
//        log.info("CONTEXT: " + context);
        String correlationId = UUID.randomUUID().toString();
        String responseBody = "";
        String contextJson = context.toJson();
        log.info("CONTEXT JSON: " + contextJson);

        try {
            // Отправка запроса в MQTT
//            log.info("MQTT PUBLISH TRY...");
            String payload = String.format("correlationId=%s&context=%s",
                    correlationId, contextJson);
            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));
//            log.info("MQTT PUBLISH OK");
        } catch (Exception e) {
        }
        return responseBody;
    }

    private static void handleMqttMessageJson(String topic, MqttMessage jsonInput) {
        log.info("GET MESSAGE FROM TOPIC: " + topic);
        log.info("MESSAGE : " + jsonInput);
        ObjectMapper mapper = new ObjectMapper();
        try {
            log.info("TRY MAPPER");
            Context context = mapper.readValue(jsonInput.getPayload(), Context.class);
            log.info("CONTEXT : " + context);
        } catch (IOException e) {
            log.info("ERROR MAPPER");
            throw new RuntimeException(e);
        }
    }

    private static void handleMqttMessageId(String topic, MqttMessage message) {
        log.info("RECIEVED MESSAGE FROM TOPIC ID: " + topic);
        log.info("MESSAGE : " + message);
        String payload = new String(message.getPayload());
        Map<String, String> params = parseParams(payload);

        if (params.containsKey("correlationId")) {
            String correlationId = params.get("correlationId");
//            log.info("ID : " + correlationId);
            String contextJson = params.getOrDefault("context", "");
//            log.info("CONTEXT JSON : " + contextJson);
            responseManager.completeResponse(correlationId, contextJson);
        }
    }

    private static void handleMqttFromUdyMessage(String topic, MqttMessage message) {
        log.info("RECIEVED MESSAGE FROM TOPIC: " + topic);
        log.debug("MESSAGE : " + message);
        String payload = new String(message.getPayload());
        Map<String, String> params = parseParams(payload);
//        получить json context из сообщения
        String contextJson = "";
        String correlationId = "";
        if (params.containsKey("correlationId")) {
            correlationId = params.get("correlationId");
            contextJson = params.getOrDefault("context", "");
            log.debug("CONTEXT JSON : " + contextJson);
            responseManager.completeResponse(correlationId, contextJson);
        }
//      получить объект контекст из json
        Context context = Context.fromJson(contextJson);
        //        обработка контекста
        context = HandlerAll.processContext(context);
//         положить в пэйлоад сообщения ид и контекст
        payload = String.format("correlationId=%s&context=%s",
                correlationId, context.toJson());

//        log.info("Send message: " + payload);
        try {
            MqttMessage responseMessage = new MqttMessage(payload.getBytes());
            mqttClient.publish("from_lms_id", responseMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private static void handleVoiceRequestAndSendAnswer(String topic, MqttMessage request) {
        log.info("GET MESSAGE FROM TOPIC ID: " + topic);
        log.debug("MESSAGE : " + request);
        String payload = new String(request.getPayload());
        Map<String, String> params = parseParams(payload);
        String contextJson = "";
        String correlationId = "";
//        получить из реквеста ID и CONTEXT
        if (params.containsKey("correlationId")) {
            correlationId = params.get("correlationId");
            log.debug("ID : " + correlationId);
            contextJson = params.getOrDefault("context", "");
            Context context = Context.fromJson(contextJson);
            AliceRequest aliceRequest = AliceRequest.fromJson(context.body);
            String userId = aliceRequest.session.userId;
            String applicationId = aliceRequest.session.application.applicationId;
            String command = aliceRequest.request.command;
            log.debug("APPID : " + applicationId);
            log.info("command : " + command);
            aliceId = applicationId;

//          выполнить голосовую команду c ID колонки(комнаты) и получить ответ
                String answer = SwitchVoiceCommand.switchVoice(applicationId, command);
                log.info("ANSWER : " + answer);

//         положить в пэйлоад сообщения ид и ответ
                payload = String.format("correlationId=%s&context=%s",
                        correlationId, answer);

            try {
                MqttMessage responseMessage = new MqttMessage(payload.getBytes());
                mqttClient.publish("from_lms_id", responseMessage);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            log.info("FINISH");
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
}
