package org.knovash.squeezealice.volumio;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Log4j2
public class RequestsVolumio {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String get(String url) {
        log.info("URL: " + url);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("User-Agent", "SqueezeAlice/1.0")
                    .version(HttpClient.Version.HTTP_1_1)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                log.error("GET {} failed with status {}", url, response.statusCode());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            log.error("GET {} exception: {}", url, e.getMessage());
            return null;
        }
    }

    public static String volumioRequest(String baseUrl, VolumioRequest volumioReq) {
        String url = baseUrl + volumioReq.getEndpoint();
        log.info("REQUEST: {} {}", volumioReq.getMethod(), url);
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("User-Agent", "SqueezeAlice/1.0")
                    .version(HttpClient.Version.HTTP_1_1);

            if ("GET".equalsIgnoreCase(volumioReq.getMethod())) {
                builder.GET();
            } else if ("POST".equalsIgnoreCase(volumioReq.getMethod())) {
                String bodyJson = mapper.writeValueAsString(volumioReq.getBody());
                builder.POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                        .header("Content-Type", "application/json");
            } else {
                log.error("Unsupported HTTP method: {}", volumioReq.getMethod());
                return null;
            }

            HttpRequest request = builder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                log.error("{} {} failed with status {}", volumioReq.getMethod(), url, response.statusCode());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            log.error("{} {} exception: {}", volumioReq.getMethod(), url, e.getMessage());
            return null;
        }
    }
}