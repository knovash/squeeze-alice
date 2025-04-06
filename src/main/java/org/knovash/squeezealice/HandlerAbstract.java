package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Log4j2
public abstract class HandlerAbstract implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("ABSTRACT HANDLER START >>>>>>>>>>>>>>>");
        Context context = Context.contextCreate(httpExchange);
        processContext(context, httpExchange);
        sendResponse(httpExchange, context);
        log.info("ABSTRACT HANDLER FINISH <<<<<<<<<<<<<<<");
    }

    private void sendResponse(HttpExchange exchange, Context context) throws IOException {
//        exchange.getResponseHeaders().putAll(context.responseHeaders);
//        // Обработка редиректов
//        if (context.code >= 300 && context.code < 400) {
//            exchange.sendResponseHeaders(context.code, -1); // Без тела ответа
//            return;
//        }

        byte[] responseBytes = context.bodyResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().putAll(context.headers);
        exchange.sendResponseHeaders(context.code, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected abstract Context processContext(Context context, HttpExchange httpExchange);
}