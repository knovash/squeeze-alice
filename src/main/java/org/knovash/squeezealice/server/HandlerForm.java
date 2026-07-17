package org.knovash.squeezealice.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Log4j2
public class HandlerForm implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info(Main.line);
        log.info("HANDLE FORM START >>>");
        Context context = Context.contextCreate(httpExchange);
        ProcessForm.processFormContext(context);
        sendResponse(httpExchange, context);
        log.info("HANDLE FORM FINISH <<<");
        log.info(Main.line);
    }

    private void sendResponse(HttpExchange exchange, Context context) throws IOException {
        // Копируем заголовки из контекста
        exchange.getResponseHeaders().putAll(context.responseHeaders);

        int code = context.code;
        if (code >= 300 && code < 400) {
            // Редирект – тело не отправляем
            exchange.sendResponseHeaders(code, -1);
        } else {
            // Обычный ответ – устанавливаем Content-Type и отправляем тело
            context.responseHeaders.set("Content-Type", "text/html; charset=UTF-8");
            context.responseHeaders.set("X-Content-Type-Options", "nosniff");
            byte[] responseBytes = context.bodyResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(code, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
}