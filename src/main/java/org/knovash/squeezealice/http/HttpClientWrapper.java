package org.knovash.squeezealice.http;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.knovash.squeezealice.Main.start;

@Log4j2
public class HttpClientWrapper {

    private final HttpClient httpClient;

    public HttpClientWrapper() {
        this.httpClient = HttpClientFactory.getHttpClient();
    }

    // ---------- Публичные методы ----------
    public HttpResponseResult doGet(String url, Map<String, String> headers) {
        log.info(start);
        log.info(url);
        log.info(headers);
        return executeRequest(new HttpGet(url), headers);
    }

    public HttpResponseResult doPost(String url, String body, Map<String, String> headers) {
        HttpPost request = new HttpPost(url);
        if (body != null) {
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        }
        return executeRequest(request, headers);
    }

    public HttpResponseResult doPostBytes(String url, byte[] body, Map<String, String> headers) {
        HttpPost request = new HttpPost(url);
        if (body != null) {
            request.setEntity(new ByteArrayEntity(body, ContentType.APPLICATION_OCTET_STREAM));
        }
        return executeRequest(request, headers);
    }

    public HttpResponseResult doPut(String url, String body, Map<String, String> headers) {
        HttpPut request = new HttpPut(url);
        if (body != null) {
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        }
        return executeRequest(request, headers);
    }

    public HttpResponseResult doDelete(String url, Map<String, String> headers) {
        return executeRequest(new HttpDelete(url), headers);
    }

    // ДОБАВЛЕН МЕТОД HEAD
    public HttpResponseResult doHead(String url, Map<String, String> headers) {
        return executeRequest(new HttpHead(url), headers);
    }

    // ---------- Основной метод выполнения ----------
    private HttpResponseResult executeRequest(HttpUriRequest request, Map<String, String> headers) {
        if (headers != null) {
            headers.forEach(request::setHeader);
        }
        try {
            log.debug("Request: {} {}", request.getMethod(), request.getURI());
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) log.info("STATUS CODE: " + statusCode);
            byte[] bodyBytes;
            if (response.getEntity() != null) {
                bodyBytes = EntityUtils.toByteArray(response.getEntity());
            } else {
                log.info("NULL");
                bodyBytes = new byte[0];
            }

            //byte[] bodyBytes = EntityUtils.toByteArray(response.getEntity());
            String bodyString = convertToUtf8(bodyBytes);

            log.debug("Response status: {}", statusCode);
            if (bodyString != null && !bodyString.isEmpty()) {
                log.trace("Response body: {}", bodyString);
            }

            return new HttpResponseResult(statusCode, bodyString, bodyBytes, response.getAllHeaders());

        } catch (IOException e) {
            log.error("HTTP request failed: {}", e.getMessage(), e);
            return HttpResponseResult.error("Request failed: " + e.getMessage());
        }
    }

    private String convertToUtf8(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        if (utf8.contains("\uFFFD")) {
            try {
                return new String(bytes, "Windows-1251");
            } catch (UnsupportedEncodingException e) {
                log.warn("Windows-1251 not supported, fallback to UTF-8");
            }
        }
        return utf8;
    }
}