package org.knovash.squeezealice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.knovash.squeezealice.Main.config;

@Log4j2
public class InfoClient {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Info fetchInfo(String baseUrl) {
        try {
            String url = baseUrl + "/info";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), Info.class);
            } else {
                log.error("Ошибка при запросе /info, код: {}", response.statusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Не удалось получить Info: ", e);
            return null;
        }
    }

    public static void setInfo(String baseUrl) {
        Info info = fetchInfo(baseUrl);
        config.yandexSstTttsApiKey = info.yandexSstTttsApiKey;
        config.yandextSkillTokenDeveloper = info.yandextSkillTokenDeveloper;
        config.skillId = info.skillId;
        log.info("CONFIG: " + config);
        config.write();
    }

    public static class Info {

        public String yandextSkillTokenDeveloper;
        public String yandexSstTttsApiKey;
        public String skillId;

//        @Override
//        public String toString() {
//            return "Info{" +
//                    "yandextSkillTokenDeveloper='" + yandextSkillTokenDeveloper + '\'' +
//                    ", yandexSstTttsApiKey='" + yandexSstTttsApiKey + '\'' +
//                    ", skillId='" + skillId + '\'' +
//                    '}';
//        }
    }
}