package org.knovash.squeezealice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

@Log4j2
public class YandexJwtUtils {

//    Обмен токена на информацию о пользователе
//    https://yandex.ru/dev/id/doc/ru/user-information

    public static String getJwtByOauth(String oauthToken) {
        log.info("START GET JWT TOKEN BY OAUTH TOKEN: " + oauthToken);
        String url = "https://login.yandex.ru/info?format=jwt";

        HttpURLConnection connection = null;
        try {
            // Создаем соединение
            URL apiUrl = new URL(url);
            connection = (HttpURLConnection) apiUrl.openConnection();

            // Устанавливаем метод и заголовки
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "OAuth " + oauthToken);
            connection.setRequestProperty("Accept", "application/jwt");
            // Получаем ответ
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Читаем ответ
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                log.info("JWT TOKEN FINISH");
                return response.toString();
            } else {
                log.info("ERROR");
                return "ERROR";
//                throw new Exception("HTTP error code: " + responseCode);
            }
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String parseYandexJwtForKey(String jwtToken, String key) {
        log.info("START PARSE JWT FOR KEY: " + key);
        // Разбиваем JWT на части
        String[] parts = jwtToken.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token");
        }
        // Декодируем payload (вторую часть)
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        // Парсим JSON
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> claims = null;
        try {
            claims = mapper.readValue(payload, Map.class);
        } catch (JsonProcessingException e) {
            log.info("ERROR: " + e);
            return null;
//            throw new RuntimeException(e);
        }

        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            System.out.printf("%-15s: %s%n", entry.getKey(), entry.getValue());
        }

        Map.Entry<String, Object> set = claims.entrySet().stream()
//                .peek(c -> log.info(c))
                .filter(c -> c.getKey().equals(key))
                .findFirst()
                .orElse(null);
        if (set == null) return null;
        String result = String.valueOf(set.getValue());
        log.info("KEY: "+key+ " VALUE:"+result);
        return result;
    }


    public static String getValueByTokenAndKey(String token, String key){
       String jwtToken= YandexJwtUtils.getJwtByOauth(token);
       String value = YandexJwtUtils.parseYandexJwtForKey(jwtToken,key);
       return value;
    }

}