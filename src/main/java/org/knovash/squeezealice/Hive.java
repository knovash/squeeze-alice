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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.knovash.squeezealice.Main.config;

@Log4j2
public class Hive {

    private  MqttClient mqttClient;
    public  String correlationId = "";
    private  final String hiveBroker = config.hiveBroker;
    private  final String hiveUsername = config.hiveUsername;
    private  final String hivePassword = config.hivePassword;
    private  final ResponseManager responseManager = new ResponseManager();
    public  String topicRecieveDevice = "to_lms_id";// подписаться
    public  String topicPublish = "from_lms_id";// отправить сюда
    public  long spotifyExpiresAt;

    public  void start() {
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
            subscribeByYandex();
            log.info("MQTT STARTED OK");
        } catch (MqttException e) {
            log.info("MQTT ERROR: " + e);
        }
    }

    public  void subscribeByYandex() {
        if (config.yandexUid == null || config.yandexUid.equals("")) {
            log.info("SUBSCRIBE BY YANDEX FAIL: " + config.yandexUid);
            return;
        }
        log.info("SUBSCRIBE BY YANDEX: " + topicRecieveDevice + config.yandexUid);
        subscribe(topicRecieveDevice + config.yandexUid);
    }

    public  void subscribe(String subscribeToTopic) {
        log.info("SUBSCRIBE TO TOPIC: " + subscribeToTopic);
        try {
            mqttClient.subscribe(subscribeToTopic, (topic, message) -> handleDeviceAndPublish(topic, message));
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private  void handleDeviceAndPublish(String topicRecieved, MqttMessage message) {
        log.info("");
        log.info("---------------------------------------------------------------------------------------------");
        log.info("RECIEVED MESSAGE FROM TOPIC: " + topicRecieved);
        String payload = new String(message.getPayload());
        Map<String, String> params = Parser.run(payload);

// получить токен Yandex. не отвечать
        if ("yandex_callback_token".equals(params.get("action"))){
            takeYandexTokenFromMessage(params);
            return;
        }
// получить токен Spotify. не отвечать
            if ("spotify_callback_token".equals(params.get("action"))){
            takeSpotifyTokenFromMessage(params);
            return;
        }
// получить рефлеш токен Spotify. не отвечать
            if ("spotify_callback_refresh_token".equals(params.get("action"))){
            takeSpotifyRefreshTokenFromMessage(params);
            return;
        }

// получить контекст, выполнить действия, отправить контекст в брокер
        correlationId = ""; // TODO
        if (!params.containsKey("correlationId")) {
            log.info("ERROR: NO CORRELATION ID IN MESSAGE");
            return;
        }
        correlationId = params.get("correlationId");
// полученный контекст
        String contextJson = params.getOrDefault("context", "");
        if (contextJson.equals("")) {
            log.info("ERROR: NO CONTEXT IN MESSAGE");
            return;
        }
        Context context = Context.fromJson(contextJson);
// выполнить действия с контекстом
        context = HandlerAll.processContext(context);
// положить в пэйлоад ответа: ид, топик,  контекст
        payload = "correlationId=" + correlationId + "&" +
                "userTopicId=" + topicRecieveDevice + "&" +
                "context=" + context.toJson();
// отправить сообщение в топик
        try {
            log.info("PUBLISH RESPONSE TO TOPIC: " + topicPublish);
            mqttClient.publish(topicPublish, new MqttMessage(payload.getBytes()));
        } catch (MqttException e) {
            log.info("ERROR: " + e);
        }
    }

    public  void publish(String topic, String message) {
        log.info("PUBLISH TO TOPIC: " + topic);
        try {
            mqttClient.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            log.info("ERROR: " + e);
        }
    }

    // яндекс запрос токена
// спотифай запрос токена
    public  String publishAndWaitForResponse(String topic, Context context, Integer timeout, String action, String correlationId) {
        return publishAndWaitForResponse(topic, context, timeout, action, correlationId, null);
    }

    // спотифай запрос рефреш токена
    public  String publishAndWaitForResponse(String topic, Context context, Integer timeout, String action, String correlationId, String text) {
        log.info("MQTT PUBLISH TO TOPIC: " + topic + " AND WAIT FOR CONTEXT RESPONSE");
        if (context == null) context = new Context();
        if (correlationId == null) correlationId = UUID.randomUUID().toString();
        String responseBody = "";
        String contextJson = context.toJson();
// генерация топика для колбэка с токеном
        String callbackTopic = "callback" + correlationId;
// подписаться на сгенерированый топик
        subscribe(callbackTopic);
// подготовка пэйлоад
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
// если таймаут больше 4 то навык ответит что Навык не отвечает
// 4 - недождалась ответа, но иногда может быть Навык неотвечает
// для УДЯ было 10
                responseBody = future.get(timeout, TimeUnit.SECONDS);
                log.info("MQTT RESPONSE RECIEVED OK");
            } catch (TimeoutException e) {
                log.info("MQTT ERROR NO RESPONSE: " + e);
                responseBody = "---";
            }
        } catch (Exception e) {
            log.info("MQTT ERROR: " + e);
        }
        return responseBody;
    }

    private  class ResponseManager {

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

    public  void unsubscribe(String topic) {
        log.info("MQTT UNSUBSCRIBE TOPIC: " + topic);
        try {
            mqttClient.unsubscribe(topic);
        } catch (MqttException e) {
            log.info("MQTT UNSUBSCRIBE ERROR: " + e);
        }
    }

    public  void stop() {
        log.info("MQTT STOP");
        try {
            mqttClient.disconnect();
            mqttClient.close();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        mqttClient = null;
        log.info("MQTT CLIENT CLOSED");
    }

    public  void takeYandexTokenFromMessage(Map<String, String> params) {
        log.info("RECIEVED YANDEX TOKEN");
        String token = params.getOrDefault("token", null);
        if (token == null) return;
        Main.yandexToken = token;
        config.yandexToken = token;
        String currentUid = config.yandexUid;
        String newUid = YandexJwtUtils.getValueByTokenAndKey(token, "uid");
        String yandexName = YandexJwtUtils.getValueByTokenAndKey(token, "display_name");
        if (newUid.equals(currentUid)) return;
// если полученый уид отличается то переподписаться на новый топик нового пользователя
        this.unsubscribe(topicRecieveDevice + currentUid);
        this.subscribe(topicRecieveDevice + newUid);
        config.yandexUid = newUid;
        config.yandexName = yandexName;
        config.write();
        log.info("TOKEN: " + token);
        responseManager.completeResponse(correlationId, "OK");
    }

    public  void takeSpotifyTokenFromMessage(Map<String, String> params) {
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
// получить имя пользователя Spotify
        config.spotifyName = SpotifyUserParser.parseUserInfo(Spotify.me()).getDisplayName();
        config.write();
        PlayerState ps = Spotify.playerState;
        log.info(ps);
        responseManager.completeResponse(correlationId, "OK");
    }

    public  void takeSpotifyRefreshTokenFromMessage(Map<String, String> params) {
        log.info("RECIEVED SPOTIFY REFRESH TOKEN");
        String refreshTokenResponse = params.getOrDefault("refreshTokenResponse", null);
        log.info("SPOTIFY REFRESH TOKEN RESPONSE: " + refreshTokenResponse);
// json body response от Spotify
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
    }

}
