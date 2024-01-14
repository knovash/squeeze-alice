package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.pojo.pojoActions.Device;
import org.knovash.squeezealice.pojo.pojoActions.Response;
import org.knovash.squeezealice.pojo.pojoActions.ActionResult;
import org.knovash.squeezealice.utils.HttpUtils;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerAction implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("START -------------------------------");
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        String host = HttpUtils.getHeaderValue(httpExchange, "Host");
        log.info("REQUEST: " + method + " " + "http://" + host + path);
        String xRequestId = HttpUtils.getHeaderValue(httpExchange, "X-request-id");
        String body = HttpUtils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
        if (body == null) return;
        Response response = JsonUtils.jsonToPojo(body, Response.class);
        response.request_id = xRequestId;

        response.payload.devices.stream().forEach(d -> deviseSetResult(d));

        log.info("BODY POJO: " + response);
        String json = JsonUtils.pojoToJson(response);
        log.info("RESPONSE: " + json);
        httpExchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();

        log.info("DEVICES: " + response.payload.devices.size());
        response.payload.devices.forEach(device -> DeviceActions.runInstance(device));
        log.info("END -------------------------------");
    }

    public static Device deviseSetResult(Device device){
        device.capabilities.get(0).state.action_result = new ActionResult();
        device.capabilities.get(0).state.action_result.status = "DONE";
        return device;
    }
}