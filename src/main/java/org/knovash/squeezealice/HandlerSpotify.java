package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.requests.Html;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerSpotify implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response;
        log.info("");
        log.info(" ---===[ REQUEST /spotify ]===---");
        log.info("PATH: " + httpExchange.getRequestURI().getPath());
        response = Html.formSpotifyLogin;
        log.info("OK");
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}

