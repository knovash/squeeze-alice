package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerIndex implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String head;
        String query;
        String response;
        log.info("");
        log.info(" ---===[ REQUEST FROM Kuzja ]===---");
        head = httpExchange.getRequestHeaders().values().toString();
        query = httpExchange.getRequestURI().getQuery();
        log.info("HEAD: " + head);
        log.info("QUERY: " + query);
        response = "Hello!\n" +
                "/cmd?action=channel&player=homepod&value=1  -  Play favorites 1 from LMS on player HomePod\n"+
                "/cmd?action=turnon&player=homepod  -  Start play music on HomePod\n" +
                "/cmd?action=turnoff&player=homepod  -  Stop play music on HomePod\n" +
                "/cmd?action=log  -  Show server log\n" +
                "/cmd?action=state  -  Show server state\n";
        log.info("RESPONSE: " + response);
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}

