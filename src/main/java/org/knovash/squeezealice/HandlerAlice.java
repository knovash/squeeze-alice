package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerAlice implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String head;
        String query;
        String json;
        String response = "REQUEST IS EMPTY";
        log.info("");
        log.info(" ---===[ REQUEST FROM Alice ]===---");
        head = httpExchange.getRequestHeaders().values().toString();
        query = httpExchange.getRequestURI().getQuery();
        json = Utils.readBodyJsonCommand(httpExchange);
        log.info("HEAD: " + head);
        log.info("QUERY: " + query);
        log.info("BODY: " + json); // текст из диалога
        if (json != null) {
            log.info("COMMAND: " + json);
            response = SwitchAlice.action(json);
        }
        log.info("RESPONSE: " + response);
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}

