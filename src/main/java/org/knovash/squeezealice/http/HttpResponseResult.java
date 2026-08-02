package org.knovash.squeezealice.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.http.Header;

@Data
@AllArgsConstructor
public class HttpResponseResult {
    private int statusCode;
    private String body;
    private byte[] bodyBytes;          // для бинарных данных (например, аудио)
    private Header[] headers;

    // Конструктор для текстовых ответов (тело сохраняется как строка)
    public HttpResponseResult(int statusCode, String body, Header[] headers) {
        this(statusCode, body, null, headers);
    }

    // Конструктор для бинарных ответов (тело сохраняется как байты)
    public HttpResponseResult(int statusCode, byte[] bodyBytes, Header[] headers) {
        this(statusCode, null, bodyBytes, headers);
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    // Метод для создания результата с ошибкой
    public static HttpResponseResult error(String message) {
        return new HttpResponseResult(500, message, null);
    }
}