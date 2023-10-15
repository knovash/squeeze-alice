package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerKuzja implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String query;
        String response;
        log.info("");
        log.info(" ---===[ REQUEST /cmd ]===---");
        log.info("PATH: " + httpExchange.getRequestURI().getPath());
        query = httpExchange.getRequestURI().getQuery();
        log.info("QUERY: " + query);
        response = Switch.action(query);
        log.info("RESPONSE: " + response);
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}

