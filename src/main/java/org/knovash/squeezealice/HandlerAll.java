package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.*;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;
import org.knovash.squeezealice.web.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;


@Log4j2
public class HandlerAll implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        log.info("---------------------------------------------------------------------------------------------");
        log.info("HTTP HANDLER START >>>>>>>>>>>>");
//        извлечение данных из запроса в контекст
        Context context = Context.contextCreate(httpExchange);
        log.info("CLIENT ID IN QUERY: " + context.queryMap.get("client_id"));

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

// context приходит из http хэндлера - локальные команды или из mqtt хэндлера - команды яндекс
    public static Context processContext(Context context) {
        String path = context.path;
        log.info("SWITCH CONTEXT PATH: " + path);
        switch (path) {
//            web pages
            case ("/"):
                return PageIndex.action(context);
            case ("/players"):
                return PagePlayers.action(context);
            case ("/manual"):
                return PageManual.action(context);
//            case ("/spotify"):
//                return PageSpotify.action(context);
            case ("/lms"):
                return PageLms.action(context);
//                tasker query commands
            case ("/cmd"):
                context = SwitchQueryCommand.action(context);
                break;

// Запрос от Яндекс приходит на сервер в облаке https://alice-lms.zeabur.app
// там запрос разбирается в context
// context отправляется в топик MQTT to_lms_ + id пользователя яндекс id4098...
// сюда context приходит из MQTT метода handleDeviceAndPublish
// тут context обрабатывается, возвращается в handleDeviceAndPublish
// и отправлятся в топик из которого забирает облачный сервер и отдает ответ в Яндекс умный дом
            case ("/alice/"):
//                log.info("CASE ALICE");
                context = SwitchVoiceCommand.action(context);
                break;
//                yandex smart home commands
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
            default:
                log.info("PATH ERROR " + path);
                context = PageIndex.action(context);
                break;
        }
        log.info("FINISH. RETURN CONTEXT");
        return context;
    }
}
