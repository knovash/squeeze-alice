package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.web.Html;
import org.knovash.squeezealice.utils.HttpUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@Log4j2
public class HandlerRedirect implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        String response;
        log.info("");
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        String host = HttpUtils.getHeaderValue(httpExchange, "Host");
        log.info("REQUEST " + method + " " + "http://" + host + path);
        // получить хедеры
        log.info("HEADERS: " + httpExchange.getRequestHeaders().entrySet());
        String xRequestId = HttpUtils.getHeaderValue(httpExchange, "X-request-id");
        String authorization = HttpUtils.getHeaderValue(httpExchange, "Authorization");
        String contentType = HttpUtils.getHeaderValue(httpExchange, "Content-Type");
        log.info("HEADER X-request-id : " + xRequestId);
        log.info("HEADER Authorization : " + authorization);
        log.info("HEADER Content-Type : " + contentType);
        // получить боди
        String body = HttpUtils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
        // получить кюри
        String query = httpExchange.getRequestURI().getQuery();
        log.info("QUERY: " + query);


        //        QUERY:
//        scope=12345
//        &state=https://social.yandex.ru/broker2/authz_in_web/0862c244c69448e2acca4d75d0f6cf9b/callback
//        &redirect_uri=https://social.yandex.net/broker/redirect&response_type=code
//        &client_id=0d17cba2ab254d838ac1ddcedabc4191
        String scope = null;
        String state = null;
        String redirect_uri = null;
        String client_id = null;
        HashMap<String, String> parameters = new HashMap<>();
        Optional.ofNullable(Arrays.asList(query.split("&"))).orElseGet(Collections::emptyList)
                .stream()
                .filter(s -> s.contains("="))
                .map(s -> s.split("="))
                .filter(Objects::nonNull)
                .forEach(s -> parameters.put(s[0], s[1]));
        if (parameters.containsKey("scope")) scope = parameters.get("scope");
        if (parameters.containsKey("state")) state = parameters.get("state");
        if (parameters.containsKey("redirect_uri")) redirect_uri = parameters.get("redirect_uri");
        if (parameters.containsKey("client_id")) client_id = parameters.get("client_id");
        log.info("scope: " + scope);
        log.info("state: " + state);
        log.info("redirect_uri: " + redirect_uri);
        log.info("client_id: " + client_id);

//        String json = "{\"access_token\":\"" + Yandex.bearerToken + "\"," +
//                "\"token_type\":\"bearer\"," +
//                "\"expires_in\":4294967296}";
//        Content content = Fluent.httpPostJsonToUriGetContent(redirect_uri, json);
//        log.info("CONTENT: " + content.asString());

        response = Html.auth;

        log.info("RESPONSE: " + response);
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}

