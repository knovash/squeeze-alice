package org.knovash.squeezealice;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.*;
import org.knovash.squeezealice.utils.HttpUtils;
import org.knovash.squeezealice.voice.VoiceCommand;
import org.knovash.squeezealice.web.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

@Log4j2
public class Handler implements HttpHandler {

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
        HashMap<String, String> queryMap = HttpUtils.getQueryParameters(query);
        log.info("OK");
        Context context = new Context();
        context.body = body;
        context.headers = headers;
        context.path = path;
        context.xRequestId = xRequestId;
        context.query = query;
        context.queryMap = queryMap;

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
                context = PageIndex.action(context);
                break;
            case ("/players"):
                context = PagePlayers.action(context);
                break;
            case ("/speakers"):
                context = PageSpeakers.action(context);
                break;
            case ("/spotify"):
                context = PageSpotify.action(context);
                break;
            case ("/yandex"):
                context = PageYandex.action(context);
                break;
            case ("/alice/"):
                context = VoiceCommand.action(context);
                break;
            case ("/cmd"):
//                context = ActionKuzja.action(context);
                context = SwitchQueryCommand.action(query,context);
                break;
            case ("/v1.0/user/unlink"):
                context = ActionUserUnlink.action(context);
                break;
            case ("/token"):
                context = ActionToken.action(context);
                break;
            case ("/v1.0/user/devices/action"):
                context = ActionAction.action(context);
                break;
            case ("/v1.0/user/devices"):
                context = ActionUserDevices.action(context);
                break;
            case ("/auth"):
                log.info("CASE AUTH !!!!!!!!!");
                String scope = context.queryMap.get("scope");
                String state = context.queryMap.get("state");
                String redirect_uri = context.queryMap.get("redirect_uri");
                String client_id = context.queryMap.get("client_id");
                context.json = "REDIRECT";
                context.code = 302;
//                String cs = "12345";
                String location = redirect_uri + "?client_id=" + client_id + "&state=" + state + "&code=" + scope;
                log.info("redirectUri: " + location);
                httpExchange.getResponseHeaders().add("Location", location);
                break;
            default:
                log.info("PATH ERROR " + path);
                context = PageIndex.action(context);
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