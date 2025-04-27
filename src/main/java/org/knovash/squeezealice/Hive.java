package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.spotify.SpotifyUserParser;
import org.knovash.squeezealice.spotify.spotify_pojo.PlayerState;
import org.knovash.squeezealice.yandex.YandexJwtUtils;
import org.json.JSONObject;

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
    public static String topicRecieveDevice = "to_lms_id";// подписаться
    public static String topicPublish = "from_lms_id";// отправить сюда
    public static long spotifyExpiresAt;

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
            subscribeByYandexEmail();
            log.info("MQTT STARTED OK");
        } catch (MqttException e) {
            log.info("MQTT ERROR: " + e);
        }
    }

    public static void subscribeByYandexEmail() {
        if (config.yandexUid == null || config.yandexUid.equals("")) {
            log.info("SUBSCRIBE BY YANDEX EMAIL FAIL email:" + config.yandexUid);
            return;
        }
        log.info("SUBSCRIBE BY YANDEX EMAIL: " + topicRecieveDevice + config.yandexUid);
        subscribe(topicRecieveDevice + config.yandexUid);
    }

    public static void subscribe(String subscribeToTopic) {
        log.info("SUBSCRIBE TO TOPIC: " + subscribeToTopic);
        try {
            mqttClient.subscribe(subscribeToTopic, (topic, message) -> handleDeviceAndPublish(topic, message));
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleDeviceAndPublish(String topicRecieved, MqttMessage message) {
        log.info("RECIEVED MESSAGE FROM TOPIC: " + topicRecieved);
//        log.info("MESSAGE : " + message);
        String payload = new String(message.getPayload());

// Map<String, String> params = parseParams(payload);
        Map<String, String> params = Parser.run(payload);
//        log.info("PARAMS : " + params);

        String contextJson = "";
        String correlationId = "";
        if (!params.containsKey("correlationId")) return;
        correlationId = params.get("correlationId");

// получить токен Yandex
        if (params.containsKey("action") && params.getOrDefault("action", null).equals("yandex_callback_token")) {
            log.info("RECIEVED YANDEX TOKEN");
            String token = params.getOrDefault("token", null);
            if (token == null) return;
            Main.yandexToken = token;
            config.yandexToken = token;
            String currentUid = config.yandexUid;
            String newUid = YandexJwtUtils.getValueByTokenAndKey(token, "uid");
            String yandexName = YandexJwtUtils.getValueByTokenAndKey(token, "display_name");
            if (newUid.equals(currentUid)) return;

            Hive.unsubscribe(Hive.topicRecieveDevice + currentUid);
            Hive.subscribe(Hive.topicRecieveDevice + newUid);
            config.yandexUid = newUid;
            config.yandexName = yandexName;
            config.write();

            log.info("TOKEN: " + token);
            responseManager.completeResponse(correlationId, "OK");
            return;
        }

// получить токен Spotify
        if (params.containsKey("action") && params.getOrDefault("action", null).equals("spotify_callback_token")) {
            log.info("RECIEVED SPOTIFY TOKEN");
            String token = params.getOrDefault("token", null);
            String refreshToken = params.getOrDefault("refreshToken", null);
            spotifyExpiresAt = Long.parseLong(params.getOrDefault("expiresAt", null));
            if (token == null) return;
            log.info("TOKEN: " + token);
            log.info("REFRESH TOKEN: " + refreshToken);
            log.info("EXPIRES AT: " + spotifyExpiresAt);
            long currentTime = System.currentTimeMillis();
            log.info("TIME NOW: " + currentTime);
            long delta = spotifyExpiresAt - currentTime;
            log.info("DELTA: " + delta);
            config.spotifyToken = token;
            config.spotifyRefreshToken = refreshToken;
            config.spotifyTokenExpiresAt = spotifyExpiresAt;
//            получить имя пользователя Spotify
            config.spotifyName = SpotifyUserParser.parseUserInfo(Spotify.me()).getDisplayName();
            config.write();
            PlayerState ps = Spotify.playerState;
            log.info(ps);
            responseManager.completeResponse(correlationId, "OK");
            return;
        }


// получить рефлеш токен Spotify
        if (params.containsKey("action") && params.getOrDefault("action", null).equals("spotify_callback_refresh_token")) {
            log.info("RECIEVED SPOTIFY REFRESH TOKEN");
            String refreshTokenResponse = params.getOrDefault("refreshTokenResponse", null);
            log.info("RE: " + refreshTokenResponse);

//            json body response от Spotify
            JSONObject jsonObject = new JSONObject(refreshTokenResponse);
            String accessToken = jsonObject.getString("access_token");
            String tokenType = jsonObject.getString("token_type");
            int expiresIn = jsonObject.getInt("expires_in");
            String scope = jsonObject.getString("scope");
            log.info("Access Token: " + accessToken);
            log.info("Token Type: " + tokenType);
            log.info("Expires in: " + expiresIn + " seconds");
            log.info("Scopes: " + scope);

            spotifyExpiresAt = System.currentTimeMillis() + expiresIn;
            config.spotifyToken = accessToken;
            config.spotifyTokenExpiresAt = System.currentTimeMillis() + expiresIn;

            responseManager.completeResponse(correlationId, "OK");
            return;
        }

// полученный контекст
        contextJson = params.getOrDefault("context", "");
        Context context = Context.fromJson(contextJson);
//        log.info("HEADERS: " + context.headers.entrySet());


// обработка контекста
        context = HandlerAll.processContext(context);

//        log.info("CONTEXT JSON: " + context.toJson());


// положить в пэйлоад ответа: ид, топик,  контекст
        payload = "correlationId=" + correlationId + "&" +
                "userTopicId=" + topicRecieveDevice + "&" +
                "context=" + context.toJson();
////        подписаться на сгенерированый топик для ответа с токеном
//        subscribe(topicRecieveDevice);
//        отправить запрос получения токена
//        log.info("PAYLOAD: " + payload);
        try {
            log.info("PUBLISH RESPONSE TO TOPIC: " + topicPublish);
            MqttMessage responseMessage = new MqttMessage(payload.getBytes());
            mqttClient.publish(topicPublish, responseMessage);
        } catch (MqttException e) {
            log.info("ERROR: " + e);
        }
    }

//    private static Map<String, String> parseParams(String message) {
//        Map<String, String> result = new HashMap<>();
//        if (message == null || message.isEmpty()) return result;
//// Ищем параметры по ключам с учетом их позиции
//        int ctxStart = message.indexOf("context=");
//        if (ctxStart == -1) return result;
//// Выделяем correlationId и requestId до начала context
//        String prefix = message.substring(0, ctxStart);
//        String[] parts = prefix.split("&");
//        for (String part : parts) {
//            String[] kv = part.split("=", 2);
//            if (kv.length != 2) continue;
//            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
//            String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
//            if (key.equals("correlationId") || key.equals("requestId")) {
//                result.put(key, value);
//            }
//        }
//// Извлекаем context как всю оставшуюся часть строки
//        String contextValue = message.substring(ctxStart + "context=".length());
//        result.put("context", URLDecoder.decode(contextValue, StandardCharsets.UTF_8));
//        return result;
//    }


    //  это для паблиша
    public static String publishContextWaitForContext(String topic, Context context, Integer timeout, String action, String correlationId) {
        log.info("WITHOUT TEXT text null");
        return publishContextWaitForContext(topic, context, timeout, action, correlationId, null);
    }

    // ЭТО РАБОЧИЙ СЕЙЧАС МЕТОД ДЛЯ УДЯ КОМАНД
    public static String publishContextWaitForContext(String topic, Context context, Integer timeout, String action, String correlationId, String text) {
        log.info("MQTT PUBLISH TO TOPIC: " + topic);
        log.info("TEXT: " + text);
        if (context == null) context = new Context();
        if (correlationId == null) correlationId = UUID.randomUUID().toString();
        String responseBody = "";
        String contextJson = context.toJson();

//        генерация топика для колбэка с токеном
        String callbackTopic = "callback" + correlationId;
        //     подписаться на сгенерированый топик
        subscribe(callbackTopic);

//        подготовка пэйлоад
        try {

            String payload = "correlationId=" + correlationId + "&" +
                    "callbackTopic=" + callbackTopic + "&" +
                    "action=" + action + "&" +
                    "text=" + text + "&" +
                    "context=" + contextJson;
            log.info("PAYLOAD: " + payload);

// Отправка запроса в MQTT
            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));
// Ожидание ответа
            CompletableFuture<String> future = responseManager.waitForResponse(correlationId);
// Получение ответа
            try {
                log.info("MQTT WAIT FOR RESPONSE...");
// если таймаут больше 4 то навык ответит раньше что Навык не отвечает
// 4 - недождалась ответа, но иногда может быть Навык неотвечает
// для УДЯ было 10
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

    public static void unsubscribe(String topic) {
        log.info("HIVE UNSUBSCRIBE TOPIC: " + topic);
        try {
            mqttClient.unsubscribe(topic);
        } catch (MqttException e) {
            log.info("MQTT UNSUBSCRIBE ERROR: " + e);
        }
    }

    public static void stop() {
        log.info("HIVE STOP");
        try {
            mqttClient.disconnect();
            mqttClient.close();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        mqttClient = null;
        log.info("MQTT CLIENT CLOSED");
    }
}
