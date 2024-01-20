package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.HttpUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

@Log4j2
public class ActionAuth  {

    public static Context action(Context context) {
        log.info("");
        String body = context.body;
        Headers headers = context.headers;
        String path = context.path;
        String xRequestId = context.xRequestId;
        String query = context.query;
//
//
//        log.info("");
//        String method = httpExchange.getRequestMethod();
//        String path = httpExchange.getRequestURI().getPath();
//        String host = HttpUtils.getHeaderValue(httpExchange, "Host");
//        log.info("REQUEST " + method + " " + "http://" + host + path);
//        log.info("HEADERS: " + httpExchange.getRequestHeaders().entrySet());
//        String query = httpExchange.getRequestURI().getQuery();
//        log.info("QUERY: " + query);
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
//        httpExchange.getResponseHeaders().add("Location", location);
//        httpExchange.sendResponseHeaders(302, response.getBytes().length);
//        OutputStream outputStream = httpExchange.getResponseBody();
//        outputStream.write(response.getBytes());
//        outputStream.flush();
//        outputStream.close();

        headers.add("Location", location);
        context.headers = headers;
        context.json = response;
        context.code = 302;
        return context;
    }
}