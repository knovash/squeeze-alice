package org.knovash.squeezealice.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Switch;
import org.knovash.squeezealice.utils.HttpUtils;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerKuzja implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String query;
        String response;
        log.info("");
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        String host = HttpUtils.getHeaderValue(httpExchange, "Host");
        log.info("REQUEST " + method + " " + "http://" + host + path);
        query = httpExchange.getRequestURI().getQuery();
        query = query.replaceAll("\\+"," ");
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

