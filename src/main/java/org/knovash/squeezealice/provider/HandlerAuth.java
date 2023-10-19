package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@Log4j2
public class HandlerAuth implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        log.info(" ---===[ REQUEST /auth ]===---");
        log.info("PATH: " + httpExchange.getRequestURI().getPath());
        // получить хедеры
        log.info("HEADERS: " + httpExchange.getRequestHeaders().entrySet());
        String xRequestId = HttpUtils.getHeaderValue(httpExchange, "X-request-id");
        String authorization = HttpUtils.getHeaderValue(httpExchange, "Authorization");
        String contentType = HttpUtils.getHeaderValue(httpExchange, "Content-Type");
        String referer = HttpUtils.getHeaderValue(httpExchange, "Referer");
        String host = HttpUtils.getHeaderValue(httpExchange, "Host");
        log.info("HEADER X-request-id : " + xRequestId);
        log.info("HEADER Authorization : " + authorization);
        log.info("HEADER Content-Type : " + contentType);
        log.info("HEADER Referer : " + referer);
        log.info("HEADER Host : " + host);
        String body = HttpUtils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
        String query = httpExchange.getRequestURI().getQuery();
        log.info("QUERY: " + query);

        String scope = null;
        String state = null;
        String redirect_uri = null;
        String client_id = null;
        if (query != null) {
            HashMap<String, String> parameters = HttpUtils.getQueryParameters(query);
            scope = HttpUtils.getParameter(parameters, "scope");
            state = HttpUtils.getParameter(parameters, "state");
            redirect_uri = HttpUtils.getParameter(parameters, "redirect_uri");
            client_id = HttpUtils.getParameter(parameters, "client_id");
            log.info("scope: " + scope);
            log.info("state: " + state);
            log.info("redirect_uri: " + redirect_uri);
            log.info("client_id: " + client_id);
        }

        String response = "REDIRECT";
        String code = "12345";
        String location = redirect_uri + "?client_id=" + client_id + "&state=" + state + "&code=" + code;
        log.info("redirectUri: " + location);

        httpExchange.getResponseHeaders().add("Location", location);
        httpExchange.sendResponseHeaders(302, response.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}