package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.HttpUtils;

import java.io.IOException;

@Log4j2
public class HandlerCheck implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        String host = HttpUtils.getHeaderValue(httpExchange, "Host");
        log.info("REQUEST " + method + " " + "http://" + host + path);
        // получить хедеры
        log.info("HEADERS: " + httpExchange.getRequestHeaders().entrySet());
        String xRequestId = HttpUtils.getHeaderValue(httpExchange, "X-request-id");
        String authorization = HttpUtils.getHeaderValue(httpExchange, "Authorization");
        String contentType = HttpUtils.getHeaderValue(httpExchange, "Content-Type");
        log.info("HEADER X-request-id : " + xRequestId);
        log.info("HEADER Authorization : " + authorization);
        log.info("HEADER Content-Type : " + contentType);
        // получить боди
        String body = HttpUtils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
        // получить кюри
        String query = httpExchange.getRequestURI().getQuery();
        log.info("QUERY: " + query);

        // ответить 200 OK

        httpExchange.sendResponseHeaders(200, 0);
        log.info("END");
    }
}