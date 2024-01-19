package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.pojo.pojoActions.Response;
import org.knovash.squeezealice.utils.HttpUtils;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerToken implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        String response;
        log.info("");
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        String host = HttpUtils.getHeaderValue(httpExchange, "Host");
        log.info("REQUEST " + method + " " + "http://" + host + path);
        log.info("HEADERS: " + httpExchange.getRequestHeaders().entrySet());
        String body = HttpUtils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
        String query = httpExchange.getRequestURI().getQuery();
        log.info("QUERY: " + query);

        Root root = JsonUtils.jsonToPojo(body, Root.class);
//        root.access_token

        response = " {\"access_token\":\"" + SmartHome.bearerToken + "\",\"token_type\":\"bearer\",\"expires_in\":4294967296}";
        log.info("RESPONSE: " + response);
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    private class Root{
        public String access_token;
        public String token_type;
        public long expires_in;
    }
}