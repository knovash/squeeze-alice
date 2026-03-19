package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
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

    private static final String CLIENT_ID = "squeeze-alice-client2";

    public MqttClient mqttClient;
    private final String hiveBroker = config.hiveBroker;
    private final String hiveUsername = config.hiveUsername;
    private final String hivePassword = config.hivePassword;
    private final ResponseManager responseManager = new ResponseManager();

    public String topicRecieveDevice = "to_lms_id"; // подписка на команды от облака
    public String topicPublish = "from_lms_id";     // публикация ответов в облако
    public long spotifyExpiresAt;

    private volatile boolean isConnected = false;
    private final Set<String> activeSubscriptions = new ConcurrentSkipListSet<>();

    public void start() {
        start(hiveBroker, hiveUsername, hivePassword);
    }

    public void start(String broker, String username, String password) {
        try {
            mqttClient = new MqttClient(broker, CLIENT_ID, new MemoryPersistence());
            mqttClient.setCallback(this);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30);
            options.setMaxReconnectDelay(30000);
            options.setUserName(username);
            options.setPassword(password.toCharArray());

            mqttClient.connect(options);
            log.info("MQTT STARTED with clientId: {}", CLIENT_ID);
            isConnected = true;
        } catch (MqttException e) {
            log.error("MQTT INITIAL CONNECTION ERROR: {}", e.getMessage());
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("MQTT CONNECTION ESTABLISHED. Reconnect: {}", reconnect);
        isConnected = true;

        if (!activeSubscriptions.isEmpty()) {
            log.info("Restoring {} subscriptions...", activeSubscriptions.size());
            for (String topic : activeSubscriptions) {
                try {
                    mqttClient.subscribe(topic);
                    log.info("Subscription restored: {}", topic);
                } catch (MqttException e) {
                    log.error("Failed to restore subscription: {}", topic, e);
                }
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT CONNECTION LOST: {}", cause.getMessage());
        log.error("MQTT CONNECTION LOST: {}", cause.toString(), cause);
        isConnected = false;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        handleDeviceAndPublish(topic, message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // nothing
    }

    public void subscribeByYandex() {
        if (config.yandexUid == null || config.yandexUid.isEmpty()) {
            log.info("SUBSCRIBE BY YANDEX FAIL: uid is null or empty");
            return;
        }
        String topic = topicRecieveDevice + config.yandexUid;
        log.info("SUBSCRIBE BY YANDEX: {}", topic);
        subscribe(topic);
    }

    public void subscribe(String topic) {
        log.info("SUBSCRIBE TO TOPIC: {}", topic);
        activeSubscriptions.add(topic);

        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.subscribe(topic);
            } catch (MqttException e) {
                log.error("SUBSCRIBE ERROR: {}", e.getMessage());
            }
        } else {
            log.warn("MQTT client not connected – subscription will be restored later");
        }
    }

    public void unsubscribe(String topic) {
        log.info("MQTT UNSUBSCRIBE TOPIC: {}", topic);
        activeSubscriptions.remove(topic);
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.unsubscribe(topic);
            } catch (MqttException e) {
                log.error("UNSUBSCRIBE ERROR: {}", e.getMessage());
            }
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

        String callbackTopic = "callback" + correlationId;
        subscribe(callbackTopic);

        String responseBody = "";
        try {
            String payload = "correlationId=" + correlationId + "&" +
                    "callbackTopic=" + callbackTopic + "&" +
                    "action=" + action + "&" +
                    "text=" + (text != null ? text : "") + "&" +
                    "context=" + context.toJson();

//            log.info("PAYLOAD: {}", payload);
            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));

            CompletableFuture<String> future = responseManager.waitForResponse(correlationId);
            try {
                long start = System.currentTimeMillis();
                responseBody = future.get(timeout, TimeUnit.SECONDS);
                log.info("MQTT RESPONSE RECEIVED OK in {} ms", System.currentTimeMillis() - start);
            } catch (TimeoutException e) {
                log.warn("MQTT RESPONSE TIMEOUT after {} s", timeout);
                responseBody = "TIMEOUT";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("MQTT RESPONSE INTERRUPTED", e);
                responseBody = "INTERRUPTED";
            } catch (ExecutionException e) {
                log.error("MQTT RESPONSE EXECUTION ERROR", e.getCause());
                responseBody = "ERROR";
            }
        } catch (MqttException e) {
            log.error("PUBLISH-AND-WAIT ERROR: {}", e.getMessage());
        } finally {
            unsubscribe(callbackTopic);
        }
        return responseBody;
    }

    private void handleDeviceAndPublish(String topicReceived, MqttMessage message) {
        log.info("RECEIVED MESSAGE FROM TOPIC: " + topicReceived);
        String payload = new String(message.getPayload());
        Map<String, String> params = Parser.run(payload);

        // Специальные действия по токенам
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

        if (!params.containsKey("correlationId")) {
            log.error("ERROR: NO CORRELATION ID IN MESSAGE");
            return;
        }
        String corrId = params.get("correlationId");
        if (corrId == null) {
            log.error("ERROR: correlationId is null");
            return;
        }

        String contextJson = params.getOrDefault("context", "");
        if (contextJson.isEmpty()) {
            log.error("ERROR: NO CONTEXT IN MESSAGE");
            return;
        }

        Context context = Context.fromJson(contextJson);
        context = HandlerAll.switchPath(context);

        String responsePayload = "correlationId=" + corrId + "&" +
                "userTopicId=" + topicReceived + "&" +
                "context=" + context.toJson();

        try {
            log.info("PUBLISHING RESPONSE TO TOPIC: {} WITH CORRELATION {}", topicPublish, corrId);
            mqttClient.publish(topicPublish, new MqttMessage(responsePayload.getBytes()));
            log.info("RESPONSE PUBLISHED SUCCESSFULLY");
        } catch (MqttException e) {
            log.error("PUBLISH ERROR: {}", e.getMessage(), e);
        }
    }

    public void takeYandexTokenFromMessage(Map<String, String> params) {
        log.info("RECEIVED YANDEX TOKEN");
        String token = params.get("token");
        if (token == null) {
            log.error("No token in message");
            return;
        }

        String corrId = params.get("correlationId");
        if (corrId == null) {
            log.error("No correlationId in token message");
            return;
        }

        Main.yandexToken = token;
        config.yandexToken = token;
        String currentUid = config.yandexUid;
        String newUid = YandexJwtUtils.getValueByTokenAndKey(token, "uid");
        String yandexName = YandexJwtUtils.getValueByTokenAndKey(token, "display_name");

        if (!newUid.equals(currentUid)) {
            unsubscribe(topicRecieveDevice + currentUid);
            subscribe(topicRecieveDevice + newUid);
            config.yandexUid = newUid;
            config.yandexName = yandexName;
            config.write();

//            lmsPlayers.checkRooms();
            lmsPlayers.write();
        }

        log.info("YANDEX UID UPDATED: {}", newUid);
        responseManager.completeResponse(corrId, "OK");
        log.info("Future completed for correlationId: {}", corrId);
    }

    public void takeSpotifyTokenFromMessage(Map<String, String> params) {
        log.info("RECEIVED SPOTIFY TOKEN");
        String token = params.get("token");
        if (token == null) {
            log.error("No token in message");
            return;
        }

        String refreshToken = params.get("refreshToken");
        String expiresAtStr = params.get("expiresAt");
        if (expiresAtStr == null) {
            log.error("No expiresAt in token message");
            return;
        }
        try {
            spotifyExpiresAt = Long.parseLong(expiresAtStr);
        } catch (NumberFormatException e) {
            log.error("Invalid expiresAt format: {}", expiresAtStr);
            return;
        }

        String corrId = params.get("correlationId");
        if (corrId == null) {
            log.error("No correlationId in token message");
            return;
        }

        log.info("Spotify token received");
        config.spotifyToken = token;
        config.spotifyRefreshToken = refreshToken;
        config.spotifyTokenExpiresAt = spotifyExpiresAt;
        config.write();

        responseManager.completeResponse(corrId, "OK");
        log.info("Future completed for correlationId: {}", corrId);
    }

    public void takeSpotifyRefreshTokenFromMessage(Map<String, String> params) {
        log.info("RECEIVED SPOTIFY REFRESH TOKEN RESPONSE");
        String refreshTokenResponse = params.get("refreshTokenResponse");
        if (refreshTokenResponse == null) {
            log.error("No refreshTokenResponse in message");
            return;
        }

        JSONObject jsonObject = new JSONObject(refreshTokenResponse);
        String accessToken = jsonObject.getString("access_token");
        int expiresIn = jsonObject.getInt("expires_in");

        String corrId = params.get("correlationId");
        if (corrId == null) {
            log.error("No correlationId in token message");
            return;
        }

        spotifyExpiresAt = System.currentTimeMillis() + expiresIn * 1000L;
        config.spotifyToken = accessToken;
        config.spotifyTokenExpiresAt = spotifyExpiresAt;
        config.write();

        log.info("Spotify token refreshed, expires in {} seconds", expiresIn);
        responseManager.completeResponse(corrId, "OK");
        log.info("Future completed for correlationId: {}", corrId);
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected() && isConnected;
    }

    public void stop() {
        log.info("MQTT STOPPING...");
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

    private class ResponseManager {
        private final ConcurrentMap<String, CompletableFuture<String>> responses = new ConcurrentHashMap<>();

        public CompletableFuture<String> waitForResponse(String correlationId) {
            CompletableFuture<String> future = new CompletableFuture<>();
            responses.put(correlationId, future);
            return future;
        }

        public void completeResponse(String correlationId, String contextJson) {
            log.info("Completing response for correlationId: {}", correlationId);
            CompletableFuture<String> future = responses.remove(correlationId);
            if (future != null) {
                future.complete(contextJson);
            } else {
                log.warn("No future found for correlationId: {}", correlationId);
            }
        }
    }
}