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
        String json;
        String applicationId;
        String command;
        String playerName;
        String response = "повторите";
        log.info("");
        log.info(" ---===[ REQUEST FROM Alice ]===---");
        json = Utils.httpExchangeGetJsonBody(httpExchange);

        command = Utils.jsonGetValue(json, "command");
        log.info("COMMAND: " + command); // текст из диалога

        applicationId = Utils.jsonGetValue(json, "application_id");
        log.info("APP ID: " + applicationId); // текст из диалога

        playerName = Utils.appIdPlayer(applicationId);

        if (command != null) {
            log.info("COMMAND: " + command);
            response = SwitchAlice.action(command, playerName);
        }
        log.info("RESPONSE: " + response);
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
        log.info("END");
    }
}

