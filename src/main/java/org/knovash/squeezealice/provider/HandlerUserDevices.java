package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.pojoUserDevices.Payload;
import org.knovash.squeezealice.provider.pojoUserDevices.Response;
import org.knovash.squeezealice.utils.HttpUtils;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerUserDevices implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        String host = HttpUtils.getHeaderValue(httpExchange, "Host");
        log.info("REQUEST " + method + " " + "http://" + host + path);
        String xRequestId = HttpUtils.getHeaderValue(httpExchange, "X-request-id");
        // ответить. отправить объект с моими девайсами
        // создать новый response, payload с девайсами сохраненными в Yandex.devices
        Response response = new Response();
        response.request_id = xRequestId;
        response.payload = new Payload();
        response.payload.user_id = SmartHome.user_id;
        response.payload.devices = SmartHome.devices;
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