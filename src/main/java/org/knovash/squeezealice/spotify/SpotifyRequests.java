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
        // Если нет токена вообще — ничего не делаем
        if (config.spotifyToken == null || config.spotifyToken.isEmpty()) {
            log.debug("No access token, cannot refresh");
            return;
        }
        // Если нет refresh token — тоже не можем обновить
        if (config.spotifyRefreshToken == null || config.spotifyRefreshToken.isEmpty()) {
            log.debug("No refresh token, cannot refresh");
            return;
        }
        long now = System.currentTimeMillis();
        if (now > config.spotifyTokenExpiresAt) {
            log.info("Spotify token expired. Requesting refresh...");
            String sessionId = UUID.randomUUID().toString();
            hive.publishAndWaitForResponse("from_local_request", null, 10,
                    "token_spotify_refresh", sessionId, config.spotifyRefreshToken);
            log.info("Token refreshed. New expiry: {}", config.spotifyTokenExpiresAt);
        }
    }

    /**
     * Универсальный метод выполнения HTTP-запроса к Spotify API.
     */
    private static String executeRequest(HttpUriRequest request, boolean expectBody) {
        refreshTokenIfExpired();
        // Если после refresh токен всё ещё отсутствует, не пытаемся делать запрос
        if (config.spotifyToken == null || config.spotifyToken.isEmpty()) {
            log.error("No access token, aborting request to {}", request.getURI());
            return null;
        }
        request.setHeader(new BasicHeader("Authorization", "Bearer " + config.spotifyToken));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            log.debug("Request to {} returned {}", request.getURI(), statusCode);

            if (statusCode == 204) {
                return null;
            }

            String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode == 200) {
                return json;
            } else if (statusCode == 401) {
                log.warn("Received 401, forcing token refresh and retry...");
                refreshTokenIfExpired();
                if (config.spotifyToken == null || config.spotifyToken.isEmpty()) {
                    log.error("Still no access token after refresh, cannot retry");
                    return null;
                }
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

    public static String requestGet(String uri) {
        return executeRequest(new HttpGet(uri), true);
    }

    public static String requestPut(String uri) {
        return executeRequest(new HttpPut(uri), false);
    }

    public static String requestPost(String uri) {
        return executeRequest(new HttpPost(uri), true);
    }
}