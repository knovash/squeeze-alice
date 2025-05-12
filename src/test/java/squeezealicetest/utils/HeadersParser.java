package squeezealicetest.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HeadersParser {

    public static com.sun.net.httpserver.Headers parseHeaders(String headersJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Парсим JSON в Map<String, List<String>>
            Map<String, List<String>> headersMap = objectMapper.readValue(
                    headersJson,
                    new TypeReference<Map<String, List<String>>>() {}
            );

            // Создаем объект заголовков
            com.sun.net.httpserver.Headers headers = new com.sun.net.httpserver.Headers();

            // Заполняем заголовки
            for (Map.Entry<String, List<String>> entry : headersMap.entrySet()) {
                String headerName = entry.getKey();
                for (String value : entry.getValue()) {
                    headers.add(headerName, value);
                }
            }
            return headers;

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse headers JSON", e);
        }
    }
}