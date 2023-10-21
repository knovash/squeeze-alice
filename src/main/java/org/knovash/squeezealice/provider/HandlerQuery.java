package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Server;
import org.knovash.squeezealice.provider.pojo.Payload;
import org.knovash.squeezealice.utils.HttpUtils;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class HandlerQuery implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        String host = HttpUtils.getHeaderValue(httpExchange, "Host");
        log.info("REQUEST " + method + " " + "http://" + host + path);
        // получить хедеры
        log.info("HEADERS: " + httpExchange.getRequestHeaders().entrySet());
        String authorization = HttpUtils.getHeaderValue(httpExchange, "Authorization");
        String contentType = HttpUtils.getHeaderValue(httpExchange, "Content-Type");
        String xRequestId = HttpUtils.getHeaderValue(httpExchange, "X-request-id");
//        log.info("HEADER Authorization : " + authorization);
//        log.info("HEADER Content-Type : " + contentType);
//        log.info("HEADER X-request-id : " + xRequestId);
        // получить боди
        String body = HttpUtils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
//         получить кюри
//        String query = httpExchange.getRequestURI().getQuery();
//        log.info("QUERY: " + query);

        String json;
        if (body == null) {
            json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[]}}";
        } else {

            // запрос о состоянии девайса id=0
//        BODY: {"devices":[{"id":"0"}]}
            int device_id = Integer.parseInt(JsonUtils.jsonGetValue(body, "id"));
            log.info("STATE FOR DEVICE ID: " + device_id +
                    " NAME: " + SmartHome.devices.get(device_id).name +
                    " LMS NAME: " + SmartHome.devices.get(device_id).customData.lmsName);

            Payload payload = JsonUtils.jsonToPojo(body, Payload.class);
//            log.info("PAYLOAD: " + payload);
            List<String> jsonDevices = payload.devices.stream().map(d -> updateDevice(Integer.valueOf(d.id))).collect(Collectors.toList());
//            log.info("JSON UPDATED DEVICES: " + jsonDevices);
            json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":" + jsonDevices + "}}";
        }

        log.info("RESPONSE: " + json);
        httpExchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();
        log.info("QUERY OK");
    }

    public static String updateDevice(Integer device_id) {
        // обратиться к девайсу и обновить все его значения
        Player player = Server.playerByName(SmartHome.devices.get(device_id).customData.lmsName);
        Integer volume = 0;
        Boolean power = false;
        if (player != null) {
            volume = Integer.valueOf(player.volume());
//            log.info("VOLUME: " + volume);
            String playerMode = player.mode();
            if (playerMode.equals("play")) power = true;
            log.info("POWER: " + playerMode + " power " + power);
        }
        String device = "{\"id\":\"" + device_id + "\",\"capabilities\":[" +
                "{\"type\":\"devices.capabilities.range\"," +
                "\"state\":{\"instance\":\"volume\",\"value\":" + volume + "}}" +
                "," +
                "{\"type\":\"devices.capabilities.range\"," +
                //TODO channel
                "\"state\":{\"instance\":\"channel\",\"value\":" + 2 + "}}" +
                "," +
                "{\"type\":\"devices.capabilities.on_off\"," +
                "\"state\":{\"instance\":\"on\",\"value\":" + power + "}}" + "]}";
        return device;
    }
}