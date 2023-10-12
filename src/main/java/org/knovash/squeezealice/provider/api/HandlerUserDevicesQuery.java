package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Utils;
import org.knovash.squeezealice.provider.pojo.capability.Capability;
import org.knovash.squeezealice.provider.pojo.Payload;
import org.knovash.squeezealice.provider.pojo.property.Property;
import org.knovash.squeezealice.provider.pojo.Response;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerUserDevicesQuery implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        log.info(" ---===[ REQUEST DEVICES QUERY ]===---");
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

        // из реквеста девайсы, ид, кастомдата, вернуть их сосотояние
        Payload payload = JsonUtils.jsonToPojo(body, Payload.class);
        payload.devices.forEach(device -> log.info("DEVICE ID: " + device.id));

        // изменить состояние девайсов по ид
        Capability capability = new Capability();


        Property property = new Property();
        payload.devices.forEach(device -> device.name = "CHANGED");
        log.info("DEVICES STATE: " + payload.devices);

        // создать новый response c обновленным payload с состоянием девайсов
        Response response = new Response();
        response.request_id = xRequestId;
        response.payload = payload;

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