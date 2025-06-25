package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.spotify.SpotifyUserParser;
import org.knovash.squeezealice.yandex.YandexJwtUtils;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class Hive implements MqttCallbackExtended {

    private MqttClient mqttClient;
    public String correlationId = "";
    private final String hiveBroker = config.hiveBroker;
    private final String hiveUsername = config.hiveUsername;
    private final String hivePassword = config.hivePassword;
    private final ResponseManager responseManager = new ResponseManager();
    public String topicRecieveDevice = "to_lms_id";// подписаться
    public String topicPublish = "from_lms_id";// отправить сюда
    public long spotifyExpiresAt;

    // отслеживание состояния подключения
    private volatile boolean isConnected = false;
    // восстановление подписок после переподключения
    private final Set<String> activeSubscriptions = new ConcurrentSkipListSet<>();
    // управление переподключениями
    private ScheduledExecutorService reconnectScheduler;

    public void start(String hiveBroker, String hiveUsername,String hivePassword) {
        try {
            mqttClient = new MqttClient(hiveBroker, MqttClient.generateClientId(), new MemoryPersistence());
            mqttClient.setCallback(this);
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
            isConnected = true;
        } catch (MqttException e) {
            log.error("MQTT INITIAL CONNECTION ERROR: {}", e.getMessage());
            scheduleReconnection();
        }
    }

    public void start() {
        try {
            mqttClient = new MqttClient(hiveBroker, MqttClient.generateClientId(), new MemoryPersistence());
            mqttClient.setCallback(this);
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
            isConnected = true;
        } catch (MqttException e) {
            log.error("MQTT INITIAL CONNECTION ERROR: {}", e.getMessage());
            scheduleReconnection();
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("MQTT CONNECTION ESTABLISHED. Reconnect: {}", reconnect);
        isConnected = true;

        // Восстанавливаем подписки после переподключения
        if (reconnect && !activeSubscriptions.isEmpty()) {
            log.info("Restoring {} subscriptions...", activeSubscriptions.size());
            activeSubscriptions.forEach(topic -> {
                try {
                    mqttClient.subscribe(topic);
                    log.info("Subscription restored: {}", topic);
                } catch (MqttException e) {
                    log.error("Failed to restore subscription: {}", topic, e);
                }
            });
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT CONNECTION LOST: {}", cause.getMessage());
        isConnected = false;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        handleDeviceAndPublish(topic, message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    public void subscribeByYandex() {
        if (config.yandexUid == null || config.yandexUid.equals("")) {
            log.info("SUBSCRIBE BY YANDEX FAIL: {}", config.yandexUid);
            return;
        }
        String topic = topicRecieveDevice + config.yandexUid;
        log.info("SUBSCRIBE BY YANDEX: {}", topic);
        subscribe(topic);

    }

    public void subscribe(String subscribeToTopic) {
        log.info("SUBSCRIBE TO TOPIC: {}", subscribeToTopic);
        try {
            // Добавляем в список активных подписок
            activeSubscriptions.add(subscribeToTopic);

            mqttClient.subscribe(subscribeToTopic, (topic, message) -> {
                messageArrived(topic, message);
            });
        } catch (MqttException e) {
            log.error("SUBSCRIBE ERROR: {}", e.getMessage());
        }
    }

    private void handleDeviceAndPublish(String topicRecieved, MqttMessage message) {
//        log.info("");
//        log.info("---------------------------------------------------------------------------------------------");
        log.info("\nRECEIVED MESSAGE FROM TOPIC: " + topicRecieved);
        String payload = new String(message.getPayload());
        Map<String, String> params = Parser.run(payload);

        // Обработка специальных действий
        switch (params.getOrDefault("action", "")) {
            case "yandex_callback_token":
                takeYandexTokenFromMessage(params);
                return;
            case "spotify_callback_token":
                takeSpotifyTokenFromMessage(params);
                return;
            case "spotify_callback_refresh_token":
                takeSpotifyRefreshTokenFromMessage(params);
                return;
        }

        // Обработка обычных сообщений
        if (!params.containsKey("correlationId")) {
            log.error("ERROR: NO CORRELATION ID IN MESSAGE");
            return;
        }
        correlationId = params.get("correlationId");

        String contextJson = params.getOrDefault("context", "");
        if (contextJson.isEmpty()) {
            log.error("ERROR: NO CONTEXT IN MESSAGE");
            return;
        }

        Context context = Context.fromJson(contextJson);
        context = HandlerAll.processContext(context);

        String responsePayload = "correlationId=" + correlationId + "&" +
                "userTopicId=" + topicRecieveDevice + "&" +
                "context=" + context.toJson();

        try {
            log.info("PUBLISH RESPONSE TO TOPIC: {}", topicPublish);
            mqttClient.publish(topicPublish, new MqttMessage(responsePayload.getBytes()));
        } catch (MqttException e) {
            log.error("PUBLISH ERROR: {}", e.getMessage());
        }
    }

    public void publish(String topic, String message) {
        if (!isConnected()) {
            log.warn("SKIPPED PUBLISH - MQTT NOT CONNECTED");
            return;
        }

        log.info("PUBLISH TO TOPIC: {}", topic);
        try {
            mqttClient.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            log.error("PUBLISH ERROR: {}", e.getMessage());
        }
    }

    public String publishAndWaitForResponse(String topic, Context context, Integer timeout, String action, String correlationId) {
        return publishAndWaitForResponse(topic, context, timeout, action, correlationId, null);
    }

    public String publishAndWaitForResponse(String topic, Context context, Integer timeout, String action, String correlationId, String text) {
        if (!isConnected()) {
            log.warn("SKIPPED PUBLISH-AND-WAIT - MQTT NOT CONNECTED");
            return "MQTT_NOT_CONNECTED";
        }

        log.info("MQTT PUBLISH TO TOPIC: {} AND WAIT FOR RESPONSE", topic);
        if (context == null) context = new Context();
        if (correlationId == null) correlationId = UUID.randomUUID().toString();

        String responseBody = "";
        String callbackTopic = "callback" + correlationId;

        subscribe(callbackTopic);

        try {
            String payload = "correlationId=" + correlationId + "&" +
                    "callbackTopic=" + callbackTopic + "&" +
                    "action=" + action + "&" +
                    "text=" + text + "&" +
                    "context=" + context.toJson();

            log.info("PAYLOAD: {}", payload);
            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));

            CompletableFuture<String> future = responseManager.waitForResponse(correlationId);
            try {
                log.info("MQTT WAIT FOR RESPONSE...");
                responseBody = future.get(timeout, TimeUnit.SECONDS);
                log.info("MQTT RESPONSE RECEIVED OK");
            } catch (TimeoutException e) {
                log.warn("MQTT RESPONSE TIMEOUT: {}", e.getMessage());
                responseBody = "TIMEOUT";
            }
        } catch (Exception e) {
            log.error("PUBLISH-AND-WAIT ERROR: {}", e.getMessage());
        } finally {
            unsubscribe(callbackTopic);
        }
        return responseBody;
    }

    private class ResponseManager {

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

    public void unsubscribe(String topic) {
        log.info("MQTT UNSUBSCRIBE TOPIC: {}", topic);
        try {
            activeSubscriptions.remove(topic);
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.unsubscribe(topic);
            }
        } catch (MqttException e) {
            log.error("UNSUBSCRIBE ERROR: {}", e.getMessage());
        }
    }

    public void stop() {
        log.info("MQTT STOPPING...");
        if (reconnectScheduler != null) {
            reconnectScheduler.shutdownNow();
        }

        if (mqttClient != null) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
                log.info("MQTT CLIENT DISCONNECTED");
            } catch (MqttException e) {
                log.error("DISCONNECT ERROR: {}", e.getMessage());
            }
        }
        isConnected = false;
        activeSubscriptions.clear();
    }

    public void takeYandexTokenFromMessage(Map<String, String> params) {
        log.info("RECEIVED YANDEX TOKEN");
        String token = params.get("token");
        if (token == null) return;

        Main.yandexToken = token;
        config.yandexToken = token;
        String currentUid = config.yandexUid;
        String newUid = YandexJwtUtils.getValueByTokenAndKey(token, "uid");
        String yandexName = YandexJwtUtils.getValueByTokenAndKey(token, "display_name");

        if (!newUid.equals(currentUid)) {
            // Переподписываемся на новый топик пользователя
            unsubscribe(topicRecieveDevice + currentUid);
            subscribe(topicRecieveDevice + newUid);
            config.yandexUid = newUid;
            config.yandexName = yandexName;
            config.write();

            lmsPlayers.checkRooms();
            lmsPlayers.write();
        }

        log.info("YANDEX UID UPDATED: {}", newUid);
        responseManager.completeResponse(correlationId, "OK");
    }

    public void takeSpotifyTokenFromMessage(Map<String, String> params) {
        log.info("RECEIVED SPOTIFY TOKEN");
        String token = params.get("token");
        String refreshToken = params.get("refreshToken");
        spotifyExpiresAt = Long.parseLong(params.get("expiresAt"));

        if (token == null) return;

        log.info("Spotify token received");
        config.spotifyToken = token;
        config.spotifyRefreshToken = refreshToken;
        config.spotifyTokenExpiresAt = spotifyExpiresAt;

        // Получаем имя пользователя Spotify
        config.spotifyName = SpotifyUserParser.parseUserInfo(Spotify.me()).getDisplayName();
        config.write();

        log.info("Spotify user: {}", config.spotifyName);
        responseManager.completeResponse(correlationId, "OK");
    }

    public void takeSpotifyRefreshTokenFromMessage(Map<String, String> params) {
        log.info("RECEIVED SPOTIFY REFRESH TOKEN RESPONSE");
        String refreshTokenResponse = params.get("refreshTokenResponse");
        if (refreshTokenResponse == null) return;

        JSONObject jsonObject = new JSONObject(refreshTokenResponse);
        String accessToken = jsonObject.getString("access_token");
        int expiresIn = jsonObject.getInt("expires_in");

        spotifyExpiresAt = System.currentTimeMillis() + expiresIn * 1000L;
        config.spotifyToken = accessToken;
        config.spotifyTokenExpiresAt = spotifyExpiresAt;
        config.write();

        log.info("Spotify token refreshed, expires in {} seconds", expiresIn);
        responseManager.completeResponse(correlationId, "OK");
    }

    public boolean isConnected() {
        boolean mqttConnected = mqttClient.isConnected();
        boolean state = isConnected && mqttClient != null && mqttConnected;
//        log.info("MQTT isConnected: " + isConnected);
//        log.info("MQTT mqttClient: " + mqttClient.getClientId());
        log.info("MQTT mqttConnected: " + mqttConnected);
//        log.info("MQTT STATE: " + state);
        return state;
    }

    private void scheduleReconnection() {
        if (reconnectScheduler != null && !reconnectScheduler.isShutdown()) {
            reconnectScheduler.shutdownNow();
        }

        reconnectScheduler = Executors.newSingleThreadScheduledExecutor();
        reconnectScheduler.scheduleAtFixedRate(() -> {
            if (!isConnected()) {
                log.warn("Attempting to reconnect to MQTT broker...");
                try {
                    stop();
                    start();
                } catch (Exception e) {
                    log.error("Reconnection attempt failed: {}", e.getMessage());
                }
            }
        }, 5, 30, TimeUnit.SECONDS); // Первая попытка через 5 сек, затем каждые 30 сек
    }


    public void periodicCheckStart() {
        // Запуск периодической проверки MQTT
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (hive != null && !hive.isConnected()) {
                log.warn("MQTT connection lost! Attempting to reconnect...");
                hive.stop();
                hive.start();
                hive.subscribeByYandex();
            }
        }, 1, 1, TimeUnit.MINUTES); // Проверка каждую минуту
    }


}