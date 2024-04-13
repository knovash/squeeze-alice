package org.knovash.squeezealice;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.*;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.utils.HandlerUtils;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;
import org.knovash.squeezealice.web.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

@Log4j2
public class Handler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        log.info("START");
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        String host = HandlerUtils.getHeaderValue(httpExchange, "Host");
        log.info("REQUEST: " + method + " " + "http://" + host + path);
        Headers headers = httpExchange.getRequestHeaders();
        log.info("HEADERS: " + headers.entrySet());
        String xRequestId = HandlerUtils.getHeaderValue(httpExchange, "X-request-id");
        log.info("XREQUESTID: " + xRequestId);
        String body = HandlerUtils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
        String query = httpExchange.getRequestURI().getQuery();
        log.info("QUERY: " + query);

        HashMap<String, String> queryMap = HandlerUtils.getQueryMap(query);

        Context context = new Context();
        context.body = body;
        context.headers = headers;
        context.path = path;
        context.xRequestId = xRequestId;
        context.query = query;
        context.queryMap = queryMap;

        switch (path) {
            case ("/"):
                context = PageIndex.action(context);
                break;
            case ("/speakers"):
                context = PageDevices.action(context);
                break;
            case ("/players"):
                context = PagePlayers.action(context);
                break;
            case ("/spotify"):
                context = PageSpotify.action(context);
                break;
            case ("/yandex"):
                context = PageYandex.action(context);
                break;
            case ("/cmd"):
                context = SwitchQueryCommand.action(context);
                break;
            case ("/alice/"):
                context = SwitchVoiceCommand.action(context);
                break;
            case ("/v1.0"):
                context = ProviderCheck.action(context);
                break;
            case ("/v1.0/user/unlink"):
                context = ProviderUserUnlink.action(context);
                break;
            case ("/v1.0/user/devices"):
                context = ProviderUserDevices.action(context);
                break;
            case ("/v1.0/user/devices/query"):
                context = ProviderQuery.action(context);
                break;
            case ("/v1.0/user/devices/action"):
                context = ProviderAction.action(context);
                break;
            case ("/auth"):
                context = YandexAuth.action(context);
                break;
            case ("/token"):
                context = YandexToken.action(context);
                break;
            case ("/spoti_auth"):
                log.info("SPOTI AUTH");
                context = SpotifyAuth.requestUserAuthorization(context);
                break;
//            case ("/spoti_refresh"):
//                log.info("SPOTI AUTH");
//                SpotifyAuth.requestRefresh();
//                break;
            case ("/spoti_callback"):
                log.info("SPOTI CALLBACK");
                context = SpotifyAuth.callback(context);
                break;
            default:
                log.info("PATH ERROR " + path);
                context = PageIndex.action(context);
                break;
        }

        String json = context.json;
        int code = context.code;
//        log.info("CODE: " + code);
        httpExchange.getResponseHeaders().putAll(context.headers);
//        log.info("HEADERS: " + httpExchange.getResponseHeaders().entrySet());
        log.info("RESPONSE: " + json);
        httpExchange.sendResponseHeaders(code, json.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();
        log.info("FINISH");
    }
}