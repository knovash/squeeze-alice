package org.knovash.squeezealice.utils;

import com.sun.net.httpserver.HttpExchange;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class HttpUtils {


    public static String httpExchangeGetBody(HttpExchange httpExchange) throws IOException {
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

    public static String createQuery(Map<String, String> queryMap) {

        log.info("sss " + queryMap);
        String query = queryMap.entrySet().stream()
                .map(f -> f.getKey() + "=[" + f.getValue() + "]")
                .collect(Collectors.toList()).toString();
        query = query.substring(1, query.length() - 1);
        return query;
    }

    public static HashMap<String, String> getQueryParameters(String query) {
        HashMap<String, String> parameters = new HashMap<>();
        Optional.ofNullable(Arrays.asList(query.split("&"))).orElseGet(Collections::emptyList)
                .stream()
                .filter(s -> s.contains("="))
                .map(s -> s.split("="))
                .filter(Objects::nonNull)
                .forEach(s -> parameters.put(s[0], s[1]));
        return parameters;
    }

    public static String getParameter(HashMap<String, String> parameters, String name) {
        String value=null;
        if (parameters.containsKey(name)) value = parameters.get(name);
        return value;
    }

    public static String getParameter(String query, String name) {
        HashMap<String, String> parameters =  HttpUtils.getQueryParameters(query);
        String value=null;
        if (parameters.containsKey(name)) value = parameters.get(name);
        return value;
    }
}
