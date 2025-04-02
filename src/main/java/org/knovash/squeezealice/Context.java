package org.knovash.squeezealice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.HandlerUtils;

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
    public Headers headers;
    public String body;
    public String xRequestId;
    public String query;
    public HashMap<String, String> queryMap;

    public static Context contextCreate(HttpExchange httpExchange) {
//      METHOD
        String method = httpExchange.getRequestMethod();
//      PATH
        String path = httpExchange.getRequestURI().getPath();
        if(path.equals("/favicon.ico")){
            log.info("/favicon.ico");
           return null;
        }
//      HEADERS
        Headers headers = httpExchange.getRequestHeaders();

        String host = headers.getFirst("Host");
        String xRequestId = headers.getFirst("X-Request-Id");
        log.info("REQUEST: " + method + " " + "http://" + host + path);
        log.info("PATH: " + path);
//        log.info("HEADERS: " + headers.entrySet());
//        log.info("XREQUESTID: " + xRequestId);
//      BODY
        String body = null;
        try {
            body = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("BODY: " + body);
//      QUERY
        String query = httpExchange.getRequestURI().getQuery();
        HashMap<String, String> queryMap = HandlerUtils.convertQueryToMap(query);
        log.info("QUERY: " + query);
//        log.info("QUERY MAP: " + queryMap);

        Context context = new Context();
        context.body = body;
        context.headers = headers;
        context.path = path;
        context.xRequestId = xRequestId;
        context.query = query;
        context.queryMap = queryMap;

//        context.requestHeaders = httpExchange.getRequestHeaders(); // Заголовки запроса
//        context.responseHeaders = new Headers(); // Инициализация заголовков ответа

//        context.responseHeaders.forEach((k, v) ->
//                httpExchange.getResponseHeaders().put(k, v);


        return context;
    }

    @Override
    public String toString() {
        return "Context{" + "\n" +
                " path = " + path + "\n" +
                " headers = " + headers.entrySet() + "\n" +
                " xRequestId = " + xRequestId + "\n" +
                " query = " + query + "\n" +
                " queryMap = " + queryMap + "\n" +
                " body = " + body + "\n" +
                " bodyResponse= " + bodyResponse + "\n" +
                " code = " + code + "\n" +
                '}';
    }

    // Аннотация для игнорирования поля при сериализации (если нужно)
//    @JsonIgnore
//    public String query; // исключаем из JSON, т.к. используется только для логики

    // Метод для преобразования объекта в JSON
    public String toJson() {
//        log.info("CONTEXT TO JSON START");
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.info("CONTEXT TO JSON ERROR :(");
            e.printStackTrace();
            return null;
        }
    }

    // Статический метод для создания объекта из JSON
    public static Context fromJson(String json) {
//        log.info("CONTEXT FROM JSON START");
//        log.info("JSON: " + json);
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Context.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }


//    public void setRedirect(String location) {
//        this.code = 302;
//        this.responseHeaders.set("Location", location);
//        this.bodyResponse = ""; // Важно очистить тело ответа
//    }
}