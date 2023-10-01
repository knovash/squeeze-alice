package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class Handler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String command = null;
        log.info("");
        log.info(" ---===[ REQUEST ]===---");
        String query = httpExchange.getRequestURI().getQuery();
        String head = httpExchange.getRequestHeaders().values().toString();
        log.info("HEAD " + head);
        log.info("QUERY " + query);
        command = Utils.getCommand(httpExchange);
        log.info("COMMAND " + command);
        String response = "SKIP";
        if (command == null) response = Switch.action(query);
        if (command != null) response = SwitchAlice.action(command);
        if (command != null) response = AliceParser.createResponse(response);
        log.info("RESPONSE " + response);
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}

