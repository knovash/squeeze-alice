package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.handler.ActionAlice;
import org.knovash.squeezealice.handler.ActionKuzja;
import org.knovash.squeezealice.utils.HttpUtils;
import org.knovash.squeezealice.web.*;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerUniversal implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        String host = HttpUtils.getHeaderValue(httpExchange, "Host");
        log.info("REQUEST: " + method + " " + "http://" + host + path);
        Headers headers = httpExchange.getRequestHeaders();
        log.info("HEADERS: " + headers.entrySet());
        String xRequestId = HttpUtils.getHeaderValue(httpExchange, "X-request-id");
        String body = HttpUtils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
        String query = httpExchange.getRequestURI().getQuery();
        log.info("QUERY: " + query);

        Context context = new Context();
        context.body = body;
        context.headers = headers;
        context.path = path;
        context.xRequestId = xRequestId;
        context.query = query;

        switch (path) {
            case ("/v1.0/user/devices/query"):
                context = ActionQuery.action(context);
                break;
            case ("/v1.0"):
                context = ActionCheck.action(context);
                break;
            case ("/bearer"):
                context = ActionTokenBearer.action(context);
                break;
            case ("/refresh"):
                context = ActionTokenBearer.action(context);
                break;
            case ("/"):
                context = ActionIndex.action(context);
                break;
            case ("/players"):
                context = ActionPlayers.action(context);
                break;
            case ("/speakers"):
                context = ActionSpeakers.action(context);
                break;
            case ("/spotify"):
                context = ActionSpotify.action(context);
                break;
            case ("/yandex"):
                context = ActionYandex.action(context);
                break;
            case ("/alice/"):
                context = ActionAlice.action(context);
                break;
            case ("/cmd"):
                context = ActionKuzja.action(context);
                break;

            default:
                log.info("PATH ERROR " + path);
                break;
        }
        String json = context.json;
        int code = context.code;
        log.info("RESPONSE: " + json);
        httpExchange.sendResponseHeaders(code, json.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();
        log.info("QUERY OK");
    }
}