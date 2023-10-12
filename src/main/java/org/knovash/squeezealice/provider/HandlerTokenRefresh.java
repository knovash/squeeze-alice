package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerTokenRef implements HttpHandler {

//    https://yandex.ru/dev/dialogs/smart-home/doc/reference/resources.html

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String head;
        String query;
        String response;
        log.info("");
        log.info(" ---===[ REQUEST TOKEN REF ]===---");
        head = httpExchange.getRequestHeaders().values().toString();
        query = httpExchange.getRequestURI().getQuery();
        log.info("HEAD: " + head);
        log.info("QUERY: " + query);



        response = "TOKEN REF";

        log.info("RESPONSE: " + response);
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}

