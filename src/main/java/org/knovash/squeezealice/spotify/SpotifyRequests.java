package org.knovash.squeezealice.spotify;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.http.HttpClientWrapper;
import org.knovash.squeezealice.http.HttpResponseResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class SpotifyRequests {

    private static final HttpClientWrapper httpClient = new HttpClientWrapper();

    private static synchronized void refreshTokenIfExpired() {
        log.info(start);
        if (config.spotifyToken == null || config.spotifyToken.isEmpty()) {
            log.debug("No access token, cannot refresh");
            return;
        }
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
        log.info(finish);
    }

    private static HttpResponseResult executeRequest(String method, String uri, String body) {
        refreshTokenIfExpired();
        if (config.spotifyToken == null || config.spotifyToken.isEmpty()) {
            log.error("No access token, aborting request to {}", uri);
            return HttpResponseResult.error("No access token");
        }
        else log.info("TOKEN OK");

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + config.spotifyToken);
        headers.put("Content-Type", "application/json");

        HttpResponseResult result;

        switch (method.toUpperCase()) {
            case "GET":
                result = httpClient.doGet(uri, headers);
                break;
            case "POST":
                result = httpClient.doPost(uri, body, headers);
                break;
            case "PUT":
                result = httpClient.doPut(uri, body, headers);
                break;
            default:
                return HttpResponseResult.error("Unsupported method");
        }

        if (result.getStatusCode() == 401) {
            log.warn("Received 401, forcing token refresh and retry...");
            refreshTokenIfExpired();
            if (config.spotifyToken != null && !config.spotifyToken.isEmpty()) {
                headers.put("Authorization", "Bearer " + config.spotifyToken);
                switch (method.toUpperCase()) {
                    case "GET":
                        result = httpClient.doGet(uri, headers);
                        break;
                    case "POST":
                        result = httpClient.doPost(uri, body, headers);
                        break;
                    case "PUT":
                        result = httpClient.doPut(uri, body, headers);
                        break;
                }
            }
        }
        if (result.getStatusCode() == 204) {
            log.info("204");
        }
        log.info("RESULT: " + result.getStatusCode());
        return result;
    }

    public static String requestGet(String uri) {
        HttpResponseResult result = executeRequest("GET", uri, null);
        log.info("RESULT: " + result);
        if (result.isSuccess()) {
            log.info("OK");
            return result.getBody();
        } else {
            log.error("GET request failed: {}", result.getStatusCode());
            return null;
        }
    }

    public static String requestPut(String uri) {
        HttpResponseResult result = executeRequest("PUT", uri, null);
        if (result.isSuccess()) {
            return result.getBody();
        } else {
            log.error("PUT request failed: {}", result.getStatusCode());
            return null;
        }
    }

    public static String requestPost(String uri) {
        HttpResponseResult result = executeRequest("POST", uri, null);
        if (result.isSuccess()) {
            return result.getBody();
        } else {
            log.error("POST request failed: {}", result.getStatusCode());
            return null;
        }
    }
}