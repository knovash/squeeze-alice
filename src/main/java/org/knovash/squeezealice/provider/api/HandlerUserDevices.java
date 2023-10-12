package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Utils;
import org.knovash.squeezealice.provider.pojo.Response;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerUserDevices implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        log.info(" ---===[ REQUEST USER DEVICES ]===---");
        log.info("PATH: " + httpExchange.getRequestURI().getPath());
        // получить хедеры
        String xRequestId = null;
        String authorization = null;
        String contentType = null;
        log.info("HEADERS: " + httpExchange.getRequestHeaders().entrySet());
        if (httpExchange.getRequestHeaders().containsKey("X-request-id"))
            xRequestId = httpExchange.getRequestHeaders().get("X-request-id").get(0);
        if (httpExchange.getRequestHeaders().containsKey("Authorization"))
            authorization = httpExchange.getRequestHeaders().get("Authorization").get(0);
        if (httpExchange.getRequestHeaders().containsKey("Content-Type"))
            contentType = httpExchange.getRequestHeaders().get("Content-Type").get(0);
        log.info("HEADER X-request-id : " + xRequestId);
        log.info("HEADER Authorization : " + authorization);
        log.info("HEADER Content-Type : " + contentType);
        // получить боди
        String body = Utils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
        // получить кюри
        String query = httpExchange.getRequestURI().getQuery();
        log.info("QUERY: " + query);

        // создать новый response, payload с девайсами сохраненными в Yandex.devices
        Response response = new Response();
        response.request_id = xRequestId;
        response.payload.user_id = Yandex.user_id;
        response.payload.devices = Yandex.devices;

        log.info("RESPONSE: " + response);
        String json = JsonUtils.pojoToJson(response);
        log.info("JSON: " + json);
        httpExchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}