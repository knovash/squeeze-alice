package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Utils;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerUserUnlink implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response;
        log.info("");
        log.info(" ---===[ REQUEST USER UNLINK ]===---");
        log.info("PATH: " + httpExchange.getRequestURI().getPath());
        // получить хедеры
        String xRequestId = null;
        String authorization = null;
        String contentType = null;
        log.info("HEADERS: " + httpExchange.getRequestHeaders().entrySet());
        if (httpExchange.getRequestHeaders().containsKey("X-request-id"))
            xRequestId = httpExchange.getRequestHeaders().get("X-request-id").get(0);
        if (httpExchange.getRequestHeaders().containsKey("Authorization"))
            authorization = httpExchange.getRequestHeaders().get("Authorization").get(0);
        if (httpExchange.getRequestHeaders().containsKey("Content-Type"))
            contentType = httpExchange.getRequestHeaders().get("Content-Type").get(0);
        log.info("HEADER X-request-id : " + xRequestId);
        log.info("HEADER Authorization : " + authorization);
        log.info("HEADER Content-Type : " + contentType);
        // получить боди
        String body = Utils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
        // получить кюри
        String query = httpExchange.getRequestURI().getQuery();
        log.info("QUERY: " + query);


        response = "{\"request_id\":\"" + xRequestId + "\"}";
        log.info("RESPONSE: " + response);
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}

