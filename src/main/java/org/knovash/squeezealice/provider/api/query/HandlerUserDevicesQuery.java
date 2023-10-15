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
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerUserDevicesQuery implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        log.info(" ---===[ REQUEST POST https://example.com/v1.0/user/devices/query ]===---");
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

        // запрос о состоянии девайса id=0
        // {"devices":[{"id":"0"}]}
        Integer id = Integer.valueOf(JsonUtils.jsonGetValue(body, "id"));
        log.info("QUERY FOR STATE DEVICE ID: " + id);
        Device device = Yandex.devices.get(id);
        log.info("QUERY FOR STATE DEVICE ID: " + device.name);

        // обратиться к девайсу и обновить все его значения
        Player player = Server.playerByName("HomePod");
        String volume = player.volume();
        log.info("VOLUME: " + volume);

        device.properties.get(0).state.value = Integer.parseInt(volume);


        // создать новый response c обновленным payload с девайсом и его новыми значениями
        Response response = new Response();
        response.request_id = xRequestId;
        response.payload.user_id = Yandex.clientId;
        response.payload.devices.add(device);

        // отправить респонс
        String json = JsonUtils.pojoToJson(response);


        json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[{\n" +
                "\"id\":\"0\",\n" +
                "\"capabilities\":[{\n" +
                "\"type\":\"devices.capabilities.range\",\n" +
                "\"state\":{\"instance\":\"volume\",\"value\":"+volume+"}}]}]}}";


        log.info("RESPONSE: " + json);
        httpExchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();
        log.info("END");
    }
}