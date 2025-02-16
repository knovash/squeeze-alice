package org.knovash.squeezealice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Log4j2
public class HandlerUtils {

    public static String httpExchangeGetBody(HttpExchange httpExchange) throws IOException {


//        String requestBody = new String(
//                httpExchange.getRequestBody().readAllBytes(),
//                StandardCharsets.UTF_8);

        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
        if (isr == null) return null;
        BufferedReader br = new BufferedReader(isr);
        int b;
        StringBuilder buf = new StringBuilder(512);
        while ((b = br.read()) != -1) {
            buf.append((char) b);
        }
        br.close();
        isr.close();
        String body = buf.toString();
        if (body.equals("")) return null;
        return body;
    }

    public static String getHeaderValue(HttpExchange httpExchange, String headerKey) {
        String headerValue = null;
        if (httpExchange.getRequestHeaders().containsKey(headerKey))
            headerValue = httpExchange.getRequestHeaders().get(headerKey).get(0);
        return headerValue;
    }

    public static HashMap<String, String> convertQueryToMap(String query) {
        if (query == null) return new HashMap<>();
        query = query.replace("+", " ");
        HashMap<String, String> parameters = new HashMap<>();
        Optional.ofNullable(Arrays.asList(query.split("&"))).orElseGet(Collections::emptyList)
                .stream()
                .filter(s -> s.contains("="))
                .map(s -> s.split("="))
                .filter(Objects::nonNull)
                .forEach(s -> parameters.put(s[0], s[1]));
        return parameters;
    }

    public static Context contextCreate(HttpExchange httpExchange) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        Handler.httpExchange2 = httpExchange;
//        METHOD
        String method = httpExchange.getRequestMethod();
        jsonNode.put("method", method);
//        PATH
        String path = httpExchange.getRequestURI().getPath();
        jsonNode.put("path", path);

        String host = HandlerUtils.getHeaderValue(httpExchange, "Host");
        log.info("REQUEST: " + method + " " + "http://" + host + path);

//      HEADERS
        Headers headers = httpExchange.getRequestHeaders();
        log.info("HEADERS: " + headers.entrySet());
        String xRequestId = HandlerUtils.getHeaderValue(httpExchange, "X-request-id");
        log.info("XREQUESTID: " + xRequestId);
        jsonNode.put("headers", headers.entrySet().toString());

//      BODY
        String body = null;
        try {
            body = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("BODY: " + body);
        jsonNode.put("body", body);

//      QUERY
        String query = httpExchange.getRequestURI().getQuery();
        log.info("QUERY: " + query);
        HashMap<String, String> queryMap = HandlerUtils.convertQueryToMap(query);
        ObjectNode queryParamsNode = convertQueryToJson(query);
        jsonNode.set("queryParams", queryParamsNode);
        log.info("jsonNode.toPrettyString: " + jsonNode.toPrettyString());
        String httpJson = jsonNode.toString();
        log.info("HTTP JSON: " + httpJson);

        Context context = new Context();
        context.body = body;
        context.headers = headers;
        context.path = path;
        context.xRequestId = xRequestId;
        context.query = query;
        context.queryMap = queryMap;

        return context;
    }

    // Парсинг query-параметров
    public static ObjectNode convertQueryToJson(String query) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();

        if (query == null || query.isEmpty()) {
            return jsonNode;
        }

        Arrays.stream(query.split("&"))
                .map(pair -> pair.split("=", 2))
                .forEach(pair -> {

                    String key = null;
                    try {
                        key = URLDecoder.decode(pair[0], "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    String value = null;
                    try {
                        value = pair.length > 1 ?
                                URLDecoder.decode(pair[1], "UTF-8") :
                                "";
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    jsonNode.put(key, value);

                });

        return jsonNode;
    }
}
