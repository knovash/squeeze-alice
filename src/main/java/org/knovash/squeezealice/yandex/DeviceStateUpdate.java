package org.knovash.squeezealice.yandex;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.config;

@Data
@AllArgsConstructor
@Log4j2
public class DeviceStateUpdate {
    private String deviceId;   // external_id устройства в Яндекс
    private String type;       // "on_off" или "range"
    private String instance;   // "on" или "volume"
    private Object value;      // Boolean для on_off, Integer для volume

    public static void sendBatchStateUpdates(List<DeviceStateUpdate> updates) {
//        log.info("START");
//        log.info(updates);
        if (updates == null || updates.isEmpty()) {
            return;
        }

        // Группируем обновления по deviceId
        Map<String, List<DeviceStateUpdate>> grouped = updates.stream()
                .collect(Collectors.groupingBy(DeviceStateUpdate::getDeviceId));

        // Формируем JSON для Яндекс API
        List<Map<String, Object>> devicesList = new ArrayList<>();

        for (Map.Entry<String, List<DeviceStateUpdate>> entry : grouped.entrySet()) {
            String deviceId = entry.getKey();
            List<DeviceStateUpdate> deviceUpdates = entry.getValue();

            List<Map<String, Object>> capabilities = new ArrayList<>();
            for (DeviceStateUpdate upd : deviceUpdates) {
                Map<String, Object> capability = new HashMap<>();
                capability.put("type", "devices.capabilities." + upd.getType());

                Map<String, Object> state = new HashMap<>();
                state.put("instance", upd.getInstance());
                state.put("value", upd.getValue());
                capability.put("state", state);

                capabilities.add(capability);
            }

            Map<String, Object> deviceMap = new HashMap<>();
            deviceMap.put("id", deviceId);
            deviceMap.put("capabilities", capabilities);
            devicesList.add(deviceMap);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", config.yandexUid);
        payload.put("devices", devicesList);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ts", System.currentTimeMillis() / 1000.0);
        requestBody.put("payload", payload);

        String jsonBody = JsonUtils.pojoToJson(requestBody);

        CompletableFuture.runAsync(() -> {
            try {
                String url = "https://dialogs.yandex.net/api/v1/skills/" + config.skillId + "/callback/state";
                log.info("POST " + url);
                HttpResponse response = Request.Post(url)
                        .setHeader("Authorization", "OAuth " + config.yandextSkillTokenDeveloper)
                        .setHeader("Content-Type", "application/json")
                        .bodyString(jsonBody, ContentType.APPLICATION_JSON)
                        .execute()
                        .returnResponse();

                int statusCode = response.getStatusLine().getStatusCode();
                log.info("STATUS CODE: " + statusCode);

                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

//                log.info("RESPONSE: " + responseBody);
//                if (statusCode != 202) {
//                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
//                    log.error("Batch update failed. Status: {} Body: {}", statusCode, responseBody);
//                }
            } catch (IOException e) {
                log.error("Error sending batch state update", e);
            }
        });

        log.info("FINISH");
    }
}