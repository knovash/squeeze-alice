package org.knovash.squeezealice.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.requests.Html;
import org.knovash.squeezealice.utils.HttpUtils;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerSpotify implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response;
        log.info("");
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        String host = HttpUtils.getHeaderValue(httpExchange, "Host");
        log.info("REQUEST " + method + " " + "http://" + host + path);

        response = Html.formSpotifyLogin;

        log.info("WEB RESPONSE OK");
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}

