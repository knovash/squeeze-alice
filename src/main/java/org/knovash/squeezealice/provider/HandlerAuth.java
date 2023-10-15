package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.requests.Html;

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
        // получить боди
        String body = HttpUtils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
        // получить кюри
        String query = httpExchange.getRequestURI().getQuery();
        log.info("QUERY: " + query);

        // получить параметры из кюри
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


//response = Request.Post("https://accounts.spotify.com/api/token?grant_type=client_credentials")
//        .setHeader("Authorization", "Basic " + base64)
//        .setHeader("Content-Type", "application/x-www-form-urlencoded")
//        .execute();
//        json = response.returnContent().asString();
//        log.info("json: " + json);


// json = "{"scope":"scope","state":"state","redirect_uri":"redirect_uri",
// "client_id":"client_id","code":"12345"}";
//    String json = "{\"access_token\":\"" + Yandex.bearerToken + "\"," +
//            "\"token_type\":\"bearer\"," +
//            "\"expires_in\":4294967296}";


//    String json = "{\"scope\":\"scope\"," +
//            "\"state\":\"state\"," +
//            "\"redirect_uri\":\"redirect_uri\"," +
//            "\"client_id\":\"client_id\"," +
//            "\"code\":\"12345\"}";
//
//    Map<String, String> q = new HashMap<>();
//        q.put("state", state);
//                q.put("scope", scope);
//                q.put("client_id", client_id);
//                String querydMy = HttpUtils.createQuery(q);


//        String uri = redirect_uri;
//        String queryMy = "scope=" + scope + "&" +
//                "state=" + state + "&" +
//                "redirect_uri=" + redirect_uri + "&" +
//                "client_id=" + client_id + "&" +
//                "code=" + "12345";
//        Content content = Fluent.httpGetQueryToUri(uri, queryMy);
//        log.info("CONTENT: " + content.asString());