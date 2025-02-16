package org.knovash.squeezealice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.knovash.squeezealice.provider.*;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.utils.HandlerUtils;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;
import org.knovash.squeezealice.web.PageIndex;
import org.knovash.squeezealice.web.PagePlayers;
import org.knovash.squeezealice.web.PageSpotify;
import org.knovash.squeezealice.web.PageYandex;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;

@Log4j2
public class Handler implements HttpHandler {

    public static HttpExchange httpExchange2;
    public static Headers headers;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("START ---------------------------------------------");
        Context context = HandlerUtils.contextCreate(httpExchange);
//          обработка контекста и обращение в LMS
        context = Handler.switchContext(context);
//          вернуть ответ от сервера
        String json = context.json;
        int code = context.code;
        log.info("CODE: " + code);
        httpExchange2.getResponseHeaders().putAll(context.headers);
        httpExchange2.sendResponseHeaders(code, json.getBytes().length);
        OutputStream outputStream = httpExchange2.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();
        log.info("FINISH");
    }

    public static Context switchContext(Context context) {
        String path = context.path;
        log.info("SWITCH PATH: " + path);

        switch (path) {
            case ("/"):
                context = PageIndex.action(context);
                break;
            case ("/refresh"):
                PageIndex.refresh();
                context = PageIndex.action(context);
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
            case ("/auth"): // сюда первый запрос от Яндекса для привязки акаунта
                context = YandexAuth.action(context);
                break;
            case ("/token"): // сюда второй запрос от Яндекса для привязки акаунта
                context = YandexToken.action(context);
                break;
            case ("/spoti_auth"):
                context = SpotifyAuth.requestUserAuthorization(context);
                break;
            case ("/spoti_callback"):
                context = SpotifyAuth.callback(context);
                break;
            default:
                log.info("PATH ERROR " + path);
                context = PageIndex.action(context);
                break;
        }
        log.info("FINISH SWITCH CONTEXT");
        return context;
    }




}