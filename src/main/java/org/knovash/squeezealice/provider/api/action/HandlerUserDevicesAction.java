package org.knovash.squeezealice.provider.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Server;
import org.knovash.squeezealice.provider.HttpUtils;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.provider.pojo.Device;
import org.knovash.squeezealice.provider.pojo.Response;
import org.knovash.squeezealice.provider.pojo.device.ActionResult;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

@Log4j2
public class HandlerUserDevicesAction implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        log.info(" ---===[ REQUEST POST https://example.com/v1.0/user/devices/action ]===---");
        log.info("PATH: " + httpExchange.getRequestURI().getPath());
        // получить хедеры
        log.info("HEADERS: " + httpExchange.getRequestHeaders().entrySet());
        String xRequestId = HttpUtils.getHeaderValue(httpExchange, "X-request-id");
        String authorization = HttpUtils.getHeaderValue(httpExchange, "Authorization");
        String contentType = HttpUtils.getHeaderValue(httpExchange, "Content-Type");
        log.info("HEADER X-request-id : " + xRequestId);
        log.info("HEADER Authorization : " + authorization);
        log.info("HEADER Content-Type : " + contentType);
        // получить боди
        String body = HttpUtils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
        // получить кюри
        String query = httpExchange.getRequestURI().getQuery();
        log.info("QUERY: " + query);


        // запрос на изменение сосотояния
        Response response = JsonUtils.jsonToPojo(body, Response.class);
        log.info("ACTION: " + response);
        log.info("ACTION: " + response.payload.devices.get(0).capabilities.get(0));


        Integer vol = response.payload.devices.get(0).capabilities.get(0).state.value;
        log.info("ACTION VOLUME: " + vol);


        // обратиться к девайсу и изменить его состояние
        Player player = Server.playerByName("HomePod");
        log.info("VOLUME BEFORE: " + player.volume());
        player.volume(String.valueOf(vol));
        log.info("VOLUME AFTER: " + player.volume());

        Device device = Yandex.devices.get(0);
        log.info("DEVICE: " + device);


        response.request_id = xRequestId;
        response.payload.user_id = Yandex.clientId;
        ActionResult actionResult = new ActionResult();
        actionResult.status = "DONE"; // DONE ERROR
        log.info("-TMP- RESPONSE: " + response);
        actionResult.error_code = "ERR CODE OK";
        actionResult.error_message = "DEVICE_STUCK";
        response.payload.devices = new ArrayList<>();
        response.payload.devices.add(device);


//         отправить респонс
        String json = JsonUtils.pojoToJson(response);

        json = "{\"request_id\":\""+xRequestId+"\",\"payload\":{\"devices\":[{\"id\":\"0\",\n" +
                "\"capabilities\":[{\"type\":\"devices.capabilities.range\",\n" +
                "\"state\":{\"instance\":\"volume\",\"action_result\":{\"status\":\"DONE\"}}}]}]}}";

//        String json = json2;
        log.info("RESPONSE: " + json);
        httpExchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();
        log.info("END");
    }
}




//{"request_id":"EE109B31-FF6C-48BD-80DB-4D07A9AFEBB3","payload":{"devices":[{"id":"0",
//"capabilities":[{"type":"devices.capabilities.range",
//"state":{"instance":"volume","action_result":{"status":"DONE"}}}]}]}}