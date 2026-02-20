package org.knovash.squeezealice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Context {

    public String bodyResponse;
    public int code;
    public String path;

    // Заголовки запроса – при сериализации будут записаны как "headers"
    // (для совместимости со старыми версиями alicebroker)
    @JsonProperty("headers")
    public Headers requestHeaders = new Headers();

    // Заголовки ответа – не сериализуются, чтобы не ломать старый код
    @JsonIgnore
    public Headers responseHeaders = new Headers();

    public String body;
    public String xRequestId;
    public String query;
    public HashMap<String, String> queryMap;

    // Для тестов
    public static Context contextCreate(String path, String headersJson, String body, String query) {
        Headers headers = HeadersParser.parseHeaders(headersJson);
        String xRequestId = headers.getFirst("X-request-id");
        HashMap<String, String> queryMap = (HashMap<String, String>) Parser.run(query);
        Context context = new Context();
        context.body = body;
        context.requestHeaders = headers;
        context.responseHeaders = new Headers();
        context.path = path;
        context.xRequestId = xRequestId;
        context.query = query;
        context.queryMap = queryMap;
        return context;
    }

    public static Context contextCreate(HttpExchange httpExchange) {
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        if (path.equals("/favicon.ico")) {
            log.info("/favicon.ico");
            return null;
        }

        Headers requestHeaders = httpExchange.getRequestHeaders();
        String host = requestHeaders.getFirst("Host");
        String xRequestId = requestHeaders.getFirst("X-request-id");
        log.info("REQUEST: " + method + " http://" + host + path);

        String body = null;
        try {
            body = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("BODY: " + body);

        String query = httpExchange.getRequestURI().getQuery();
        HashMap<String, String> queryMap = (HashMap<String, String>) Parser.run(query);
        log.info("QUERY: " + query);

        Context context = new Context();
        context.body = body;
        context.requestHeaders = requestHeaders;
        context.responseHeaders = new Headers();
        context.path = path;
        context.xRequestId = xRequestId;
        context.query = query;
        context.queryMap = queryMap;

        return context;
    }

    @Override
    public String toString() {
        return "Context{" + "\n" +
                " path = " + path + "\n" +
                " requestHeaders = " + requestHeaders.entrySet() + "\n" +
                " responseHeaders = " + responseHeaders.entrySet() + "\n" +
                " xRequestId = " + xRequestId + "\n" +
                " query = " + query + "\n" +
                " queryMap = " + queryMap + "\n" +
                " body = " + body + "\n" +
                " bodyResponse= " + bodyResponse + "\n" +
                " code = " + code + "\n" +
                '}';
    }

    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.info("CONTEXT TO JSON ERROR :(");
            e.printStackTrace();
            return null;
        }
    }

    public static Context fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Context.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setRedirect(String location) {
        this.code = 302;
        this.responseHeaders.set("Location", location);
        this.bodyResponse = "";
    }
}