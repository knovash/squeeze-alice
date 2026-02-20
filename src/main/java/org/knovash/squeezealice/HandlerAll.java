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
        log.info("\nRECIEVED HTTP REQUEST");
        Context context = Context.contextCreate(httpExchange);
        log.info("CLIENT ID IN QUERY: " + context.queryMap.get("client_id"));
        context = HandlerAll.processContext(context);
        String response = context.bodyResponse;

        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        // используем responseHeaders для ответа
        httpExchange.getResponseHeaders().putAll(context.responseHeaders);
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
            case "/":
                return PageIndex.action(context);
            case "/players":
                return PagePlayers.action(context);
            case "/manual":
                return PageManual.action(context);
            case "/lms":
                return PageLms.action(context);
            case "/cmd":
                context = SwitchQueryCommand.action(context);
                break;
            case "/alice/":
                context = SwitchVoiceCommand.action(context);
                break;
            case "/v1.0":
                context = ProviderCheck.providerCheckRun(context);
                break;
            case "/v1.0/user/unlink":
                context = ProviderUserUnlink.providerUserUnlinkRun(context);
                break;
            case "/v1.0/user/devices":
                context = ProviderUserDevices.providerUserDevicesRun(context);
                break;
            case "/v1.0/user/devices/query":
                context = ProviderQuery.providerQueryRun(context);
                break;
            case "/v1.0/user/devices/action":
                context = ProviderAction.providerActionRun(context);
                break;
            default:
                log.info("PATH ERROR " + path);
                context = PageIndex.action(context);
                break;
        }
        return context;
    }
}