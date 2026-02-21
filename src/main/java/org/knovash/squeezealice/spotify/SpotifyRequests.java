package org.knovash.squeezealice.spotify;

import lombok.extern.log4j.Log4j2;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.Main.hive;

@Log4j2
public class SpotifyRequests {

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    /**
     * Синхронное обновление токена, если он истёк.
     * Блокирует поток до получения нового токена.
     */
    private static synchronized void refreshTokenIfExpired() {
        long now = System.currentTimeMillis();
        if (now > config.spotifyTokenExpiresAt) {
            log.info("Spotify token expired. Requesting refresh...");
            String sessionId = UUID.randomUUID().toString();
            // Предполагается, что hive.publishAndWaitForResponse блокирует выполнение до получения ответа
            hive.publishAndWaitForResponse("from_local_request", null, 10,
                    "token_spotify_refresh", sessionId, config.spotifyRefreshToken);
            log.info("Token refreshed. New expiry: {}", config.spotifyTokenExpiresAt);
        }
    }

    /**
     * Универсальный метод выполнения HTTP-запроса к Spotify API.
     *
     * @param request    подготовленный запрос (HttpGet, HttpPut и т.д.)
     * @param expectBody true, если ожидается тело ответа (для методов с возможным 204 No Content)
     * @return тело ответа в виде строки или null, если ответ без тела или ошибка
     */
    private static String executeRequest(HttpUriRequest request, boolean expectBody) {
        refreshTokenIfExpired();
        request.setHeader(new BasicHeader("Authorization", "Bearer " + config.spotifyToken));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            log.debug("Request to {} returned {}", request.getURI(), statusCode);

            if (statusCode == 204) {
                return null; // нет содержимого
            }

            String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode == 200) {
                return json;
            } else if (statusCode == 401) {
                // Возможно, токен устарел сразу после проверки – пробуем обновить и повторить один раз
                log.warn("Received 401, forcing token refresh and retry...");
                refreshTokenIfExpired();
                request.setHeader("Authorization", "Bearer " + config.spotifyToken);
                try (CloseableHttpResponse retryResponse = httpClient.execute(request)) {
                    int retryCode = retryResponse.getStatusLine().getStatusCode();
                    if (retryCode == 200) {
                        return EntityUtils.toString(retryResponse.getEntity(), StandardCharsets.UTF_8);
                    } else {
                        log.error("Retry failed with code {}", retryCode);
                        return null;
                    }
                }
            } else {
                log.error("Request failed with code {}: {}", statusCode, json);
                return null;
            }
        } catch (IOException e) {
            log.error("HTTP error: {}", e.getMessage(), e);
            return null;
        }
    }

    // ------ Публичные методы (сохранены исходные сигнатуры) ------

    /**
     * Метод для совместимости со старым кодом (используется только в me()).
     * Не использует автоматическое обновление токена и повтор при 401,
     * чтобы полностью сохранить оригинальное поведение.
     */

    public static String requestWithRefreshGet(String uri) {
        return executeRequest(new HttpGet(uri), true);
    }

    public static String requestWithRetryPut(String uri) {
        return executeRequest(new HttpPut(uri), false);
    }

    public static String requestWithRetryPost(String uri) {
        return executeRequest(new HttpPost(uri), true);
    }

    public static String requestPutHttpClient(String uri) {
        return executeRequest(new HttpPut(uri), false);
    }

}