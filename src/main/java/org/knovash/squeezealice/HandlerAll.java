package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.*;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;
import org.knovash.squeezealice.web.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;


@Log4j2
public class HandlerAll implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("HTTP HANDLER START >>>>>>>>>>>>");
//        извлечение данных из запроса в контекст
        Context context = Context.contextCreate(httpExchange);
        context = HandlerAll.processContext(context);
        String response = context.bodyResponse;

//        отправка ответа сервера
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().putAll(context.headers);
        httpExchange.sendResponseHeaders(context.code, responseBytes.length);
        try (OutputStream outputStream = httpExchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
        log.info("HTTP HANDLER FINISH <<<<<<<<<<<");
    }

    public static Context processContext(Context context) {
        String path = context.path;
        log.info("SWITCH CONTEXT PATH: " + path);
        switch (path) {
            case ("/"):
                return PageIndex.action(context);
            case ("/refresh"):
                PageIndex.refresh();
                return PageIndex.action(context);
            case ("/players"):
                return PagePlayers.action(context);
            case ("/spotify"):
                return PageSpotify.action(context);
            case ("/hive"):
                return PageHive.action(context);
            case ("/yandex"):
                return PageYandex.action(context);
            case ("/cmd"):
                context = SwitchQueryCommand.action(context);
                break;
            case ("/alice/"):
                log.info("CASE ALICE");
                context = SwitchVoiceCommand.action(context);
                break;
            case ("/v1.0"):
                context = ProviderCheck.providerCheckRun(context);
                break;
            case ("/v1.0/user/unlink"):
                context = ProviderUserUnlink.providerUserUnlinkRun(context);
                break;
            case ("/v1.0/user/devices"):
                context = ProviderUserDevices.providerUserDevicesRun(context);
                break;
            case ("/v1.0/user/devices/query"):
                context = ProviderQuery.providerQueryRun(context);
                break;
            case ("/v1.0/user/devices/action"):
                context = ProviderAction.providerActionRun(context);
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
        log.info("FINISH SWITCH PATH. RETURN CONTEXT");
        return context;
    }
}
