package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Server;
import org.knovash.squeezealice.provider.pojo.ActionResult;
import org.knovash.squeezealice.provider.pojo.Response;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerAction implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        log.info("REQUEST:" +
                " method: " + httpExchange.getRequestMethod() +
                " path: https://squeeze.serveo.net" + httpExchange.getRequestURI().getPath());
        // получить хедеры
//        log.info("HEADERS: " + httpExchange.getRequestHeaders().entrySet());
        String xRequestId = HttpUtils.getHeaderValue(httpExchange, "X-request-id");
        String authorization = HttpUtils.getHeaderValue(httpExchange, "Authorization");
        String contentType = HttpUtils.getHeaderValue(httpExchange, "Content-Type");
//        log.info("HEADER X-request-id : " + xRequestId);
//        log.info("HEADER Authorization : " + authorization);
//        log.info("HEADER Content-Type : " + contentType);
        // получить боди
        String body = HttpUtils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
        // получить кюри
        String query = httpExchange.getRequestURI().getQuery();
//        log.info("QUERY: " + query);


        // запрос на изменение сосотояния
        Response response = JsonUtils.jsonToPojo(body, Response.class);
        log.info("ACTION BODY: " + response);

        Integer id = Integer.valueOf(response.payload.devices.get(0).id);
        log.info("Yandex Device ID: " + id + " " + Yandex.devices.get(id).name
                + " " + Yandex.devices.get(id).customData.lmsName);

        Integer value = response.payload.devices.get(0).capabilities.get(0).state.value;
        String instance = response.payload.devices.get(0).capabilities.get(0).state.instance;
        log.info("ACTION INSTANCE: " + instance + " VALUE: " + value);


        // обратиться к девайсу и изменить его состояние
        Player player = Server.playerByName(Yandex.devices.get(id).description);
        switch (instance) {
            case ("volume"):
                player.volume(String.valueOf(value));
                break;
            case ("channel"):
                log.info("SWITCH CHANNEL: " + value);
                player.play(value);
                break;
        }
        log.info("---------- ");

        response.payload.devices.get(0).capabilities.get(0).state.action_result = new ActionResult();
        response.payload.devices.get(0).capabilities.get(0).state.action_result.status = "DONE";
        response.payload.devices.get(0).capabilities.get(0).state.action_result.error_code = null;
        response.payload.devices.get(0).capabilities.get(0).state.action_result.error_message = null;
        response.payload.devices.get(0).capabilities.get(0).state.value = value;

        String json = JsonUtils.pojoToJson(response);
        log.info("RESPONSE FAKE: " + json);

//        json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[{\"id\":\"" + id + "\",\n" +
//                "\"capabilities\":[{\"type\":\"devices.capabilities.range\",\n" +
//                "\"state\":{\"instance\":\"" + instance + "\",\"action_result\":{\"status\":\"DONE\"}}}]}]}}";

//        log.info("RESPONSE: " + json);
        httpExchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();
        log.info("ACTION OK");
    }
}
