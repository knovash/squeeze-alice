package org.knovash.squeezealice.utils;

import com.sun.net.httpserver.HttpExchange;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Log4j2
public class HandlerUtils {


    public static String httpExchangeGetBody(HttpExchange httpExchange) throws IOException {
//  используется только в хэндлере для получения боди
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
//  используется только в хэндлере для получения хедера
        String headerValue = null;
        if (httpExchange.getRequestHeaders().containsKey(headerKey))
            headerValue = httpExchange.getRequestHeaders().get(headerKey).get(0);
        return headerValue;
    }

    public static HashMap<String, String> getQueryMap(String query) {
//  используется только в хэндлере, пределывает квери в мэп
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
}
