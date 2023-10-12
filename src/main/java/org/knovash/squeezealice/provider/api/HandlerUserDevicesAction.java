package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Utils;
import org.knovash.squeezealice.provider.pojo.ActionResult;
import org.knovash.squeezealice.provider.pojo.Response;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerUserDevicesAction implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        log.info(" ---===[ REQUEST DEVICES ACTION ]===---");
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

        Response response = JsonUtils.jsonToPojo(body, Response.class);
        response.payload.devices.forEach(device -> log.info("DEVICE ID: " + device.id));

        // узнать состояние девайсов
//        payload.devices.forEach(device -> device.properties.get(0).volume = "3");

        response.request_id = "33";
        log.info("----  " + response);

        ActionResult actionResult = new ActionResult();
        actionResult.status = "STATUS OK";
        actionResult.error_code = "ERR CODE OK";
        actionResult.error_message = "ERR MSG OK";
        log.info("ACTION RESULT" + actionResult);
        response.payload.devices.get(0).setAction_result(actionResult);

        log.info(response);
        String json = JsonUtils.pojoToJson(response);
        log.info("RESPONSE: " + json);
        httpExchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();
        log.info("END");
    }
}